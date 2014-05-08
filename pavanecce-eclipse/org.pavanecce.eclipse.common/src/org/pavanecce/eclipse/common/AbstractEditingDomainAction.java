package org.pavanecce.eclipse.common;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class AbstractEditingDomainAction extends Action {
	protected IStructuredSelection selection;

	public AbstractEditingDomainAction(IStructuredSelection selection, String name) {
		super(name);
		this.selection = selection;
	}

	protected EditingDomain getEditingDomainFor(EObject eo) {
		for (IWorkbenchWindow w : PlatformUI.getWorkbench().getWorkbenchWindows()) {
			for (IWorkbenchPage p : w.getPages()) {
				for (IEditorReference e : p.getEditorReferences()) {
					EditingDomain editingDomain = (EditingDomain) e.getEditor(true).getAdapter(EditingDomain.class);
					if (editingDomain.getResourceSet().getResources().contains(eo.eResource())) {
						return editingDomain;
					}
				}
			}
		}
		return null;
	}

}
