package com.ngeneration.apicall.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.ngeneration.apicall.Util;

import lombok.Data;

@Data
public class HttpResponse {

	private int status;
	private Map<String, String> headers = new HashMap<>();
	private InputStream stream;
	private String data;

	public void addHeader(String key, String join) {
		headers.put(key, join);
	}

	public void setStream(InputStream stream) {
		try {
			this.stream = stream;
			data = getAsString();
		} catch (Exception e) {
		}
	}

	public String getAsString() throws IOException {
		return data != null ? data : (stream == null ? null : (data = Util.readText(stream)));
	}

}
