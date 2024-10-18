package com.ngeneration.apicall.test;

import java.util.Map;
import java.util.TreeMap;

import lombok.Data;

@Data
public class HttpRequest {

	private String endpoint;
	private String method;

	private final Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private byte[] body;

}
