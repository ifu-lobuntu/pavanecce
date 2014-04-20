package org.pavanecce.cmmn.ocm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;

import org.apache.jackrabbit.core.observation.SynchronousEventListener;
import org.pavanecce.cmmn.flow.CaseFileItem;
import org.pavanecce.cmmn.flow.CaseFileItemTransition;
import org.pavanecce.cmmn.instance.AbstractSubscriptionManager;
import org.pavanecce.cmmn.instance.CaseInstance;
import org.pavanecce.cmmn.instance.CaseSubscriptionKey;
import org.pavanecce.cmmn.instance.SubscriptionManager;

public class OcmSubscriptionManager extends AbstractSubscriptionManager<OcmCaseSubscriptionInfo, OcmCaseFileItemSubscriptionInfo> implements
		SubscriptionManager, SynchronousEventListener {
	private OcmObjectPersistence persistence;
	private OcmFactory factory;
	private ThreadLocal<Set<Node>> updatedNodes = new ThreadLocal<Set<Node>>();
	private ThreadLocal<Map<String, OcmCaseSubscriptionInfo>> subscriptions = new ThreadLocal<Map<String, OcmCaseSubscriptionInfo>>();

	public abstract class NodeEventInterpreter {
		public void fireNodeEvent(Event event) {
			try {
				Node newNode = getPersistence().findNode(event.getIdentifier());
				if (newNode.getPrimaryNodeType().isNodeType("mix:referenceable")) {
					// We only support creation of objects that can be
					// referenced,
					// and ignore the rest (e.g. holder nodes for collection
					// of
					// children)
					Node parentNode = newNode.getParent();
					Object value = getPersistence().getSession().getObjectByUuid(event.getIdentifier());
					Object parentObject = null;
					String propertyName = null;
					if (parentNode.getPrimaryNodeType().isNodeType("mix:referenceable")) {
						// Collections aren't (should not be) implemented as
						// referenceables
						// TODO look for a more generic mechanism
						parentObject = getPersistence().find(parentNode.getIdentifier());
						propertyName = newNode.getDefinition().getName();
					} else if (!parentNode.getParent().getPath().equals("/")) {
						// This parentNode is only a holder for the newly
						// added node
						propertyName = newNode.getParent().getDefinition().getName();
						parentObject = getPersistence().find(parentNode.getParent().getIdentifier());
						parentNode = parentNode.getParent();
					} else {
					}
					if (parentObject != null) {
						String[] split = propertyName.split("\\:");
						propertyName = split[split.length - 1];
						if (!(parentObject instanceof OcmCaseSubscriptionInfo)) {
							OcmCaseSubscriptionInfo i = getSubscription(parentNode);
							if (i != null) {
								for (OcmCaseFileItemSubscriptionInfo si : i.getCaseFileItemSubscriptions()) {
									if (isMatch(propertyName, si)) {
										fireEvent(si, parentObject, value);
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

		protected abstract boolean isMatch(String propertyName, OcmCaseFileItemSubscriptionInfo si);

	};

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
				new NodeEventInterpreter() {
					protected boolean isMatch(String propertyName, OcmCaseFileItemSubscriptionInfo si) {
						return (si.getTransition() == CaseFileItemTransition.CREATE && si.getItemName().equals(propertyName))
								|| (si.getTransition() == CaseFileItemTransition.ADD_CHILD && si.getRelatedItemName() != null && si.getRelatedItemName()
										.equals(propertyName));
					}
				}.fireNodeEvent(event);
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
	}

	protected void fireNodeRemoved(Event event) {
		try {
			String parentPath=event.getPath().substring(0,event.getPath().lastIndexOf("/"));
			String propertyName=event.getPath().substring(event.getPath().lastIndexOf("/"));
			Node parentNode = getPersistence().getSession().getSession().getNode(parentPath);
			if (parentNode.getPrimaryNodeType().isNodeType("mix:referenceable")) {
				// Collections aren't (should not be) implemented as
				// referenceables
				// TODO look for a more generic mechanism
//				propertyName = newNode.getDefinition().getName();
			} else if (!parentNode.getParent().getPath().equals("/") && parentNode.getParent().getPrimaryNodeType().isNodeType("mix:referenceable")) {
				// This parentNode is only a holder for the newly
				// added node
				propertyName = parentNode.getDefinition().getName();
				parentNode = parentNode.getParent();
			} else {
			}
			if (parentNode.getPrimaryNodeType().isNodeType("mix:referenceable")) {
				// We only support creation of objects that can be
				// referenced,
				// and ignore the rest (e.g. holder nodes for collection
				// of
				// children)
				Object parentObject = getPersistence().find(parentNode.getIdentifier());
				if (parentObject != null) {
					String[] split = propertyName.split("\\:");
					propertyName = split[split.length - 1];
					if (!(parentObject instanceof OcmCaseSubscriptionInfo)) {
						OcmCaseSubscriptionInfo i = getSubscription(parentNode);
						if (i != null) {
							for (OcmCaseFileItemSubscriptionInfo si : i.getCaseFileItemSubscriptions()) {
								if(si.getTransition()==CaseFileItemTransition.DELETE && si.getItemName().equals(propertyName)){
									fireEvent(si, parentObject, event.getIdentifier());
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
			Property property = currentNode.getProperty(jcrPropertyName);
			if (property.getType() == PropertyType.REFERENCE) {
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
							String[] split = jcrPropertyName.split("\\:");
							String propertyName = split[split.length - 1];

							for (OcmCaseFileItemSubscriptionInfo si : i.getCaseFileItemSubscriptions()) {
								if (si.getTransition() == standardEvent && si.getRelatedItemName() != null && si.getRelatedItemName().equals(propertyName)) {
									Property prop = currentNode.getProperty(jcrPropertyName);
									if (prop.isMultiple()) {
										// TODO figure out which values are NEW
										// and which are OLD
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
