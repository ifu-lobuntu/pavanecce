package org.pavanecce.common.test.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterableElement;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.StructuredClassifier;
import org.eclipse.uml2.uml.UMLFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pavanecce.uml.common.util.UmlResourceSetFactory;
import org.pavanecce.uml.reverse.java.ProfileGenerator;
import org.pavanecce.uml.reverse.java.SimpleUmlGenerator;
import org.pavanecce.uml.reverse.java.UmlGeneratorFromJpa;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceClass;
import org.pavanecce.uml.reverse.java.sourcemodel.reflect.JavaDescriptorFactory;
import org.pavanecce.uml.test.domain.ManyEntity;
import org.pavanecce.uml.test.domain.OneEntity;
import org.pavanecce.uml.test.uml2code.test.AdaptableFileLocator;

public class JavaReverseTests {
	private static File profileFile;
	private static File simpleFile;
	private static File jpaFile;
	private UmlResourceSetFactory resourceSetFactory = new UmlResourceSetFactory(new AdaptableFileLocator());

	@BeforeClass
	public static void classSetup() {
		profileFile = new File(System.getProperty("user.home") + "/tmp/profile.uml");
		if (profileFile.exists()) {
			profileFile.delete();
		}
		simpleFile = new File(System.getProperty("user.home") + "/tmp/simple.uml");
		if (simpleFile.exists()) {
			simpleFile.delete();
		}
		jpaFile = new File(System.getProperty("user.home") + "/tmp/jpa.uml");
		if (jpaFile.exists()) {
			jpaFile.delete();
		}

	}

	@Test
	public void testProfile() throws Exception {
		Set<SourceClass> types = new HashSet<SourceClass>();
		JavaDescriptorFactory jdf = new JavaDescriptorFactory();
		types.add(jdf.getClassDescriptor(JoinTable.class));
		types.add(jdf.getClassDescriptor(ManyToMany.class));
		types.add(jdf.getClassDescriptor(CascadeType.class));
		types.add(jdf.getClassDescriptor(FetchType.class));
		Profile library = generateJavaxPersistenceProfile(types).getKey();
		Stereotype stereotype = library.getOwnedStereotype("ManyToMany");
		assertEquals("ManyToMany", stereotype.getName());
		Property mb = stereotype.getOwnedAttribute("mappedBy", null);
		assertEquals("String", mb.getType().getName());
		Property ft = stereotype.getOwnedAttribute("fetch", null);
		Enumeration fetchType = (Enumeration) ft.getType();
		assertNotNull(fetchType.getOwnedLiteral("LAZY"));
		Property targetEntity = stereotype.getOwnedAttribute("targetEntity", null);
		Class classMetaClass = (Class) targetEntity.getType();
		assertEquals("Class", classMetaClass.getName());
		assertTrue(classMetaClass.isMetaclass());
		Stereotype joinTable = library.getOwnedStereotype("JoinTable");
		Property uc = joinTable.getOwnedAttribute("uniqueConstraints", null);
		assertTrue(uc.isMultivalued());
		assertEquals("UniqueConstraint", uc.getType().getName());
	}

	private Map.Entry<Profile, ProfileGenerator> generateJavaxPersistenceProfile(Set<SourceClass> types) throws IOException, Exception {
		ResourceSet rst = resourceSetFactory.prepareResourceSet();
		ProfileGenerator sug = new ProfileGenerator();
		Profile library = UMLFactory.eINSTANCE.createProfile();
		library.setName("javax_persistence");
		Resource rs = rst.createResource(URI.createFileURI(profileFile.getCanonicalPath()));
		rs.getContents().add(library);
		sug.generateUml(types, library, new DummyProgressMonitor());
		return Collections.singletonMap(library, sug).entrySet().iterator().next();
	}

