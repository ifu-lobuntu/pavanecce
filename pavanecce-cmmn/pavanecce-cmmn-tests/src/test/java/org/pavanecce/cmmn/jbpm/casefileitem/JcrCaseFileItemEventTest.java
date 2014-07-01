package org.pavanecce.cmmn.jbpm.casefileitem;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Value;

import org.drools.core.marshalling.impl.ClassObjectMarshallingStrategyAcceptor;
import org.drools.core.marshalling.impl.SerializablePlaceholderResolverStrategy;
import org.jbpm.marshalling.impl.ProcessInstanceResolverStrategy;
import org.kie.api.marshalling.ObjectMarshallingStrategy;
import org.kie.api.runtime.Environment;
import org.pavanecce.cmmn.jbpm.jcr.JcrCasePersistence;
import org.pavanecce.cmmn.jbpm.jcr.JcrCollectionPlaceHolderResolveStrategy;
import org.pavanecce.cmmn.jbpm.jcr.JcrPlaceHolderResolveStrategy;
import org.pavanecce.cmmn.jbpm.jcr.JcrSubscriptionManager;
import org.pavanecce.cmmn.jbpm.jcr.JcrUtil;
import org.pavanecce.cmmn.jbpm.jpa.JpaCollectionPlaceHolderResolverStrategy;
import org.pavanecce.cmmn.jbpm.jpa.JpaPlaceHolderResolverStrategy;
import org.pavanecce.common.jcr.JcrSessionFactory;

public class JcrCaseFileItemEventTest extends AbstractCaseFileItemEventTest {
	private JcrSessionFactory jcrSessionFactory;
	Node housePlan;
	Node house;
	Node constructionCase;

	@Override
	public JcrCasePersistence getPersistence() {
		if (super.persistence == null) {
			super.persistence = new JcrCasePersistence(getJcrSessionFactory(), getRuntimeManager());
		}
		return (JcrCasePersistence) super.persistence;
	}

	@Override
	protected ObjectMarshallingStrategy[] getPlaceholdStrategies(Environment env) {
		return new ObjectMarshallingStrategy[] { new ProcessInstanceResolverStrategy(), new JcrPlaceHolderResolveStrategy(env),
				new JpaPlaceHolderResolverStrategy(env), new JcrCollectionPlaceHolderResolveStrategy(env), new JpaCollectionPlaceHolderResolverStrategy(env),
				new SerializablePlaceholderResolverStrategy(ClassObjectMarshallingStrategyAcceptor.DEFAULT) };
	}

	protected JcrSessionFactory getJcrSessionFactory() {
		if (this.jcrSessionFactory == null) {
			JcrSubscriptionManager sm = new JcrSubscriptionManager(getRuntimeManager());
			this.jcrSessionFactory = new JcrSessionFactory(getJcrSession(), sm);
			sm.setJcrObjectPersistenceFactory(jcrSessionFactory);
		}
		return jcrSessionFactory;
	}

	@Override
	protected void prepareEnvironment(Environment env) {
		super.prepareEnvironment(env);
		env.set(JcrSessionFactory.JCR_SESSION_FACTORY, getJcrSessionFactory());
	}

	protected JcrSubscriptionManager getSubscriptionManager() {
		return (JcrSubscriptionManager) getJcrSessionFactory().getEventListener();
	}

	@Override
	protected void addWallPlanAsChildToHousePlan() throws Exception {
		try {
			getPersistence().start();
			housePlan = getPersistence().find(Node.class, housePlan.getIdentifier());
			Node wallPlans = housePlan.addNode("test:wallPlans", "test:wallPlans");
			Node wallPlan = wallPlans.addNode("test:wallPlan", "test:wallPlan");
			wallPlan.setProperty("test:uuid", UUID.randomUUID().toString());
			wallPlan.setProperty("test:shortDescription", "This wall is skew");
			getPersistence().commit();
		} catch (Exception e) {
			throw JcrUtil.convertException(e);
		}
	}

	@Override
	protected void removeWallPlansAsReferenceFromHouse() {
		try {
			getPersistence().start();
			house = getPersistence().find(Node.class, house.getIdentifier());
			house.setProperty("test:wallPlans", (Value[]) null);
			getPersistence().commit();
		} catch (Exception e) {
			throw JcrUtil.convertException(e);
		}
	}

