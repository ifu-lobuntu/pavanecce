package org.pavanecce.cmmn.flow.builder;

import java.util.HashSet;

import org.drools.core.xml.BaseAbstractHandler;
import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.process.core.context.variable.VariableScope;
import org.pavanecce.cmmn.flow.CaseFileItemTransition;
import org.pavanecce.cmmn.flow.CaseFileItemOnPart;
import org.pavanecce.cmmn.flow.CaseFileItemOnPart;
import org.pavanecce.cmmn.flow.JoiningSentry;
import org.pavanecce.cmmn.flow.PlanItem;
import org.pavanecce.cmmn.flow.PlanItemTransition;
import org.pavanecce.cmmn.flow.Sentry;
import org.pavanecce.cmmn.flow.SimpleSentry;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class CaseFileItemOnPartHandler extends BaseAbstractHandler implements Handler {
	public CaseFileItemOnPartHandler() {
		super.validParents=new HashSet<Class<?>>();
		validParents.add(Sentry.class);
		super.validParents.add(SimpleSentry.class);
		super.validParents.add(JoiningSentry.class);
		super.validPeers=new HashSet<Class<?>>();
		validPeers.add(CaseFileItemOnPart.class);
		validPeers.add(CaseFileItemOnPart.class);
		validPeers.add(null);

	}
	@Override
	public Object start(String uri, String localName, Attributes attrs, ExtensibleXmlParser xmlPackageReader) throws SAXException {
		xmlPackageReader.startElementBuilder(localName, attrs);
		CaseFileItemOnPart part = new CaseFileItemOnPart();
		part.setId(IdGenerator.next(xmlPackageReader));
		part.setName(attrs.getValue("id"));
		((Sentry)xmlPackageReader.getParent()).addOnPart(part);
		part.setSourceRef(attrs.getValue("sourceRef"));

		return part;
	}

	@Override
	public Object end(String uri, String localName, ExtensibleXmlParser xmlPackageReader) throws SAXException {
		CaseFileItemOnPart part =(CaseFileItemOnPart) xmlPackageReader.getCurrent();
		NodeList elementsByTagName = xmlPackageReader.endElementBuilder().getElementsByTagName("standardEvent");
		part.setStandardEvent(CaseFileItemTransition.resolveByName(elementsByTagName.item(0).getTextContent()));
		return xmlPackageReader.getCurrent();
	}

	@Override
	public Class<?> generateNodeFor() {
		return CaseFileItemOnPart.class;
	}

}
