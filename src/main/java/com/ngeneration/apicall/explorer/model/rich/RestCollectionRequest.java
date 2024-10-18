package com.ngeneration.apicall.explorer.model.rich;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.ngeneration.apicall.explorer.BodyContent;
import com.ngeneration.apicall.explorer.CollectionEvent;
import com.ngeneration.apicall.explorer.CollectionHeader;
import com.ngeneration.apicall.explorer.CollectionQuery;
import com.ngeneration.apicall.model.ApiCallEnvironmentValue;
import com.nxtr.easymng.workspace.AbstractWorkspaceItem;

import lombok.Getter;

@Getter
public class RestCollectionRequest extends AbstractWorkspaceItem {

	private List<CollectionQuery> queries = new LinkedList<>();
	private List<CollectionEvent> events = new LinkedList<>();
	private List<CollectionHeader> headers = new LinkedList<>();
	private BodyContent bodyContent;
	private String endpoint;
	private String method = "GET";

	public RestCollectionRequest(String name) {
		this(UUID.randomUUID().toString(), name);
	}

	public RestCollectionRequest(String id, String name) {
		super(id, name);
	}

	@Override
	public void setName(String name) {
		super.setName(name);
	}

	@Override
	public void save() {
		PersistUtil.save(this);
	}

	public void setMethod(String method) {
		this.method = method == null || method.isBlank() ? "GET" : method.trim().toUpperCase();
	}

	public String getMethod() {
		return method == null ? "GET" : method;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public String getTestScript() {
		var found = events.stream().filter(event -> "test".equalsIgnoreCase(event.getListen())).findAny();
		return found.isEmpty() ? "" : found.get().getScript().getExec();
	}

	public String getPrerequestScript() {
		var found = events.stream().filter(event -> "prerequest".equalsIgnoreCase(event.getListen())).findAny();
		return found.isEmpty() ? "" : found.get().getScript().getExec();
	}

	public String getHeaderValue(String header) {
		var value = getHeader(header);
		return value == null ? null : value.getValue();
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public CollectionHeader getHeader(String string) {
		return headers.stream().filter(h -> h.getKey().equalsIgnoreCase(string)).findAny().orElse(null);
	}

	public void setBody(BodyContent content) {
		this.bodyContent = content;
	}

	public void setHeaders(List<CollectionHeader> headers2) {
		this.headers = headers2;
	}

	public void setQueries(List<CollectionQuery> queries2) {
		this.queries = queries2;
	}

	public void setEvents(List<CollectionEvent> events2) {
		this.events = events2;
	}

	public List<ApiCallEnvironmentValue> getVariables() {
		return ((CollectionProject) getPath()[1]).getVariables();
	}

}
