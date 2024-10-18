package com.ngeneration.apicall;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Util {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	public static String readText(File file) throws IOException {
		try (var stream = new FileInputStream(file)) {
			return readText(stream);
		}
	}

	public static String readText(InputStream stream) throws IOException {
		StringBuilder builder = new StringBuilder();
		for (String line : readTextLines(stream)) {
			if (builder.length() > 0)
				builder.append(System.lineSeparator());
			builder.append(line);
		}
		return builder.toString();
	}

	public static List<String> readTextLines(InputStream inputStream) throws IOException {
		var lines = new LinkedList<String>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String line = null;
		while ((line = reader.readLine()) != null)
			lines.add(line);
		return lines;
	}

	public static boolean startsWith(String text, String[] strings) {
		for (String string : strings) {
			if (text.startsWith(string))
				return true;
		}
		return false;
	}

	public static <T> T loadJson(File file, Class<T> clazz) throws IOException {
		return loadJson(new FileInputStream(file), clazz);
	}

	public static <T> T loadJson(InputStream stream, Class<T> clazz) throws IOException {
		try (InputStream is = stream) {
			return objectMapper.readerFor(clazz).readValue(is);
		}
	}

	public static <T> T loadJson(String json, Class<T> clazz) throws IOException {
		return objectMapper.readerFor(clazz).readValue(json);
	}

	public static List<String> readLines(String exec) {
		return Arrays.asList(exec.split("\r?\n"));
	}

	public static void writeText(File file, String text) throws IOException {
		FileWriter writer = new FileWriter(file);
		writer.write(text);
		writer.close();
	}

	public static String generateName(String name) {
		if (name.matches(".* copy\\([0-9]+\\)$")) {
			int index = Integer.parseInt(name.substring(name.lastIndexOf("(") + 1, name.length() - 1));
			name = name.replace("copy(" + index + ")", "copy(" + ++index + ")");
		} else
			name += "copy(1)";
		return name;
	}

	public static int indexOfStringIgnoreCase(String method, String[] dEFAULT_METHOD) {
		int i = 0;
		for (String t : dEFAULT_METHOD) {
			if (t.equalsIgnoreCase(method))
				return i;
			i++;
		}

		return -1;
	}

}
