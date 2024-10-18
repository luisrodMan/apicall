package com.ngeneration.apicall.explorer;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.ngeneration.apicall.ApiCallApplication;
import com.ngeneration.apicall.explorer.model.rich.Workspace;
import com.ngeneration.furthergui.DefaultMutableTreeNode;
import com.ngeneration.furthergui.DefaultTreeCellRenderer;
import com.ngeneration.furthergui.FButton;
import com.ngeneration.furthergui.FComponent;
import com.ngeneration.furthergui.FMenuItem;
import com.ngeneration.furthergui.FPanel;
import com.ngeneration.furthergui.FPopupMenu;
import com.ngeneration.furthergui.FScrollPane;
import com.ngeneration.furthergui.FTextField;
import com.ngeneration.furthergui.FTree;
import com.ngeneration.furthergui.TreeNode;
import com.ngeneration.furthergui.TreePath;
import com.ngeneration.furthergui.event.MouseAdapter;
import com.ngeneration.furthergui.event.MouseEvent;
import com.ngeneration.furthergui.event.PopupMenuEvent;
import com.ngeneration.furthergui.event.PopupMenuListener;
import com.ngeneration.furthergui.layout.BorderLayout;
import com.ngeneration.furthergui.math.Padding;
import com.nxtr.easymng.Application;
import com.nxtr.easymng.View;
import com.nxtr.easymng.ViewAdapter;

public class CollectionsView extends ViewAdapter {

	public static final String VIEW_ID = CollectionsView.class.toString();

	public static String TEST_PATH = "invalidPath\\test.json";

	private static DefaultMutableTreeNode root;
	private static FTree tree = new FTree(root = new DefaultMutableTreeNode("root"));
	private FScrollPane scrollPane = new FScrollPane(tree);
	private FPanel view = new FPanel(new BorderLayout());

	private FTextField searchField = new FTextField("");
	private FButton linkWithEditorBtn = new FButton("()");

	public static Object getNodeAt(final FTree tree, int x, int y) {
		final TreePath selPath = tree.getPathForLocation(x, y);
		return selPath != null ? selPath.getLastPathComponent() : null;
	}

	public static AbstractExplorerNode getRequestAt(FTree tree, int x, int y) {
		Object data = getNodeAt(tree, x, y);
		System.out.println("node???" + data);
		return data != null ? (AbstractExplorerNode) ((DefaultMutableTreeNode) data).getUserObject() : null;
	}

	public CollectionsView(ApiCallApplication main) {
		tree.setRooVisible(false);
		view.setPadding(new Padding());
		FPopupMenu treePopupMenu = new FPopupMenu();
		treePopupMenu.addPopupMenuListener(new PopupMenuListener() {

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				treePopupMenu.add(new FMenuItem("New Test")).addActionListener(main.getActions().get("newTest"));
				treePopupMenu.add(new FMenuItem("Add Request to Test"))
						.addActionListener(main.getActions().get("addRequestsToTest"));
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				treePopupMenu.removeAll();
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				// TODO Auto-generated method stub

			}

		});
		tree.setComponentPopupMenu(treePopupMenu);
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				if (event.getClickCount() == 2 && event.getButton() == MouseEvent.BUTTON1) {
					AbstractExplorerNode selRow = getRequestAt(tree, event.getX(), event.getY());
					if (selRow != null) {
						View view = selRow.getView();
						if (view != null) {
							main.addView(view);
							main.setActiveView(view.getId());
						}
					}
				}
			}
		});
		tree.setCellRenderer(new DefaultTreeCellRenderer() {
			@Override
			public FComponent getTreeCellRendererComponent(FTree tree, Object value, boolean sel, boolean expanded,
					boolean leaf, int row, boolean hasFocus) {
				super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
				if (value instanceof DefaultMutableTreeNode) {
					Object userdata = ((DefaultMutableTreeNode) value).getUserObject();
					if (userdata instanceof AbstractExplorerNode) {
						AbstractExplorerNode data = (AbstractExplorerNode) userdata;
						setText(data.getName());
					}
				}
				return this;
			}
		});

		FPanel topPanel = new FPanel(new BorderLayout(), new Padding(5));
		topPanel.add(searchField, BorderLayout.CENTER);
		topPanel.add(linkWithEditorBtn, BorderLayout.EAST);
		view.add(topPanel, BorderLayout.NORTH);
		view.add(scrollPane, BorderLayout.CENTER);
		linkWithEditorBtn.addActionListener(e -> {
			View view = Application.getInstance().getActiveView();
			if (view != null) {
				AbstractExplorerNode node = getNodeById(view.getId());
				if (node != null) {
					expandAndSelect(node);
					reload(node);
				}
			}
		});
