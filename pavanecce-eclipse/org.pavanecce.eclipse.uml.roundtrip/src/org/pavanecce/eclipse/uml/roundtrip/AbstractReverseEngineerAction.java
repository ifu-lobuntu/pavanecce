package org.pavanecce.eclipse.uml.roundtrip;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.command.AbstractCommand;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Profile;
import org.pavanecce.eclipse.common.AbstractEditingDomainAction;

public abstract class AbstractReverseEngineerAction extends AbstractEditingDomainAction {
	protected IEditorPart selectedEditor;
	protected static Command DO_NOTHING = new AbstractCommand() {
		@Override
		public void redo() {
		}

		@Override
		public void execute() {
		}
	};

	public AbstractReverseEngineerAction(IStructuredSelection selection, String name) {
		super(selection, name);
	}

	public void run() {

		Shell shell = Display.getCurrent().getActiveShell();
		Collection<IFile> files = new HashSet<IFile>();
		for (IWorkbenchWindow w : PlatformUI.getWorkbench().getWorkbenchWindows()) {
			for (IWorkbenchPage p : w.getPages()) {
				for (IEditorReference e : p.getEditorReferences()) {
					try {
						EditingDomain editingDomain = (EditingDomain) e.getEditor(true).getAdapter(EditingDomain.class);
						if (editingDomain != null) {
							if (e.getEditorInput() instanceof IFileEditorInput) {
								IFileEditorInput editorInput = (IFileEditorInput) e.getEditorInput();
								final String fileExtension = editorInput.getFile().getLocation().getFileExtension();
								if (fileExtension.equals(".di") || fileExtension.equals(fileExtension)) {
									files.add(editorInput.getFile());
								}
							}
						}
					} catch (PartInitException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(shell, new LabelProvider() {
			@Override
			public String getText(Object element) {
				IFile file = (IFile) element;
				return file.getProject().getName() + "/" + file.getProjectRelativePath();
			}
		});
		dialog.setElements(files.toArray());
		dialog.setTitle("Models in Workspace");
		dialog.setMessage("Select the targetmodel:");
		dialog.open();
		if (dialog.getFirstResult() != null) {
			IFile file = (IFile) dialog.getFirstResult();
			reverseInto(file);
		}
	}

	public void reverseInto(IFile file) {
		setSelectedEditor(file.getName());
		try {
			EditingDomain ed = (EditingDomain) selectedEditor.getAdapter(EditingDomain.class);
			final EList<Resource> resources = ed.getResourceSet().getResources();
			Package model=null;
			for (Resource resource : resources) {
				if(resource.getURI().trimFileExtension().lastSegment().equals(file.getLocation().removeFileExtension().lastSegment()) && resource.getURI().fileExtension().equals("uml")){
					model=(Package) resource.getContents().get(0);
				}
			}
			ed.getCommandStack().execute(buildCommand(model));
			file.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	protected boolean canReverseInto(Profile ouf) {
		return ouf instanceof Model;
	}

	private void setSelectedEditor(String fileName) {
		outer: for (IWorkbenchWindow w : PlatformUI.getWorkbench().getWorkbenchWindows()) {
			for (IWorkbenchPage p : w.getPages()) {
				for (IEditorReference e : p.getEditorReferences()) {
					try {
						if (e.getEditorInput() instanceof IFileEditorInput) {
							IFileEditorInput editorInput = (IFileEditorInput) e.getEditorInput();
							if (editorInput.getFile().getName().equals(fileName)) {
								selectedEditor = e.getEditor(true);
								break outer;
							}
						}
					} catch (PartInitException e1) {
						e1.printStackTrace();
					}
				}
			}
		}

	}

	protected abstract Command buildCommand(org.eclipse.uml2.uml.Package model);
}
