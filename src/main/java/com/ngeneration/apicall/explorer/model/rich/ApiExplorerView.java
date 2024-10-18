package com.ngeneration.apicall.explorer.model.rich;

import com.nxtr.easymng.Application;
import com.nxtr.easymng.explorer.ExplorerItemUnit;
import com.nxtr.easymng.explorer.ExplorerView;
import com.nxtr.easymng.workspace.IWorkspace;

public class ApiExplorerView extends ExplorerView {

	public ApiExplorerView(Application application) {
		super(application);
	}

	@Override
	public ExplorerItemUnit setupWorkspace(IWorkspace iWorkspace) {
		return new RestCollectionFolderUnit((Workspace) iWorkspace);
	}

}
