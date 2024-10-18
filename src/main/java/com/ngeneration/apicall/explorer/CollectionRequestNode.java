package com.ngeneration.apicall.explorer;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ngeneration.apicall.Util;
import com.ngeneration.apicall.test.HttpRequest;
import com.nxtr.easymng.View;

public class CollectionRequestNode extends AbstractExplorerNode {

	private static Gson gson = new Gson();

	private List<CollectionQuery> queries;
	private List<CollectionEvent> events;
	private List<CollectionHeader> headers;
	private BodyContent body;

	private JsonObject object;

	private String name;
	private View view;

	public CollectionRequestNode(String id, String name, JsonObject object) {
		super(id);
		this.object = object;
		this.name = name;
	}

	protected View generateView() {
		return null;// return new RequestNodeEditor(this);
	}

	private JsonObject getRequest() {
		return object;
	}

	public String getMethod() {
		return getRequest().get("method").getAsString();
	}

	public String getEndpoint() {
		return getRequest().get("endpoint").getAsString();
	}

	public void setMethod(String method) {
		getRequest().addProperty("method", method);
	}

	public void setEndpoint(String method) {
		getRequest().addProperty("endpoint", method);
	}

	public CollectionHeader getHeader(String name) {
		for (CollectionHeader hname : getHeaders())
			if (name.equalsIgnoreCase(hname.key))
				return hname;
		return null;
	}

	public void setBody(BodyContent body) {
		this.body = body;
	}

	public String getHeaderValue(String string) {
		CollectionHeader cheader = getHeader(string);
		return cheader == null ? null : cheader.getValue();
	}

	public List<CollectionHeader> getHeaders() {
		var jsonHeader = getRequest().get("header");
		if (headers == null) {
			headers = new LinkedList<>();
		}
		if (jsonHeader != null) {
			getRequest().get("header").getAsJsonArray().forEach(e -> {
				JsonObject object = e.getAsJsonObject();
				headers.add(new CollectionHeader(!getBoolean(object, "disabled"), getString(object, "key"),
						getString(object, "value"), getString(object, "type"), getString(object, "description")));
			});
		}
		return new LinkedList<>(headers);
	}

	public void setHeaders(List<CollectionHeader> headers) {
		this.headers = new LinkedList<>(headers == null ? new LinkedList<>() : headers);
	}

	public void setQueries(List<CollectionQuery> headers) {
		this.queries = new LinkedList<>(headers == null ? new LinkedList<>() : headers);
	}

	public void setEvents(List<CollectionEvent> headers) {
		this.events = new LinkedList<>(headers == null ? new LinkedList<>() : headers);
	}

	public List<CollectionQuery> getQueries() {
		if (queries == null) {
			queries = new LinkedList<>();
			JsonElement queryE = getRequest().get("query");
			if (queryE != null) {
				queryE.getAsJsonArray().forEach(e -> {
					JsonObject object = e.getAsJsonObject();
					queries.add(new CollectionQuery(!getBoolean(object, "disabled"), getString(object, "key"),
							getString(object, "value"), getString(object, "description")));
				});
			}
		}
		return new LinkedList<>(queries);
	}

	public List<CollectionEvent> getEvents() {
		if (events == null) {
			events = new LinkedList<>();
			JsonElement queryE = getRequest().get("event");
			if (queryE != null) {
				queryE.getAsJsonArray().forEach(e -> {
					JsonObject object = e.getAsJsonObject();
					JsonElement scriptE = object.get("script");
					List<String> lines = new LinkedList<>();
					if (scriptE != null) {
						JsonElement xd = scriptE.getAsJsonObject().get("exec");
						if (xd != null)
							xd.getAsJsonArray().forEach(ee -> lines.add(ee.getAsString().replace("\\\"", "")));
					}
//					JsonElement scriptE = objec.getAsJsonObject().get("exec");

					events.add(new CollectionEvent(getString(object, "listen"), new CollectionScript(
							String.join(System.lineSeparator(), lines), getString(object, "type"))));
				});
			}
		}
		return new LinkedList<>(events);
	}

