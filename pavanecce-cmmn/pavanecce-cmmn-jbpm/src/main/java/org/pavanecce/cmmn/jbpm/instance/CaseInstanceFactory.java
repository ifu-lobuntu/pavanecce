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

package org.pavanecce.cmmn.jbpm.instance;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.jbpm.process.core.ContextContainer;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.AbstractProcessInstanceFactory;
import org.jbpm.process.instance.InternalProcessRuntime;
import org.jbpm.process.instance.ProcessInstance;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.kie.api.definition.process.Process;
import org.kie.internal.process.CorrelationKey;
import org.pavanecce.cmmn.jbpm.flow.Case;
import org.pavanecce.cmmn.jbpm.flow.CaseParameter;

public class CaseInstanceFactory extends AbstractProcessInstanceFactory implements Externalizable {
	// Temporary HACK - find the right place to map caseKeys with
	// knowledgeRuntimes
	private static Map<String, InternalKnowledgeRuntime> knowledgeRuntimes = new HashMap<String, InternalKnowledgeRuntime>();
	private static final long serialVersionUID = 510l;

	public ProcessInstance createProcessInstance() {
		return new CaseInstance();
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
	}

	public void writeExternal(ObjectOutput out) throws IOException {
	}

	public ProcessInstance createProcessInstance(Process process, CorrelationKey correlationKey, InternalKnowledgeRuntime kruntime,
			Map<String, Object> parameters) {
		CaseInstance processInstance = (CaseInstance) createProcessInstance();
		processInstance.setKnowledgeRuntime(kruntime);
		processInstance.setProcess(process);
		Case theCase = (Case) process;
		synchronized (knowledgeRuntimes) {
			knowledgeRuntimes.put(theCase.getCaseKey(), kruntime);
		}

		((InternalProcessRuntime) kruntime.getProcessRuntime()).getProcessInstanceManager().addProcessInstance(processInstance, correlationKey);
		// set variable default values
		// TODO: should be part of processInstanceImpl?
		VariableScope variableScope = (VariableScope) ((ContextContainer) process).getDefaultContext(VariableScope.VARIABLE_SCOPE);
		VariableScopeInstance variableScopeInstance = (VariableScopeInstance) processInstance.getContextInstance(VariableScope.VARIABLE_SCOPE);
		// set input parameters
		if (parameters != null) {
			if (variableScope != null) {
				for (CaseParameter caseParameter : theCase.getInputParameters()) {
					Object var = parameters.get(caseParameter.getName());
					variableScopeInstance.setVariable(caseParameter.getVariable().getName(), var);
					SubscriptionManager subscriptionManager = (SubscriptionManager) kruntime.getEnvironment().get(SubscriptionManager.ENV_NAME);
					subscriptionManager.subscribe(processInstance, caseParameter.getVariable(), var);
				}
			} else {
				throw new IllegalArgumentException("This process does not support parameters!");
			}
		}

		return processInstance;
	}

	public static InternalKnowledgeRuntime getEventManager(String caseKey) {
		synchronized (knowledgeRuntimes) {
			return knowledgeRuntimes.get(caseKey);
		}
	}

}
