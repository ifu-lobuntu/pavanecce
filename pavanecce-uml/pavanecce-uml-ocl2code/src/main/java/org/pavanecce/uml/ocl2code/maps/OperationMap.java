package org.pavanecce.uml.ocl2code.maps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.StateMachine;
import org.pavanecce.common.code.metamodel.CodePackageReference;
import org.pavanecce.common.code.metamodel.CodeTypeReference;
import org.pavanecce.common.util.NameConverter;
import org.pavanecce.uml.common.util.EmfParameterUtil;
import org.pavanecce.uml.common.util.EmfWorkspace;
import org.pavanecce.uml.ocl2code.common.UmlToCodeMaps;
import org.pavanecce.uml.uml2code.StdlibMap;

public class OperationMap extends PackageableElementMap {
	private NamedElement operation = null;
	private ClassifierMap operationTypeMap = null;
	private Map<Parameter, ClassifierMap> params = null;
	private CodeTypeReference handlerPath;
	private ArrayList<CodeTypeReference> paramTypePaths;
	private List<Parameter> parameters;
	private Parameter returnParameter;

	static public String javaPlusOperName() {
		return "plus";
	}

	public OperationMap(UmlToCodeMaps CodeUtil, NamedElement operation, List<Parameter> parameters) {
		super(CodeUtil, operation);
		this.parameters = parameters;
		this.operation = operation;
		for (Parameter parameter : parameters) {
			if (parameter.getDirection() == ParameterDirectionKind.RETURN_LITERAL) {
				this.returnParameter = parameter;
				this.operationTypeMap = CodeUtil.buildClassifierMap((Classifier) parameter.getType(), getCollectionKind(parameter));
			}
		}
	}

	public OperationMap(UmlToCodeMaps CodeUtil, Operation operation) {
		this(CodeUtil, operation, operation.getOwnedParameters());
	}

	public List<CodeTypeReference> javaParamTypePathsWithReturnInfo() {
		List<CodeTypeReference> javaParamTypePaths = new ArrayList<CodeTypeReference>();
		if (isLongRunning()) {
			// javaParamTypePaths.add(new CodeTypeReference(IToken.class.getName()));
		}
		javaParamTypePaths.addAll(javaParamTypePaths());
		return javaParamTypePaths;
	}

	public boolean isLongRunning() {
		return operation instanceof StateMachine;
	}

	public String javaOperName() {
		if (operation instanceof Behavior) {
			Behavior behaviour = (Behavior) operation;
			if (behaviour.getSpecification() != null) {
				return behaviour.getSpecification().getName();
			} else {
				return behaviour.getName();
			}
		} else {
			String in = operation.getName();
			String result = in;
			if (in.length() == 1) {
				switch (in.charAt(0)) {
				case '+':
					result = "plus";
					break;
				case '-':
					result = "minus";
					break;
				case '/':
					result = "divide";
					break;
				case '<':
					result = "less";
					break;
				case '>':
					result = "more";
					break;
				case '*':
					result = "times";
					break;
				case '=':
					result = "singleEquals";
					break;
				default:
					result = "unknownOperator";
					break;
				}
			} else if (in.length() == 2) {
				if (in.equals("<="))
					result = "lessEquals";
				if (in.equals(">="))
					result = "moreEquals";
				if (in.equals("<>"))
					result = "doubleEquals";
			}
			return result;
		}
	}

	public CodeTypeReference javaReturnTypePath() {
		if (operationTypeMap != null) {
			return operationTypeMap.javaTypePath();
		} else {
			return StdlibMap.Void;
		}
	}

	public CodeTypeReference javaParamTypePath(Parameter elem) {
		return getParamMap().get(elem).javaTypePath();
	}

	public CodeTypeReference javaReturnDefaultTypePath() {
		if (operationTypeMap != null) {
			return operationTypeMap.javaDefaultTypePath();
		} else {
			return StdlibMap.Void;
		}
	}

