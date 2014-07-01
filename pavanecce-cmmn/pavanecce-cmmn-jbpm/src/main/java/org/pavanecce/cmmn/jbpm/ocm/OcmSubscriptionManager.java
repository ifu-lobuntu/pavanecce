package org.pavanecce.cmmn.jbpm.ocm;

import static org.pavanecce.cmmn.jbpm.flow.CaseFileItemTransition.ADD_CHILD;
import static org.pavanecce.cmmn.jbpm.flow.CaseFileItemTransition.CREATE;
import static org.pavanecce.cmmn.jbpm.flow.CaseFileItemTransition.DELETE;
import static org.pavanecce.cmmn.jbpm.flow.CaseFileItemTransition.REMOVE_CHILD;
import static org.pavanecce.cmmn.jbpm.flow.CaseFileItemTransition.UPDATE;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;

import org.apache.jackrabbit.core.observation.SynchronousEventListener;
import org.apache.jackrabbit.ocm.mapper.model.BeanDescriptor;
import org.apache.jackrabbit.ocm.mapper.model.ClassDescriptor;
import org.apache.jackrabbit.ocm.query.Filter;
import org.apache.jackrabbit.ocm.query.Query;
import org.kie.api.runtime.manager.RuntimeManager;
import org.pavanecce.cmmn.jbpm.event.AbstractPersistentSubscriptionManager;
import org.pavanecce.cmmn.jbpm.event.CaseFileItemSubscriptionInfo;
import org.pavanecce.cmmn.jbpm.event.CaseSubscriptionInfo;
import org.pavanecce.cmmn.jbpm.event.CaseSubscriptionKey;
import org.pavanecce.cmmn.jbpm.event.DemarcatedSubscriptionContext;
import org.pavanecce.cmmn.jbpm.event.SubscriptionManager;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItemTransition;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.CaseInstance;
import org.pavanecce.common.ocm.ObjectContentManagerFactory;
import org.pavanecce.common.ocm.OcmObjectPersistence;
import org.pavanecce.common.util.ObjectPersistence;

