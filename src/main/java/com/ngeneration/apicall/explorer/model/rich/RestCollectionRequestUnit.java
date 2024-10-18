package com.ngeneration.apicall.explorer.model.rich;

import com.nxtr.easymng.explorer.ExplorerItemUnit;

public class RestCollectionRequestUnit extends ExplorerItemUnit {

	private RestCollectionRequest request;

	public RestCollectionRequestUnit(RestCollectionRequest request) {
		super(request);
		this.request = request;
	}

	@Override
	public String getTags() {
		return "rest-request";
	}

}
