package org.pavanecce.cmmn.jbpm.flow;

import java.io.Serializable;
import java.util.List;

import org.jbpm.workflow.core.node.DataAssociation;

public interface PlanItemDefinition extends Serializable,CMMNElement {

	List<DataAssociation> getInAssociations();

	List<CaseParameter> getInputs();

}
