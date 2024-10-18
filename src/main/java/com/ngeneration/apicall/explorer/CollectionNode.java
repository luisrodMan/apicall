package com.ngeneration.apicall.explorer;

import java.util.List;

import com.ngeneration.apicall.model.ApiCallEnvironmentValue;
import com.ngeneration.apicall.test.TestSuitNode;
import com.nxtr.easymng.View;

public class CollectionNode extends AbstractExplorerNode {

	private String name;
	private View view;
	private List<ApiCallEnvironmentValue> variables;

	public CollectionNode(String id, String name, List<ApiCallEnvironmentValue> variables) {
		super(id);
		this.name = name;
		this.variables = variables;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public View getView() {
		return view != null ? view : (view = generateView());
	}

	private View generateView() {
		return null;
	}

	@Override
	public boolean canContain(Class<? extends AbstractExplorerNode> clazz) {
		return CollectionRequestNode.class.isAssignableFrom(clazz)
				|| CollectionFolderNode.class.isAssignableFrom(clazz);
	}

}
