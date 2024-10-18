package com.ngeneration.apicall.explorer.model.rich;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class CustomListDeserializer extends StdDeserializer<RestCollectionFolder> {

	public CustomListDeserializer() {
		this(null);
	}

	public CustomListDeserializer(Class<?> vc) {
		super(vc);
	}

	@Override
	public RestCollectionFolder deserialize(JsonParser jsonparser, DeserializationContext context)
			throws IOException, JsonProcessingException {
		JsonNode node = jsonparser.getCodec().readTree(jsonparser);
		RestCollectionFolder folder = context.readTreeAsValue(node, RestCollectionFolder.class);
		System.out.println("folder size: " + folder.getItems().size());
		System.out.println("view.:" + jsonparser.currentValue());
		return folder;
	}
}