	@Override
	protected Map<String, Object> prepareCaseParameters() {
		try {
			getPersistence().start();
			Node cases = getJcrSession().getRootNode().getNode("cases");
			constructionCase = cases.addNode("case1", "test:constructionCase");
			constructionCase.setProperty("test:uuid", UUID.randomUUID().toString());
			constructionCase.setProperty("test:name", "MyConstructionCase");
			housePlan = constructionCase.addNode("test:housePlan", "test:housePlan");
			housePlan.setProperty("test:uuid", UUID.randomUUID().toString());
			house = constructionCase.addNode("test:house", "test:house");
			house.setProperty("test:uuid", UUID.randomUUID().toString());
			house.setProperty("test:description", "MyHouse");
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("housePlan", housePlan);
			result.put("house", house);
			getPersistence().commit();
			return result;
		} catch (Exception e) {
			throw JcrUtil.convertException(e);
		}
	}

	@Override
	protected String getProcessFile() {
		return "test/casefileitem/JcrCaseFileItemEventTests.cmmn";
	}

	@Override
	protected void addRoofPlanAsChildToHousePlan() {
		try {
			getPersistence().start();
			housePlan = getPersistence().find(Node.class, housePlan.getIdentifier());
			Node roofPlan = housePlan.addNode("test:roofPlan", "test:roofPlan");
			roofPlan.setProperty("test:uuid", UUID.randomUUID().toString());
			roofPlan.setProperty("test:shortDescription", "My Roof has a hole in it");
			getPersistence().commit();
		} catch (Exception e) {
			throw JcrUtil.convertException(e);
		}
	}

	@Override
	protected void addRoofPlanAsReferenceToHouse() {
		try {
			getPersistence().start();
			housePlan = getPersistence().find(Node.class, housePlan.getIdentifier());
			house = getPersistence().find(Node.class, house.getIdentifier());
			Node roofPlan = housePlan.getNode("test:roofPlan");
			house.setProperty("test:roofPlan", roofPlan);
			getPersistence().commit();
		} catch (Exception e) {
			throw JcrUtil.convertException(e);
		}
	}

	@Override
	protected void addWallPlanAsReferenceToHouse() throws Exception {
		try {
			getPersistence().start();
			housePlan = getPersistence().find(Node.class, housePlan.getIdentifier());
			house = getPersistence().find(Node.class, house.getIdentifier());
			NodeIterator wallPlans = housePlan.getNode("test:wallPlans").getNodes("test:wallPlan");
			Set<Value> values = new HashSet<Value>();
			while (wallPlans.hasNext()) {
				Node object = wallPlans.nextNode();
				values.add(house.getSession().getValueFactory().createValue(object));
			}
			house.setProperty("test:wallPlans", values.toArray(new Value[values.size()]));
			getPersistence().commit();
		} catch (Exception e) {
			throw JcrUtil.convertException(e);
		}
	}

	@Override
	protected void removeRoofPlanAsChildFromHousePlan() {
		try {
			getPersistence().start();
			housePlan = getPersistence().find(Node.class, housePlan.getIdentifier());
			Node roofPlan = housePlan.getNode("test:roofPlan");
			roofPlan.remove();
			getPersistence().commit();
		} catch (Exception e) {
			throw JcrUtil.convertException(e);
		}
	}

	@Override
	protected void removeRoofPlanAsReferenceFromHouse() {
		try {
			try {
				getPersistence().start();
				house = getPersistence().find(Node.class, house.getIdentifier());
				house.setProperty("test:roofPlan", (Node) null);
				getPersistence().commit();
			} catch (Exception e) {
				throw JcrUtil.convertException(e);
			}
		} catch (Exception e) {
			throw JcrUtil.convertException(e);
		}
	}

	@Override
	protected void updateDescriptionOnHouse() {
		try {
			getPersistence().start();
			house = getPersistence().find(Node.class, house.getIdentifier());
			house.setProperty("test:description", "In need of something new");
			getPersistence().commit();
		} catch (Exception e) {
			throw JcrUtil.convertException(e);
		}
	}

	@Override
	protected void removeWallPlansFromHousePlan() {
		try {
			getPersistence().start();
			housePlan = getPersistence().find(Node.class, housePlan.getIdentifier());
			housePlan.getNode("test:wallPlans").remove();
			getPersistence().commit();
		} catch (Exception e) {
			throw JcrUtil.convertException(e);
		}
	}

}
