package org.pavanecce.cmmn.jbpm.flow.builder;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.drools.core.process.core.datatype.impl.type.ObjectDataType;
import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.jbpm.compiler.xml.ProcessBuildData;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.pavanecce.cmmn.jbpm.flow.Case;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItem;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItemDefinition;
import org.pavanecce.cmmn.jbpm.flow.CaseParameter;
import org.pavanecce.cmmn.jbpm.flow.Definitions;
import org.pavanecce.cmmn.jbpm.flow.HumanTask;
import org.pavanecce.cmmn.jbpm.flow.PlanItemDefinition;
import org.pavanecce.cmmn.jbpm.flow.Role;
import org.pavanecce.cmmn.jbpm.flow.Stage;
import org.pavanecce.cmmn.jbpm.instance.CaseEvent;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class CaseHandler extends PlanItemContainerHandler implements Handler {
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
		String id = attrs.getValue("id");
		String name = attrs.getValue("name");
		String packageName = attrs.getValue("http://www.jboss.org/drools", "packageName");
		// String dynamic = attrs.getValue("http://www.jboss.org/drools",
		// "adHoc");
		// String version = attrs.getValue("http://www.jboss.org/drools",
		// "version");

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
		process.setAutoComplete(false);
		// if (version != null) {
		// process.setVersion(version);
		// }

		((ProcessBuildData) parser.getData()).addProcess(process);
		// register the definitions object as metadata of process.
		// process.setMetaData("Definitions", parser.getParent());
		// register bpmn2 imports as meta data of process
		// for unique id's of nodes, start with one to avoid returning wrong
		// nodes for dynamic nodes
		super.startNodeContainer(process,parser);
		VariableScope variableScope = (VariableScope) process.getDefaultContext(VariableScope.VARIABLE_SCOPE);
		List<Variable> variables = variableScope.getVariables();
		Variable var = new Variable();
		var.setName("currentEvent");
		var.setType(new ObjectDataType(CaseEvent.class.getName()));
		variables.add(var);
		return process;
	}



	@Override
	public Object end(final String uri, final String localName, final ExtensibleXmlParser parser) throws SAXException {
		parser.endElementBuilder();

		Case process = (Case) parser.getCurrent();
		VariableScope variableScope = process.getVariableScope();
		List<Variable> variables = variableScope.getVariables();
		for (Variable variable : variables) {
			if (variable instanceof CaseFileItem) {
				CaseFileItem c = (CaseFileItem) variable;
				if (c.getTargetRefs() != null) {
					for (String string : c.getTargetRefs()) {
						c.addTarget(findCaseFileItemById(variableScope, string));
					}
				}
			}
		}
		linkParametersToCaseFileItems(variableScope, process.getInputParameters());
		linkParametersToCaseFileItems(variableScope, process.getOutputParameters());
		Collection<PlanItemDefinition> planItemDefinitions = process.getPlanItemDefinitions();
		for (PlanItemDefinition pi : planItemDefinitions) {
			if (pi instanceof HumanTask) {
				HumanTask ht = (HumanTask) pi;
				for (Role role : process.getRoles()) {
					if (role.getElementId().equals(ht.getPerformerRef())) {
						ht.setPerformer(role);
						ht.getWork().setParameter("ActorId", role.getName());
					}
				}
				linkParametersToCaseFileItems(variableScope, ht.getInputs());
				linkParametersToCaseFileItems(variableScope, ht.getOutputs());
			}
		}
		linkPlanItems(process,parser);
		for (PlanItemDefinition planItemDefinition : planItemDefinitions) {
			if(planItemDefinition instanceof Stage){
				super.linkPlanItems((Stage) planItemDefinition, parser);
			}
		}
		return process;
	}

	private void linkParametersToCaseFileItems(VariableScope variableScope, List<CaseParameter> inputParameters) {
		for (CaseParameter caseParameter : inputParameters) {
			caseParameter.setVariable(findCaseFileItemById(variableScope, caseParameter.getBindingRef()));
		}
	}

	@Override
	public Class<?> generateNodeFor() {
		return Case.class;
	}

}
