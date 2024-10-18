package com.ngeneration.apicall.explorer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CollectionQuery {
	private boolean enabled;
	private String key;
	private String value;
	private String description;
}