package org.pavanecce.cmmn.ocm;

import javax.jcr.Node;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;

import org.apache.jackrabbit.core.observation.SynchronousEventListener;
import org.pavanecce.cmmn.flow.CaseFileItem;
import org.pavanecce.cmmn.flow.CaseFileItemTransition;
import org.pavanecce.cmmn.instance.AbstractSubscriptionManager;
import org.pavanecce.cmmn.instance.CaseInstance;
import org.pavanecce.cmmn.instance.CaseSubscriptionKey;
import org.pavanecce.cmmn.instance.SubscriptionManager;

;
public class OcmSubscriptionManager extends AbstractSubscriptionManager<OcmCaseSubscriptionInfo, OcmCaseFileItemSubscriptionInfo> implements
		SubscriptionManager, SynchronousEventListener {
	private OcmObjectPersistence persistence;
	private OcmFactory factory;

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
			switch (event.getType()) {
			case Event.NODE_ADDED:
				fireNodeAdded(event);
				break;
			case Event.NODE_REMOVED:
				break;
			case Event.PROPERTY_CHANGED:
				break;
			}
		}
	}

	protected void fireNodeAdded(Event event) {
		try {
			Node newNode = getPersistence().findNode(event.getIdentifier());
			Node parentNode = newNode.getParent();
			Object o=null;
			String propertyName = null;
			if (parentNode.getPrimaryNodeType().isNodeType("mix:referenceable")) {
				// Collections aren't (should not be) implemented as
				// referenceables
				// TODO look for a more generic mechanism
				o = getPersistence().find(parentNode.getIdentifier());
				propertyName = newNode.getDefinition().getName();
			} else if (!parentNode.getParent().getPath().equals("/")) {
				propertyName = newNode.getParent().getDefinition().getName();
				o = getPersistence().find(parentNode.getParent().getIdentifier());
			}else{
			}
			if (o != null) {
				String[] split = propertyName.split("\\:");
				propertyName = split[split.length - 1];
				if (!(o instanceof OcmCaseSubscriptionInfo)) {
					OcmCaseSubscriptionInfo i = getPersistence().find(OcmCaseSubscriptionInfo.class, new OcmCaseSubscriptionKey(o));
					if (i != null) {
						for (OcmCaseFileItemSubscriptionInfo si : i.getCaseFileItemSubscriptions()) {
							if (si.getTransition() == CaseFileItemTransition.CREATE && si.getItemName().equals(propertyName)) {
								fireEvent(si, o);
							} else if (si.getTransition() == CaseFileItemTransition.ADD_CHILD || si.getTransition() == CaseFileItemTransition.ADD_REFERENCE) {
								fireEvent(si, o);
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

	private OcmObjectPersistence getPersistence() {
		if (persistence == null) {
			persistence = new OcmObjectPersistence(factory);
			persistence.start();
		}
		return persistence;
	}

}
