package org.pavanecce.eclipse.roundtrip.menu;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.uml2.uml.Model;
import org.pavanecce.eclipse.common.AdapterFinder;
import org.pavanecce.eclipse.common.ICompoundContributionItem;
import org.pavanecce.eclipse.uml.reverse.db.DynamicReverseDbMenu;
import org.pavanecce.eclipse.uml.reverse.java.DynamicReverseJavaMenu;

public class ReversalContributionFactory extends ExtensionContributionFactory{
	Expression visibleWhen = new Expression(){
		@Override
		public EvaluationResult evaluate(IEvaluationContext context) throws CoreException{
			return EvaluationResult.TRUE;
		}
	};
	public ReversalContributionFactory(){
	}
	@Override
	public void createContributionItems(IServiceLocator serviceLocator,IContributionRoot additions){
		ISelectionService s = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
		if(s.getSelection() instanceof IStructuredSelection){
			MenuManager menuManager = new MenuManager("Reverse");
			IStructuredSelection selection = (IStructuredSelection) s.getSelection();
			maybeAddMenu(menuManager, new DynamicReverseDbMenu(selection));
			maybeAddMenu(menuManager, new DynamicReverseJavaMenu(selection));
			EObject adaptObject = AdapterFinder.adaptObject(selection.getFirstElement());
			if(adaptObject instanceof Model){
				menuManager.add(new GenerateJavaAction(selection));
			}
			if(!menuManager.isEmpty()){
				additions.addContributionItem(menuManager, visibleWhen);
			}
		}
	}
	public void maybeAddMenu(MenuManager menuManager,ICompoundContributionItem menu){
		IContributionItem[] contributionItems = menu.getContributionItems();
		if(contributionItems.length>0){
			menuManager.add(menu);
		}
	}
}
