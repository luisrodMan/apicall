package com.ngeneration.apicall.test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.script.ScriptException;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.ngeneration.apicall.ApiCallApplication;
import com.ngeneration.apicall.Util;
import com.ngeneration.apicall.explorer.CollectionsView;
import com.ngeneration.apicall.explorer.CollectionRequestNode;
import com.ngeneration.apicall.explorer.RequestNodeEditor;
import com.ngeneration.apicall.explorer.AbstractExplorerNode;
import com.ngeneration.apicall.model.ApiCallEnvironmentValue;
import com.ngeneration.apicall.net.HttpInvoker;
import com.ngeneration.apicall.texteditor.TextEditor;
import com.ngeneration.apicall.util.ApiCallUtil;
import com.ngeneration.furthergui.DefaultListCellRenderer;
import com.ngeneration.furthergui.FButton;
import com.ngeneration.furthergui.FComponent;
import com.ngeneration.furthergui.FLabel;
import com.ngeneration.furthergui.FList;
import com.ngeneration.furthergui.FPanel;
import com.ngeneration.furthergui.FScrollPane;
import com.ngeneration.furthergui.FSplitPane;
import com.ngeneration.furthergui.FTabbedPane;
import com.ngeneration.furthergui.FTextField;
import com.ngeneration.furthergui.event.KeyAdapter;
import com.ngeneration.furthergui.event.KeyEvent;
import com.ngeneration.furthergui.event.MouseAdapter;
import com.ngeneration.furthergui.event.MouseEvent;
import com.ngeneration.furthergui.layout.BorderLayout;
import com.ngeneration.furthergui.layout.FlowLayout;
import com.ngeneration.furthergui.math.Padding;
import com.nxtr.easymng.Application;
import com.nxtr.easymng.Bundle;
import com.nxtr.easymng.Editor;
import com.nxtr.easymng.ExecutableView;
import com.nxtr.easymng.ViewAdapter;

public class TestView extends ViewAdapter implements Editor, ExecutableView {

	private FComponent view = new FPanel(new BorderLayout());
	private FLabel iterationLB = new FLabel("Iterations");
	private FLabel nameLB = new FLabel("Name");
	private FTextField nameTxt = new FTextField(20);
	private FTextField iterationTxt = new FTextField(5);
	private FButton createBtn = new FButton("Run");
	private FList<TestRequest> requestList = new FList<>();

	private FTabbedPane tabs = new FTabbedPane();
	private TextEditor scriptEditor = new TextEditor();
	private TextEditor dataEditor = new TextEditor();

	private FTabbedPane mainTabs = new FTabbedPane();
	private TestSuit testSuit;

	private TestRequest lastSelectedRequest;
	private TestSuitNode testSuitNode;
	private FPanel resultiew = new FPanel(new BorderLayout());
	private TestSuitComponent resultComponent;

	public TestView() {

	}

	public TestView(TestSuitNode model) {
		setTestSuit(model);

	}

