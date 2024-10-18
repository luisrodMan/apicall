package com.ngeneration.apicall.explorer;

import java.util.LinkedList;
import java.util.List;

import com.ngeneration.furthergui.DefaultMutableTreeNode;
import com.nxtr.easymng.View;

public abstract class AbstractExplorerNode {

	private DefaultMutableTreeNode underLayingNode;
	private List<AbstractExplorerNode> nodes = new LinkedList<>();
	private AbstractExplorerNode parent;
	private String id;

	public AbstractExplorerNode(String id) {
		underLayingNode = new DefaultMutableTreeNode(this);
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public abstract String getName();

	public AbstractExplorerNode getParent() {
		return parent;
	}

	public final void addNode(int index, AbstractExplorerNode node) {
		nodes.add(index, node);
		underLayingNode.add(index, node.getUnderLayingNode());
		node.parent = this;
	}

	public List<AbstractExplorerNode> getNodes() {
		return nodes;
	}

	public AbstractExplorerNode getNodeByName(String name) {
		if (nodes != null && name != null)
			return nodes.stream().filter(f -> f.getName().equals(name)).findAny().orElse(null);
		return null;
	}

	public AbstractExplorerNode getNodeByIdRecursive(String id) {
		return getNodeByIdRecursive(nodes, id);
	}

	private AbstractExplorerNode getNodeByIdRecursive(List<AbstractExplorerNode> nodes2, String id2) {
		AbstractExplorerNode childFound = null;
		if (nodes != null) {
			for (AbstractExplorerNode child : nodes) {
				if (child.getId().equals(id))
					childFound = child;
				else
					childFound = child.getNodeByIdRecursive(child.nodes, id);
				if (childFound != null)
					break;
			}
		}
		return childFound;
	}

	public AbstractExplorerNode getNodeById(String id) {
		if (nodes != null)
			return nodes.stream().filter(f -> f.getId().equals(id)).findAny().orElse(null);
		return null;
	}

	public String[] getPath() {
		AbstractExplorerNode node = this;
		int i = 0;
		while (node != null) {
			node = node.getParent();
			i++;
		}
		String[] path = new String[i];
		node = this;
		while (node != null) {
			path[--i] = node.getId();
			node = node.getParent();
		}
		return path;
	}

	DefaultMutableTreeNode getUnderLayingNode() {
		return underLayingNode;
	}

	public int getNodeCount() {
		return nodes.size();
	}

	public abstract View getView();

	public boolean canContain(Class<? extends AbstractExplorerNode> clazz) {
		return false;
	}

}
