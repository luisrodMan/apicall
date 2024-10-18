package com.ngeneration.apicall.explorer.model.rich;

import java.util.List;
import java.util.UUID;

import com.nxtr.easymng.workspace.AbstractWorkspaceItem;

public class RestCollectionFolder extends AbstractWorkspaceItem {

	public RestCollectionFolder(String name) {
		this(UUID.randomUUID().toString(), name);
	}

	public RestCollectionFolder(String id, String name) {
		super(id, name);
	}

	@Override
	public void setName(String name) {
		super.setName(name);
	}

	@Override
	public void addItems(int index, List<AbstractWorkspaceItem> items) {
		var optional = items.stream()
				.filter(item -> !(item instanceof RestCollectionFolder || item instanceof RestCollectionRequest))
				.findAny();
		if (optional.isPresent())
			throw new RuntimeException("invalid item to add: " + optional.get());
		super.addItems(index, items);
	}

	public void addItem(AbstractWorkspaceItem item) {
		addItem(super.getItemsCount(), item);
	}

	@Override
	public void save() {
		PersistUtil.save(this);
	}

}
