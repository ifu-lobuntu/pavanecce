package org.pavanecce.common.text.workspace;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TextDirectory extends TextOutputNode {
	private Set<TextOutputNode> children = new HashSet<TextOutputNode>();

	public TextDirectory(TextOutputNode parent, String name) {
		super(parent, name);
	}

	public TextDirectory(TextOutputNode parent, String name, boolean delete) {
		super(parent, name);
		super.shouldDelete = delete;
	}

	public Set<TextOutputNode> getChildren() {
		return children;
	}

	public SourceFolder getSourceFolder() {
		return ((TextDirectory) getParent()).getSourceFolder();
	}

	public TextFile findOrCreateTextFile(List<String> path) {
		TextOutputNode root = findNode(path.get(0));
		if (path.size() == 1) {
			if (root == null) {
				root = new TextFile(this, path.get(0), getSourceFolder().shouldOverwriteFiles());
				children.add(root);
			} else {
				root.restore();
			}
			return (TextFile) root;
		} else {
			if (root == null) {
				root = new TextDirectory(this, path.get(0));
				children.add(root);
			} else {
				root.restore();
			}
			return ((TextDirectory) root).findOrCreateTextFile(path.subList(1, path.size()));
		}
	}

	public TextOutputNode markNodeForDeletion(List<String> path, boolean targetIsFile) {
		TextOutputNode root = findNode(path.get(0));
		if (path.size() == 1) {
			if (root == null) {
				if (targetIsFile) {
					root = new TextFile(this, path.get(0));
				} else {
					root = new TextDirectory(this, path.get(0), true);
				}
				children.add(root);
			}
			root.markForDeletion();
		} else {
			if (root == null) {
				root = new TextDirectory(this, path.get(0), false);
				children.add(root);
				root.markForDeletion();
			}
			root = ((TextDirectory) root).markNodeForDeletion(path.subList(1, path.size()), targetIsFile);
		}
		return root;
	}

	public TextDirectory findOrCreateTextDirectory(List<String> path) {
		TextDirectory root = (TextDirectory) findNode(path.get(0));
		if (root == null) {
			root = new TextDirectory(this, path.get(0));
			children.add(root);
		}
		if (path.size() == 1) {
			return root;
		} else {
			return root.findOrCreateTextDirectory(path.subList(1, path.size()));
		}
	}

	private TextOutputNode findNode(String name) {
		for (TextOutputNode n : children) {
			if (n.name.equals(name)) {
				return n;
			}
		}
		return null;
	}

	public boolean hasChild(String name) {
		TextOutputNode child = findNode(name);
		return child != null && child.hasContent();
	}

	public String getSourceFolderRelativePath() {
		StringBuilder sb = new StringBuilder();
		appendNameToSourceFolderRelativePath(sb);
		return sb.substring(1);
	}

	public String getProjectRelativePath() {
		StringBuilder sb = new StringBuilder();
		sb.append("/");
		sb.append(getSourceFolder().getProjectRelativePath());
		appendNameToSourceFolderRelativePath(sb);
		return sb.substring(1);
	}

	private void appendNameToSourceFolderRelativePath(StringBuilder sb) {
		if (!(getParent() instanceof SourceFolder) && getParent() instanceof TextDirectory) {
			((TextDirectory) getParent()).appendNameToSourceFolderRelativePath(sb);
		}
		sb.append("/");
		sb.append(getName());
	}

	@Override
	public boolean hasContent() {
		for (TextOutputNode child : this.getChildren()) {
			if (child.hasContent()) {
				return true;
			}
		}
		return false;
	}

}
