package org.pavanecce.eclipse.uml.reverse.java;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.pavanecce.uml.reverse.java.AbstractUmlGenerator;
import org.pavanecce.uml.reverse.java.SimpleUmlGenerator;

public class ReverseEngineerSimpleClassesAction extends AbstractReverseEngineerJavaAction{

	public ReverseEngineerSimpleClassesAction(IStructuredSelection selection){
		super(selection,"Reverse Engineer Normal Classes");
	}

	@Override
	protected AbstractUmlGenerator createGenerator(){
		return new SimpleUmlGenerator();
	}
}
