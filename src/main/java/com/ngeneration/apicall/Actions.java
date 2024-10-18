package com.ngeneration.apicall;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.swing.filechooser.FileNameExtensionFilter;

import com.google.gson.Gson;
import com.ngeneration.apicall.explorer.AbstractExplorerNode;
import com.ngeneration.apicall.explorer.CollectionFolderNode;
import com.ngeneration.apicall.explorer.CollectionRequestNode;
import com.ngeneration.apicall.explorer.EnvironmentView;
import com.ngeneration.apicall.explorer.model.rich.CollectionProject;
import com.ngeneration.apicall.explorer.model.rich.RestCollectionFolder;
import com.ngeneration.apicall.explorer.model.rich.RestCollectionFolderUnit;
import com.ngeneration.apicall.explorer.model.rich.RestCollectionRequest;
import com.ngeneration.apicall.model.ApiCallEnvironment;
import com.ngeneration.apicall.postman.Postman;
import com.ngeneration.apicall.test.TestRequest;
import com.ngeneration.furthergui.FButton;
import com.ngeneration.furthergui.FComponent;
import com.ngeneration.furthergui.FDialog;
import com.ngeneration.furthergui.FFileChooser;
import com.ngeneration.furthergui.FOptionPane;
import com.ngeneration.furthergui.graphics.Color;
import com.ngeneration.furthergui.layout.FlowLayout;
import com.ngeneration.furthergui.math.Padding;
import com.nxtr.easymng.Application;
import com.nxtr.easymng.Editor;
import com.nxtr.easymng.ExecutableView;
import com.nxtr.easymng.FireActionEvent;
import com.nxtr.easymng.View;
import com.nxtr.easymng.explorer.ExplorerItemUnit;
import com.nxtr.easymng.explorer.ExplorerView;
import com.nxtr.easymng.explorer.IExplorerItemUnit;

public class Actions {

	private Gson gson = new Gson();

	private FFileChooser getFileChooser() {
		String lastDir = (String) Application.getInstance().getProperty(Application.LAST_DIRECTORY);
		FFileChooser fc = new FFileChooser();
		if (lastDir != null) {
			File file = new File(lastDir);
			if (file.isDirectory())
				fc.setCurrentDirectory(file);
		}
		return fc;
	}

	private int showOpenDialog(FFileChooser chooser) {
		int res = chooser.showOpenDialog(Application.getInstance().getControl());
		File file = chooser.getSelectedFile();
		if (file != null) {
			file = !file.isDirectory() ? file.getParentFile() : file;
			Application.getInstance().setProperty(Application.LAST_DIRECTORY, file.getAbsolutePath());
		}
		return res;
	}

	public void newAction(FireActionEvent event) {
		FDialog window = new FDialog();
		window.getContainerComponent().setPadding(new Padding(10));
		window.getContainerComponent().setLayout(new FlowLayout());
		String[] ids = "Request,Collection,Environment,Workspace".split(",");
		for (var id : ids) {
			FButton item = new NewItem(id);
			item.addActionListener(e -> window.dispose());
			item.addActionListener(ApiCallApplication.getInstance().getActions().get("new" + id));
			window.getContainerComponent().add(item);
		}
		window.setDimension(window.getPrefferedSize());
		window.setVisible(true);
	}

	private class NewItem extends FButton {
		public NewItem(String id) {
			super(id);
			setPadding(new Padding(15));
			setHoverBackground(Color.RED);
			setOpaque(false);
		}
	}

	public void newWorkspaceAction(FireActionEvent event) {
		String[] name = new String[] { FOptionPane.showInputDialog("Workspace name") };
		// validate name
		while (name[0] != null) {
			if (ApiCallApplication.getInstance().getWorkspaceContainer().getWorkspaces().stream().map(c -> c.getName())
					.filter(c -> name[0].equals(c)).findAny().isPresent()) {
				FOptionPane.showConfirmDialog(null, "", "This workspace name already exists!");
				name[0] = FOptionPane.showInputDialog("Workspace name", name[0]);
			} else
				break;
		}
		if (name[0] != null) {
			var newworkspace = ApiCallApplication.getInstance().createWorkspace(name[0]);
			ApiCallApplication.getInstance().setActiveWorkspace(newworkspace);
			ApiCallApplication.getInstance().getWorkspace().save();
		}
	}

