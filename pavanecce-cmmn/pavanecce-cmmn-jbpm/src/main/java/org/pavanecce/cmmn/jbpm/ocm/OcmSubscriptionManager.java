package org.pavanecce.cmmn.jbpm.ocm;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
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
import org.pavanecce.cmmn.jbpm.flow.CaseFileItem;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItemTransition;
import org.pavanecce.cmmn.jbpm.instance.AbstractSubscriptionManager;
import org.pavanecce.cmmn.jbpm.instance.CaseInstance;
import org.pavanecce.cmmn.jbpm.instance.CaseSubscriptionKey;
import org.pavanecce.cmmn.jbpm.instance.SubscriptionManager;

public class OcmSubscriptionManager extends AbstractSubscriptionManager<OcmCaseSubscriptionInfo, OcmCaseFileItemSubscriptionInfo> implements
		SubscriptionManager, SynchronousEventListener {
	private OcmObjectPersistence persistence;
	private OcmFactory factory;
	private ThreadLocal<Set<Node>> updatedNodes = new ThreadLocal<Set<Node>>();
	private ThreadLocal<Map<String, OcmCaseSubscriptionInfo>> subscriptions = new ThreadLocal<Map<String, OcmCaseSubscriptionInfo>>();

	public OcmSubscriptionManager(OcmFactory factory) {
		this.factory = factory;
	}

	@Override
	public void subscribe(CaseInstance process, CaseFileItem item, Object target) {
		subscribeToUnknownNumberOfObjects(process, item, target, getPersistence());
	}

	@Override
	public void unsubscribe(CaseInstance process, CaseFileItem caseFileItem, Object target) {
		unsubscribeFromUnknownNumberOfObjects(process, getPersistence(), caseFileItem, target);
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
		this.subscriptions.set(null);
		super.flushEntityManagers();
	}

	private void fireNodeAddedEvent(Event event) {
		try {
			PropertyNodeInfo info = determinePropertyNodeInfo(event);
			if (info != null) {
				for (OcmCaseFileItemSubscriptionInfo si : info.subscriptionInfo.getCaseFileItemSubscriptions()) {
					if (isMatchingAddChild(si, info.javaPropertyName) || isMatchingCreate(si, info.javaPropertyName)) {
						fireEvent(si, info.parentObject, getPersistence().find(event.getIdentifier()));
					}
				}
			}
			getPersistence().commit();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static class PropertyNodeInfo {
		public String propertyClassName;
		public Node parentNode;
		public String javaPropertyName;
		public Object parentObject;
		public OcmCaseSubscriptionInfo subscriptionInfo;
	}

	protected void fireNodeRemoved(Event event) {
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
				for (OcmCaseFileItemSubscriptionInfo si : info.subscriptionInfo.getCaseFileItemSubscriptions()) {
					if (isMatchingRemoveChild(si, info.javaPropertyName) || isMatchingDelete(si, info.javaPropertyName)) {
						fireEvent(si, info.parentObject, empty);
					}
				}
			}
			getPersistence().commit();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected PropertyNodeInfo determinePropertyNodeInfo(Event event) throws RepositoryException, PathNotFoundException, ItemNotFoundException,
			AccessDeniedException {
		String parentPath = event.getPath().substring(0, event.getPath().lastIndexOf("/"));
		String jcrPropertyName = event.getPath().substring(event.getPath().lastIndexOf("/"));
		PropertyNodeInfo info = new PropertyNodeInfo();
		info.parentNode = getPersistence().getSession().getSession().getNode(parentPath);
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
					info.propertyClassName = beanDescriptor.getClassDescriptor().getClassName();
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
				if (info.subscriptionInfo == null) {
					info = null;
				} else {
					info.parentObject = getPersistence().find(info.parentNode.getIdentifier());
				}
			}
		}
		return info;
	}

	protected boolean isBuiltInNodeType(String jcrNodeType) {
		return jcrNodeType.startsWith("nt:") || jcrNodeType.startsWith("mix:");
	}

	protected String getJavaPropertyName(String jcrPropertyName) {
		String[] split = jcrPropertyName.split("\\:");
		String propertyName = split[split.length - 1];
		return propertyName;
	}

	protected void fireUpdateEvents() {
		try {
			Set<Node> set = this.updatedNodes.get();
			if (set != null) {
				for (Node node : set) {
					OcmCaseSubscriptionInfo s = getSubscription(node);
					if (s != null) {
						for (OcmCaseFileItemSubscriptionInfo fis : s.getCaseFileItemSubscriptions()) {
							if (fis.getTransition() == CaseFileItemTransition.UPDATE) {
								Object object = getPersistence().find(node.getIdentifier());
								fireEvent(fis, object, object);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			throw convertExcept(e);
		}
	}

	protected void firePropertyEvent(Event event) {
		try {
			String path = event.getPath();
			String[] split = path.split("\\/");
			String jcrPropertyName = split[split.length - 1];
			Node currentNode = getPersistence().getSession().getSession().getNodeByIdentifier(event.getIdentifier());
			int propertyType = getPropertyType(jcrPropertyName, currentNode);
			if (propertyType == PropertyType.REFERENCE) {
				switch (event.getType()) {
				case Event.PROPERTY_ADDED:
					fireReferenceUpdated(event, currentNode, CaseFileItemTransition.ADD_REFERENCE, jcrPropertyName);
					break;
				case Event.PROPERTY_REMOVED:
					fireReferenceUpdated(event, currentNode, CaseFileItemTransition.REMOVE_REFERENCE, jcrPropertyName);
					break;
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

	protected int getPropertyType(String jcrPropertyName, Node currentNode) throws PathNotFoundException, RepositoryException {
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

	protected void fireReferenceUpdated(Event event, Node currentNode, CaseFileItemTransition standardEvent, String jcrPropertyName) {
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
							String propertyName = getJavaPropertyName(jcrPropertyName);

							for (OcmCaseFileItemSubscriptionInfo si : i.getCaseFileItemSubscriptions()) {
								if (si.getTransition() == standardEvent && si.getRelatedItemName() != null && si.getRelatedItemName().equals(propertyName)) {
									if (currentNode.hasProperty(jcrPropertyName) && event.getType() == Event.PROPERTY_ADDED
											|| standardEvent == CaseFileItemTransition.ADD_REFERENCE) {
										fireReferenceAddedEvents(currentNode, jcrPropertyName, currentObject, si);
									} else if (!currentNode.hasProperty(jcrPropertyName) && event.getType() == Event.PROPERTY_REMOVED
											|| standardEvent == CaseFileItemTransition.REMOVE_REFERENCE) {
										fireReferenceRemovedEvents(currentObject, si);
									} else if (currentNode.hasProperty(jcrPropertyName) && event.getType() == Event.PROPERTY_CHANGED) {
										/*
										 * Now we're in trouble. We don't know
										 * what is old and what is new
										 */
										fireReferenceAddedEvents(currentNode, jcrPropertyName, currentObject, si);
										fireReferenceRemovedEvents(currentObject, si);
									}
								}
							}
						}
					}
				}
			}
			getPersistence().commit();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void fireReferenceAddedEvents(Node currentNode, String jcrPropertyName, Object currentObject, OcmCaseFileItemSubscriptionInfo si)
			throws PathNotFoundException, RepositoryException, ValueFormatException {
		Property prop = currentNode.getProperty(jcrPropertyName);
		if (isPropertyMultiple(currentNode, jcrPropertyName)) {
			Value[] values = prop.getValues();
			for (Value value : values) {
				if (value.getType() == PropertyType.REFERENCE) {
					fireEvent(si, currentObject, getPersistence().getSession().getObjectByUuid(value.getString()));
				}
			}
		} else {
			fireEvent(si, currentObject, getPersistence().getSession().getObjectByUuid(prop.getString()));
		}
	}

	protected void fireReferenceRemovedEvents(Object currentObject, OcmCaseFileItemSubscriptionInfo si) {
		/*
		 * TODO This is not good enough. Still need to get the old value from
		 * somewhere
		 */
		Object oldValue = currentObject;
		fireEvent(si, currentObject, oldValue);
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

	protected OcmCaseSubscriptionInfo getSubscription(Node node) throws RepositoryException {
		Map<String, OcmCaseSubscriptionInfo> map = this.subscriptions.get();
		if (map == null) {
			this.subscriptions.set(map = new HashMap<String, OcmCaseSubscriptionInfo>());
		}
		OcmCaseSubscriptionInfo result = map.get(node.getIdentifier());
		if (result == null) {
			map.put(node.getIdentifier(), result = getPersistence().getSubscription(node.getIdentifier()));
		}
		return result;
	}

	private OcmObjectPersistence getPersistence() {
		if (persistence == null) {
			persistence = new OcmObjectPersistence(factory);
			persistence.start();
		}
		return persistence;
	}

}
