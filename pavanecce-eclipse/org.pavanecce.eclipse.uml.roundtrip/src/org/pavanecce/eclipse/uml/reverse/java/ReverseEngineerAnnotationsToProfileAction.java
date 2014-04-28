package org.pavanecce.eclipse.uml.reverse.java;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.uml2.uml.Profile;
import org.pavanecce.uml.reverse.java.AbstractUmlGenerator;
import org.pavanecce.uml.reverse.java.ProfileGenerator;

public class ReverseEngineerAnnotationsToProfileAction extends AbstractReverseEngineerJavaAction{

	public ReverseEngineerAnnotationsToProfileAction(IStructuredSelection selection){
		super(selection,"Reverse Engineer Annotations to Profile");
	}

	@Override
	protected AbstractUmlGenerator createGenerator(){
		return new ProfileGenerator();
	}
	@Override
	protected boolean canReverseInto(Profile ouf){
		return ouf instanceof Profile;
	}
}
