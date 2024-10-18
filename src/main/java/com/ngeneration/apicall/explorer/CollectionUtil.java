package com.ngeneration.apicall.explorer;

public class CollectionUtil {

	public static AbstractExplorerNode getNode(String[] path, AbstractExplorerNode collections) {
		String name = path[0];
		return !collections.getId().equals(name) ? null : getNode(1, path, collections);
	}

	private static AbstractExplorerNode getNode(int index, String[] path, AbstractExplorerNode node) {
		String name = path[index];
		var child = node.getNodeById(name);
		if (index == path.length - 1 || child == null)
			return child;
		else
			return getNode(++index, path, child);
	}

}