	public BodyContent getBody() {
		JsonElement node = getRequest().get("body");
		if (body == null && node != null) {
			JsonObject object = node.getAsJsonObject();
			JsonElement rawNode = object.get("raw");
			JsonElement optionsNode = object.get("options");
			String raw = null;
			final Map<String, Map<String, String>> options = new HashMap<>();
			if (rawNode != null)
				raw = rawNode.getAsString();
			if (optionsNode != null) {
				optionsNode.getAsJsonObject().entrySet().forEach(e -> {
					final Map<String, String> values = new HashMap<>();
					e.getValue().getAsJsonObject().entrySet().forEach(kv -> {
						values.put(kv.getKey(), kv.getValue().getAsString());
					});
					options.put(e.getKey(), values);
				});
			}
			body = new BodyContent(object.get("mode").getAsString(), raw, options);
		}
		return body;
	}

	private boolean getBoolean(JsonObject object, String name) {
		JsonElement el = object.get(name);
		return el == null ? false : el.getAsBoolean();
	}

	private String getString(JsonObject object, String name) {
		JsonElement el = object.get(name);
		return el == null ? null : el.getAsString();
	}

	public void saveNode() throws IOException {
		JsonObject object = new JsonObject();
		object.addProperty("method", this.object.get("method").getAsString());
		object.addProperty("endpoint", this.object.get("endpoint").getAsString());
		if (body != null) {
			JsonObject content = new JsonObject();
			content.addProperty("raw", body.getRaw());
			content.addProperty("mode", body.getMode());
			Map<String, Map<String, String>> options = body.getOptions();
			if (options != null && !options.isEmpty()) {
				JsonObject optionsObject = new JsonObject();
				content.add("options", optionsObject);
				for (Map.Entry<String, Map<String, String>> m : options.entrySet()) {
					JsonObject options2Object = new JsonObject();
					optionsObject.add(m.getKey(), options2Object);
					for (Map.Entry<String, String> e : m.getValue().entrySet()) {
						options2Object.addProperty(e.getKey(), e.getValue());
					}
				}
			}
			object.add("body", content);
		}

		if (headers != null && !headers.isEmpty()) {
			JsonArray array = new JsonArray();
			for (CollectionHeader header : headers) {
				JsonObject headerObject = new JsonObject();
				headerObject.addProperty("disabled", !header.isEnabled());
				headerObject.addProperty("key", header.getKey());
				headerObject.addProperty("value", header.getValue());
				headerObject.addProperty("type", header.getType());
				headerObject.addProperty("description", header.getDescription());
				array.add(headerObject);
			}
			object.add("header", array);
		}
		if (queries != null && !queries.isEmpty()) {
			JsonArray array = new JsonArray();
			for (CollectionQuery header : queries) {
				JsonObject headerObject = new JsonObject();
				headerObject.addProperty("disabled", !header.isEnabled());
				headerObject.addProperty("key", header.getKey());
				headerObject.addProperty("value", header.getValue());
				headerObject.addProperty("description", header.getDescription());
				array.add(headerObject);
			}
			object.add("query", array);
		}
		if (events != null && !events.isEmpty()) {
			JsonArray eventsJson = new JsonArray();
			for (CollectionEvent event : events) {
				final JsonArray execArray = new JsonArray();
				Util.readLines(event.getScript().getExec()).forEach(l -> execArray.add(l));
				JsonObject scriptObject = new JsonObject();
				scriptObject.add("exec", execArray);
				scriptObject.addProperty("type", event.getScript().getType());
				JsonObject eventObject = new JsonObject();
				eventObject.addProperty("listen", event.getListen());
				eventObject.add("script", scriptObject);
				eventsJson.add(eventObject);
			}
			object.add("event", eventsJson);
		}

//		File file = ApiCallApplication.getInstance().getWorkspace().getNodeFile(this);
//		Util.writeText(file, gson.toJson(object));
//		this.object = object;
	}

	public HttpRequest getHttpRquest() {
		HttpRequest request = new HttpRequest();
		request.setEndpoint(getEndpoint());
		request.setMethod(getMethod());
		String contentType = getHeaderValue("content-type");
		if (contentType != null && getBody() != null && getBody().getRaw() != null)
			request.setBody(getBody().getRaw().getBytes());
		for (CollectionHeader h : getHeaders())
			if (h.isEnabled())
				request.getHeaders().put(String.valueOf(h.getKey()), String.valueOf(h.getValue()));
		return request;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public View getView() {
		// TODO Auto-generated method stub
		return view != null ? view : (view = generateView());
	}

	public String getPrerequestScript() {
		return getEventById("prerequest");
	}

	public String getTestScript() {
		return getEventById("test");
	}

	private String getEventById(String string) {
		for (CollectionEvent event : getEvents()) {
			String script = event.getScript().getExec();
			if (string.equalsIgnoreCase(event.getListen()))
				return script;
		}
		return null;
	}

}