	public void newCollectionAction(FireActionEvent event) {
		String[] name = new String[] { FOptionPane.showInputDialog("Collection name") };
		// validate name
		while (name[0] != null) {
			if (!ApiCallApplication.getInstance().getWorkspace().getItemsByName(name[0]).isEmpty()) {
				FOptionPane.showConfirmDialog(null, "", "This collection name already exists!");
				name[0] = FOptionPane.showInputDialog("Collection name", name[0]);
			} else
				break;
		}
		if (name[0] != null) {
			ApiCallApplication.getInstance().getWorkspace()
					.importProject(new CollectionProject(UUID.randomUUID().toString(), name[0]));
		}
	}

	public void newEnvironmentAction(FireActionEvent event) {
		FFileChooser fileChooser = getFileChooser();
		fileChooser.setFileFilter(new FileNameExtensionFilter("Json Files", "json"));
		if (showOpenDialog(fileChooser) == FFileChooser.APPROVE_OPTION) {
			try {
				ApiCallEnvironment environment = Postman
						.loadApiCallEnvironmentFromPostman(fileChooser.getSelectedFile());
				((ApiCallApplication) event.getApplication()).importEnronment(environment);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void newRequestAction(FireActionEvent event) {

	}

	public void importEnvironmentAction(FireActionEvent event) {
		FFileChooser fileChooser = getFileChooser();
		fileChooser.setFileFilter(new FileNameExtensionFilter("Json Files", "json"));
		if (showOpenDialog(fileChooser) == FFileChooser.APPROVE_OPTION) {
			try {
				ApiCallEnvironment environment = Postman
						.loadApiCallEnvironmentFromPostman(fileChooser.getSelectedFile());
				((ApiCallApplication) event.getApplication()).importEnronment(environment);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void showEnvironmentAction(FireActionEvent event) {
		ApiCallApplication application = (ApiCallApplication) event.getApplication();
		ApiCallEnvironment environment = application.getSelectedEnvironment();
		if (application.getViewById(environment.getId()) != null)
			application.setActiveView(environment.getId());
		else
			application.addView(new EnvironmentView(environment));
	}

	public void saveAction(FireActionEvent event) {
		View view = getActiveView();
		System.out.println("saving hhhxhdhdxhdxhdxhhdxhdxhdx");
		if (view instanceof Editor)
			((Editor) view).doSave();
	}

	public void closeViewAction(FireActionEvent event) {
		View view = getActiveView();
		if (view instanceof Editor)
			Application.getInstance().closeView(view.getId());
	}

	public void executeAction(FireActionEvent event) {
		View view = getActiveView();
		if (view instanceof ExecutableView)
			((ExecutableView) view).execute();
	}

	public void newTestAction(FireActionEvent event) {
//		TestSuit model = new TestSuit();
//
//		CollectionExplorerView explorer = getExplorerView();
//		if (explorer != null) {
//			List<AbstractExplorerNode> selectedNodes = explorer.getSelectedNodes();
//			if (selectedNodes.isEmpty())
//				throw new RuntimeException("not implemented");
//			var container = explorer.getFirstContainerRecursive(selectedNodes, TestSuitNode.class);
//			if (container == null)
//				throw new RuntimeException("Not implemented");
//			while (true) {
//				var name = FOptionPane.showInputDialog("Enter test name");
//				if (name == null)
//					return;
//				model.setName(name);
//				if (container.getNodeByName(name) != null)
//					FOptionPane.showConfirmDialog(getFrameWindow(), "", "Name already exists!");
//				else
//					break;
//			}
//			try {
//				model.setRequests(getSelectedRequests(selectedNodes));
//				String id = ApiCallApplication.getInstance().getWorkspace().registrerNodeInWorkspace(container, model);
//				var node = new TestSuitNode(id, model);
//				node.save();
//				explorer.reload(container);
//				Application.getInstance().addView(new TestView(node));
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		} else
//			throw new RuntimeException("Not implemented");
	}

	public void addRequestToTestAction(FireActionEvent event) {
//		var explorer = getExplorerView();
//		if (explorer != null) {
//			var selectedNodes = explorer.getSelectedNodes();
//			var requests = getSelectedRequests(selectedNodes);
//			var view = Application.getInstance().getActiveView();
//			if (!requests.isEmpty() && view instanceof TestView)
//				((TestView) view).addRequests(requests);
//		} else
//			throw new RuntimeException("Not implemented");
	}

	public void nextEditorAction(FireActionEvent event) {
		var viewIds = ((ApiCallApplication) Application.getInstance()).getActiveViewOrder();
		var lastId = viewIds.size() > 1 ? viewIds.get(viewIds.size() - 2) : null;
		if (lastId != null)
			Application.getInstance().setActiveView(lastId);
	}

	private FComponent getFrameWindow() {
		return Application.getInstance().getControl();
	}

	private static View getActiveView() {
		return Application.getInstance().getActiveView();
	}

//	private static CollectionExplorerView getExplorerView() {
//		return (CollectionExplorerView) Application.getInstance().getViewById(CollectionExplorerView.VIEW_ID);
//	}

	private List<TestRequest> getSelectedRequests(List<AbstractExplorerNode> nodes) {
		var requests = new LinkedList<TestRequest>();
		var ids = new LinkedList<String>();
		var names = new LinkedList<String>();
		appendRequests(nodes, ids, names);
		for (int i = 0; i < ids.size(); i++)
			requests.add(new TestRequest(ids.get(i), names.get(i)));
		return requests;
	}

	private void appendRequests(List<AbstractExplorerNode> nodes, List<String> ids, List<String> names) {
		for (AbstractExplorerNode node : nodes) {
			if (node instanceof CollectionRequestNode) {
				var p = node.getPath();
				ids.add(p[p.length - 1]);
				names.add(node.getName());
			} else if (node instanceof CollectionFolderNode)
				appendRequests(node.getNodes(), ids, names);
		}
	}

	public void addRestRequestAction(FireActionEvent event) {
		var selected = getSelectionInExplorer(RestCollectionFolderUnit.class);
		if (selected != null) {
			var newRequest = new RestCollectionRequest("New Request");
			((RestCollectionFolder) selected.getModel()).addItem(newRequest);
			newRequest.save();
			selected.getModel().save();
			selected.expand();
			Application.getInstance().addViewAndSetActive(newRequest);
		}
	}

	public void addRestFolderAction(FireActionEvent event) {
		var selected = getSelectionInExplorer(RestCollectionFolderUnit.class);
		if (selected != null) {
			var newFolder = new RestCollectionFolder("New Folder");
			long index = selected.getChildren().stream().filter(RestCollectionFolderUnit.class::isInstance).count();
			((RestCollectionFolder) selected.getModel()).addItems((int) index, List.of(newFolder));
			newFolder.save();
			selected.getModel().save();
			selected.expand();
			// selected.setToSelection();
			Application.getInstance().addViewAndSetActive(newFolder);
		}
	}

	public void renameNodeAction(FireActionEvent event) {
		var selected = getSelectionInExplorer(ExplorerItemUnit.class);
		if (selected != null) {
			var model = selected.getModel();
			String newName = FOptionPane.showInputDialog("Name", model.getName());
			if (newName != null && !newName.isBlank()) {
				if (model instanceof RestCollectionFolder folder)
					folder.setName(newName);
				else if (model instanceof RestCollectionRequest request)
					request.setName(newName);
				model.save();
				Application.getInstance().getViewById(ExplorerView.class.getName()).getComponent().repaint();
			}
		}
	}
	
	public void deleteResourceAction(FireActionEvent event) {
		var selected = getSelectionInExplorer(ExplorerItemUnit.class);
		if (selected != null) {
			var model = selected.getModel();
			String newName = FOptionPane.showInputDialog("Name", model.getName());
			if (newName != null && !newName.isBlank()) {
				if (model instanceof RestCollectionFolder folder)
					folder.setName(newName);
				else if (model instanceof RestCollectionRequest request)
					request.setName(newName);
				model.save();
				Application.getInstance().getViewById(ExplorerView.class.getName()).getComponent().repaint();
			}
		}
	}

	private static <T> T getSelectionInExplorer(Class<? extends T> clazz) {
		return clazz.cast(getSelectionInExplorer().stream().filter(c -> clazz.isAssignableFrom(c.getClass())).findAny()
				.orElse(null));
	}

	private static List<IExplorerItemUnit> getSelectionInExplorer() {
		if (Application.getInstance().getViewById(ExplorerView.class.getName()) instanceof ExplorerView explorer)
			return explorer.getSelected();
		return List.of();
	}

}
