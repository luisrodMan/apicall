package com.ngeneration.apicall.explorer.model.rich;

import java.util.List;

import com.nxtr.easymng.explorer.ExplorerItemUnit;
import com.nxtr.easymng.workspace.AbstractWorkspaceItem;
import com.nxtr.easymng.workspace.IWorkspaceItem;
import com.nxtr.easymng.workspace.IWorkspaceItemPath;
import com.nxtr.easymng.workspace.WorkspaceItemListener;

public class RestCollectionFolderUnit extends ExplorerItemUnit {

	private int type;

	public RestCollectionFolderUnit(RestCollectionFolder folder) {
		super(folder);
		setupFolder(this, folder);
		this.type = 1;
	}

	public RestCollectionFolderUnit(CollectionProject folder) {
		super(folder);
		setupFolder(this, folder);
		this.type = 2;
	}

	public RestCollectionFolderUnit(Workspace folder) {
		super(folder);
		setupFolder(this, folder);
		this.type = 3;
	}

	public static void setupFolder(ExplorerItemUnit root, AbstractWorkspaceItem folder) {
		root.addChildren(0, folder.getItems().stream().map(CollectionUnitFactory::createUnit).toList());
		folder.addItemListener(new WorkspaceItemListener() {
			@Override
			public void onWorkspaceItemRemoved(IWorkspaceItemPath path, List<IWorkspaceItem> workspaceItem) {
				throw new RuntimeException("xd");
			}

			@Override
			public void onWorkspaceItemAdded(IWorkspaceItemPath path, int index, List<IWorkspaceItem> workspaceItem) {
				root.addChildren(index, workspaceItem.stream().map(CollectionUnitFactory::createUnit).toList());
			}
		});
	}

	@Override
	public String getTags() {
		return type == 1 ? "rest-folder" : (type == 2 ? "rest-project" : "workspace");
	}

}
