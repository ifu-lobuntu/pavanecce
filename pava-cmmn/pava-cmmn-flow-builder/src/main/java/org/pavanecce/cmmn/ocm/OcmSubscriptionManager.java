package org.pavanecce.cmmn.ocm;

import javax.jcr.Node;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.pavanecce.cmmn.flow.CaseFileItem;
import org.pavanecce.cmmn.flow.CaseFileItemTransition;
import org.pavanecce.cmmn.instance.AbstractSubscriptionManager;
import org.pavanecce.cmmn.instance.CaseInstance;
import org.pavanecce.cmmn.instance.CaseSubscriptionKey;
import org.pavanecce.cmmn.instance.SubscriptionManager;
import org.pavanecce.cmmn.jpa.JpaCaseFileItemSubscriptionInfo;
import org.pavanecce.cmmn.jpa.JpaCaseSubscriptionInfo;
import org.pavanecce.cmmn.jpa.JpaCaseSubscriptionKey;

;
public class OcmSubscriptionManager extends AbstractSubscriptionManager<JpaCaseSubscriptionInfo, JpaCaseFileItemSubscriptionInfo> implements
		SubscriptionManager, EventListener {
	private OcmObjectPersistence p;

	public OcmSubscriptionManager(ObjectContentManager p) {
		this.p = new OcmObjectPersistence(p);
	}

	@Override
	public void subscribe(CaseInstance process, CaseFileItem item, Object target) {
		subscribeToUnknownNumberOfObjects(process, item, target, p);
	}

	@Override
	public void unsubscribe(CaseInstance process, CaseFileItem caseFileItem, Object target) {
		unsubscribeFromUnknownNumberOfObjects(process, p, caseFileItem, target);
	}

	@Override
	protected JpaCaseFileItemSubscriptionInfo createCaseFileItemSubscriptionInfo() {
		return new JpaCaseFileItemSubscriptionInfo();
	}

	@Override
	protected JpaCaseSubscriptionInfo createCaseSubscriptionInfo(Object currentInstance) {
		return new JpaCaseSubscriptionInfo(currentInstance);
	}

	@Override
	protected CaseSubscriptionKey createCaseSubscriptionKey(Object currentInstance) {
		return new JpaCaseSubscriptionKey(currentInstance);
	}

	@Override
	public void onEvent(EventIterator events) {
		while(events.hasNext()){
			Event event = events.nextEvent();
			switch(event.getType()){
			case Event.NODE_ADDED:
				fireNodeAdded(event);
				break;
			case Event.NODE_REMOVED:
				break;
			case Event.PROPERTY_CHANGED:
				break;
			}
		}
		// TODO Auto-generated method stub
		
	}

	protected void fireNodeAdded(Event event)  {
		try {
			Object o =p.find(event.getIdentifier());
			Node n =p.findNode(event.getIdentifier());
			Node parent = n.getParent();
			String parentName = parent.getDefinition().getName();
			OcmCaseSubscriptionInfo i = p.find(OcmCaseSubscriptionInfo.class, new OcmCaseSubscriptionKey(parent));
			for (OcmCaseFileItemSubscriptionInfo si : i.getCaseFileItemSubscriptions()) {
				if(si.getTransition()==CaseFileItemTransition.CREATE && si.getItemName().equals(parentName)){
					fireEvent(si, o);
				}else if(si.getTransition()==CaseFileItemTransition.ADD_CHILD || si.getTransition()==CaseFileItemTransition.ADD_REFERENCE){
					fireEvent(si, o);
				}
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
