package com.ngeneration.apicall.explorer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CollectionEvent {
	private String listen;
	private CollectionScript script;
}