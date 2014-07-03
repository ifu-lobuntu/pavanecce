package org.pavanecce.uml.test.uml2code.jpa;

import java.util.Collection;
import java.util.HashMap;

import javax.persistence.EntityManagerFactory;
import javax.script.ScriptContext;

import org.hibernate.ejb.HibernatePersistence;
import org.junit.BeforeClass;
import org.pavanecce.common.code.metamodel.CodeClass;
import org.pavanecce.common.code.metamodel.CodeClassifier;
import org.pavanecce.common.code.metamodel.CodePackage;
import org.pavanecce.common.jpa.JpaObjectPersistence;
import org.pavanecce.common.test.util.ConstructionCaseExample;
import org.pavanecce.uml.uml2code.AbstractCodeGenerator;
import org.pavanecce.uml.uml2code.java.JavaCodeGenerator;
import org.pavanecce.uml.uml2code.jpa.JpaCodeDecorator;

public class JpaTests extends AbstractPersistenceTest {

	@BeforeClass
	public static void setup() throws Exception {
		example = new ConstructionCaseExample("JpaPersistence");
		example.generateCode(new JavaCodeGenerator(), new JpaCodeDecorator());
		Thread.currentThread().setContextClassLoader(example.getClassLoader());
		TestPersistenceUnitInfo pui = new TestPersistenceUnitInfo("construction", example.getClassLoader());
		addMappedClasses(pui, example.getAdaptor().getCodeModel(), example.getCodeGenerator());
		HibernatePersistence hibernatePersistence = new HibernatePersistence();
		EntityManagerFactory emf = hibernatePersistence.createContainerEntityManagerFactory(pui, new HashMap<String, String>());
		example.initScriptingEngine();
		example.getJavaScriptContext().setAttribute("p", new JpaObjectPersistence(emf), ScriptContext.ENGINE_SCOPE);
	}

	protected static void addMappedClasses(TestPersistenceUnitInfo pui, CodePackage codePackage, AbstractCodeGenerator jcg) {
		for (CodeClassifier cc : codePackage.getClassifiers().values()) {
			if (cc instanceof CodeClass) {
				pui.getManagedClassNames().add(jcg.toQualifiedName(cc.getTypeReference()));
			}
		}
		Collection<CodePackage> values = codePackage.getChildren().values();
		for (CodePackage child : values) {
			addMappedClasses(pui, child, jcg);
		}
	}

}
