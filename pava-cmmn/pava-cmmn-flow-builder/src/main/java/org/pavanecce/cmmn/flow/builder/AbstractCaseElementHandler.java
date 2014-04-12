package org.pavanecce.cmmn.flow.builder;

import java.util.HashSet;

import org.drools.core.xml.BaseAbstractHandler;
import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.pavanecce.cmmn.flow.Case;
import org.pavanecce.cmmn.flow.CaseParameter;
import org.pavanecce.cmmn.flow.HumanTaskNode;
import org.pavanecce.cmmn.flow.PlanItem;
import org.pavanecce.cmmn.flow.Role;
import org.pavanecce.cmmn.flow.Sentry;
import org.xml.sax.SAXException;

public abstract class AbstractCaseElementHandler extends BaseAbstractHandler implements Handler{

	public AbstractCaseElementHandler() {
		this.validParents = new HashSet();
		this.validParents.add(Case.class);

		this.validPeers = new HashSet();
		this.validPeers.add(null);
		this.validPeers.add(Sentry.class);
		this.validPeers.add(PlanItem.class);
		this.validPeers.add(HumanTaskNode.class);
		this.validPeers.add(Variable.class);
		this.validPeers.add(Role.class);
		this.validPeers.add(CaseParameter.class);
	}
	@Override
	public Object end(String uri, String localName, ExtensibleXmlParser xmlPackageReader) throws SAXException {
		return xmlPackageReader.getCurrent();
	}


}
