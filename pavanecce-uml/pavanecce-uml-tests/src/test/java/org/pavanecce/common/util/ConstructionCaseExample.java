package org.pavanecce.common.util;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.ocl.expressions.CollectionKind;
import org.eclipse.uml2.uml.AggregationKind;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.resource.UMLResource;
import org.pavanecce.common.text.filegeneration.TextFileGenerator;
import org.pavanecce.uml.common.util.UmlResourceSetFactory;
import org.pavanecce.uml.ocltocode.OclCodeBuilder;
import org.pavanecce.uml.uml2code.AbstractCodeGenerator;
import org.pavanecce.uml.uml2code.AdaptableFileLocator;
import org.pavanecce.uml.uml2code.codemodel.CodeModelBuilder;
import org.pavanecce.uml.uml2code.codemodel.UmlCodeModelVisitorAdaptor;
import org.pavanecce.uml.uml2code.java.AssociationCollectionCodeDecorator;
import org.pavanecce.uml.uml2code.java.JavaCodeGenerator;
import org.pavanecce.uml.uml2code.jpa.AbstractJavaCodeDecorator;

public class ConstructionCaseExample extends AbstractPotentiallyJavaCompilingTest {
	private ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
	protected CodeModelBuilder builder;
	private Model model;
	private UmlCodeModelVisitorAdaptor adaptor;
	protected UmlResourceSetFactory resourceSetFactory = new UmlResourceSetFactory(new AdaptableFileLocator());
	private String testName;
	private Class roomPlan;
	private Class wallPlan;
	private Class wall;
	private Class housePlan;
	private Class house;
	private Class constructionCase;
	private ResourceSet rst;
	private TextFileGenerator textFileGenerator;

	public ConstructionCaseExample(String name) {
		this.testName = name;
		rst = resourceSetFactory.prepareResourceSet();
		Resource r = rst.createResource(URI.createFileURI("tmp.uml"));
		setModel(UMLFactory.eINSTANCE.createModel());
		getModel().setName("test");
		r.getContents().add(getModel());
		constructionCase = (Class) getModel().createOwnedType("ConstructionCase", UMLPackage.eINSTANCE.getClass_());
		Model primitiveTypes = (Model) rst.getResource(URI.createURI(UMLResource.UML_PRIMITIVE_TYPES_LIBRARY_URI), true).getContents().get(0);
		constructionCase.createOwnedAttribute("name", primitiveTypes.getOwnedType("String"));
		house = (Class) getModel().createOwnedType("House", UMLPackage.eINSTANCE.getClass_());
		createOneToOne(constructionCase, house, AggregationKind.COMPOSITE_LITERAL);
		housePlan = (Class) getModel().createOwnedType("HousePlan", UMLPackage.eINSTANCE.getClass_());
		createOneToOne(constructionCase, housePlan, AggregationKind.COMPOSITE_LITERAL);
		wall = (Class) getModel().createOwnedType("Wall", UMLPackage.eINSTANCE.getClass_());
		wallPlan = (Class) getModel().createOwnedType("WallPlan", UMLPackage.eINSTANCE.getClass_());
		createOneToMany(house, wall, CollectionKind.SET_LITERAL, AggregationKind.COMPOSITE_LITERAL);
		createOneToMany(housePlan, wallPlan, CollectionKind.SET_LITERAL, AggregationKind.COMPOSITE_LITERAL);
		roomPlan = (Class) getModel().createOwnedType("RoomPlan", UMLPackage.eINSTANCE.getClass_());
		roomPlan.createOwnedAttribute("name", primitiveTypes.getOwnedType("String"));
		createOneToMany(housePlan, roomPlan, CollectionKind.SET_LITERAL, AggregationKind.COMPOSITE_LITERAL);
		createManyToMany(roomPlan, wallPlan, CollectionKind.SET_LITERAL);
	}

	public Class getConstructionCase() {
		return constructionCase;
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

	protected void createAssociation(Class from, Class to, CollectionKind collectionKind, AggregationKind aggregationKind, int fromMultiplicity, int toMultiplicity) {
		Association ass = (Association) from.getPackage().createOwnedType(from.getName() + to.getName(), UMLPackage.eINSTANCE.getAssociation());
		Property toEnd = ass.createNavigableOwnedEnd(NameConverter.decapitalize(to.getName()), to);
		toEnd.setUpper(toMultiplicity);
		toEnd.setAggregation(aggregationKind);
		toEnd.setIsUnique(collectionKind == CollectionKind.SET_LITERAL || collectionKind == CollectionKind.ORDERED_SET_LITERAL);
		toEnd.setIsOrdered(collectionKind == CollectionKind.SEQUENCE_LITERAL || collectionKind == CollectionKind.ORDERED_SET_LITERAL);
		Property fromEnd = ass.createNavigableOwnedEnd(NameConverter.decapitalize(from.getName()), from);
		fromEnd.setUpper(fromMultiplicity);
	}

	public void generateCode(AbstractCodeGenerator codeGenerator, AbstractJavaCodeDecorator... decorators) throws Exception {
		setup(new CodeModelBuilder(true), codeGenerator, decorators);
	}

	public void setup(CodeModelBuilder codeModelBuilder, AbstractCodeGenerator codeGenerator, AbstractJavaCodeDecorator... decorators) throws Exception {
		this.builder = codeModelBuilder;
		oldContextClassLoader = Thread.currentThread().getContextClassLoader();
		super.setup();
		super.setJavaCodeGenerator(codeGenerator);
		if (getCodeGenerator() instanceof JavaCodeGenerator) {
			JavaCodeGenerator jcg = (JavaCodeGenerator) getCodeGenerator();
			jcg.addDecorator(new AssociationCollectionCodeDecorator());
			for (AbstractJavaCodeDecorator cd : decorators) {
				jcg.addDecorator(cd);
			}
		}
		setAdaptor(new UmlCodeModelVisitorAdaptor());
		this.getAdaptor().startVisiting(builder, getModel());
		this.getAdaptor().startVisiting(new OclCodeBuilder(), getModel());
		this.textFileGenerator = super.generateSourceCode(this.getAdaptor().getCodeModel());
		if (getCodeGenerator() instanceof JavaCodeGenerator) {
			setClassLoader(super.compile(textFileGenerator.getNewFiles()));
		}
	}

	public TextFileGenerator getTextFileGenerator() {
		return textFileGenerator;
	}

	public ClassLoader getOldContextClassLoader() {
		return oldContextClassLoader;
	}

	public CodeModelBuilder getBuilder() {
		return builder;
	}

	public UmlResourceSetFactory getResourceSetFactory() {
		return resourceSetFactory;
	}

	public Class getRoomPlan() {
		return roomPlan;
	}

	public Class getWallPlan() {
		return wallPlan;
	}

	public Class getWall() {
		return wall;
	}

	public Class getHousePlan() {
		return housePlan;
	}

	public Class getHouse() {
		return house;
	}

	@Override
	protected String getTestName() {
		return this.testName;
	}

	public void after() {
		Thread.currentThread().setContextClassLoader(oldContextClassLoader);
	}

	public UmlCodeModelVisitorAdaptor getAdaptor() {
		return adaptor;
	}

	public void setAdaptor(UmlCodeModelVisitorAdaptor adaptor) {
		this.adaptor = adaptor;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

	public Classifier getType(String string) {
		TreeIterator<Notifier> allContents = rst.getAllContents();
		while (allContents.hasNext()) {
			Notifier notifier = allContents.next();
			if (notifier instanceof Classifier && ((Classifier) notifier).getName().equals(string)) {
				return (Classifier) notifier;
			}
		}
		return null;
	}

}