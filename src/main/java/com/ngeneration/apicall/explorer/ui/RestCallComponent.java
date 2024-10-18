package com.ngeneration.apicall.explorer.ui;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.script.ScriptException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import com.ngeneration.apicall.ApiCallApplication;
import com.ngeneration.apicall.Util;
import com.ngeneration.apicall.explorer.BodyContent;
import com.ngeneration.apicall.explorer.CollectionEvent;
import com.ngeneration.apicall.explorer.CollectionHeader;
import com.ngeneration.apicall.explorer.CollectionQuery;
import com.ngeneration.apicall.explorer.CollectionScript;
import com.ngeneration.apicall.explorer.model.rich.RestCollectionRequest;
import com.ngeneration.apicall.explorer.ui.PropertiesTable.PropertiesDataModel;
import com.ngeneration.apicall.model.Headers;
import com.ngeneration.apicall.net.HttpInvoker;
import com.ngeneration.apicall.test.HttpRequest;
import com.ngeneration.apicall.test.HttpResponse;
import com.ngeneration.apicall.texteditor.TextEditor;
import com.ngeneration.apicall.util.ApiCallUtil;
import com.ngeneration.furthergui.ButtonGroup;
import com.ngeneration.furthergui.Cursor;
import com.ngeneration.furthergui.DefaultTableModel;
import com.ngeneration.furthergui.FButton;
import com.ngeneration.furthergui.FComboBox;
import com.ngeneration.furthergui.FComponent;
import com.ngeneration.furthergui.FLabel;
import com.ngeneration.furthergui.FPanel;
import com.ngeneration.furthergui.FRadioButton;
import com.ngeneration.furthergui.FScrollPane;
import com.ngeneration.furthergui.FSplitPane;
import com.ngeneration.furthergui.FTabbedPane;
import com.ngeneration.furthergui.FTable;
import com.ngeneration.furthergui.FTextField;
import com.ngeneration.furthergui.event.ItemEvent;
import com.ngeneration.furthergui.event.ItemListener;
import com.ngeneration.furthergui.event.KeyAdapter;
import com.ngeneration.furthergui.event.KeyEvent;
import com.ngeneration.furthergui.layout.BorderLayout;
import com.ngeneration.furthergui.layout.FlowLayout;
import com.ngeneration.furthergui.math.Padding;

public class RestCallComponent {

	private final String[] DEFAULT_METHOD = new String[] { "GET", "POST", "PUT", "DELETE", "OPTIONS" };
	private final String[] DEFAULT_CONTENT_TYPES = new String[] { "none", "form-data", "x-www-form-urlencoded", "raw",
			"binary" };
	private final String[] DEFAULT_RAW_TYPES = new String[] { "TEXT", "JavaScript", "JSON", "HTML", "XML" };

	private FSplitPane mainSplit = new FSplitPane(FSplitPane.VERTICAL);
	private FPanel view = new FPanel(new BorderLayout());
	private FTextField endpointComponent = new FTextField();
	private FButton sendBtn = new FButton("SEND");

	private FTabbedPane requestPanel = new FTabbedPane();
	private FTable paramsEndpointTable;
	private FTable requestHeadersTable;
	private FTable responseHeadersTable = new FTable(new DefaultTableModel(new String[] { "", "Key", "Value" }, 0));

	private TextEditor requestTextPane = new TextEditor();
	private TextEditor responseTextPane = new TextEditor();
	private TextEditor requestPreScriptTextPane = new TextEditor();
	private TextEditor requestTestScriptTextPane = new TextEditor();

	private FLabel responseStatusLabel = new FLabel();

	private FComboBox<String> methodComponent = new FComboBox<String>();
	private RestCollectionRequest data;

	private ButtonGroup bodyModeGroup = new ButtonGroup();
	private FComboBox<String> rawType = new FComboBox<String>();
	private List<FRadioButton> contentModes = new LinkedList<>();
	private HttpResponse lastResponse;
	private String lastResponseBody;

	public RestCallComponent(RestCollectionRequest node) {
		this.data = node;

		requestPanel.setPadding(new Padding(6));
		requestTextPane.steEditable(false);
		requestTextPane.setLanguage(TextEditor.JSON_LANG);
		responseTextPane.setCursor(Cursor.getStandardCursor(Cursor.TEXT_CURSOR));
		responseTextPane.setLanguage(TextEditor.JSON_LANG);

		methodComponent.setEditable(true);
		for (String method : DEFAULT_METHOD)
			methodComponent.addItem(method);
		int selectedMethodIndex = Util.indexOfStringIgnoreCase(data.getMethod(), DEFAULT_METHOD);
		if (selectedMethodIndex != -1)
			methodComponent.setSelectedIndex(selectedMethodIndex);
		else
			methodComponent.addItem(data.getMethod());
		endpointComponent.setText(data.getEndpoint());
		endpointComponent.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.VK_ENTER && !event.isControlDown())
					execute();
			}
		});

		requestHeadersTable = new PropertiesTable(PropertiesDataModel.buildForHeaders(data.getHeaders()));
		paramsEndpointTable = new PropertiesTable(PropertiesDataModel.buildForQueries(data.getQueries()));

		requestTestScriptTextPane.setInitialText(data.getTestScript());
		requestPreScriptTextPane.setInitialText(data.getPrerequestScript());

		FPanel north = new FPanel(new BorderLayout());
		north.add(methodComponent, BorderLayout.WEST);
		north.add(endpointComponent, BorderLayout.CENTER);
		north.add(sendBtn, BorderLayout.EAST);
		north.setPadding(new Padding(10));
