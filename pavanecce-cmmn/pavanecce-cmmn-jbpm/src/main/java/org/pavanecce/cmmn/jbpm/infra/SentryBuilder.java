package org.pavanecce.cmmn.jbpm.infra;

import org.drools.compiler.lang.descr.ProcessDescr;
import org.jbpm.process.builder.ProcessBuildContext;
import org.jbpm.process.builder.ProcessNodeBuilder;
import org.kie.api.definition.process.Node;
import org.kie.api.definition.process.Process;
import org.pavanecce.cmmn.jbpm.flow.Sentry;

public class SentryBuilder implements ProcessNodeBuilder {

	@Override
	public void build(Process process, ProcessDescr processDescr, ProcessBuildContext context, Node node) {
		Sentry sentry = (Sentry) node;
		sentry.setCondition(PlanItemBuilder.build(context, node, sentry.getCondition()));
	}

}