//		if (main.getWorkspace() != null)
//			setupTree(main.getWorkspace());
//		tree.setShowsRootHandles(true);
//		tree.expandPath(new TreePath(root.getPath()));
//		main.addApiCallApplicationListener(new ApiCallAplicationListenerAdapter() {
//			@Override
//			public void onWorkspaceChanged(ApiCallApplicationEvent event, WorkspaceInfo oldWorkspace) {
//				setupTree(event.getApiCallApplication().getWorkspace());
//			}
//
//			@Override
//			public void onCollectionAdded(ApiCallApplicationEvent event, String collectionId) {
//				appendCollection(collectionId);
////				((DefaultTreeModel) tree.getModel()).reload();
//			}
//
//		});
	}

	public AbstractExplorerNode getNodeById(String id) {
		for (int i = 0; i < root.getChildCount(); i++) {
			DefaultMutableTreeNode realNode = (DefaultMutableTreeNode) root.getChildAt(i);
			AbstractExplorerNode n = (AbstractExplorerNode) realNode.getUserObject();
			if (n.getId().equals(id))
				return n;
			n = n.getNodeByIdRecursive(id);
			if (n != null)
				return n;
		}
		return null;
	}

	private void setupTree(Workspace workspace) {
		((DefaultMutableTreeNode) root).clear();
		if (workspace != null) {
			AbstractExplorerNode collections = workspace.getCollections();
			root.setUserObject(collections);
			System.out.println("importing nodes: " + collections.getNodes().size());
			collections.getNodes().forEach(c -> root.add(c.getUnderLayingNode()));
		}
	}

	private void expandAndSelect(AbstractExplorerNode node) {
		tree.clearSelection();
		TreeNode[] originalPath = node.getUnderLayingNode().getPath();
		TreeNode[] path = originalPath;
		tree.addSelectionPath(new TreePath(path));
		if (path.length > 0 && path[path.length - 1].isLeaf())
			path = Arrays.copyOfRange(path, 0, path.length - 1);
		TreePath treePath = new TreePath(path);
		tree.expandPath(treePath);
		tree.scrollPathToVisible(new TreePath(originalPath));
	}

	public List<AbstractExplorerNode> getSelectedNodes() {
		List<AbstractExplorerNode> nodes = new LinkedList<>();
		TreePath[] selection = tree.getSelectionPaths();
		if (selection != null) {
			for (TreePath path : selection) {
				Object data = path.getLastPathComponent();
				AbstractExplorerNode cn = (AbstractExplorerNode) ((DefaultMutableTreeNode) data).getUserObject();
				nodes.add(cn);
			}
		}
		return nodes;
	}

	private void appendCollection(String collectionId) {

	}

	public void reload(AbstractExplorerNode node) {
		AbstractExplorerNode n = (AbstractExplorerNode) ((DefaultMutableTreeNode) getUnderLayingNode(node))
				.getUserObject();
		tree.reload(getUnderLayingNode(node));
	}

	private TreeNode getUnderLayingNode(AbstractExplorerNode node) {
		String[] path = node.getPath();
		return getUnderLayingNode(root, path, 1);
	}

	private TreeNode getUnderLayingNode(TreeNode parent, String[] path, int index) {
		if (index < path.length) {
			for (int i = 0; i < parent.getChildCount(); i++) {
				DefaultMutableTreeNode realNode = (DefaultMutableTreeNode) parent.getChildAt(i);
				AbstractExplorerNode n = (AbstractExplorerNode) realNode.getUserObject();
				if (n.getId() == path[index])
					return realNode;
				else {
					TreeNode node = getUnderLayingNode(realNode, path, index + 1);
					if (node != null)
						return node;
					else
						break;
				}
			}
		}
		return null;
	}

	public AbstractExplorerNode getFirstContainer(List<AbstractExplorerNode> nodes,
			Class<? extends AbstractExplorerNode> nodeType) {
		return nodes.stream().filter(n -> n != null && n.canContain(nodeType)).findAny().orElse(null);
	}

	@Override
	public String getTitle() {
		return "Explorer";
	}

	@Override
	public FComponent getComponent() {
		return view;
	}

	public AbstractExplorerNode getFirstContainerRecursive(List<AbstractExplorerNode> selectedNodes,
			Class<? extends AbstractExplorerNode> nodeType) {
		while (selectedNodes != null) {
			var container = getFirstContainer(selectedNodes, nodeType);
			if (container != null)
				return container;
			selectedNodes = selectedNodes.stream().map(n -> n.getParent()).toList();
			if (selectedNodes.stream().allMatch(n -> n == null))
				break;
		}
		return null;
	}

}
