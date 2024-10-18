package com.ngeneration.apicall.explorer;

import com.ngeneration.apicall.ApiCallApplication;
import com.ngeneration.apicall.explorer.model.rich.Workspace;
import com.ngeneration.apicall.explorer.ui.PropertiesTable;
import com.ngeneration.apicall.explorer.ui.PropertiesTable.PropertiesDataModel;
import com.ngeneration.apicall.model.ApiCallEnvironment;
import com.ngeneration.apicall.model.ApiCallEnvironmentValue;
import com.ngeneration.furthergui.FComponent;
import com.ngeneration.furthergui.FPanel;
import com.ngeneration.furthergui.FScrollPane;
import com.ngeneration.furthergui.FTable;
import com.ngeneration.furthergui.layout.BorderLayout;
import com.nxtr.easymng.Editor;
import com.nxtr.easymng.ViewAdapter;

public class EnvironmentView extends ViewAdapter implements Editor {

	private ApiCallEnvironment environment;
	private FComponent view;

	public EnvironmentView() {
	}

	public EnvironmentView(ApiCallEnvironment env) {
		this.environment = env;
	}

	@Override
	public String getId() {
		return (environment != null ? environment.getId() : super.getId());
	}

	@Override
	public String getTitle() {
		return (environment != null ? environment.getName() : "Error loading view..");
	}

	@Override
	public FComponent getComponent() {
		return view == null ? view = createView(environment) : view;
	}

	FTable table;

	private FComponent createView(ApiCallEnvironment environment2) {
		var view = new FPanel();
		view.setLayout(new BorderLayout());
		table = new PropertiesTable(PropertiesDataModel.buildForEnvironment(environment));
		view.add(new FScrollPane(table));
		return view;
	}

	@Override
	public void doSave() {
		System.out.println("saving xdxdxddxxddx");
		environment.getValues().clear();
		for (int i = 0; i < table.getRowCount() - 1; i++)
			environment.getValues().add(new ApiCallEnvironmentValue((boolean) table.getValueAt(i, 0),
					(String) table.getValueAt(i, 1), (String) table.getValueAt(i, 2)));
//		ApiCallApplication.getInstance().getWorkspace().saveEnvironment(environment);
	}

	@Override
	public void doSaveAs() {

	}

	@Override
	public boolean isDirty() {
		return true;
	}

}
