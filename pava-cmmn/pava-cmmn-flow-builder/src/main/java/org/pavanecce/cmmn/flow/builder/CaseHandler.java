package org.pavanecce.cmmn.flow.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.drools.core.xml.BaseAbstractHandler;
import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.jbpm.compiler.xml.ProcessBuildData;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.workflow.core.impl.ConnectionImpl;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.Join;
import org.jbpm.workflow.core.node.Split;
import org.jbpm.workflow.core.node.StartNode;
import org.kie.api.definition.process.Node;
import org.pavanecce.cmmn.flow.Case;
import org.pavanecce.cmmn.flow.CaseFileItem;
import org.pavanecce.cmmn.flow.CaseFileItemDefinition;
import org.pavanecce.cmmn.flow.CaseParameter;
import org.pavanecce.cmmn.flow.Definitions;
import org.pavanecce.cmmn.flow.HumanTaskNode;
import org.pavanecce.cmmn.flow.OnCaseFileItemPart;
import org.pavanecce.cmmn.flow.OnPart;
import org.pavanecce.cmmn.flow.OnPlanItemPart;
import org.pavanecce.cmmn.flow.PlanItem;
import org.pavanecce.cmmn.flow.PlanItemDefinition;
import org.pavanecce.cmmn.flow.Role;
import org.pavanecce.cmmn.flow.Sentry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class CaseHandler extends BaseAbstractHandler implements Handler {
	private static String DEFAULT = org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE;

	private static final Logger logger = LoggerFactory.getLogger(CaseHandler.class);

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
			packageName = "org.pavanecce.cmmn";
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
		StartNode start = new StartNode();
		start.setId(0);
		process.addNode(start);
		start.setName("defaultStart");
		Split split = new Split(Split.TYPE_AND);
		split.setId(1);
		process.addNode(split);
		split.setName("defaultSplit");
		new ConnectionImpl(start, DEFAULT, split, DEFAULT);
		EndNode end = new EndNode();
		end.setName("defaultEnd");
		end.setId(2);
		process.addNode(end);
		return process;
	}

	public Object end(final String uri, final String localName, final ExtensibleXmlParser parser) throws SAXException {
		parser.endElementBuilder();

		Case process = (Case) parser.getCurrent();
		Node defaultStart = process.getNode(1);
		Node defaultEnd = process.getNode(2);
		Join defaultJoin = null;

		// TODO rather check if there is no final node - exitCriteria will be
		// final nodes
		if (defaultEnd.getIncomingConnections().isEmpty()) {
			defaultJoin = new Join();
			defaultJoin.setId(3);
			defaultJoin.setType(Join.TYPE_AND);
			process.addNode(defaultJoin);
			new ConnectionImpl(defaultJoin, DEFAULT, defaultEnd, DEFAULT);
		}
		VariableScope variableScope = process.getVariableScope();
		for (CaseParameter caseParameter : process.getCaseParameters()) {
			caseParameter.setVariable(findCaseFileItemById(variableScope, caseParameter.getBindingRef()));
		}
		List<Variable> variables = variableScope.getVariables();
		for (Variable variable : variables) {
			CaseFileItem c = (CaseFileItem) variable;
			if (c.getTargetRefs() != null) {
				for (String string : c.getTargetRefs()) {
					c.addTarget(findCaseFileItemById(variableScope, string));
				}
			}
		}
		for (Node node : process.getNodes()) {
			if (node instanceof PlanItem) {
				PlanItem planItem = (PlanItem) node;
				if (defaultJoin != null) {
					new ConnectionImpl(planItem, DEFAULT, defaultJoin, DEFAULT);
				}
				planItem.setDefinition(findPlanItemDefinition(process, planItem.getDefinitionRef()));
				for (String string : new ArrayList<String>(planItem.getEntryCriteria().keySet())) {
					Sentry entry = findSentry(process, string);
					planItem.putEntryCriterion(string, entry);
					new ConnectionImpl(entry, DEFAULT, planItem, DEFAULT);
				}
				for (String string : new ArrayList<String>(planItem.getExitCriteria().keySet())) {
					Sentry exit = findSentry(process, string);
					planItem.putExitCriterion(string, exit);
				}
			} else if (node instanceof Sentry) {
				new ConnectionImpl(defaultStart, DEFAULT, node, DEFAULT);
				Sentry sentry = (Sentry) node;
				for (OnPart onPart : sentry.getOnParts()) {
					if (onPart instanceof OnPlanItemPart) {
						OnPlanItemPart apip = (OnPlanItemPart) onPart;
						apip.setPlanItem(findPlanItem(process, apip.getSourceRef()));
					} else {
						OnCaseFileItemPart ocfip = (OnCaseFileItemPart) onPart;
						ocfip.setCaseFileItem(findCaseFileItemById(variableScope, ocfip.getSourceRef()));
					}
				}
			}
		}
		for (PlanItemDefinition pi : process.getPlanItemDefinitions()) {
			if (pi instanceof HumanTaskNode) {
				for (Role role : process.getRoles()) {
					if (role.getElementId().equals(((HumanTaskNode) pi).getPerformerRef())) {
						((HumanTaskNode) pi).setPerformer(role);
					}

				}
			}
		}
		// 3. Link Parameter Mappings
		return process;
	}

	private CaseFileItem findCaseFileItemById(VariableScope variableScope, String caseFileItemId) {
		List<Variable> variables = variableScope.getVariables();
		CaseFileItem binding = null;
		for (Variable variable : variables) {
			if (((CaseFileItem) variable).getElementId().equals(caseFileItemId)) {
				binding = (CaseFileItem) variable;
			}
		}
		return binding;
	}

	private PlanItem findPlanItem(RuleFlowProcess process, String sourceRef) {
		for (Node node : process.getNodes()) {
			if (node instanceof PlanItem && ((PlanItem) node).getElementId().equals(sourceRef)) {
				return (PlanItem) node;
			}
		}
		return null;
	}

	private PlanItemDefinition findPlanItemDefinition(Case process, String definitionRef) {
		for (PlanItemDefinition node : process.getPlanItemDefinitions()) {
			if (node.getElementId().equals(definitionRef)) {
				return node;
			}
		}
		return null;
	}

	private Sentry findSentry(RuleFlowProcess process, String elementId) {
		Node[] nodes = process.getNodes();
		for (Node node : nodes) {
			if (node instanceof Sentry && ((Sentry) node).getElementId().equals(elementId)) {
				return (Sentry) node;
			}
		}
		return null;
	}

	public Class<?> generateNodeFor() {
		return Case.class;
	}

}
