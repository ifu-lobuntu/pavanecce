package org.pavanecce.cmmn.flow.builder;

import java.util.HashSet;
import java.util.List;

import org.drools.core.process.core.datatype.impl.type.ObjectDataType;
import org.drools.core.xml.BaseAbstractHandler;
import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.jbpm.process.core.ContextContainer;
import org.jbpm.process.core.Process;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.workflow.core.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class CaseFileItemHandler extends BaseAbstractHandler implements Handler{
	public CaseFileItemHandler() {
		initValidParents();
		initValidPeers();
		this.allowNesting = false;
	}

	protected void initValidParents() {
		this.validParents = new HashSet<Class<?>>();
		this.validParents.add(Process.class);
	}

	protected void initValidPeers() {
		this.validPeers = new HashSet<Class<?>>();
		this.validPeers.add(null);
		this.validPeers.add(Variable.class);
		this.validPeers.add(Node.class);
	}
	@SuppressWarnings("unchecked")
	public Object start(final String uri, final String localName,
			            final Attributes attrs, final ExtensibleXmlParser parser)
			throws SAXException {
		parser.startElementBuilder(localName, attrs);

		final String name = attrs.getValue("name");

		Object parent = parser.getParent();
		if (parent instanceof ContextContainer) {
		    ContextContainer contextContainer = (ContextContainer) parent;
		    VariableScope variableScope = (VariableScope) 
                contextContainer.getDefaultContext(VariableScope.VARIABLE_SCOPE);
			List variables = variableScope.getVariables();
			Variable variable = new Variable();
			variable.setMetaData("DataObject", "true");
			variable.setName(name);
			//TODO map to possible java type
			ObjectDataType dataType = new ObjectDataType("java.lang.String");
			variable.setType(dataType);
			variables.add(variable);
			return variable;
		}

		return new Variable();
	}

	public Object end(final String uri, final String localName,
			          final ExtensibleXmlParser parser) throws SAXException {
		parser.endElementBuilder();
		return parser.getCurrent();
	}

	public Class<?> generateNodeFor() {
		return Variable.class;
	}
}
