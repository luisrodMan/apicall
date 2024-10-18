package com.ngeneration.apicall.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiCallEnvironmentValue {

	private boolean enabled;
	private String key;
	private String value;
	private String type;

	public ApiCallEnvironmentValue(boolean enabled, String key, String value) {
		this(key, value);
		this.enabled = enabled;
	}

	public ApiCallEnvironmentValue(String key, String value) {
		this.key = key;
		this.value = value;
	}

}