	private void setTestSuit(TestSuitNode model) {
		this.testSuitNode = model;
		this.testSuit = testSuitNode.getModel();
		nameTxt.setText(testSuit.getName());
		iterationTxt.setText(testSuit.getIterations() + "");
		dataEditor.setLanguage(TextEditor.JSON_LANG);
		dataEditor.setText(testSuit.getData());
		if (!testSuit.getScripts().isEmpty())
			scriptEditor.setText(testSuit.getScripts().get(0).getCode());
		TestRequest iterationElement = new TestRequest();
		iterationElement.setName("iteration");
		iterationElement.setData(testSuit.getData());
		if (testSuit.getScripts() != null)
			iterationElement.getScripts().addAll(testSuit.getScripts());
		requestList.addElement(iterationElement);
		lastSelectedRequest = iterationElement;
		setIds();
		FPanel rightPanel = new FPanel();
		FPanel dataPanel = new FPanel(new BorderLayout());
		dataPanel.add(dataEditor.getView(), BorderLayout.CENTER);
		FPanel scriptPanel = new FPanel(new BorderLayout());
		scriptPanel.add(scriptEditor.getView(), BorderLayout.CENTER);
		rightPanel.setLayout(new BorderLayout());
		rightPanel.add(tabs);
		mainTabs.setTabPlacement(FTabbedPane.BOTTOM);
		tabs.addTab("Data", dataPanel);
		tabs.addTab("Scripts", scriptPanel);

		FPanel toolbar = new FPanel(new FlowLayout(FlowLayout.LEFT));
		toolbar.add(nameLB);
		toolbar.add(nameTxt);
		toolbar.add(iterationLB);
		toolbar.add(iterationTxt);
		toolbar.add(createBtn);

		FPanel requestContainer = new FPanel(new BorderLayout());
		requestContainer.setPadding(new Padding());
		FPanel requestToolbar = new FPanel(new FlowLayout(FlowLayout.RIGHT));
		FButton requestDownBtn = new FButton("D");
		FButton requestUpBtn = new FButton("U");
		FButton requestDuplicateBtn = new FButton("Duplicate");

		requestDownBtn.addActionListener(e -> duplicateSelection());
		requestUpBtn.addActionListener(e -> moveSelectionUp());
		requestDownBtn.addActionListener(e -> moveSelectionDown());
		requestToolbar.add(requestDuplicateBtn);
		requestToolbar.add(requestUpBtn);
		requestToolbar.add(requestDownBtn);
		requestContainer.add(requestToolbar, BorderLayout.NORTH);
		requestContainer.add(new FScrollPane(requestList));

		view.add(toolbar, BorderLayout.NORTH);
		view.add(requestContainer, BorderLayout.WEST);
		view.add(rightPanel, BorderLayout.CENTER);

		requestList.setCellRenderer(new DefaultListCellRenderer<TestRequest>() {
			public FComponent getListCellRendererComponent(FList<TestRequest> list, TestRequest value, int index,
					boolean isSelected, boolean cellHasFocus) {
				FComponent c = super.getRendererComponent(list, value, isSelected, cellHasFocus, index);
				((FLabel) c).setText(((TestRequest) value).getName());
				return c;
			}
		});
		requestList.addKeyListener(new KeyAdapter() {
			public void keyPressed(com.ngeneration.furthergui.event.KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.VK_DELETE) {
					int[] indeices = requestList.getSelectedIndices();
					for (int i = indeices.length - 1; i > -1; i--) {
						if (indeices[i] > 0)
							requestList.remove(indeices[i]);
					}
				}
			}
		});
		requestList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg) {
				if (!arg.getValueIsAdjusting() && requestList.getSelectedItem() != null) {
					saveStatus(lastSelectedRequest);
					var selectedRequest = requestList.getSelectedItem();
					lastSelectedRequest = selectedRequest;
					dataEditor.setText("");
					scriptEditor.setText("");
					if (!selectedRequest.getScripts().isEmpty())
						scriptEditor.setText(selectedRequest.getScripts().get(0).getCode());
					if (selectedRequest.getData() != null)
						dataEditor.setText(selectedRequest.getData());
				}
			}
		});
		requestList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(com.ngeneration.furthergui.event.MouseEvent event) {
				openRequestOnDoubleClick(event);
			}
		});
		createBtn.addActionListener(e -> execute());
		mainTabs.addTab("Setup", view);
		mainTabs.addTab("Results", resultiew);
	}

	private void openRequestOnDoubleClick(MouseEvent event) {
		if (event.getClickCount() == 2) {
			var list = (FList<TestRequest>) event.getSource();
			int index = list.locationToIndex(event.getPoint());
			if (index > 0) {
				if (list.getCellBounds(index, index).contains(event.getPoint())) {
					var viewId = list.getElementAt(index).getId();
					if (Application.getInstance().getViewById(viewId) != null)
						Application.getInstance().setActiveView(viewId);
					else {
						var node = ((CollectionsView) Application.getInstance()
								.getViewById(CollectionsView.VIEW_ID)).getNodeById(viewId);
						if (node instanceof CollectionRequestNode) {
							Application.getInstance().addView(node.getView());
							Application.getInstance().setActiveView(node.getView().getId());
						}
					}
				}
			}
		}
	}

	public void addRequests(List<TestRequest> requests) {
		requests.forEach(requestList::addElement);
	}

	private int[] getSelectedIndices() {
		var selection = requestList.getSelectedIndices();
		if (selection.length > 0 && selection[0] == 0)
			return Arrays.copyOfRange(selection, 1, selection.length);
		return selection;
	}

	private void moveSelectionUp() {
		var selection = getSelectedIndices();
		if (selection.length > 0) {
			int firstSelected = selection[0];
			var inBetweenList = getInBetweenSelection(selection);
			if (inBetweenList.isEmpty() && selection[0] > 1) {
				inBetweenList.add(requestList.getElementAt(selection[0] - 1));
				firstSelected--;
			}
			int[] i = new int[] { firstSelected + selection.length };
			var toReinsert = new LinkedList<TestRequest>();
			inBetweenList.forEach(item -> {
				requestList.removeElement(item);
				toReinsert.add(item);
			});
			toReinsert.forEach(item -> requestList.insertElementAt(i[0]++, item));
			requestList.clearSelection();
			requestList.addSelectionInterval(firstSelected, firstSelected + selection.length - 1);
		}
	}

	private void moveSelectionDown() {
		var selection = getSelectedIndices();
		if (selection.length > 0) {
			var inBetweenList = getInBetweenSelection(selection);
			if (inBetweenList.isEmpty() && selection[selection.length - 1] < requestList.getSize() - 1)
				inBetweenList.add(requestList.getElementAt(selection[selection.length - 1] + 1));
			int[] i = new int[] { selection[0] };
			inBetweenList.forEach(item -> {
				requestList.removeElement(item);
				requestList.insertElement(item, i[0]++);
			});
			requestList.clearSelection();
			int firstSelection = selection[0] + inBetweenList.size();
			requestList.addSelectionInterval(firstSelection, firstSelection + selection.length - 1);
		}
	}

	private void duplicateSelection() {
		var selection = getSelectedIndices();
		if (selection.length > 0) {
			int i = selection[selection.length - 1] + 1;
			for (var r : requestList.getSelectedValuesList().stream()
					.map(item -> new TestRequest(item.getId(), item.getName())).collect(Collectors.toList()))
				requestList.insertElementAt(++selection[selection.length - 1], r);
			requestList.clearSelection();
			requestList.setSelectionInterval(i, i + selection.length - 1);
		}
	}

	private List<TestRequest> getInBetweenSelection(int[] selection) {
		List<TestRequest> inBetweenList = new LinkedList<>();
		for (int i = selection[0], j = 0; i <= selection[selection.length - 1]; i++) {
			if (selection[j] == i)
				j++;
			else
				inBetweenList.add(requestList.getElementAt(i));
		}
		return inBetweenList;
	}

	private void saveStatus(TestRequest lasRequest) {
		if (lasRequest != null) {
			lasRequest.setData(dataEditor.getText());
			if (lasRequest.getScripts().isEmpty())
				lasRequest.getScripts().add(new Script());
			lasRequest.getScripts().get(0).setCode(scriptEditor.getText());
		}
	}

	private void setIds() {
		for (var request : testSuit.getRequests())
			requestList.addElement(request);
		requestList.setSelectedIndex(0);
		saveStatus(requestList.getSelectedItem());
	}

	private TestRequest createCopy(TestRequest request) {
		var copy = new TestRequest();
		copy.setRequest(request.getRequest());
		copy.setId(request.getId());
		copy.setName(request.getName());
		copy.setData(request.getData());
		copy.getScripts().addAll(request.getScripts());
		return copy;
	}

	@Override
	public String getId() {
		return testSuitNode.getId();
	}

	@Override
	public String getTitle() {
		return testSuit.getName();
	}

	@Override
	public FComponent getComponent() {
		// TODO Auto-generated method stub
		return mainTabs;
	}

	@Override
	public void doSave() {
		testSuit.setName(nameTxt.getText());
		testSuit.setIterations(Integer.parseInt(iterationTxt.getText()));
		saveStatus(requestList.getSelectedItem());
		testSuit.setData(requestList.getElementAt(0).getData());
		if (!requestList.getElementAt(0).getScripts().isEmpty()) {
			String script = requestList.getElementAt(0).getScripts().get(0).getCode();
			if (script != null && !script.trim().isEmpty()) {
				if (requestList.getElementAt(0).getScripts().isEmpty())
					requestList.getElementAt(0).getScripts().add(new Script());
				requestList.getElementAt(0).getScripts().get(0).setCode(script);
			}
		}
		testSuit.setRequests(new LinkedList<>());
		for (int i = 1; i < requestList.getSize(); i++) {
			testSuit.getRequests().add(requestList.getElementAt(i));
		}
		try {
			this.testSuitNode.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isDirty() {
		return true;
	}

	@Override
	public void execute() {
		String name = nameTxt.getName();
		int iterations = 0;
		if (name == null || name.isEmpty()) {
			System.out.println("ingrese el nombre");
			return;

		}
		try {
			iterations = Integer.parseInt(iterationTxt.getText());
			if (iterations < 1)
				throw new RuntimeException();
		} catch (Exception e) {
			System.out.println("iteraciones invalidas");
			return;
		}
		saveStatus(requestList.getSelectedItem());
		var header = requestList.getElementAt(0);
		var suit = new TestSuit();
		suit.setName(name);
		suit.setIterations(iterations);
		suit.setScripts(header.getScripts());
		suit.setData(header.getData());
		for (int i = 1; i < requestList.getSize(); i++)
			suit.getRequests().add(createCopy(requestList.getElementAt(i)));
		if (resultComponent != null)
			resultiew.remove(resultComponent.getContainer());
		resultComponent = new TestSuitComponent(suit);
		resultiew.add(resultComponent.getContainer(), BorderLayout.CENTER);
		resultComponent.execute(iterations);
		mainTabs.setSelectedIndex(1);
	}

	private class TestSuitComponent {

		private FPanel container = new FPanel(new BorderLayout());
		private TestSuit suit;
		private List<TestIteration> iterationList = new LinkedList<>();

		public TestSuitComponent(TestSuit s) {
			this.suit = s;
		}

		public FPanel getContainer() {
			return container;
		}

		public void execute(int iterations) {
			iterationList.clear();
			container.removeAll();

			FList<TestRequest> requests = new FList<>();
			requests.setCellRenderer(new DefaultListCellRenderer<>() {
				@Override
				public FComponent getRendererComponent(FList<TestRequest> list, TestRequest value, boolean isSelected,
						boolean cellHasFocus, int index) {
					var component = super.getRendererComponent(list, value, isSelected, cellHasFocus, index);
					((FLabel) component).setText(((TestRequest) value).getName());
					return component;
				}
			});
			requestList.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent event) {
					openRequestOnDoubleClick(event);
				}
			});

			for (int i = 0; i < iterations; i++) {
				var testIteration = new TestIteration();
				testIteration.setTitle("Iteration " + (i + 1));
				var r = new TestRequest();
				r.setName("Iteration " + (i + 1));
				r.setData(suit.getData());
				r.getScripts().addAll(suit.getScripts());
				requests.addElement(r);
				for (var rr : suit.getRequests()) {
					var ss = createCopy(rr);
					requests.addElement(ss);
					testIteration.getRequests().add(ss);
				}
				iterationList.add(testIteration);
			}
			if (requests.getSize() < 1) {
				System.out.println("no tests to run");
				return;

			}

			final FPanel responseContainer = new FPanel(new BorderLayout());
			final FPanel toolbar = new FPanel(new FlowLayout(FlowLayout.RIGHT));
			final FLabel statusLb = new FLabel("Status: ");
			final FLabel testStatusLb = new FLabel("Running...");
			final FButton exportBtn = new FButton("Export");
			exportBtn.addActionListener(e -> {
				var baseDir = new File(CollectionsView.TEST_PATH).getParentFile();
				var folder = new File(baseDir, "results");
				if (folder.exists()) {
					System.out.println("folder results already exists..");
					return;
				}
				folder.mkdir();
				File pfolder = null;
				for (var iteration : iterationList) {
					pfolder = new File(folder, iteration.getTitle());
					pfolder.mkdir();
					int i = 1;
					boolean finish = false;
					for (var test : iteration.getRequests()) {
						if (test.getResponse() != null) {
							String name = i++ + ".- " + test.getName() + ".json";
							name = name.replaceAll("[\\/<>\"*?|:]", "~");
							try {
								Gson gson = new GsonBuilder().setPrettyPrinting().create();
								String res = "";
								try {
									res = test.getResponse().getAsString();
								} catch (Exception e2) {
									e2.printStackTrace();
								}
								Util.writeText(new File(pfolder, name), ((res == null || res.isEmpty()) ? ""
										: gson.toJson(JsonParser.parseString(res))));
							} catch (Exception e2) {
								e2.printStackTrace();
							}
						} else {
							finish = true;
							break;
						}
					}
					if (finish)
						break;
				}
				System.out.println("DONE....");
			});
			toolbar.add(testStatusLb);
			toolbar.add(statusLb);
			toolbar.add(exportBtn);
			final TextEditor responseTxt = new TextEditor();
			responseTxt.setLanguage(TextEditor.JSON_LANG);
			responseContainer.add(toolbar, BorderLayout.NORTH);
			responseContainer.add(responseTxt.getView());
			var split = new FSplitPane();
			split.setLeftComponent(new FScrollPane(requests));
			split.setRightComponent(responseContainer);
			container.add(split);
			container.revalidate();
			requests.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent arg0) {
					if (!arg0.getValueIsAdjusting()) {
						var r = requests.getSelectedItem();
						responseTxt.setText("");
						statusLb.setText("Status: ---");
						if (r.getResponse() != null) {
							statusLb.setText("status: " + r.getResponse().getStatus());
							String xd = null;
							try {
								xd = r.getResponse().getAsString();
								if (xd != null) {
									Gson gson = new GsonBuilder().setPrettyPrinting().create();
									responseTxt.setText(gson.toJson(JsonParser.parseString(xd)));
								} else
									responseTxt.setText(xd);
							} catch (Exception e) {
								responseTxt.setText(xd);
								e.printStackTrace();
							}
						}
					}
				}
			});

			new Thread(() -> {
//				int iteration = 0;
//				try {
//					ApiCallApplication.getInstance()
//							.printToLog("Test starting: " + testSuit.getName() + System.lineSeparator());
//					for (var testIteration : iterationList) {
//						testStatusLb.setText("Running iteration: " + (iteration + 1));
//						String requestId = testIteration.getRequests().get(0).getId();
//						var requestNode = getNodeById(requestId);
//						var collectionVariables = requestNode.getVariables();
//						var params = new HashMap<String, String>();
//						for (var script : suit.getScripts()) {
//							var iterationData = new HashMap<String, Object>();
//							iterationData.put("index", iteration);
//							iterationData.put("title", testIteration.getTitle());
//							String data = suit.getData();
//							if (data == null || data.trim().isBlank())
//								data = "{}";
//							iterationData.put("data", data);
//							params.put("iteration", new Gson().toJson(iterationData));
////							if (script.getCode() != null && !script.getCode().trim().isEmpty())
////								runScript(script.getCode(), params, collectionVariables);
//							testIteration.setTitle(String
//									.valueOf(new Gson().fromJson(params.get("iteration"), Map.class).get("title")));
//						}
//						params.clear();
//						for (var r : testIteration.getRequests()) {
//							HttpRequest httpRequest = null;
//							String prerequestScript = "";
//							String requestScript = "";
//							var editor = (CollectionRequestNodeEditor) ApiCallApplication.getInstance()
//									.getViewById(r.getId());
//							if (editor != null) {
//								httpRequest = editor.getHttpRequest();
//								prerequestScript = editor.getPreRequestScript();
//								requestScript = editor.getTestScript();
//							} else {
//								var crequestNode = (CollectionRequestNode) getNodeById(r.getId());
//								httpRequest = crequestNode.getHttpRquest();
//								prerequestScript = crequestNode.getPrerequestScript();
//								requestScript = crequestNode.getTestScript();
//							}
//							r.setRequest(httpRequest);
////							runScript(prerequestScript, params, collectionVariables);
//							for (var s : r.getScripts())
////								if (s.getCode() != null && !s.getCode().isBlank())
////									runScript(s.getCode(), params, collectionVariables);
////							ApiCallUtil.prepareCall(r.getRequest(), collectionVariables,
////									ApiCallApplication.getInstance().getSelectedEnvironment().getValues());
//							try {
//								r.setResponse(HttpInvoker.invoke(r.getRequest()));
//							} catch (Exception e) {
//								HttpResponse response = new HttpResponse();
//								response.setData(e.getClass().getName() + ": " + e.getMessage());
//								response.setStatus(100);
//								r.setResponse(response);
//								testStatusLb.setText(
//										"Error! on iteration: " + (iteration + 1) + " request: " + r.getName());
//								e.printStackTrace();
//								break;
//							}
//							try {
//								params = new HashMap<>();
//								params.put("responseBody", r.getResponse().getAsString());
//								var responseCodeObject = new HashMap<String, Object>();
//								responseCodeObject.put("code", r.getResponse().getStatus());
//								params.put("responseCode", new Gson().toJson(responseCodeObject));
////								runScript(requestScript, params, collectionVariables);
//							} catch (ScriptException e) {
//								System.out.println("error on script: " + e.getMessage());
//								break;
//							} catch (IOException e) {
//								e.printStackTrace();
//								HttpResponse response = new HttpResponse();
//								response.setData(e.getClass().getName() + ": " + e.getMessage());
//								response.setStatus(100);
//								r.setResponse(response);
////								testStatusLb.setText("Error! on iteration: " + (iteratoin + 1) + " request: " + r.getName());
//							}
//						}
//						iteration++;
//					}
//					testStatusLb.setText("DONE! ");
//				} catch (ScriptException e) {
//					testStatusLb.setText("Error! on iteration: " + (iteration + 1));
//					e.printStackTrace();
//				}
			}).start();
		}

		private AbstractExplorerNode getNodeById(String requestId) {
			var explorer = ((CollectionsView) Application.getInstance()
					.getViewById(CollectionsView.VIEW_ID));
			return explorer.getNodeById(requestId);
		}

	}

	private void runScript(String text, Map<String, String> params, List<ApiCallEnvironmentValue> vars)
			throws ScriptException {
		if (text != null && !text.isBlank())
			ApiCallApplication.getInstance().executeScript(text, vars, params);
	}

	@Override
	public void save(Bundle bundle) {
		if (testSuit != null) {
			super.save(bundle);
			bundle.put("id", testSuitNode.getId());
			bundle.put("name", testSuitNode.getName());
			bundle.put("selectedTab", mainTabs.getSelectedIndex());
			bundle.put("selectedTab2", tabs.getSelectedIndex());
		}
	}

	@Override
	public void restore(Bundle bundle) {
		super.restore(bundle);
		String id = bundle.getString("id");
		String name = bundle.getString("name");
		int selectedTab = bundle.getInt("selectedTab", 0);
		int selectedTab2 = bundle.getInt("selectedTab2", 0);
		var explorer = (CollectionsView) Application.getInstance().getViewById(CollectionsView.VIEW_ID);
		var node = explorer.getNodeById(id);
		if (node != null) {
			setTestSuit((TestSuitNode) node);
			mainTabs.setSelectedIndex(selectedTab);
			tabs.setSelectedIndex(selectedTab2);
		} else
			mainTabs.addTab("Error ~ " + name, new FLabel("Test not found - " + name + " id : " + id));
	}

	@Override
	public void doSaveAs() {

	}

}
