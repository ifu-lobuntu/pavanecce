package org.pavanecce.uml.ocl2code.maps;

import org.eclipse.uml2.uml.Vertex;
import org.pavanecce.common.code.metamodel.CodeTypeReference;
import org.pavanecce.common.util.NameConverter;
import org.pavanecce.uml.common.util.EmfStateMachineUtil;
import org.pavanecce.uml.ocl2code.common.UmlToCodeMaps;

public class StateMap extends PackageableElementMap {
	private Vertex state = null;

	public StateMap(UmlToCodeMaps CodeUtil, Vertex s) {
		super(CodeUtil, s);
		state = s;
	}

	public String javaFieldName() {
		return EmfStateMachineUtil.getStatePath(state).replaceAll("\\:\\:", "_");
	}

	public String getter() {
		return "get" + NameConverter.capitalize(javaFieldName());
	}

	public CodeTypeReference javaType() {
		return codeUtil.classifierPathname(state);
	}

	public String getOnEntryMethod() {
		return "onEntryOf" + state.getName();
	}

	public String getOnExitMethod() {
		return "onExitOf" + state.getName();
	}

	public Vertex getState() {
		return state;
	}
}
