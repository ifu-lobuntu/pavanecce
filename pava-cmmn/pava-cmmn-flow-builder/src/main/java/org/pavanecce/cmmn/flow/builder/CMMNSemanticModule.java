package org.pavanecce.cmmn.flow.builder;

import org.drools.core.xml.DefaultSemanticModule;

public class CMMNSemanticModule extends DefaultSemanticModule{
	public static final String CMMN_URI="http://www.omg.org/spec/CMMN/20121031/MODEL";
	public CMMNSemanticModule() {
		super(CMMN_URI);
		super.addHandler("case", new CaseHandler());
		super.addHandler("definitions", new DefinitionsHandler());
		super.addHandler("caseFileItem", new CaseFileItemHandler());
		super.addHandler("caseFileItemDefinition", new CaseFileItemDefinitionHandler());
		super.addHandler("sentry", new SentryHandler());
		super.addHandler("caseRoles",new RoleHandler());
		super.addHandler("planItem", new PlanItemHandler());
		super.addHandler("humanTask", new HumanTaskHandler());
		super.addHandler("planItemOnPart", new OnPlanItemHandler());
		super.addHandler("caseFileItemOnPart", new OnCaseFileItemHandler());
	}

}