//		north.setBackground(Color.CYAN);
		view.add(north, BorderLayout.NORTH);
		sendBtn.addActionListener(e -> execute());
		sendBtn.setPadding(new Padding(10));

		FPanel paramsContainer = new FPanel(new BorderLayout());
		paramsContainer.add(new FScrollPane(paramsEndpointTable));

		FPanel headersContainer = new FPanel(new BorderLayout());
		headersContainer.add(new FScrollPane(requestHeadersTable));

		FPanel bodyContainer = new FPanel(new BorderLayout());
		bodyContainer.setPadding(new Padding());
		FRadioButton selectedContentTypeNode = null;
		FRadioButton conentTypeRaw = null;
		BodyContent body = data.getBodyContent();
		FPanel bodyContainerToolbar = new FPanel(new FlowLayout(FlowLayout.LEFT));
		bodyContainerToolbar.setPadding(new Padding(10, 15, 10, 6));
		for (String contentType : DEFAULT_CONTENT_TYPES) {
			FRadioButton contentTypeOption = new FRadioButton(contentType);
			contentModes.add(contentTypeOption);
			bodyModeGroup.add(contentTypeOption);
			if (body != null && contentType.equalsIgnoreCase(body.getMode()))
				selectedContentTypeNode = contentTypeOption;
			if (contentType.equals("raw"))
				conentTypeRaw = contentTypeOption;
			bodyContainerToolbar.add(contentTypeOption);
			if (bodyModeGroup.getButtonCount() == 1)
				contentTypeOption.setSelected(true);
		}
		rawType.setEnabled(false);
		for (String rawTypeText : DEFAULT_RAW_TYPES)
			rawType.addItem(rawTypeText);
		conentTypeRaw.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent event) {
				requestTextPane.setEditable(((FRadioButton) event.getSource()).isSelected());
				rawType.setEnabled(((FRadioButton) event.getSource()).isSelected());
			}
		});
		if (selectedContentTypeNode != null)
			bodyModeGroup.setSelected(selectedContentTypeNode, true);
		if (body != null) {
			String rawContent = body.getRaw();
			if (rawContent != null)
				requestTextPane.setInitialText(rawContent);
			String language = null;
			if (body.getOptions() != null) {
				Map<String, String> rawOptions = body.getOptions().get("raw");
				if (rawOptions != null) {
					language = rawOptions.get("language");
				}
				if (language == null) {
					language = data.getHeaderValue("content-type");
					if (language != null)
						language = language.toLowerCase().replace("application/", "");

				}
			}
			if (language != null) {
				for (int i = 0; i < rawType.getItemCount(); i++) {
					if (rawType.getItemAt(i).equalsIgnoreCase(language)) {
						rawType.setSelectedIndex(i);
						break;
					}
				}
			}
		}

		bodyContainerToolbar.add(rawType);
		bodyContainer.add(bodyContainerToolbar, BorderLayout.NORTH);
		bodyContainer.add(requestTextPane.getView());

