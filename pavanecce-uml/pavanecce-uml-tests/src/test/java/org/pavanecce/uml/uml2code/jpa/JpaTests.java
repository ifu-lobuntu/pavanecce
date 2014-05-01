package org.pavanecce.uml.uml2code.jpa;

import java.util.HashMap;

import javax.persistence.EntityManagerFactory;
import javax.script.ScriptContext;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.ocl.expressions.CollectionKind;
import org.eclipse.uml2.uml.AggregationKind;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.resource.UMLResource;
import org.hibernate.ejb.HibernatePersistence;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pavanecce.common.jpa.JpaObjectPersistence;
import org.pavanecce.common.text.filegeneration.TextFileGenerator;
import org.pavanecce.common.util.NameConverter;
import org.pavanecce.uml.common.util.UmlResourceSetFactory;
import org.pavanecce.uml.uml2code.AbstractJavaCompilingTest;
import org.pavanecce.uml.uml2code.AdaptableFileLocator;
import org.pavanecce.uml.uml2code.codemodel.CodeModelBuilder;
import org.pavanecce.uml.uml2code.codemodel.UmlCodeModelVisitorAdaptor;
import org.pavanecce.uml.uml2code.java.JavaCodeGenerator;

public class JpaTests extends AbstractJavaCompilingTest {
	private ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
	protected CodeModelBuilder builder;
	protected Model model;
	protected UmlCodeModelVisitorAdaptor adaptor;
	protected UmlResourceSetFactory resourceSetFactory = new UmlResourceSetFactory(new AdaptableFileLocator());

	@After
	public void after() {
		Thread.currentThread().setContextClassLoader(oldContextClassLoader);
	}

	@Before
	public void setup() throws Exception {
		oldContextClassLoader = Thread.currentThread().getContextClassLoader();
		super.setup();
		super.javaCodeGenerator = new JavaCodeGenerator();
		javaCodeGenerator.addDecorator(new JpaCodeDecorator());
		this.builder = new CodeModelBuilder();
		adaptor = new UmlCodeModelVisitorAdaptor();
		ResourceSet rst = resourceSetFactory.prepareResourceSet();
		Resource r = rst.createResource(URI.createFileURI("tmp.uml"));
		model = UMLFactory.eINSTANCE.createModel();
		model.setName("test");
		r.getContents().add(model);
		Class constructionCase = (Class) model.createOwnedType("ConstructionCase", UMLPackage.eINSTANCE.getClass_());
		Model primitiveTypes = (Model) rst.getResource(URI.createURI(UMLResource.UML_PRIMITIVE_TYPES_LIBRARY_URI), true).getContents().get(0);
		constructionCase.createOwnedAttribute("name", primitiveTypes.getOwnedType("String"));
		Class house = (Class) model.createOwnedType("House", UMLPackage.eINSTANCE.getClass_());
		createOneToOne(constructionCase, house, AggregationKind.COMPOSITE_LITERAL);
		Class housePlan = (Class) model.createOwnedType("HousePlan", UMLPackage.eINSTANCE.getClass_());
		createOneToOne(constructionCase, housePlan, AggregationKind.COMPOSITE_LITERAL);
		Class wall = (Class) model.createOwnedType("Wall", UMLPackage.eINSTANCE.getClass_());
		Class wallPlan = (Class) model.createOwnedType("WallPlan", UMLPackage.eINSTANCE.getClass_());
		createOneToMany(house, wall, CollectionKind.SET_LITERAL, AggregationKind.COMPOSITE_LITERAL);
		createOneToMany(housePlan, wallPlan, CollectionKind.SET_LITERAL, AggregationKind.COMPOSITE_LITERAL);
		createManyToMany(house, wallPlan, CollectionKind.SET_LITERAL);
		this.adaptor.startVisiting(builder, model);
		TextFileGenerator textWorkspace = super.generateJava(this.adaptor.getCodeModel());
		ClassLoader cl = super.compile(textWorkspace.getNewFiles());
		TestPersistenceUnitInfo pui = new TestPersistenceUnitInfo("construction", cl);
		super.addMappedClasses(pui, adaptor.getCodeModel());
		HibernatePersistence hibernatePersistence = new HibernatePersistence();
		EntityManagerFactory emf = hibernatePersistence.createContainerEntityManagerFactory(pui, new HashMap<String, String>());
		initScriptingEngine();
		Thread.currentThread().setContextClassLoader(cl);
		javaScriptContext.setAttribute("p", new JpaObjectPersistence(emf), ScriptContext.ENGINE_SCOPE);
	}

	private void createOneToMany(Class from, Class to, CollectionKind collectionKind, AggregationKind aggregationKind) {
		createAssociation(from, to, collectionKind, aggregationKind, 1, -1);
	}

