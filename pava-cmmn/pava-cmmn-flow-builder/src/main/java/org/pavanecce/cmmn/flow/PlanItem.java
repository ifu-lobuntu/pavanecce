package org.pavanecce.cmmn.flow;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.drools.core.process.core.ParameterDefinition;
import org.drools.core.process.core.Work;
import org.drools.core.process.core.impl.WorkImpl;
import org.jbpm.workflow.core.node.WorkItemNode;

public class PlanItem extends WorkItemNode implements CMMNElement {
	private static final Work NO_WORK = new WorkImpl();
	static{
		NO_WORK.setName("NoWork");
	}
	private Map<String, Sentry> entryCriteria = new HashMap<String, Sentry>();
	private Map<String, Sentry> exitCriteria = new HashMap<String, Sentry>();
	private String definitionRef;
	private PlanItemDefinition definition;
	private String elementId;
	public Map<String, Sentry> getEntryCriteria() {
		return Collections.unmodifiableMap(entryCriteria);
	}
	public Map<String, Sentry> getExitCriteria() {
		return Collections.unmodifiableMap(exitCriteria);
	}
	public void addEntryCriterionRef(String s){
		entryCriteria.put(s, null);
	}
	public void addExitCriterionRef(String s){
		exitCriteria.put(s, null);
	}
	public void putEntryCriterion(String s,Sentry c){
		entryCriteria.put(s, c);
	}
	public void putExitCriterion(String s,Sentry c){
		exitCriteria.put(s, c);
	}
	public String getDefinitionRef() {
		return definitionRef;
	}
	public void setDefinitionRef(String definitionRef) {
		this.definitionRef = definitionRef;
	}
	public void setDefinition(PlanItemDefinition findPlanItemDefinition) {
		this.definition=findPlanItemDefinition;
	}
	public PlanItemDefinition getDefinition() {
		return definition;
	}
	public String getElementId() {
		return elementId;
	}
	public void setElementId(String elementId) {
		this.elementId = elementId;
	}
	@Override
	public Work getWork() {
		if(definition instanceof WorkItemNode){
			return ((WorkItemNode) definition).getWork();
		}else{
			//TODO rethink this
			return NO_WORK;
		}
	}
	
}