public class OcmSubscriptionManager extends AbstractPersistentSubscriptionManager<OcmCaseSubscriptionInfo, OcmCaseFileItemSubscriptionInfo> implements
		SubscriptionManager, SynchronousEventListener {
	private OcmCasePersistence persistence;
	private ObjectContentManagerFactory factory;
	private ThreadLocal<Set<Node>> updatedNodes = new ThreadLocal<Set<Node>>();
	private RuntimeManager runtimeManager;

	public OcmSubscriptionManager(RuntimeManager runtimeManager) {
		super();
		this.runtimeManager = runtimeManager;
	}

	public void setOcmFactory(ObjectContentManagerFactory factory) {
		this.factory = factory;
	}

	@Override
	protected OcmCaseFileItemSubscriptionInfo createCaseFileItemSubscriptionInfo() {
		return new OcmCaseFileItemSubscriptionInfo();
	}

	@Override
	protected OcmCaseSubscriptionInfo createCaseSubscriptionInfo(Object currentInstance) {
		return new OcmCaseSubscriptionInfo(currentInstance);
	}

	@Override
	protected CaseSubscriptionKey createCaseSubscriptionKey(Object currentInstance) {
		return new OcmCaseSubscriptionKey(currentInstance);
	}

	@Override
	public void onEvent(EventIterator events) {
		try {
			while (events.hasNext()) {
				Event event = events.nextEvent();
				try {
					if (event.getPath().startsWith("/subscriptions")) {
						continue;
					}
				} catch (RepositoryException e) {
				}
				switch (event.getType()) {
				case Event.NODE_ADDED:
					fireNodeAddedEvent(event);
					break;
				case Event.NODE_REMOVED:
					fireNodeRemoved(event);
					break;
				case Event.PROPERTY_CHANGED:
				case Event.PROPERTY_ADDED:
				case Event.PROPERTY_REMOVED:
					firePropertyEvent(event);
					break;
				}
			}
			fireUpdateEvents();
			this.updatedNodes.set(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void fireNodeAddedEvent(Event event) {
		try {
			PropertyNodeInfo info = determinePropertyNodeInfo(event);
			if (info != null) {
				Object object = getPersistence().find(event.getIdentifier());
				if (info.subscriptionInfo != null) {
					fireAddsAndCreates(info, object, info.subscriptionInfo.getCaseFileItemSubscriptions());
				}
				fireAddsAndCreates(info, object, DemarcatedSubscriptionContext.getSubscriptionsInScopeForFor(object, CREATE));
				fireAddsAndCreates(info, object, DemarcatedSubscriptionContext.getSubscriptionsInScopeForFor(info.parentObject, ADD_CHILD));
			}
		} catch (RuntimeException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void fireAddsAndCreates(PropertyNodeInfo info, Object object, Set<? extends CaseFileItemSubscriptionInfo> caseFileItemSubscriptions) {
		for (CaseFileItemSubscriptionInfo si : caseFileItemSubscriptions) {
			if (isMatchingAddChild(si, info.javaPropertyName) || isMatchingCreate(si, info.javaPropertyName)) {
				queueEvent(si, info.parentObject, object);
			}
		}
	}

	public static class PropertyNodeInfo {
		public String propertyClassName;
		public Node parentNode;
		public String javaPropertyName;
		public Object parentObject;
		public OcmCaseSubscriptionInfo subscriptionInfo;
	}

	private void fireNodeRemoved(Event event) {
		try {
			PropertyNodeInfo info = determinePropertyNodeInfo(event);
			if (info != null) {
				Object empty = Class.forName(info.propertyClassName).newInstance();
				Member idMember = OcmIdUtil.INSTANCE.findIdMember(empty.getClass());
				if (idMember instanceof Field) {
					((Field) idMember).setAccessible(true);
					((Field) idMember).set(empty, event.getIdentifier());
				} else {
					((Method) idMember).setAccessible(true);
					((Method) idMember).invoke(empty, event.getIdentifier());
				}
				if (info.subscriptionInfo != null) {
					fireRemovesAndDeletes(info, empty, info.subscriptionInfo.getCaseFileItemSubscriptions());
				}
				fireRemovesAndDeletes(info, empty, DemarcatedSubscriptionContext.getSubscriptionsInScopeForFor(empty, DELETE));
				fireRemovesAndDeletes(info, empty, DemarcatedSubscriptionContext.getSubscriptionsInScopeForFor(info.parentObject, REMOVE_CHILD));

			}
		} catch (RuntimeException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void fireRemovesAndDeletes(PropertyNodeInfo info, Object empty, Set<? extends CaseFileItemSubscriptionInfo> caseFileItemSubscriptions) {
		for (CaseFileItemSubscriptionInfo si : new HashSet<CaseFileItemSubscriptionInfo>(caseFileItemSubscriptions)) {
			if (isMatchingRemoveChild(si, info.javaPropertyName) || isMatchingDelete(si, info.javaPropertyName)) {
				queueEvent(si, info.parentObject, empty);
			}
		}
	}

	private PropertyNodeInfo determinePropertyNodeInfo(Event event) throws RepositoryException, PathNotFoundException, ItemNotFoundException,
			AccessDeniedException, NoSuchFieldException, SecurityException, ClassNotFoundException {
		String parentPath = event.getPath().substring(0, event.getPath().lastIndexOf("/"));
		String jcrPropertyName = event.getPath().substring(event.getPath().lastIndexOf("/"));
		PropertyNodeInfo info = new PropertyNodeInfo();
		try {
			info.parentNode = getPersistence().getObjectContentManager().getSession().getNode(parentPath);
		} catch (Exception e) {
			return null;
		}
		info.javaPropertyName = getJavaPropertyName(jcrPropertyName);
		info.propertyClassName = null;
		if (info.parentNode.getPrimaryNodeType().isNodeType("mix:referenceable")) {
			// The node added/removed is either an @Bean property or the Node
			// holding the collection when an entry is added to the collection
			// for the first time or when the entire collection is removed
			if (isBuiltInNodeType(info.parentNode.getDefinition().getRequiredPrimaryTypeNames()[0])) {
				info = null;
			} else {
				ClassDescriptor cd = getPersistence().getClassDescriptor(info.parentNode.getDefinition().getRequiredPrimaryTypeNames()[0]);
				BeanDescriptor beanDescriptor = cd.getBeanDescriptor(info.javaPropertyName);
				if (beanDescriptor == null) {
					// It is the node holding a collection that is being
					// removed. In such a case there will be a
					// collectionDescriptor, not a bean descriptor.
					// Abort.
					info = null;
				} else {
					info.propertyClassName = Class.forName(cd.getClassName()).getDeclaredField(beanDescriptor.getFieldName()).getType().getName();
				}
			}
		} else if (!info.parentNode.getParent().getPath().equals("/") && info.parentNode.getParent().getPrimaryNodeType().isNodeType("mix:referenceable")) {
			// The parentNode is only a holder for the newly
			// added node in a collection node. Get to the actual parent
			info.javaPropertyName = getJavaPropertyName(info.parentNode.getDefinition().getName());
			info.parentNode = info.parentNode.getParent();
			if (isBuiltInNodeType(info.parentNode.getDefinition().getRequiredPrimaryTypeNames()[0])) {
				info = null;
			} else {
				ClassDescriptor cd = getPersistence().getClassDescriptor(info.parentNode.getDefinition().getRequiredPrimaryTypeNames()[0]);
				info.propertyClassName = cd.getCollectionDescriptor(info.javaPropertyName).getElementClassName();
			}
		} else {
			info = null;
		}
		if (info != null) {
			if (info.parentNode.getDefinition().getRequiredPrimaryTypes()[0].getName().equals("i:caseFileItemSubscription")) {
				info = null;
			} else {
				info.subscriptionInfo = getSubscription(info.parentNode);
				info.parentObject = getPersistence().find(info.parentNode.getIdentifier());
			}
		}
		return info;
	}

	private boolean isBuiltInNodeType(String jcrNodeType) {
		return jcrNodeType.startsWith("nt:") || jcrNodeType.startsWith("mix:");
	}

	private String getJavaPropertyName(String jcrPropertyName) {
		String[] split = jcrPropertyName.split("\\:");
		String propertyName = split[split.length - 1];
		return propertyName;
	}

	private void fireUpdateEvents() {
		try {
			Set<Node> set = this.updatedNodes.get();
			if (set != null) {
				for (Node node : set) {
					OcmCaseSubscriptionInfo s = getSubscription(node);
					if (s != null) {
						Set<? extends CaseFileItemSubscriptionInfo> caseFileItemSubscriptions = s.getCaseFileItemSubscriptions();
						fireUpdates(node, caseFileItemSubscriptions);
					}
					boolean hasClass = false;
					try {
						hasClass = getPersistence().getClassDescriptor(node.getPrimaryNodeType().getName()) != null;
					} catch (Exception e) {

					}
					if (hasClass) {
						Object find = getPersistence().find(null, node.getIdentifier());
						fireUpdates(node, DemarcatedSubscriptionContext.getSubscriptionsInScopeForFor(find, UPDATE));
					}
				}
			}
		} catch (Exception e) {
			throw convertExcept(e);
		}
	}

	protected void fireUpdates(Node node, Set<? extends CaseFileItemSubscriptionInfo> caseFileItemSubscriptions) throws RepositoryException {
		for (CaseFileItemSubscriptionInfo fis : caseFileItemSubscriptions) {
			if (fis.getTransition() == CaseFileItemTransition.UPDATE) {
				Object object = getPersistence().find(node.getIdentifier());
				queueEvent(fis, object, object);
			}
		}
	}

	private void firePropertyEvent(Event event) {
		try {
			String path = event.getPath();
			String[] split = path.split("\\/");
			String jcrPropertyName = split[split.length - 1];
			Node currentNode = getPersistence().getObjectContentManager().getSession().getNodeByIdentifier(event.getIdentifier());
			int propertyType = getPropertyType(jcrPropertyName, currentNode);
			if (propertyType == PropertyType.REFERENCE) {
				switch (event.getType()) {
				case Event.PROPERTY_ADDED:
					fireReferenceUpdated(event, currentNode, CaseFileItemTransition.ADD_REFERENCE, jcrPropertyName);
					break;
				case Event.PROPERTY_REMOVED:
					fireReferenceUpdated(event, currentNode, CaseFileItemTransition.REMOVE_REFERENCE, jcrPropertyName);
					break;
				case Event.PROPERTY_CHANGED:
					fireReferenceUpdated(event, currentNode, CaseFileItemTransition.ADD_REFERENCE, jcrPropertyName);
					fireReferenceUpdated(event, currentNode, CaseFileItemTransition.REMOVE_REFERENCE, jcrPropertyName);
				}
			} else {
				Set<Node> set = updatedNodes.get();
				if (set == null) {
					updatedNodes.set(set = new HashSet<Node>());
				}
				set.add(currentNode);
			}
		} catch (Exception e) {
			throw convertExcept(e);
		}
	}

	private int getPropertyType(String jcrPropertyName, Node currentNode) throws PathNotFoundException, RepositoryException {
		if (currentNode.hasProperty(jcrPropertyName)) {
			Property property = currentNode.getProperty(jcrPropertyName);
			int propertyType = property.getType();
			return propertyType;
		} else {
			NodeType def = currentNode.getDefinition().getRequiredPrimaryTypes()[0];
			for (PropertyDefinition pd : def.getDeclaredPropertyDefinitions()) {
				if (pd.getName().equals(jcrPropertyName)) {
					return pd.getRequiredType();
				}
			}
			return PropertyType.STRING;
		}
	}

	private RuntimeException convertExcept(Exception e) {
		return (RuntimeException) (e instanceof RuntimeException ? e : new RuntimeException(e));
	}

	private void fireReferenceUpdated(Event event, Node currentNode, CaseFileItemTransition standardEvent, String jcrPropertyName) {
		try {
			if (currentNode.getPrimaryNodeType().isNodeType("mix:referenceable")) {
				// We only support creation of objects that can be referenced,
				// and ignore the rest (e.g. holder nodes for collection of
				// children)
				Object currentObject = getPersistence().find(event.getIdentifier());
				if (currentObject != null) {
					if (!(currentObject instanceof OcmCaseSubscriptionInfo)) {
						OcmCaseSubscriptionInfo i = getSubscription(currentNode);
						if (i != null) {
							fireReferenceUpdate(event, currentNode, standardEvent, jcrPropertyName, currentObject, i.getCaseFileItemSubscriptions());
						}
						fireReferenceUpdate(event, currentNode, standardEvent, jcrPropertyName, currentObject,
								DemarcatedSubscriptionContext.getSubscriptionsInScopeForFor(currentObject, standardEvent));
					}
				}
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void fireReferenceUpdate(Event event, Node currentNode, CaseFileItemTransition standardEvent, String jcrPropertyName, Object currentObject,
			Collection<? extends CaseFileItemSubscriptionInfo> caseFileItemSubscriptions) throws RepositoryException, PathNotFoundException,
			ValueFormatException {
		String propertyName = getJavaPropertyName(jcrPropertyName);
		for (CaseFileItemSubscriptionInfo si : caseFileItemSubscriptions) {
			if (si.getTransition() == standardEvent && (si.getRelatedItemName() == null || si.getRelatedItemName().equals(propertyName))) {
				if (currentNode.hasProperty(jcrPropertyName) && event.getType() == Event.PROPERTY_ADDED
						&& standardEvent == CaseFileItemTransition.ADD_REFERENCE) {
					fireReferenceAddedEvents(currentNode, jcrPropertyName, currentObject, si);
				} else if (event.getType() == Event.PROPERTY_REMOVED && standardEvent == CaseFileItemTransition.REMOVE_REFERENCE) {
					fireReferenceRemovedEvents(currentObject, si);
				} else if (currentNode.hasProperty(jcrPropertyName) && event.getType() == Event.PROPERTY_CHANGED) {
					/*
					 * TODO Now we're in trouble. We don't know what is old and what is new
					 */
					if (standardEvent == CaseFileItemTransition.REMOVE_REFERENCE) {
						fireReferenceRemovedEvents(currentObject, si);
					} else {
						fireReferenceAddedEvents(currentNode, jcrPropertyName, currentObject, si);
					}
				} else {
				}
			}
		}
	}

	private void fireReferenceAddedEvents(Node currentNode, String jcrPropertyName, Object currentObject, CaseFileItemSubscriptionInfo si)
			throws PathNotFoundException, RepositoryException, ValueFormatException {
		Property prop = currentNode.getProperty(jcrPropertyName);
		if (isPropertyMultiple(currentNode, jcrPropertyName)) {
			Value[] values = prop.getValues();
			for (Value value : values) {
				if (value.getType() == PropertyType.REFERENCE) {
					queueEvent(si, currentObject, getPersistence().getObjectContentManager().getObjectByUuid(value.getString()));
				}
			}
		} else {
			queueEvent(si, currentObject, getPersistence().getObjectContentManager().getObjectByUuid(prop.getString()));
		}
	}

	private void fireReferenceRemovedEvents(Object currentObject, CaseFileItemSubscriptionInfo si) {
		/*
		 * TODO This is not good enough. Still need to get the old value from somewhere
		 */
		Object oldValue = currentObject;
		queueEvent(si, currentObject, oldValue);
	}

	private boolean isPropertyMultiple(Node currentNode, String jcrPropertyName) throws PathNotFoundException, RepositoryException {
		if (currentNode.hasProperty(jcrPropertyName)) {
			Property property = currentNode.getProperty(jcrPropertyName);
			return property.isMultiple();
		} else {
			NodeType def = currentNode.getDefinition().getRequiredPrimaryTypes()[0];
			for (PropertyDefinition pd : def.getDeclaredPropertyDefinitions()) {
				if (pd.getName().equals(jcrPropertyName)) {
					return pd.isMultiple();
				}
			}
			return false;
		}
	}

	private OcmCaseSubscriptionInfo getSubscription(Node node) throws RepositoryException {
		OcmCaseSubscriptionKey key = getPersistence().getSubscription(node);
		if (key != null) {
			Map<CaseSubscriptionKey, CaseSubscriptionInfo<?>> cachedSubscriptions = getCachedSubscriptions(getPersistence().getDelegate());
			CaseSubscriptionInfo<?> ocmCaseSubscriptionInfo = cachedSubscriptions.get(key);
			if (ocmCaseSubscriptionInfo == null) {
				CaseSubscriptionInfo<?> find = getPersistence().find(null, key);
				if (find != null) {
					cachedSubscriptions.put(key, ocmCaseSubscriptionInfo = find);
				}
			}
			return (OcmCaseSubscriptionInfo) ocmCaseSubscriptionInfo;
		} else {
			return null;
		}
	}

	private OcmCasePersistence getPersistence() {
		if (persistence == null) {
			persistence = new OcmCasePersistence(factory, runtimeManager);
			persistence.start();
		}
		return persistence;
	}

	@Override
	public ObjectPersistence getObjectPersistence(CaseInstance p) {
		return getPersistence();
	}

	@Override
	protected Collection<OcmCaseSubscriptionInfo> getAllSubscriptionsAgainst(CaseInstance caseInstance, ObjectPersistence em) {
		OcmObjectPersistence oop = (OcmObjectPersistence) em;
		Filter f = oop.getObjectContentManager().getQueryManager().createFilter(OcmCaseFileItemSubscriptionInfo.class);
		f.addEqualTo("processInstanceId", caseInstance.getId());
		Query q = oop.getObjectContentManager().getQueryManager().createQuery(f);
		@SuppressWarnings("unchecked")
		Collection<OcmCaseFileItemSubscriptionInfo> t = oop.getObjectContentManager().getObjects(q);
		Map<OcmCaseSubscriptionKey, OcmCaseSubscriptionInfo> result = new HashMap<OcmCaseSubscriptionKey, OcmCaseSubscriptionInfo>();
		for (OcmCaseFileItemSubscriptionInfo i : t) {
			result.put(i.getCaseSubscription().getId(), i.getCaseSubscription());
		}
		return result.values();
	}

}