	private void createManyToMany(Class from, Class to, CollectionKind collectionKind) {
		createAssociation(from, to, collectionKind, AggregationKind.NONE_LITERAL, -1, -1);
	}

	private void createOneToOne(Class from, Class to, AggregationKind aggregationKind) {
		createAssociation(from, to, CollectionKind.SET_LITERAL, aggregationKind, 1, 1);
	}

	protected void createAssociation(Class from, Class to, CollectionKind collectionKind, AggregationKind aggregationKind, int fromMultiplicity,
			int toMultiplicity) {
		Association ass = (Association) from.getPackage().createOwnedType(from.getName() + to.getName(), UMLPackage.eINSTANCE.getAssociation());
		Property toEnd = ass.createNavigableOwnedEnd(NameConverter.decapitalize(to.getName()), to);
		toEnd.setUpper(toMultiplicity);
		toEnd.setAggregation(aggregationKind);
		toEnd.setIsUnique(collectionKind == CollectionKind.SET_LITERAL || collectionKind == CollectionKind.ORDERED_SET_LITERAL);
		toEnd.setIsOrdered(collectionKind == CollectionKind.SEQUENCE_LITERAL || collectionKind == CollectionKind.ORDERED_SET_LITERAL);
		Property fromEnd = ass.createNavigableOwnedEnd(NameConverter.decapitalize(from.getName()), from);
		fromEnd.setUpper(fromMultiplicity);
	}

	@Test
	public void testOneToOne() throws Exception {
		// GIVEN UML model built, CodeModel generated, JPA Code Generated,
		// Classes Compiler, EntityManagerFactory Created:
		// WHEN

		// THEN

		super.javaScriptEngine.eval("ConstructionCase=Packages.test.ConstructionCase;");
		super.javaScriptEngine.eval("House=Packages.test.House;");
		super.javaScriptEngine.eval("Wall=Packages.test.Wall;");
		super.javaScriptEngine.eval("var constructionCase=new ConstructionCase();");
		super.javaScriptEngine.eval("var house=new House();");
		super.javaScriptEngine.eval("constructionCase.setHouse(house);");
		super.javaScriptEngine.eval("p.start();");
		super.javaScriptEngine.eval("p.persist(constructionCase);");
		super.javaScriptEngine.eval("p.commit();");
		super.javaScriptEngine.eval("p.close();");
		super.javaScriptEngine.eval("p.start();");
		super.javaScriptEngine.eval("house=p.find(House,house.getId());");
		assertNotNull(javaScriptEngine.eval("house.getConstructionCase();"));
		assertEquals(javaScriptEngine.eval("constructionCase.getId();"), javaScriptEngine.eval("house.getConstructionCase().getId();"));
		super.javaScriptEngine.eval("constructionCase=p.find(ConstructionCase,constructionCase.getId());");
		super.javaScriptEngine.eval("constructionCase.setHouse(null);");
		super.javaScriptEngine.eval("p.commit();");
		super.javaScriptEngine.eval("p.close();");
		super.javaScriptEngine.eval("p.start();");
		super.javaScriptEngine.eval("constructionCase=p.find(ConstructionCase,constructionCase.getId());");
		assertNull(javaScriptEngine.eval("constructionCase.getHouse();"));
	}

	@Test
	public void testOneToManyChildren() throws Exception {
		// GIVEN UML model built, CodeModel generated, JPA Code Generated,
		// Classes Compiler, EntityManagerFactory Created:
		// WHEN

		// THEN

		super.javaScriptEngine.eval("ConstructionCase=Packages.test.ConstructionCase;");
		super.javaScriptEngine.eval("House=Packages.test.House;");
		super.javaScriptEngine.eval("Wall=Packages.test.Wall;");
		super.javaScriptEngine.eval("var constructionCase=new ConstructionCase();");
		super.javaScriptEngine.eval("var house=new House();");
		super.javaScriptEngine.eval("constructionCase.setHouse(house);");
		super.javaScriptEngine.eval("var wall=new Wall();");
		super.javaScriptEngine.eval("wall.setHouse(house);");
		super.javaScriptEngine.eval("house.getWall().add(wall);");
		super.javaScriptEngine.eval("p.start();");
		super.javaScriptEngine.eval("p.persist(constructionCase);");
		super.javaScriptEngine.eval("p.commit();");
		super.javaScriptEngine.eval("p.close();");
		super.javaScriptEngine.eval("p.start();");
		super.javaScriptEngine.eval("house=p.find(House,house.getId());");
		assertEquals(1, javaScriptEngine.eval("house.getWall().size()"));
	}

	@Override
	protected String getTestName() {
		return "SimplePersistence";
	}

}