	public List<CodeTypeReference> javaParamTypePaths() {
		if (paramTypePaths == null) {
			paramTypePaths = new ArrayList<CodeTypeReference>();

			Iterator<?> it = getArgumentParameters().iterator();
			while (it.hasNext()) {
				Parameter param = (Parameter) it.next();
				paramTypePaths.add(javaParamTypePath(param));
			}
		}
		return paramTypePaths;
	}

	public String eventConsumerMethodName() {
		return "consume" + NameConverter.capitalize(operation.getName()) + "Occurrence";
	}

	private Map<Parameter, ClassifierMap> getParamMap() {
		if (params == null) {
			params = new HashMap<Parameter, ClassifierMap>();
			for (Parameter p : parameters) {
				Classifier type = (Classifier) p.getType();
				if (type == null) {
					type = codeUtil.getLibrary().getDefaultType();
				}
				params.put(p, codeUtil.buildClassifierMap(type, p));
			}
		}
		return params;
	}

	public boolean hasContract() {
		return operation != getContractDefiningElement();
	}

	private NamedElement getContractDefiningElement() {
		if (operation instanceof Operation) {
			return operation;
		} else {
			Behavior root = (Behavior) operation;
			while (root.getSuperClasses().size() == 1 && root.getSuperClasses().get(0) instanceof Behavior) {
				root = (Behavior) root.getSuperClasses().get(0);
				if (root.getSpecification() != null) {
					return root.getSpecification();
				}
			}
			return root;
		}
	}

	public CodeTypeReference handlerPath() {
		if (handlerPath == null) {
			CodePackageReference pn = codeUtil.packagePathname(getContractDefiningElement().getNamespace());
			this.handlerPath = new CodeTypeReference(true, pn, NameConverter.capitalize(operation.getName()) + "Handler"
					+ EmfWorkspace.getUniqueNumericId(operation), Collections.<String, String> emptyMap());
		}
		return handlerPath;
	}

	public String callbackListener() {
		return NameConverter.capitalize(operation.getName()) + "Listener";
	}

	public String callbackOperName() {
		return baseMethodName() + "Complete";
	}

	protected String baseMethodName() {
		return "on" + NameConverter.capitalize(getContractDefiningElement().getName());
	}

	public String eventGeratorMethodName() {
		String javaName = operation.getName();
		return "generate" + NameConverter.capitalize(javaName) + "Event";
	}

	public CodeTypeReference eventHandlerPath() {
		return handlerPath();
	}

	public boolean hasMessageStructure() {
		if (operation instanceof Operation) {
			return false;
		} else {
			return operation instanceof StateMachine;
		}
	}

	public CodeTypeReference messageStructurePath() {
		if (operation instanceof Operation) {
			return codeUtil.classifierPathname((operation));
		} else {
			return codeUtil.classifierPathname(operation);
		}
	}

	public List<Parameter> getArgumentParameters() {
		return EmfParameterUtil.getArgumentParameters(operation);
	}

	public List<Parameter> getResultParameters() {
		return EmfParameterUtil.getResultParameters(operation);
	}

	public Parameter getReturnParameter() {
		return returnParameter;
	}

	public Collection<Constraint> getPreConditions() {
		if (operation instanceof Operation) {
			return ((Operation) operation).getPreconditions();
		} else {
			return ((Behavior) operation).getPreconditions();
		}
	}

	public Classifier getContext() {
		if (operation instanceof Operation) {
			return (Classifier) ((Operation) operation).getOwner();
		} else {
			return ((Behavior) operation).getContext();
		}
	}

	public List<Constraint> getPostConditions() {
		if (operation instanceof Operation) {
			return ((Operation) operation).getPostconditions();
		} else {
			return ((Behavior) operation).getPostconditions();
		}
	}

	public List<Parameter> getExceptionParameters() {
		List<Parameter> result = new ArrayList<Parameter>();
		for (Parameter parameter : this.parameters) {
			if (parameter.isException()) {
				result.add(parameter);
			}
		}
		return result;
	}

	public NamedElement getNamedElement() {
		return operation;
	}

	public String multiName() {
		return javaOperName() + "WithMultiplePotentialOwners";
	}
}
