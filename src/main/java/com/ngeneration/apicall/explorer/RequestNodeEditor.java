package com.ngeneration.apicall.explorer;

import java.util.List;

import com.ngeneration.apicall.explorer.model.rich.RestCollectionRequest;
import com.ngeneration.apicall.explorer.ui.RestCallComponent;
import com.ngeneration.apicall.test.HttpRequest;
import com.ngeneration.furthergui.FComponent;
import com.ngeneration.furthergui.FLabel;
import com.ngeneration.furthergui.FPanel;
import com.nxtr.easymng.Application;
import com.nxtr.easymng.Bundle;
import com.nxtr.easymng.Editor;
import com.nxtr.easymng.ExecutableView;
import com.nxtr.easymng.ViewAdapter;
import com.nxtr.easymng.workspace.IWorkspaceItem;

public class RequestNodeEditor extends ViewAdapter implements Editor, ExecutableView {

	private RestCollectionRequest node;
	private FComponent view;
	private RestCallComponent restCall;

	public RequestNodeEditor() {
	}

	public RequestNodeEditor(RestCollectionRequest request) {
		this.node = request;
		restCall = new RestCallComponent(request);
	}

	@Override
	public String getId() {
		return node == null ? this.toString() : node.getId();
	}

	@Override
	public String getTitle() {
		return node == null ? getId() : node.getName();
	}

	@Override
	public FComponent getComponent() {
		return view != null ? view : (view = restCall.getView());
	}

	@Override
	public void save(Bundle bundle) {
		if (node != null) {
			bundle.put("path", List.of(node.getPath()).stream().map(IWorkspaceItem::getId).toArray());
			bundle.put("selectedRequestTab", restCall.getSelectedRequestTabIndex());
			bundle.put("selectedResponseTab", restCall.getSelectedResponseTabIndex());
			bundle.put("mainDivider", restCall.getSplitValue());
		}
	}

	@Override
	public void restore(Bundle bundle) {
		String[] path = bundle.getArray("path");
		if (path != null) {
			node = (RestCollectionRequest) Application.getInstance().getWorkspace().getItemByPath(path);
			System.out.println("restoring: " + String.join("/", path));
		}
		view = null;
		if (node == null) {
			restCall = null;
			view = new FPanel();
			view.add(new FLabel("Occurred error trying to visualize: "
					+ (path == null ? this.toString() : String.join("/", "path"))));
		} else {
			restCall = new RestCallComponent(node);
			restCall.setSelectedRequestTabIndex(bundle.getInt("selectedRequestTab", 0));
//			restCall.setSelectedRequestTabIndex(bundle.getInt("selectedRequestTab", 0));
			restCall.setSplitValue(bundle.getInt("mainDivider", 0));
		}
	}

	@Override
	public void doSave() {
		try {
			restCall.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void doSaveAs() {

	}

	@Override
	public boolean isDirty() {
		return true;
	}

	public RestCollectionRequest getNode() {
		return node;
	}

	public HttpRequest getHttpRequest() {
		return restCall.getRequest();
	}

	public String getPreRequestScript() {
		return restCall.getPrerequestScript();
	}

	public String getTestScript() {
		return restCall.getTestScript();
	}

	@Override
	public void execute() {
		restCall.execute();
	}

}