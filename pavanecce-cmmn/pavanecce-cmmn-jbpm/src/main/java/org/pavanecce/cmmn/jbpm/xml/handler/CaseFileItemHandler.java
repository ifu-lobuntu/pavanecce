package org.pavanecce.cmmn.jbpm.xml.handler;

import java.util.HashSet;
import java.util.List;

import org.drools.core.xml.BaseAbstractHandler;
import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.workflow.core.Node;
import org.pavanecce.cmmn.jbpm.flow.Case;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItem;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class CaseFileItemHandler extends BaseAbstractHandler implements Handler {
	public CaseFileItemHandler() {
		initValidParents();
		initValidPeers();
		this.allowNesting = true;
	}

	protected void initValidParents() {
		this.validParents = new HashSet<Class<?>>();
		this.validParents.add(Case.class);
		this.validParents.add(CaseFileItem.class);
		this.validParents.add(null);
	}

	protected void initValidPeers() {
		this.validPeers = new HashSet<Class<?>>();
		this.validPeers.add(null);
		this.validPeers.add(Variable.class);
		this.validPeers.add(Node.class);
	}

	@Override
	public Object start(final String uri, final String localName, final Attributes attrs, final ExtensibleXmlParser parser) throws SAXException {
		parser.startElementBuilder(localName, attrs);

		final String name = attrs.getValue("name");

		CaseFileItem variable = new CaseFileItem();
		variable.setMetaData("DataObject", "true");
		variable.setElementId(attrs.getValue("id"));
		variable.setName(name);
		variable.setDefinitionRef(attrs.getValue("definitionRef"));
		String targetRefs = attrs.getValue("targetRefs");
		String multiplicity = attrs.getValue("multiplicity");
		if (multiplicity != null) {
			variable.setCollection(!multiplicity.endsWith("One"));
		}
		if (targetRefs != null) {
			for (String string : targetRefs.split("\\ ")) {
				variable.putTarget(string, null);
			}
		}
		Case parent = (Case) parser.getParent(Case.class);
		// All CaseFileItems are at the Case level - Stages do not have
		// CaseFileItems
		VariableScope variableScope = (VariableScope) parent.getDefaultContext(VariableScope.VARIABLE_SCOPE);
		List<Variable> variables = variableScope.getVariables();
		variables.add(variable);
		if (parser.getParent() instanceof CaseFileItem) {
			((CaseFileItem) parser.getParent()).addChild(variable);
		}
		return variable;
	}

	@Override
	public Object end(final String uri, final String localName, final ExtensibleXmlParser parser) throws SAXException {
		parser.endElementBuilder();
		return parser.getCurrent();
	}

	@Override
	public Class<?> generateNodeFor() {
		return CaseFileItem.class;
	}
}
