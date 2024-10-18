package com.ngeneration.apicall.explorer;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BodyContent {
	private String mode;
	private String raw;
	private Map<String, Map<String, String>> options;
}