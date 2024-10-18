package com.ngeneration.apicall.explorer.model.rich;

public class RestCollectionUnit extends RestCollectionFolderUnit {

	private CollectionProject collection;

	public RestCollectionUnit(CollectionProject collection) {
		super(collection);
		this.collection = collection;
	}

	public CollectionProject getCollection() {
		return collection;
	}

}
