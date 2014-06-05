package org.pavanecce.cmmn.jbpm.xml.handler;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.drools.core.process.core.datatype.impl.type.ObjectDataType;
import org.drools.core.process.core.datatype.impl.type.StringDataType;
import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.jbpm.compiler.xml.ProcessBuildData;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.workflow.core.NodeContainer;
import org.kie.api.definition.process.Node;
import org.pavanecce.cmmn.jbpm.TaskParameters;
import org.pavanecce.cmmn.jbpm.event.CaseEvent;
import org.pavanecce.cmmn.jbpm.flow.Case;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItem;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItemDefinition;
import org.pavanecce.cmmn.jbpm.flow.CaseParameter;
import org.pavanecce.cmmn.jbpm.flow.CaseTask;
import org.pavanecce.cmmn.jbpm.flow.Definitions;
import org.pavanecce.cmmn.jbpm.flow.DiscretionaryItem;
import org.pavanecce.cmmn.jbpm.flow.HumanTask;
import org.pavanecce.cmmn.jbpm.flow.PlanItemDefinition;
import org.pavanecce.cmmn.jbpm.flow.PlanningTable;
import org.pavanecce.cmmn.jbpm.flow.PlanningTableContainer;
import org.pavanecce.cmmn.jbpm.flow.Role;
import org.pavanecce.cmmn.jbpm.flow.Sentry;
import org.pavanecce.cmmn.jbpm.flow.Stage;
import org.pavanecce.cmmn.jbpm.flow.StagePlanItem;
import org.pavanecce.cmmn.jbpm.flow.TableItem;
import org.pavanecce.cmmn.jbpm.flow.TaskDefinition;
import org.pavanecce.cmmn.jbpm.infra.CollectionDataType;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class CaseHandler extends PlanItemContainerHandler implements Handler {
	public static final String CURRENT_EVENT = "currentEvent";
	public static final String CONNECTIONS = "BPMN.Connections";
	public static final String LINKS = "BPMN.ThrowLinks";
	public static final String ASSOCIATIONS = "BPMN.Associations";
	public static final String ERRORS = "BPMN.Errors";
	public static final String ESCALATIONS = "BPMN.Escalations";

	static final String PROCESS_INSTANCE_SIGNAL_EVENT = "kcontext.getProcessInstance().signalEvent(\"";
	static final String RUNTIME_SIGNAL_EVENT = "kcontext.getKnowledgeRuntime().signalEvent(\"";

	public CaseHandler() {
		if ((this.validParents == null) && (this.validPeers == null)) {
			this.validParents = new HashSet<Class<?>>();
			this.validParents.add(Definitions.class);
			this.validPeers = new HashSet<Class<?>>();
			this.validPeers.add(null);
			this.validPeers.add(CaseFileItemDefinition.class);
			this.validPeers.add(RuleFlowProcess.class);
		}
	}

	@Override
	public Object start(final String uri, final String localName, final Attributes attrs, final ExtensibleXmlParser parser) throws SAXException {
		parser.startElementBuilder(localName, attrs);
		IdGenerator.reset();
		String id = attrs.getValue("id");
		String name = attrs.getValue("name");
		String packageName = attrs.getValue("http://www.jboss.org/drools", "packageName");
		Case process = new Case();
		process.setId(id);
		if (name == null) {
			name = id;
		}
		process.setName(name);
		process.setType("RuleFlow");
		if (packageName == null) {
			packageName = "org.pavanecce.cmmn.jbpm";
		}
		process.setPackageName(packageName);
		process.setDynamic(true);
		((ProcessBuildData) parser.getData()).addProcess(process);
		super.startNodeContainer(process, parser);
		VariableScope variableScope = (VariableScope) process.getDefaultContext(VariableScope.VARIABLE_SCOPE);
		List<Variable> variables = variableScope.getVariables();
		Variable var = new Variable();
		var.setName(CURRENT_EVENT);
		var.setType(new ObjectDataType(CaseEvent.class.getName()));
		variables.add(var);
		Set<String> roleNames = new HashSet<String>();
		Collection<Role> roles = process.getRoles();
		for (Role role : roles) {
			roleNames.add(role.getName());
			Variable caseRole = new Variable();
			caseRole.setName(role.getName());
			CollectionDataType coll = new CollectionDataType();
			coll.setElementClassName("java.lang.String");
			caseRole.setType(coll);
			variables.add(caseRole);
		}

		if (!roleNames.contains(TaskParameters.INITIATOR)) {
			Variable initiator = new Variable();
			initiator.setName(TaskParameters.INITIATOR);
			initiator.setType(new StringDataType());
			variables.add(initiator);
		}
		if (!roleNames.contains(TaskParameters.CASE_OWNER)) {
			Variable caseOwner = new Variable();
			caseOwner.setName(TaskParameters.CASE_OWNER);
			caseOwner.setType(new StringDataType());
			variables.add(caseOwner);
		}
		return process;
	}

	@Override
	public Object end(final String uri, final String localName, final ExtensibleXmlParser parser) throws SAXException {
		Element el = (Element) parser.endElementBuilder();
		Case process = (Case) parser.getCurrent();
		Element cpm = (Element) el.getElementsByTagName("casePlanModel").item(0);
		process.setAutoComplete("true".equals(cpm.getAttribute("autoComplete")));
		VariableScope variableScope = process.getVariableScope();
		List<Variable> variables = variableScope.getVariables();
		for (Variable variable : variables) {
			if (variable instanceof CaseFileItem) {
				CaseFileItem c = (CaseFileItem) variable;
				for (Entry<String, CaseFileItem> entry : c.getTargets().entrySet()) {
					if (entry.getValue() == null) {
						entry.setValue(findCaseFileItemById(variableScope, entry.getKey()));
					}
				}
			}
		}
		linkParametersToCaseFileItems(variableScope, process.getInputParameters());
		linkParametersToCaseFileItems(variableScope, process.getOutputParameters());
		doRoleAndDefinitionMapping(process.getPlanItemDefinitions(), process.getRoles(), process.getPlanningTable());
		Collection<PlanItemDefinition> planItemDefinitions = process.getPlanItemDefinitions();
		for (PlanItemDefinition pi : planItemDefinitions) {
			if (pi instanceof TaskDefinition) {
				if (pi instanceof HumanTask) {
					HumanTask ht = (HumanTask) pi;
					Collection<Role> roles = process.getRoles();
					for (Role role : roles) {
						if (role.getElementId().equals(ht.getPerformerRef())) {
							ht.setPerformer(role);
						}
					}
					doRoleAndDefinitionMapping(process.getPlanItemDefinitions(), roles, ht.getPlanningTable());
				}
				linkParametersToCaseFileItems(variableScope, ((TaskDefinition) pi).getInputs());
				linkParametersToCaseFileItems(variableScope, ((TaskDefinition) pi).getOutputs());
			} else if (pi instanceof Stage) {
				doRoleAndDefinitionMapping(process.getPlanItemDefinitions(), process.getRoles(), ((Stage) pi).getPlanningTable());
			}
		}
		linkPlanItems(process, parser);
		for (PlanItemDefinition planItemDefinition : planItemDefinitions) {
			if (planItemDefinition instanceof Stage) {
				super.linkPlanItems((Stage) planItemDefinition, parser);
			}
		}
		linkDiscretionaryItemCriteria(process.getPlanningTable());

		for (PlanItemDefinition planItemDefinition : planItemDefinitions) {
			if (planItemDefinition instanceof PlanningTableContainer) {
				PlanningTable planningTable = ((PlanningTableContainer) planItemDefinition).getPlanningTable();
				linkDiscretionaryItemCriteria(planningTable);
			}
		}
		copyStagePlanItems(process);
		copyDiscretionaryItems(process);
		String exitsString = cpm.getAttribute("exitCriteriaRefs");
		if (exitsString != null) {
			String[] exitCriteriaRefs = exitsString.split("\\ ");
			for (String string : exitCriteriaRefs) {
				for (Node node : process.getNodes()) {
					if (node instanceof Sentry && ((Sentry) node).getElementId().equals(string)) {
						((Sentry) node).setExitsCase(true);
					}
				}
			}
		}
		return process;
	}

	private void linkDiscretionaryItemCriteria(PlanningTable planningTable) {
		if (planningTable != null) {
			for (TableItem tableItem : planningTable.getTableItems()) {
				if (tableItem instanceof DiscretionaryItem) {
					linkDiscretionaryItemCriteria(tableItem.getParentTable().getFirstPlanItemContainer(), (DiscretionaryItem<?>) tableItem);
				} else {
					linkDiscretionaryItemCriteria((PlanningTable) tableItem);
				}
			}
		}
	}

	private void copyDiscretionaryItems(NodeContainer nc) {
		if (nc instanceof PlanningTableContainer) {
			PlanningTable pt = ((PlanningTableContainer) nc).getPlanningTable();
			if (pt != null) {
				copyDiscretionaryItemsInTable(pt);
			}
			Node[] nodes = nc.getNodes();
			for (Node node : nodes) {
				if (node instanceof NodeContainer) {
					copyDiscretionaryItems((NodeContainer) node);
				}
			}
		}
	}

	private void copyDiscretionaryItemsInTable(PlanningTable pt) {
		for (TableItem ti : pt.getTableItems()) {
			if (ti instanceof DiscretionaryItem
					&& (((DiscretionaryItem<?>) ti).getDefinition() instanceof Stage || ((DiscretionaryItem<?>) ti).getDefinition() instanceof CaseTask)) {
				((DiscretionaryItem<?>) ti).copyFromDefinition();
			} else if (ti instanceof PlanningTable) {
				copyDiscretionaryItemsInTable((PlanningTable) ti);
			}
		}
	}

	private void copyStagePlanItems(NodeContainer nc) {
		Node[] nodes = nc.getNodes();
		for (Node node : nodes) {
			if (node instanceof StagePlanItem) {
				StagePlanItem spi = (StagePlanItem) node;
				spi.copyFromStage();
				copyStagePlanItems(spi);
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected void doRoleAndDefinitionMapping(Collection<PlanItemDefinition> defs, Collection<Role> roles, TableItem pt) {
		if (pt != null) {
			doRoleMapping(defs, roles, pt.getAuthorizedRoles());
			if (pt instanceof PlanningTable) {
				Collection<TableItem> tableItems = ((PlanningTable) pt).getTableItems();
				for (TableItem tableItem : tableItems) {
					doRoleMapping(defs, roles, tableItem.getAuthorizedRoles());
					doRoleAndDefinitionMapping(defs, roles, tableItem);
				}
			} else {
				for (PlanItemDefinition pid : defs) {
					@SuppressWarnings("rawtypes")
					DiscretionaryItem di = (DiscretionaryItem<?>) pt;
					if (pid.getElementId().equals(di.getDefinitionRef())) {
						di.setDefinition(pid);
					}
				}
			}
		}
	}

	protected void doRoleMapping(Collection<PlanItemDefinition> defs, Collection<Role> roles, Map<String, Role> authorizedRoles) {
		Set<Entry<String, Role>> entrySet = authorizedRoles.entrySet();
		for (Entry<String, Role> entry : entrySet) {
			for (Role role : roles) {
				if (entry.getValue() == null && entry.getKey().equals(role.getElementId())) {
					entry.setValue(role);
				}
			}
		}
	}

	private void linkParametersToCaseFileItems(VariableScope variableScope, Collection<CaseParameter> inputParameters) {
		for (CaseParameter caseParameter : inputParameters) {
			caseParameter.setBoundVariable(findCaseFileItemById(variableScope, caseParameter.getBindingRef()));
		}
	}

	@Override
	public Class<?> generateNodeFor() {
		return Case.class;
	}

}
