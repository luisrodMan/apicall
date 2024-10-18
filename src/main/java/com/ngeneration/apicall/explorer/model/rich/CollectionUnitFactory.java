package com.ngeneration.apicall.explorer.model.rich;

import com.nxtr.easymng.explorer.IExplorerItemUnit;
import com.nxtr.easymng.workspace.IWorkspaceItem;

public class CollectionUnitFactory {

	public static IExplorerItemUnit createUnit(IWorkspaceItem item) {
		if (item instanceof RestCollectionRequest item2)
			return createRestCollectionRequest(item2);
		else if (item instanceof CollectionProject item2)
			return createRestCollectionFolder(item2);
		else if (item instanceof RestCollectionFolder item2)
			return createRestCollectionFolder(item2);
		else
			throw new RuntimeException("invalid instance " + item);
	}

	private static IExplorerItemUnit createRestCollectionFolder(CollectionProject item2) {
		return new RestCollectionFolderUnit(item2);
	}
	
	private static IExplorerItemUnit createRestCollectionFolder(RestCollectionFolder item2) {
		return new RestCollectionFolderUnit(item2);
	}

	private static IExplorerItemUnit createRestCollectionRequest(RestCollectionRequest item) {
		var model = new RestCollectionRequestUnit(item);
		return model;
	}

}
