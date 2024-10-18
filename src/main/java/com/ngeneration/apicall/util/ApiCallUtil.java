package com.ngeneration.apicall.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.ngeneration.apicall.ApiCallApplication;
import com.ngeneration.apicall.Util;
import com.ngeneration.apicall.model.ApiCallEnvironment;
import com.ngeneration.apicall.model.ApiCallEnvironmentValue;
import com.ngeneration.apicall.test.HttpRequest;
import com.ngeneration.apicall.test.TestApiModel;

public final class ApiCallUtil {

	public static TestApiModel loadTestFile(File file) throws IOException {
		return Util.loadJson(file, TestApiModel.class);
	}

	public static ApiCallEnvironment loadEnvironment(File file) throws IOException {
		return Util.loadJson(file, ApiCallEnvironment.class);
	}

	public static ApiCallEnvironment loadEnvironment(InputStream stream) throws IOException {
		return Util.loadJson(stream, ApiCallEnvironment.class);
	}

	public static void prepareCall(HttpRequest request, List<ApiCallEnvironmentValue> collectionVariables,
			List<ApiCallEnvironmentValue> environmentVariables) {
		request.setEndpoint(sanitizeVariables(request.getEndpoint(), collectionVariables, environmentVariables));
		if (request.getBody() != null)
			request.setBody(sanitizeVariables(new String(request.getBody()), collectionVariables, environmentVariables)
					.getBytes());
		for (String k : request.getHeaders().keySet())
			request.getHeaders().put(k,
					sanitizeVariables(request.getHeaders().get(k), collectionVariables, environmentVariables));

	}

	private static String sanitizeVariables(String endpoint, List<ApiCallEnvironmentValue> collectionVariables,
			List<ApiCallEnvironmentValue> environmentVariables) {
		if (endpoint != null) {
			int f = 0;
			while (true) {
				f = endpoint.indexOf("{{", f);
				if (f < 0)
					break;
				int e = endpoint.indexOf("}}", f);
				if (e < 0)
					break;
				String var = endpoint.substring(f + 2, e);
				ApiCallEnvironmentValue value = getValue(var, environmentVariables);
				if (value == null)
					value = getValue(var, collectionVariables);
				if (value == null)
					value = getValue(var, ApiCallApplication.getInstance().getGlobals().getValues());
				String val = value == null ? null : value.getValue();
				val = String.valueOf(val);
				endpoint = endpoint.substring(0, f) + val + endpoint.substring(e + 2);
				f += val.length();
			}
		}
		return endpoint;
	}

	private static ApiCallEnvironmentValue getValue(String var, List<ApiCallEnvironmentValue> variables) {
		for (ApiCallEnvironmentValue v : variables)
			if (v.getKey() != null && v.getKey().equals(var))
				return v;
		return null;
	}

}
