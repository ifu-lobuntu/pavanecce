package org.pavanecce.eclipse.roundtrip.menu;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.uml2.uml.Model;
import org.pavanecce.common.text.filegeneration.TextFileGenerator;
import org.pavanecce.common.text.filegeneration.TextNodeVisitorAdapter;
import org.pavanecce.common.text.workspace.ProjectNameStrategy;
import org.pavanecce.common.text.workspace.SourceFolderDefinition;
import org.pavanecce.common.text.workspace.SourceFolderNameStrategy;
import org.pavanecce.common.text.workspace.TextProjectDefinition;
import org.pavanecce.common.text.workspace.TextWorkspace;
import org.pavanecce.eclipse.common.AbstractEditingDomainAction;
import org.pavanecce.eclipse.common.AdapterFinder;
import org.pavanecce.uml.uml2code.codemodel.CodeModelBuilder;
import org.pavanecce.uml.uml2code.codemodel.UmlCodeModelVisitorAdaptor;
import org.pavanecce.uml.uml2code.jpa.JpaCodeGenerator;

public class GenerateJavaAction extends AbstractEditingDomainAction {

	public GenerateJavaAction(IStructuredSelection selection) {
		super(selection, "Generate Java");
	}

	@Override
	public void run() {
		try {
			Object element = AdapterFinder.adaptObject(selection.getFirstElement());
			if (element instanceof Model) {
				Model model = (Model) element;
				ElementTreeSelectionDialog dlg = new ElementTreeSelectionDialog(Display.getCurrent().getActiveShell(), WorkbenchLabelProvider
                        .getDecoratingWorkbenchLabelProvider(), new BaseWorkbenchContentProvider() {
					@Override
					public Object[] getChildren(Object element) {
						List<IResource> selectedElements = new ArrayList<IResource>();
						if (element instanceof IProject) {
							IProject p = (IProject) element;
							try {
								if (p.hasNature(JavaCore.NATURE_ID)) {
									IJavaProject jp = JavaCore.create(p);
									for (IPackageFragmentRoot pf : jp.getPackageFragmentRoots()) {
										if (pf.getResource() instanceof IFolder) {
											selectedElements.add(pf.getResource());
										}
									}
								}
							} catch (Exception e) {

							}
						}else if(element instanceof IWorkspaceRoot){
							return ((IWorkspaceRoot) element).getProjects();
						}
						return selectedElements.toArray(new Object[selectedElements.size()]);
					}
					@Override
					public boolean hasChildren(Object element) {
						return element instanceof IContainer;
					}

					@Override
					public Object[] getElements(Object element) {
						return ((IWorkspaceRoot)element).getProjects();
					}
				});
				dlg.setInput(ResourcesPlugin.getWorkspace().getRoot());
				dlg.setBlockOnOpen(true);
				dlg.open();
				if (dlg.getReturnCode() == Window.OK) {
					IFolder folder = (IFolder) dlg.getResult()[0];
					UmlCodeModelVisitorAdaptor adaptor = new UmlCodeModelVisitorAdaptor();
					CodeModelBuilder builder = new CodeModelBuilder();
					adaptor.startVisiting(builder, model);
					TextWorkspace tw = new TextWorkspace(folder.getProject().getName());
					File outputRoot = folder.getProject().getLocation().toFile().getParentFile();
					JavaTextFileGenerator jtfg = new JavaTextFileGenerator(tw,new JpaCodeGenerator());
					SourceFolderDefinition sfd = new SourceFolderDefinition(SourceFolderNameStrategy.QUALIFIER_ONLY, folder.getProjectRelativePath().toString());
					TextProjectDefinition tpd = new TextProjectDefinition(ProjectNameStrategy.SUFFIX_ONLY, folder.getProject().getName());
					jtfg.mapSourceFolder(JavaTextFileGenerator.DOMAIN, tpd, sfd);
					CodeModelVisitorAdapter cmva = new CodeModelVisitorAdapter();
					jtfg.setTextWorkspace(tw);
					cmva.startVisiting(adaptor.getCodeModel(), jtfg);
					TextFileGenerator tfg = new TextFileGenerator(outputRoot);
					TextNodeVisitorAdapter tnva = new TextNodeVisitorAdapter();
					tnva.startVisiting(tw, tfg);
					folder.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
	}
}
