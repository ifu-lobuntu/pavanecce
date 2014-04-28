package org.pavanecce.eclipse.uml.visualization;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.uml2.uml.Element;
import org.pavanecce.eclipse.common.AdapterFinder;
import org.pavanecce.eclipse.common.ICompoundContributionItem;
import org.pavanecce.eclipse.uml.visualization.actions.VisualizeAssociationsAction;
import org.pavanecce.eclipse.uml.visualization.actions.VisualizeClassesAction;
import org.pavanecce.eclipse.uml.visualization.actions.VisualizeGeneralizationTreeAction;
import org.pavanecce.eclipse.uml.visualization.actions.VisualizePackageDependenciesAction;
import org.pavanecce.eclipse.uml.visualization.actions.VisualizeSpecializationTreeAction;

public class DynamicVisualizeMenu extends CompoundContributionItem implements ICompoundContributionItem {
	private IStructuredSelection selection;

	@Override
	public IContributionItem[] getContributionItems(){
		if(AdapterFinder.adaptObject(selection.getFirstElement()) instanceof Element){
			return new IContributionItem[]{new ActionContributionItem(new VisualizeClassesAction(selection)),
					new ActionContributionItem(new VisualizeAssociationsAction(selection)),
					new ActionContributionItem(new VisualizePackageDependenciesAction(selection)),
					new ActionContributionItem(new VisualizeGeneralizationTreeAction(selection)),
					new ActionContributionItem(new VisualizeSpecializationTreeAction(selection))};
		}else{
			return new IContributionItem[]{};
		}
	}

	public DynamicVisualizeMenu(IStructuredSelection selection) {
		this.selection = selection;
	}
}
