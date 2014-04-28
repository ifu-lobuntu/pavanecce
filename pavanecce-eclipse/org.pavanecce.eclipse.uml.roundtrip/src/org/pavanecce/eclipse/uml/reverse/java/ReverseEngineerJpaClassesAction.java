package org.pavanecce.eclipse.uml.reverse.java;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.pavanecce.uml.reverse.java.AbstractUmlGenerator;
import org.pavanecce.uml.reverse.java.UmlGeneratorFromJpa;

public class ReverseEngineerJpaClassesAction extends AbstractReverseEngineerJavaAction{

	public ReverseEngineerJpaClassesAction(IStructuredSelection selection){
		super(selection,"Reverse Engineer JPA Classes");
	}

	@Override
	protected AbstractUmlGenerator createGenerator(){
		return new UmlGeneratorFromJpa();
	}
}
