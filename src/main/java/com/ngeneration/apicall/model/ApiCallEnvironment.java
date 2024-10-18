package com.ngeneration.apicall.model;

import java.util.LinkedList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiCallEnvironment {

	private String id;
	private String name;
	private final List<ApiCallEnvironmentValue> values = new LinkedList<>();

	public String toString() {
		return name;
	}

}
