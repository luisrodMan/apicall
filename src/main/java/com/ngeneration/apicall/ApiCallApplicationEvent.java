package com.ngeneration.apicall;

import lombok.Getter;

@Getter
public class ApiCallApplicationEvent {

	private ApiCallApplication apiCallApplication;
	private boolean consumed;

	public ApiCallApplicationEvent(ApiCallApplication app) {
		this.apiCallApplication = app;
	}

	public void consume() {
		consumed = true;
	}

}
