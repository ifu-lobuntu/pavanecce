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
import org.pavanecce.cmmn.flow.CaseFileItemDefinition;
import org.pavanecce.cmmn.flow.Definitions;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class DefinitionsHandler extends BaseAbstractHandler implements Handler {

	@SuppressWarnings("unchecked")
	public DefinitionsHandler() {
		if ((this.validParents == null) && (this.validPeers == null)) {
			this.validParents = new HashSet();
			this.validParents.add(null);

			this.validPeers = new HashSet();
			this.validPeers.add(null);

			this.allowNesting = false;
		}
	}

	public Object start(final String uri, final String localName,
			            final Attributes attrs, final ExtensibleXmlParser parser)
			throws SAXException {
		parser.startElementBuilder(localName, attrs);
		return new Definitions();
	}

	public Object end(final String uri, final String localName,
			          final ExtensibleXmlParser parser) throws SAXException {
		final Element element = parser.endElementBuilder();
		Definitions definitions = (Definitions) parser.getCurrent();
        String namespace = element.getAttribute("targetNamespace");
        List<Process> processes = ((ProcessBuildData) parser.getData()).getProcesses();
		Map<String, CaseFileItemDefinition> itemDefinitions = (Map<String, CaseFileItemDefinition>)
            ((ProcessBuildData) parser.getData()).getMetaData("ItemDefinitions");
        for (Process process : processes) {
            RuleFlowProcess ruleFlowProcess = (RuleFlowProcess)process;
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
		for (Node node: nodeContainer.getNodes()) {
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
			for (Variable variable: variableScope.getVariables()) {
				setVariableDataType(variable, itemDefinitions);
			}
		}
	}
	
	private void setVariableDataType(Variable variable, Map<String, CaseFileItemDefinition> itemDefinitions) {
		// retrieve type from item definition
		String itemSubjectRef = (String) variable.getMetaData("ItemSubjectRef");
        if (UndefinedDataType.getInstance().equals(variable.getType()) && itemDefinitions != null && itemSubjectRef != null) {
    		DataType dataType = new ObjectDataType();
    		CaseFileItemDefinition itemDefinition = itemDefinitions.get(itemSubjectRef);
        	if (itemDefinition != null) {
        	    String structureRef = itemDefinition.getStructureRef();
        	    
        	    if ("java.lang.Boolean".equals(structureRef) || "Boolean".equals(structureRef)) {
        	        dataType = new BooleanDataType();
        	        
        	    } else if ("java.lang.Integer".equals(structureRef) || "Integer".equals(structureRef)) {
        	        dataType = new IntegerDataType();
                    
        	    } else if ("java.lang.Float".equals(structureRef) || "Float".equals(structureRef) || "Real".equals(structureRef)) {
        	        dataType = new FloatDataType();
                    
                } else if ("java.lang.String".equals(structureRef) || "String".equals(structureRef)) {
                    dataType = new StringDataType();
                    
                } else if ("java.lang.Object".equals(structureRef) || "Object".equals(structureRef)) {
                    dataType = new ObjectDataType(structureRef);
                    
                } else {
                    dataType = new ObjectDataType(structureRef);
                }
        		
        	}
    		variable.setType(dataType);
        }
	}
	
}
