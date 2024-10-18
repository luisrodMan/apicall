package com.ngeneration.apicall.explorer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CollectionHeader {
	private boolean enabled;
	String key;
	private String value;
	private String type;
	private String description;
}