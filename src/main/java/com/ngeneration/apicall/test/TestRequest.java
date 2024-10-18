package com.ngeneration.apicall.test;

import java.util.LinkedList;
import java.util.List;

import lombok.Data;

@Data
public class TestRequest {

	private String id;
	private volatile String name;
	private volatile HttpRequest request;
	private volatile HttpResponse response;
	private final List<Script> scripts = new LinkedList<>();
	private String data;

	public TestRequest(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public TestRequest() {
	}

	public boolean equals(Object other) {
		return other == this;
	}

}
