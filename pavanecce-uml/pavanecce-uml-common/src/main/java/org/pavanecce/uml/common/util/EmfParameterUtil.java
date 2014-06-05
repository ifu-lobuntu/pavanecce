package org.pavanecce.uml.common.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;

public class EmfParameterUtil {

	public static List<Parameter> getArgumentParameters(NamedElement operation) {
		List<Parameter> result = new ArrayList<Parameter>();
		for (Parameter parameter : getParameters(operation)) {
			if (parameter.getDirection() == ParameterDirectionKind.IN_LITERAL || parameter.getDirection() == ParameterDirectionKind.INOUT_LITERAL) {
				result.add(parameter);
			}
		}
		return result;
	}

	public static String toIdentifyingString(Operation o) {
		return o.getName() + "(" + collectionToString(getArgumentParameters(o), ", ") + ")";
	}

	private static String collectionToString(List<? extends NamedElement> argumentParameters, String seperator) {
		if (argumentParameters.isEmpty()) {
			return "";
		} else {
			StringBuilder sb = new StringBuilder();
			for (NamedElement namedElement : argumentParameters) {
				sb.append(namedElement.getQualifiedName());
				sb.append(seperator);
			}
			return sb.substring(0, sb.length() - 1);
		}
	}

	public static List<Parameter> getResultParameters(NamedElement operation) {
		List<Parameter> result = new ArrayList<Parameter>();
		for (Parameter parameter : getParameters(operation)) {
			if ((parameter.getDirection() == ParameterDirectionKind.OUT_LITERAL || parameter.getDirection() == ParameterDirectionKind.INOUT_LITERAL || parameter
					.getDirection() == ParameterDirectionKind.RETURN_LITERAL)) {
				result.add(parameter);
			}
		}
		return result;
	}

	public static Parameter getReturnParameter(NamedElement operation) {
		for (Parameter parameter : getParameters(operation)) {
			if (parameter.getDirection() == ParameterDirectionKind.RETURN_LITERAL) {
				return parameter;
			}
		}
		return null;
	}

	private static List<Parameter> getParameters(NamedElement parameterOwner) {
		if (parameterOwner instanceof Behavior) {
			return ((Behavior) parameterOwner).getOwnedParameters();
		} else if (parameterOwner instanceof Operation) {
			return ((Operation) parameterOwner).getOwnedParameters();
		}
		return Collections.emptyList();
	}

}
