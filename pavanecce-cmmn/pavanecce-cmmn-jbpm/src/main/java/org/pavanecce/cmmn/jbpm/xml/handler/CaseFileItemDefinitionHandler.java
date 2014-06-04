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

package org.pavanecce.cmmn.jbpm.xml.handler;

import java.util.HashSet;
import java.util.Map;

import org.drools.core.xml.BaseAbstractHandler;
import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.jbpm.compiler.xml.ProcessBuildData;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItemDefinition;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItemDefinitionType;
import org.pavanecce.cmmn.jbpm.flow.Definitions;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class CaseFileItemDefinitionHandler extends BaseAbstractHandler implements Handler {

	public CaseFileItemDefinitionHandler() {
		if ((this.validParents == null) && (this.validPeers == null)) {
			this.validParents = new HashSet<Class<?>>();
			this.validParents.add(Definitions.class);

			this.validPeers = new HashSet<Class<?>>();
			this.validPeers.add(null);
			this.validPeers.add(CaseFileItemDefinition.class);
			this.validPeers.add(RuleFlowProcess.class);
			this.allowNesting = false;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object start(final String uri, final String localName, final Attributes attrs, final ExtensibleXmlParser parser) throws SAXException {
		parser.startElementBuilder(localName, attrs);
		String id = attrs.getValue("id");
		String type = attrs.getValue("structureRef");
		if (type == null || type.trim().length() == 0) {
			type = "java.lang.Object";
		}

		ProcessBuildData buildData = (ProcessBuildData) parser.getData();
		Map<String, CaseFileItemDefinition> itemDefinitions = (Map<String, CaseFileItemDefinition>) buildData
				.getMetaData(DefinitionsHandler.CASE_FILE_ITEM_DEFINITIONS);
		CaseFileItemDefinition caseFileItemDefinition = new CaseFileItemDefinition(id);
		caseFileItemDefinition.setDefinitionType(CaseFileItemDefinitionType.resolveByUri(attrs.getValue("definitionType")));
		caseFileItemDefinition.setStructureRef(type);
		itemDefinitions.put(id, caseFileItemDefinition);
		return caseFileItemDefinition;
	}

	@Override
	public Object end(final String uri, final String localName, final ExtensibleXmlParser parser) throws SAXException {
		parser.endElementBuilder();
		return parser.getCurrent();
	}

	@Override
	public Class<?> generateNodeFor() {
		return CaseFileItemDefinition.class;
	}

}
