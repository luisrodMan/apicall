package com.ngeneration.apicall.model;

import java.util.ArrayList;
import java.util.List;

import com.nxtr.easymng.workspace.WorkspaceInfo;

import lombok.Data;

@Data
public class WorkspaceContainer {

	private WorkspaceInfo activeWorkspace;
	private final List<WorkspaceInfo> workspaces = new ArrayList<>();

}
