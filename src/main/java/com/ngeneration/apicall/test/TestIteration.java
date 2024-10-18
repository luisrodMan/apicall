package com.ngeneration.apicall.test;

import java.util.LinkedList;
import java.util.List;

import lombok.Data;

@Data
public class TestIteration {

	private String title;
	private List<TestRequest> requests;

	public List<TestRequest> getRequests() {
		return requests != null ? requests : (requests = new LinkedList<>());
	}

}
