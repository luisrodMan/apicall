package com.ngeneration.apicall.postman;

import java.util.LinkedList;
import java.util.List;

import lombok.Data;

@Data
public class PostmanEnvironment {

	private String id;
	private String name;
	private List<PostmanEnvironmentValue> values;
	private String _postman_variable_scope;
	private String _postman_exported_at;
	private String _postman_exported_using;

	public List<PostmanEnvironmentValue> getValues() {
		if (values == null)
			values = new LinkedList<>();
		return values;
	}

}
