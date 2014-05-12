package org.pavanecce.cmmn.jbpm.xml.handler;

import org.drools.core.xml.DefaultSemanticModule;
import org.pavanecce.cmmn.jbpm.flow.JoiningSentry;
import org.pavanecce.cmmn.jbpm.flow.SimpleSentry;

public class CMMNSemanticModule extends DefaultSemanticModule{
	public static final String CMMN_URI="http://www.omg.org/spec/CMMN/20121031/MODEL";
	public CMMNSemanticModule() {
		super(CMMN_URI);
		super.addHandler("case", new CaseHandler());
		super.addHandler("definitions", new DefinitionsHandler());
		super.addHandler("caseFileItem", new CaseFileItemHandler());
		super.addHandler("caseFileItemDefinition", new CaseFileItemDefinitionHandler());
		SentryHandler sentryHandler = new SentryHandler();
		super.addHandler("sentry", sentryHandler);
		super.addHandler("caseRoles",new RoleHandler());
		super.addHandler("input",new CaseParameterHandler());
		super.addHandler("output",new CaseParameterHandler());
		super.addHandler("inputs",new CaseParameterHandler());
		super.addHandler("outputs",new CaseParameterHandler());
		super.addHandler("planItem", new PlanItemHandler());
		super.addHandler("humanTask", new HumanTaskHandler());
		super.addHandler("caseTask", new CaseTaskHandler());
		super.addHandler("parameterMapping", new ParameterMappingHandler());
		super.addHandler("userEvent", new UserEventHandler());
		super.addHandler("timerEvent", new TimerEventHandler());
		super.addHandler("planItemOnPart", new PlanItemOnPartHandler());
		super.addHandler("milestone", new MilestoneHandler());
		super.addHandler("caseFileItemOnPart", new CaseFileItemOnPartHandler());
		super.addHandler("stage", new StageHandler());
        this.handlersByClass.put( SimpleSentry.class, sentryHandler);
        this.handlersByClass.put( JoiningSentry.class, sentryHandler);

	}

}

