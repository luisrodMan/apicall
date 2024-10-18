package com.ngeneration.apicall.postman;

import lombok.Data;

@Data
public class PostmanEnvironmentValue {

	private String key;
	private String value;
	private boolean enabled;
	private String type;

}
