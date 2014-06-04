package org.pavanecce.uml.uml2code.ocm;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.script.ScriptContext;

import org.apache.jackrabbit.commons.cnd.CndImporter;
import org.apache.jackrabbit.core.TransientRepository;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.AnnotationMapperImpl;
import org.apache.jackrabbit.ocm.reflection.ReflectionUtils;
import org.junit.BeforeClass;
import org.pavanecce.common.code.metamodel.CodeClassifier;
import org.pavanecce.common.code.metamodel.CodePackage;
import org.pavanecce.common.code.metamodel.documentdb.DocumentNamespace;
import org.pavanecce.common.ocm.OcmFactory;
import org.pavanecce.common.ocm.OcmObjectPersistence;
import org.pavanecce.common.util.ConstructionCaseExample;
import org.pavanecce.common.util.FileUtil;
import org.pavanecce.uml.uml2code.java.JavaCodeGenerator;
import org.pavanecce.uml.uml2code.jpa.AbstractPersistenceTest;

public class OcmTests extends AbstractPersistenceTest {

	@SuppressWarnings("rawtypes")
	@BeforeClass
	public static void setup() throws Exception {
		FileUtil.deleteRoot(new File("./repository"));
		System.setProperty("java.naming.factory.initial", "bitronix.tm.jndi.BitronixInitialContextFactory");
		example = new ConstructionCaseExample("OcmPersistence");
		example.generateCode(new JavaCodeGenerator(), new OcmCodeDecorator());
		List<Class> classes = getClasses();
		File outputRoot = example.calculateBinaryOutputRoot();
		File testCndFile = generateCndFile(outputRoot,new CndTextGenerator());
		Repository tr = new TransientRepository();
		Session session = tr.login(new SimpleCredentials("admin", "admin".toCharArray()));
		CndImporter.registerNodeTypes(new FileReader(testCndFile), session);
		session.getRootNode().addNode("ConstructionCaseCollection");
		session.save();
//		session.logout();
		ReflectionUtils.setClassLoader(example.getClassLoader());
		OcmFactory ocmFactory = new OcmFactory(tr, "admin", "admin", new AnnotationMapperImpl(classes), null);
		OcmObjectPersistence hibernatePersistence = new OcmObjectPersistence(ocmFactory);
		example.initScriptingEngine();
		example.getJavaScriptContext().setAttribute("p", hibernatePersistence, ScriptContext.ENGINE_SCOPE);
	}
	protected static File generateCndFile(File outputRoot, CndTextGenerator cndTextGenerator) {
		UmlDocumentModelFileVisitorAdaptor a = new UmlDocumentModelFileVisitorAdaptor();
		DocumentModelBuilder docBuilder = new DocumentModelBuilder();
		a.startVisiting(docBuilder, example.getModel());
		
		DocumentNamespace documentModel = docBuilder.getDocumentModel();
		File testCndFile = new File(outputRoot,"test.cnd");
		FileUtil.write(testCndFile, cndTextGenerator.generate(documentModel));
		return testCndFile;
	}

	@SuppressWarnings("rawtypes")
	private static List<Class> getClasses() throws Exception {
		List<Class> result = new ArrayList<Class>();
		addMappedClasses(result, example.getAdaptor().getCodeModel(), (JavaCodeGenerator) example.getCodeGenerator(), example.getClassLoader());
		return result;
	}

	@SuppressWarnings("rawtypes")
	protected static void addMappedClasses(List<Class> classes, CodePackage codePackage, JavaCodeGenerator jcg, ClassLoader cl) throws Exception {
		for (CodeClassifier cc : codePackage.getClassifiers().values()) {
			classes.add(cl.loadClass(jcg.toQualifiedName(cc.getTypeReference())));
		}
		Collection<CodePackage> values = codePackage.getChildren().values();
		for (CodePackage child : values) {
			addMappedClasses(classes, child, jcg, cl);
		}
	}

}
