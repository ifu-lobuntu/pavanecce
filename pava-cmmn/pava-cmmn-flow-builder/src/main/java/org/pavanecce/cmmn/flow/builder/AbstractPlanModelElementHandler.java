package org.pavanecce.cmmn.flow.builder;

import java.util.HashSet;

import org.drools.core.xml.BaseAbstractHandler;
import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.pavanecce.cmmn.flow.HumanTask;
import org.pavanecce.cmmn.flow.PlanItem;
import org.pavanecce.cmmn.flow.Role;
import org.pavanecce.cmmn.flow.Sentry;
import org.xml.sax.SAXException;

public abstract class AbstractPlanModelElementHandler extends BaseAbstractHandler implements Handler{

	public AbstractPlanModelElementHandler() {
		this.validParents = new HashSet();
		this.validParents.add(RuleFlowProcess.class);

		this.validPeers = new HashSet();
		this.validPeers.add(null);
		this.validPeers.add(Sentry.class);
		this.validPeers.add(PlanItem.class);
		this.validPeers.add(HumanTask.class);
		this.validPeers.add(Variable.class);
		this.validPeers.add(Role.class);
	}
	@Override
	public Object end(String uri, String localName, ExtensibleXmlParser xmlPackageReader) throws SAXException {
		return xmlPackageReader.getCurrent();
	}


}
