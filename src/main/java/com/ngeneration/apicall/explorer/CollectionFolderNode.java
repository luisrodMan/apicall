package com.ngeneration.apicall.explorer;

import java.util.List;

import com.ngeneration.apicall.model.ApiCallEnvironmentValue;
import com.ngeneration.apicall.test.TestSuitNode;
import com.nxtr.easymng.View;

public class CollectionFolderNode extends AbstractExplorerNode {

	private String name;
	private View view;

	public CollectionFolderNode(String id, String name) {
		super(id);
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public View getView() {
		// TODO Auto-generated method stub
		return view != null ? view : (view = generateView());
	}

	private View generateView() {
		return new CollectionFolderView(this);
	}

	@Override
	public boolean canContain(Class<? extends AbstractExplorerNode> clazz) {
		return CollectionFolderNode.class.isAssignableFrom(clazz) || CollectionRequestNode.class.isAssignableFrom(clazz)
				|| TestSuitNode.class.isAssignableFrom(clazz);
	}

}
