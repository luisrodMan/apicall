package com.ngeneration.apicall.net;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.Gson;
import com.ngeneration.apicall.test.HttpRequest;
import com.ngeneration.apicall.test.HttpResponse;

public class HttpInvoker {

	public static HttpResponse invoke(HttpRequest request) throws IOException {
		System.out.println("request - method: " + request.getMethod());
		System.out.println("request - endpoint: " + request.getEndpoint());
		System.out.println("request - headers: " + new Gson().toJson(request.getHeaders()));
		System.out.println("request - content: " + (request.getBody() != null && request.getBody().length > 0
				? new String(request.getBody(), Charset.forName("UTF-8"))
				: null));
		URL url = new URL(request.getEndpoint());
		final HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
		con.setRequestMethod(request.getMethod());
		request.getHeaders().entrySet().forEach(e -> con.setRequestProperty(e.getKey(), e.getValue()));
		if (request.getMethod().equalsIgnoreCase("get"))
			con.connect();
		if (request.getBody() != null && request.getBody().length > 0) {
			con.setConnectTimeout(1000 * 60);
			con.setDoOutput(true);
			con.getOutputStream().write(request.getBody());
			con.getOutputStream().flush();
		}

		HttpResponse response = new HttpResponse();
		con.getHeaderFields().entrySet().forEach(e -> response.addHeader(e.getKey(), String.join(";", e.getValue())));
		response.setStatus(con.getResponseCode());
		response.setStream(con.getResponseCode() > 199 && con.getResponseCode() < 400 ? con.getInputStream()
				: con.getErrorStream());
		return response;
	}

}
