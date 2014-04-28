package org.pavanecce.eclipse.common;

import org.eclipse.jface.action.IContributionItem;

public interface ICompoundContributionItem extends IContributionItem{
	IContributionItem[] getContributionItems();

}
