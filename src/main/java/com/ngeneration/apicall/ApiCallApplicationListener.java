package com.ngeneration.apicall;

import com.nxtr.easymng.workspace.WorkspaceInfo;

public interface ApiCallApplicationListener {

	void onWorkspaceChanged(ApiCallApplicationEvent event, WorkspaceInfo oldWorkspace);

}
