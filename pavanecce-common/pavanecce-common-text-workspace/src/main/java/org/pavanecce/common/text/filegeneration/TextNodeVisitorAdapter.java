package org.pavanecce.common.text.filegeneration;

import java.util.Collection;
import java.util.Collections;

import org.pavanecce.common.text.workspace.SourceFolder;
import org.pavanecce.common.text.workspace.TextDirectory;
import org.pavanecce.common.text.workspace.TextFile;
import org.pavanecce.common.text.workspace.TextOutputNode;
import org.pavanecce.common.text.workspace.TextProject;
import org.pavanecce.common.text.workspace.TextWorkspace;

public class TextNodeVisitorAdapter {
	public void startVisiting(TextWorkspace tw, DefaultTextFileVisitor visitor) {
		visitRecursively(tw, visitor);
	}

	protected void visitRecursively(TextOutputNode tw, DefaultTextFileVisitor visitor) {
		for (TextOutputNode n : getChildren(tw)) {
			doVisits(visitor, n);
			visitRecursively(n, visitor);
		}
	}

	protected void doVisits(DefaultTextFileVisitor visitor, TextOutputNode n) {
		try {
			if (n instanceof TextProject) {
				visitor.visitTextProject((TextProject) n);
			} else if (n instanceof SourceFolder) {
				visitor.visitSourceFolder((SourceFolder) n);
			} else if (n instanceof TextDirectory) {
				visitor.visitTextFileDirectory((TextDirectory) n);
			} else if (n instanceof TextFile) {
				visitor.visitTextFile((TextFile) n);
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Collection<? extends TextOutputNode> getChildren(TextOutputNode root) {
		if (root.shouldDelete()) {
			return Collections.emptySet();
		} else if (root instanceof TextWorkspace) {
			return ((TextWorkspace) root).getTextProjects();
		} else if (root instanceof TextProject) {
			return ((TextProject) root).getSourceFolders();
		} else if (root instanceof TextDirectory) {
			return ((TextDirectory) root).getChildren();
		} else {
			return Collections.emptySet();
		}
	}

	protected void visitParentsRecursively(TextOutputNode node, DefaultTextFileVisitor visitor) {
		if (node != null) {
			visitParentsRecursively(node.getParent(), visitor);
		}
	}

	public void visitUpFirst(TextOutputNode element, DefaultTextFileVisitor visitor) {
		visitParentsRecursively(element.getParent(), visitor);
		doVisits(visitor, element);
	}
}
