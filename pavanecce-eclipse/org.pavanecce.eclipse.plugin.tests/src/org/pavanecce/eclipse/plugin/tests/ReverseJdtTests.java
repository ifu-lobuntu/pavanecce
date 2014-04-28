package org.pavanecce.eclipse.plugin.tests;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.junit.Test;
import org.pavanecce.eclipse.uml.reverse.java.ReverseEngineerSimpleClassesAction;

public class ReverseJdtTests {

	@Test
	public void test() throws Exception {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("org.vdfp.reverse.test.input");
		IJavaProject jp = JavaCore.create(project);
		IFolder folder = project.getFolder("src");
		IPackageFragmentRoot pfr = jp.getPackageFragmentRoot(folder);
		IWorkbenchPage ap = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IFile modelFile = project.getFile(new Path("models/model.di"));
		IEditorPart ed = ap.openEditor(new FileEditorInput(modelFile), "org.eclipse.papyrus.infra.core.papyrusEditor");
		ReverseEngineerSimpleClassesAction action = new ReverseEngineerSimpleClassesAction(new StructuredSelection(pfr.getChildren()));
		action.reverseInto(modelFile);
		ap.closeEditor(ed, false);
		
	}
}