	@Test
	public void testSimple() throws Exception {
		ResourceSet rst = resourceSetFactory.prepareResourceSet();
		SimpleUmlGenerator sug = new SimpleUmlGenerator();
		Set<SourceClass> types = new HashSet<SourceClass>();
		Model library = UMLFactory.eINSTANCE.createModel();
		library.setName("simple");
		Resource rs = rst.createResource(URI.createFileURI(simpleFile.getCanonicalPath()));
		rs.getContents().add(library);
		JavaDescriptorFactory jdf = new JavaDescriptorFactory();
		types.add(jdf.getClassDescriptor(Namespace.class));
		types.add(jdf.getClassDescriptor(StructuredClassifier.class));
		types.add(jdf.getClassDescriptor(Property.class));
		types.add(jdf.getClassDescriptor(ParameterableElement.class));
		sug.generateUml(types, library, new DummyProgressMonitor());
		Package uml = library.getNestedPackage("org").getNestedPackage("eclipse").getNestedPackage("uml2").getNestedPackage("uml");
		Interface classInterface = (Interface) uml.getOwnedType("StructuredClassifier");
		Interface propertyInterface = (Interface) uml.getOwnedType("Property");
		Property oa = classInterface.getAttribute("ownedAttributes", null);
		assertEquals(propertyInterface, oa.getType());
		assertTrue(oa.isMultivalued());
		assertTrue(oa.isOrdered());
		assertNull(classInterface.getOwnedOperation("getAllUsedInterfaces", this.asList("result"), this.asList(uml.getOwnedType("Interface"))));
		assertNull(classInterface.getOwnedOperation("getAllUsedInterfaces", this.asList("result"), this.asList(uml.getOwnedType("Interface"))));
		Interface namespaceInterface = (Interface) uml.getOwnedType("Namespace");
		Operation excludes = namespaceInterface.getOperation("excludeCollisions", asList("param0", "result"),
				asList(uml.getOwnedType("PackageableElement"), uml.getOwnedType("PackageableElement")));
		Parameter param0 = excludes.getOwnedParameter("param0", null);
		Interface parameterable = (Interface) uml.getOwnedType("ParameterableElement");
		EList<Operation> ownedOperations = parameterable.getOwnedOperations();
		for (Operation operation : ownedOperations) {
			if (operation.getName().equals("getTemplateParameter")) {
				fail();
			}
		}
		assertTrue(param0.isMultivalued());
		assertTrue(param0.isOrdered());
		rs.save(null);
	}

	private <T> EList<T> asList(@SuppressWarnings("unchecked") T... e) {
		BasicEList<T> result = new BasicEList<T>();
		for (T t : e) {
			result.add(t);
		}
		return result;
	}

	@Test
	public void testJpa() throws Exception {
		ResourceSet rst = resourceSetFactory.prepareResourceSet();
		UmlGeneratorFromJpa jpaGenerator = new UmlGeneratorFromJpa();
		Set<SourceClass> types = new HashSet<SourceClass>();
		Model library = UMLFactory.eINSTANCE.createModel();
		library.setName("jpa");
		Resource rs = rst.createResource(URI.createFileURI(jpaFile.getCanonicalPath()));
		rs.getContents().add(library);
		JavaDescriptorFactory jdf = new JavaDescriptorFactory();
		types.add(jdf.getClassDescriptor(OneEntity.class));
		types.add(jdf.getClassDescriptor(ManyEntity.class));
		Entry<Profile, ProfileGenerator> pgen = generateJavaxPersistenceProfile(jpaGenerator.extractProfileElements(types));
		Profile profile = pgen.getKey();
		profile.define();
		library.applyProfile(profile);
		jpaGenerator.getClassMap().putAll(pgen.getValue().getClassMap());
		jpaGenerator.generateUml(types, library, new DummyProgressMonitor());
		// do it twice to check if duplicates are generated
		jpaGenerator.generateUml(types, library, new DummyProgressMonitor());
		jpaGenerator.generateUml(types, library, new DummyProgressMonitor());
		Package uml = library.getNestedPackage("org").getNestedPackage("pavanecce").getNestedPackage("uml").getNestedPackage("test").getNestedPackage("domain");

		Class oneEntity = (Class) uml.getOwnedType("OneEntity");
		Class manyEntity = (Class) uml.getOwnedType("ManyEntity");
		assertEquals("String", oneEntity.getOwnedAttribute("name", null).getType().getName());
		assertEquals("Integer", oneEntity.getOwnedAttribute("age", null).getType().getName());
		assertEquals("AnEnum", oneEntity.getOwnedAttribute("anEnum", null).getType().getName());
		assertEquals(1, oneEntity.getAssociations().size());
		assertEquals(1, manyEntity.getAssociations().size());
		assertSame(oneEntity.getAssociations().get(0), manyEntity.getAssociations().get(0));
		Association ass = manyEntity.getAssociations().get(0);
		Property many = (Property) ass.getMember("many");
		assertTrue(many.isComposite());
		assertTrue(many.isMultivalued());
		Property one = (Property) ass.getMember("one");
		assertFalse(one.isMultivalued());
		assertFalse(one.isComposite());
		rs.save(null);
	}
}
