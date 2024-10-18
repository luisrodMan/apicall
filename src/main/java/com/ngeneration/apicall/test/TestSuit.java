package com.ngeneration.apicall.test;

import java.util.List;

import lombok.Data;

@Data
public class TestSuit {

	private volatile boolean runned;
	private String name;
	private int iterations;
	private String data;
	private List<TestRequest> requests;
	private List<Script> scripts;
	
}
