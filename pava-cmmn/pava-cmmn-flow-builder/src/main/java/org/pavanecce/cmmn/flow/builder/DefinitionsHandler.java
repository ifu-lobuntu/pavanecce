/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pavanecce.cmmn.flow.builder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.drools.core.process.core.datatype.DataType;
import org.drools.core.process.core.datatype.impl.type.BooleanDataType;
import org.drools.core.process.core.datatype.impl.type.FloatDataType;
import org.drools.core.process.core.datatype.impl.type.IntegerDataType;
import org.drools.core.process.core.datatype.impl.type.ObjectDataType;
import org.drools.core.process.core.datatype.impl.type.StringDataType;
import org.drools.core.process.core.datatype.impl.type.UndefinedDataType;
import org.drools.core.xml.BaseAbstractHandler;
import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.jbpm.compiler.xml.ProcessBuildData;
import org.jbpm.process.core.ContextContainer;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.workflow.core.NodeContainer;
import org.kie.api.definition.process.Node;
import org.kie.api.definition.process.Process;
import org.pavanecce.cmmn.flow.CaseFileItem;
import org.pavanecce.cmmn.flow.CaseFileItemDefinition;
import org.pavanecce.cmmn.flow.CaseFileItemDefinitionType;
import org.pavanecce.cmmn.flow.Definitions;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class DefinitionsHandler extends BaseAbstractHandler implements Handler {
	private static final Map<CaseFileItemDefinitionType, TypeMap> TYPE_MAP_REGISTRY = new HashMap<CaseFileItemDefinitionType, TypeMap>();
	public static final String CASE_FILE_ITEM_DEFINITIONS = "CaseFileItemDefinition";

	public static void registerTypeMap(CaseFileItemDefinitionType typeSystem, TypeMap map) {
		TYPE_MAP_REGISTRY.put(typeSystem, map);
	}

	@SuppressWarnings("unchecked")
	public DefinitionsHandler() {
		if ((this.validParents == null) && (this.validPeers == null)) {
			this.validParents = new HashSet<Class<?>>();
			this.validParents.add(null);

			this.validPeers = new HashSet<Class<?>>();
			this.validPeers.add(null);

			this.allowNesting = false;
		}
	}

	public Object start(final String uri, final String localName, final Attributes attrs, final ExtensibleXmlParser parser) throws SAXException {
		parser.startElementBuilder(localName, attrs);
		((ProcessBuildData) parser.getData()).setMetaData(CASE_FILE_ITEM_DEFINITIONS, new HashMap<String, CaseFileItemDefinition>());
		return new Definitions();
	}

	public Object end(final String uri, final String localName, final ExtensibleXmlParser parser) throws SAXException {
		final Element element = parser.endElementBuilder();
		Definitions definitions = (Definitions) parser.getCurrent();
		String namespace = element.getAttribute("targetNamespace");
		List<Process> processes = ((ProcessBuildData) parser.getData()).getProcesses();
		Map<String, CaseFileItemDefinition> itemDefinitions = (Map<String, CaseFileItemDefinition>) ((ProcessBuildData) parser.getData())
				.getMetaData(CASE_FILE_ITEM_DEFINITIONS);
		for (Process process : processes) {
			RuleFlowProcess ruleFlowProcess = (RuleFlowProcess) process;
			ruleFlowProcess.setMetaData("TargetNamespace", namespace);
			postProcessItemDefinitions(ruleFlowProcess, itemDefinitions);
		}
		definitions.setTargetNamespace(namespace);
		return definitions;
	}

	public Class<?> generateNodeFor() {
		return Definitions.class;
	}

	private void postProcessItemDefinitions(NodeContainer nodeContainer, Map<String, CaseFileItemDefinition> itemDefinitions) {
		if (nodeContainer instanceof ContextContainer) {
			setVariablesDataType((ContextContainer) nodeContainer, itemDefinitions);
		}
		for (Node node : nodeContainer.getNodes()) {
			if (node instanceof NodeContainer) {
				postProcessItemDefinitions((NodeContainer) node, itemDefinitions);
			}
			if (node instanceof ContextContainer) {
				setVariablesDataType((ContextContainer) node, itemDefinitions);
			}
		}
	}

	private void setVariablesDataType(ContextContainer container, Map<String, CaseFileItemDefinition> itemDefinitions) {
		VariableScope variableScope = (VariableScope) container.getDefaultContext(VariableScope.VARIABLE_SCOPE);
		if (variableScope != null) {
			for (Variable variable : variableScope.getVariables()) {
				setVariableDataType((CaseFileItem) variable, itemDefinitions);
			}
		}
	}

	private void setVariableDataType(CaseFileItem variable, Map<String, CaseFileItemDefinition> itemDefinitions) {
		// retrieve type from item definition
		String definitionRef = (String) variable.getDefinitionRef();
		if (UndefinedDataType.getInstance().equals(variable.getType()) && itemDefinitions != null && definitionRef != null) {
			DataType dataType = new ObjectDataType();
			CaseFileItemDefinition itemDefinition = itemDefinitions.get(definitionRef);
			if (itemDefinition != null) {
				TypeMap typeMap = TYPE_MAP_REGISTRY.get(itemDefinition.getDefinitionType());
				String type = typeMap.getType(itemDefinition.getStructureRef());
				if ("java.lang.Boolean".equals(type) || "Boolean".equals(type)) {
					dataType = new BooleanDataType();
				} else if ("java.lang.Integer".equals(type) || "Integer".equals(type)) {
					dataType = new IntegerDataType();
				} else if ("java.lang.Float".equals(type) || "Float".equals(type)) {
					dataType = new FloatDataType();
				} else if ("java.lang.String".equals(type) || "String".equals(type)) {
					dataType = new StringDataType();
				} else if ("java.lang.Object".equals(type) || "Object".equals(type)) {
					dataType = new ObjectDataType(type);
				} else {
					dataType = new ObjectDataType(type);
				}
			}
			if (variable.isCollection()) {
				CollectionDataType c = new CollectionDataType();
				c.setClassName(dataType.getStringType());
				dataType=c;
			}
			variable.setType(dataType);
		}
	}

}
