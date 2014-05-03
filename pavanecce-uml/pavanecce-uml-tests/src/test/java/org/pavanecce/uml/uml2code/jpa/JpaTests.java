package org.pavanecce.uml.uml2code.jpa;

import java.util.Collection;
import java.util.HashMap;

import javax.persistence.EntityManagerFactory;
import javax.script.ScriptContext;

import org.hibernate.ejb.HibernatePersistence;
import org.junit.BeforeClass;
import org.pavanecce.common.code.metamodel.CodeClassifier;
import org.pavanecce.common.code.metamodel.CodePackage;
import org.pavanecce.common.jpa.JpaObjectPersistence;
import org.pavanecce.common.util.ConstructionCaseExample;
import org.pavanecce.uml.uml2code.java.AssociationCollectionCodeDecorator;
import org.pavanecce.uml.uml2code.java.JavaCodeGenerator;

public class JpaTests extends AbstractPersistenceTest {


	@BeforeClass
	public static void setup() throws Exception {
		example = new ConstructionCaseExample("SimplePersistence");
		example.setup(new JavaCodeGenerator(),  new JpaCodeDecorator());
		TestPersistenceUnitInfo pui = new TestPersistenceUnitInfo("construction", example.getClassLoader());
		addMappedClasses(pui, example.getAdaptor().getCodeModel(), example.getJavaCodeGenerator());
		HibernatePersistence hibernatePersistence = new HibernatePersistence();
		EntityManagerFactory emf = hibernatePersistence.createContainerEntityManagerFactory(pui, new HashMap<String, String>());
		example.initScriptingEngine();
		example.getJavaScriptContext().setAttribute("p", new JpaObjectPersistence(emf), ScriptContext.ENGINE_SCOPE);
	}
	protected static void addMappedClasses(TestPersistenceUnitInfo pui, CodePackage codePackage, JavaCodeGenerator jcg) {
		for (CodeClassifier cc : codePackage.getClassifiers().values()) {
			pui.getManagedClassNames().add(jcg.toQualifiedName(cc.getTypeReference()));
		}
		Collection<CodePackage> values = codePackage.getChildren().values();
		for (CodePackage child : values) {
			addMappedClasses(pui, child,jcg);
		}
	}

}
