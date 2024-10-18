package com.ngeneration.apicall.postman;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.ngeneration.apicall.Util;
import com.ngeneration.apicall.model.ApiCallEnvironment;
import com.ngeneration.apicall.model.ApiCallEnvironmentValue;

public final class Postman {

	public static ApiCallEnvironment loadApiCallEnvironmentFromPostman(File file) throws IOException {
		var env = loadPostmanEnvironment(file);
		return transformApiCallEnvironment(env);
	}

	public static ApiCallEnvironment loadApiCallEnvironmentFromPostman(String src) throws IOException {
		var env = loadPostmanEnvironment(src);
		return transformApiCallEnvironment(env);
	}

	public static PostmanEnvironment loadPostmanEnvironment(File file) throws IOException {
		return loadPostmanEnvironment(new FileInputStream(file));
	}

	public static PostmanEnvironment loadPostmanEnvironment(String json) throws IOException {
		return Util.loadJson(json, PostmanEnvironment.class);
	}

	public static PostmanEnvironment loadPostmanEnvironment(InputStream stream) throws IOException {
		return Util.loadJson(stream, PostmanEnvironment.class);
	}

	private static ApiCallEnvironment transformApiCallEnvironment(PostmanEnvironment postmanEnv) {
		var env = new ApiCallEnvironment();
		env.setId(postmanEnv.getId());
		env.setName(postmanEnv.getName());
		for (var v : postmanEnv.getValues()) {
			var vv = new ApiCallEnvironmentValue();
			vv.setKey(v.getKey());
			vv.setValue(v.getValue());
			vv.setType(v.getType());
			vv.setEnabled(v.isEnabled());
			env.getValues().add(vv);
		}
		return env;
	}

}
