package com.ngeneration.apicall.explorer.model.rich;

import java.util.LinkedList;
import java.util.List;

import com.ngeneration.apicall.model.ApiCallEnvironmentValue;
import com.nxtr.easymng.workspace.IProject;

public class CollectionProject extends RestCollectionFolder implements IProject {

	public CollectionProject(String id, String name) {
		super(id, name);
	}

//	@Override
//	protected void addItems(int index, List<AbstractWorkspaceItem> items) {
//		var optional = items.stream()
//				.filter(item -> !(item instanceof RestCollectionFolder || item instanceof RestCollectionRequest))
//				.findAny();
//		if (optional.isPresent())
//			throw new RuntimeException("invalid item to add: " + optional.get());
//		super.addItems(index, items);
//	}

	@Override
	public void save() {
		PersistUtil.save(this);
	}

	public List<ApiCallEnvironmentValue> getVariables() {
		return new LinkedList<ApiCallEnvironmentValue>();
	}

}