//		paramsContainer.add(new FScrollPane(paramsEndpointTable));

		FPanel prerequestScriptContainer = new FPanel(new BorderLayout());
		FPanel testScriptToolbar1 = new FPanel(new FlowLayout(FlowLayout.RIGHT));
		FButton testRunBtn1 = new FButton("Run");
		testScriptToolbar1.add(testRunBtn1);
		prerequestScriptContainer.add(testScriptToolbar1, BorderLayout.NORTH);
		prerequestScriptContainer.add(requestPreScriptTextPane.getView());
		testRunBtn1.addActionListener(e -> {
			Map<String, String> params = new HashMap<>();
			try {
				runScript(requestPreScriptTextPane.getText(), params);
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		});

		FPanel testScriptContainer = new FPanel(new BorderLayout());
		FPanel testScriptToolbar = new FPanel(new FlowLayout(FlowLayout.RIGHT));
		FButton testRunBtn = new FButton("Run");
		testScriptToolbar.add(testRunBtn);
		testScriptContainer.add(testScriptToolbar, BorderLayout.NORTH);
		testScriptContainer.add(requestTestScriptTextPane.getView());
		testRunBtn.addActionListener(e -> {
			Map<String, String> params = new HashMap<>();
			try {
				params.put("responseBody", lastResponseBody);
				Map<String, Object> responseCodeObject = new HashMap<>();
				responseCodeObject.put("code", lastResponse.getStatus());
				params.put("responseCode", new Gson().toJson(responseCodeObject));
				runScript(requestTestScriptTextPane.getText(), params);
				ApiCallApplication.getInstance()
						.printToLog("rerun finished: " + data.getName() + System.lineSeparator());
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		});

		requestPanel.addTab("Params", paramsContainer);
		requestPanel.addTab("Headers", headersContainer);
		requestPanel.addTab("Body", bodyContainer);
		requestPanel.addTab("Pre-request Script", prerequestScriptContainer);
		requestPanel.addTab("Test", testScriptContainer);

		responseTextPane.setEditable(false);
		ButtonGroup responsePanelGroup = new ButtonGroup();
		FRadioButton responseBody = new FRadioButton("Body");
		FRadioButton responseCookies = new FRadioButton("Cookies");
		FRadioButton responseHeaders = new FRadioButton("Headers");
		FRadioButton responseTestResult = new FRadioButton("TestResults");

		responsePanelGroup.add(responseBody);
		responsePanelGroup.add(responseCookies);
		responsePanelGroup.add(responseHeaders);
		responsePanelGroup.add(responseTestResult);

		FPanel responseTop = new FPanel(new BorderLayout());
		FPanel responseTopLeft = new FPanel(new FlowLayout(FlowLayout.LEFT));
		FPanel responseTopRight = new FPanel(new FlowLayout(FlowLayout.LEFT));
		responseTop.add(responseTopLeft, BorderLayout.WEST);
		responseTop.add(responseTopRight, BorderLayout.EAST);
		responseTopLeft.add(responseBody);
		responseTopLeft.add(responseCookies);
		responseTopLeft.add(responseHeaders);
		responseTopLeft.add(responseTestResult);

		responseTopRight.add(responseStatusLabel);

		FPanel responseBodyContainer = new FPanel(new BorderLayout());
		responseBodyContainer.add(responseTextPane.getView());
		FPanel responseHeadersContainer = new FPanel(new BorderLayout());
		responseHeadersContainer.add(responseHeadersTable);

		FPanel responsePanel = new FPanel(new BorderLayout());
		responsePanel.setPadding(new Padding(6));
		responsePanel.add(responseTop, BorderLayout.NORTH);
		responsePanel.add(responseBodyContainer);

		mainSplit.setLeftComponent(requestPanel);
		mainSplit.setRightComponent(responsePanel);
		view.add(mainSplit, BorderLayout.CENTER);
	}

	public void execute() {
		responseTextPane.setText("Running...");
		responseStatusLabel.setText("..<<<<");

		new Thread(() -> {
			String finalResponse = null;
			try {
				Map<String, String> params = new HashMap<>();
				runScript(requestPreScriptTextPane.getText(), params);
				HttpRequest request = getRequest();
				ApiCallUtil.prepareCall(request, data.getVariables(),
						ApiCallApplication.getInstance().getSelectedEnvironmentValues());
				HttpResponse response = HttpInvoker.invoke(request);

				responseStatusLabel.setText("" + response.getStatus());

				String responseBody = null;
				try {
					responseBody = response.getAsString();
				} catch (Exception e) {
					e.printStackTrace();
				}
				lastResponse = response;
				lastResponseBody = responseBody;
				if (response != null) {
					Gson gson = new GsonBuilder().setPrettyPrinting().create();
					try {
						StringWriter stringWriter = new StringWriter();
						final JsonWriter jWriter = gson.newJsonWriter(stringWriter);
						jWriter.setIndent("    ");
						gson.toJson(JsonParser.parseString(responseBody), jWriter);
						finalResponse = stringWriter.toString();
					} catch (Exception e2) {
						finalResponse = responseBody;
					}
				}

				params = new HashMap<>();
				params.put("responseBody", responseBody);
				Map<String, Object> responseCodeObject = new HashMap<>();
				responseCodeObject.put("code", response.getStatus());
				params.put("responseCode", new Gson().toJson(responseCodeObject));
				runScript(requestTestScriptTextPane.getText(), params);

			} catch (IOException e) {
				finalResponse = "Cold not send request Error: " + e.toString() + ": " + e.getMessage();
				responseStatusLabel.setText("error~~");
				e.printStackTrace();
			} catch (ScriptException e2) {
				System.out.println("script error: " + e2.getMessage());
			}
			if (finalResponse != null) {
				String[] finalResponseArray = new String[] { finalResponse };
				// invoke latter{
				responseTextPane.setText(finalResponseArray[0]);
				// }
			}
		}).start();
	}

	private void runScript(String text, Map<String, String> params) throws ScriptException {
//		if (text != null && !text.isBlank())
//			ApiCallApplication.getInstance().executeScript(text, data.getVariables(), params);
	}

	public HttpRequest getRequest() {
		HttpRequest request = new HttpRequest();
		request.setEndpoint(endpointComponent.getText());
		request.setMethod(methodComponent.getSelectedItem().toString());
		for (int i = 0; i < requestHeadersTable.getRowCount(); i++) {
			if ("true".equals(String.valueOf(requestHeadersTable.getValueAt(i, 0))))
				request.getHeaders().put(String.valueOf(requestHeadersTable.getValueAt(i, 1)),
						String.valueOf(requestHeadersTable.getValueAt(i, 2)));
		}
		boolean hasContentType = request.getHeaders().containsKey(Headers.CONTENT_TYPE);
		// if not content-type check UI selection
		if (!hasContentType && (hasContentType = !contentModes.get(0).isSelected())) {
			var selected = bodyModeGroup.getSelected();
			String contentType = selected.getText().equals("raw") ? rawType.getSelectedItem() : selected.getText();
			request.getHeaders().put(Headers.CONTENT_TYPE, "application/" + contentType.toLowerCase());
		}
		if (hasContentType)
			request.setBody(requestTextPane.getText().getBytes(Charset.forName("UTF-8")));
		return request;
	}

	public int getSelectedRequestTabIndex() {
		return requestPanel.getSelectedIndex();
	}

	public int getSelectedResponseTabIndex() {
		return 0;
	}

	public int getSplitValue() {
		return mainSplit.getDividerLocation();
	}

	public void setSplitValue(int v) {
		mainSplit.setDividerLocation(v);
	}

	public void setSelectedRequestTabIndex(int v) {
		requestPanel.setSelectedIndex(v);
	}

	public void setSelectedResponseTabIndex(int v) {
//		requestPanel.setSelectedIndex(v);
	}

	public FComponent getView() {
		return view;
	}

	public void save() throws IOException {
		data.setMethod(methodComponent.getSelectedItem().toString());
		data.setEndpoint(endpointComponent.getText());
		String bodyMode = contentModes.stream().filter(r -> r.isSelected()).findAny().orElse(null).getText();
		Map<String, Map<String, String>> options = null;
		if ("raw".equals(bodyMode) && data.getHeader("content-type") == null) {
			options = new HashMap<>();
			Map<String, String> rawOptions = new HashMap<>();
			rawOptions.put("language", this.rawType.getSelectedItem().toString());
			options.put("raw", rawOptions);
		}
		BodyContent content = new BodyContent(bodyMode, requestTextPane.getText(), options);
		data.setBody(content);

		List<CollectionHeader> headers = new LinkedList<>();
		for (int i = 0; i < requestHeadersTable.getRowCount(); i++) {
			CollectionHeader header = new CollectionHeader(
					Boolean.valueOf(getString(requestHeadersTable.getValueAt(i, 0))),
					getString(requestHeadersTable.getValueAt(i, 1)), getString(requestHeadersTable.getValueAt(i, 2)),
					"text", getString(requestHeadersTable.getValueAt(i, 3)));
			headers.add(header);
		}
		List<CollectionQuery> queries = new LinkedList<>();
		for (int i = 0; i < paramsEndpointTable.getRowCount(); i++) {
			CollectionQuery header = new CollectionQuery(
					Boolean.valueOf(getString(requestHeadersTable.getValueAt(i, 0))),
					getString(requestHeadersTable.getValueAt(i, 1)), getString(requestHeadersTable.getValueAt(i, 2)),
					getString(requestHeadersTable.getValueAt(i, 3)));
			queries.add(header);
		}
		List<CollectionEvent> events = new LinkedList<>();
		if (requestPreScriptTextPane.getText() != null) {
			CollectionScript value = new CollectionScript(requestPreScriptTextPane.getText(), "text/javascript");
			CollectionEvent event = new CollectionEvent("prerequest", value);
			events.add(event);
		}
		if (requestTestScriptTextPane.getText() != null) {
			CollectionScript value = new CollectionScript(requestTestScriptTextPane.getText(), "text/javascript");
			CollectionEvent event = new CollectionEvent("test", value);
			events.add(event);
		}
		data.setHeaders(headers);
		data.setQueries(queries);
		data.setEvents(events);
		data.save();
	}

	private String getString(Object v) {
		return String.valueOf(v);
	}

	public String getPrerequestScript() {
		return requestPreScriptTextPane.getText();
	}

	public String getTestScript() {
		return requestTestScriptTextPane.getText();
	}

}
