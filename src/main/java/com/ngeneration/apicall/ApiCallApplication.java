package com.ngeneration.apicall;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.google.gson.Gson;
import com.ngeneration.apicall.explorer.EnvironmentView;
import com.ngeneration.apicall.explorer.RequestNodeEditor;
import com.ngeneration.apicall.explorer.model.rich.ApiExplorerView;
import com.ngeneration.apicall.explorer.model.rich.PersistUtil;
import com.ngeneration.apicall.explorer.model.rich.RestCollectionRequest;
import com.ngeneration.apicall.explorer.model.rich.Workspace;
import com.ngeneration.apicall.model.ApiCallEnvironment;
import com.ngeneration.apicall.model.ApiCallEnvironmentValue;
import com.ngeneration.apicall.model.WorkspaceContainer;
import com.ngeneration.apicall.postman.Postman;
import com.ngeneration.apicall.texteditor.TextEditor;
import com.ngeneration.furthergui.ButtonGroup;
import com.ngeneration.furthergui.DefaultListCellRenderer;
import com.ngeneration.furthergui.FButton;
import com.ngeneration.furthergui.FComboBox;
import com.ngeneration.furthergui.FComponent;
import com.ngeneration.furthergui.FFileChooserNative;
import com.ngeneration.furthergui.FLabel;
import com.ngeneration.furthergui.FList;
import com.ngeneration.furthergui.FList.SelectionChangeEvent;
import com.ngeneration.furthergui.FList.SelectionChangeListener;
import com.ngeneration.furthergui.FPanel;
import com.ngeneration.furthergui.FRadioButton;
import com.ngeneration.furthergui.FScrollPane;
import com.ngeneration.furthergui.FurtherApp;
import com.ngeneration.furthergui.event.Action;
import com.ngeneration.furthergui.graphics.Color;
import com.ngeneration.furthergui.layout.BorderLayout;
import com.ngeneration.furthergui.layout.FlowLayout;
import com.ngeneration.furthergui.layout.OneVisibleLayout;
import com.ngeneration.furthergui.math.Padding;
import com.nxtr.easymng.ClassViewResolver;
import com.nxtr.easymng.Setup;
import com.nxtr.easymng.View;
import com.nxtr.easymng.explorer.DefaultExplorerCellRenderer;
import com.nxtr.easymng.explorer.ExplorerItemUnit;
import com.nxtr.easymng.explorer.ExplorerView;
import com.nxtr.easymng.keys.KeyContainer;
import com.nxtr.easymng.workspace.IWorkspace;
import com.nxtr.easymng.workspace.WorkspaceInfo;

import lombok.Data;

@Data
public class ApiCallApplication extends FrameApplication {

	private static ApiCallApplication instance;
	private static final String ACTIVE_ENVIRONMENT = "ACTIVE_ENVIRONMENT";
	private static final String SCRIPT_PATH = "script.js";

	private static final File WORKSPACE_DIR = new File(".workspace");
	private static final File WORKSPACE_FILE = new File(WORKSPACE_DIR, "workspace");

	public static ApiCallApplication getInstance() {
		return instance;
	}

	private ScriptEngine ee = new ScriptEngineManager().getEngineByName("nashorn");
	private WorkspaceContainer workspaceContainer;

	private FComboBox<ApiCallEnvironment> environmentCb;
	private FList<ApiCallEnvironment> collectionList = new FList<>();
	private FButton showEnvBtn;

	private ExplorerView explorerView;
	private final ApiCallEnvironment globals = new ApiCallEnvironment("~", "Globals");
	private TextEditor console;
	private FLabel workspaceLb = new FLabel();

	public ApiCallApplication() {
		super("Api");
		instance = this;
	}

	public void printToLog(String text) {
		console.append(text);
	}

	@Override
	protected void setupView() {
		showEnvBtn = new FButton("Show Env");
		showEnvBtn.setAction(getActions().get("showEnvironment"));
		showEnvBtn.setText("Show Env");

		environmentCb = new FComboBox<>();
		environmentCb.setMaximumSize(environmentCb.getPrefferedSize());
		environmentCb
				.addActionListener(e -> setActiveEnvironment((ApiCallEnvironment) environmentCb.getSelectedItem()));
		environmentCb.setRenderer(new DefaultListCellRenderer<ApiCallEnvironment>() {
			public FComponent getRendererComponent(FList<ApiCallEnvironment> list, ApiCallEnvironment value,
					boolean isSelected, boolean cellHasFocus, int row) {
				FComponent c = super.getRendererComponent(list, value, isSelected, cellHasFocus, row);
				((FLabel) c).setText(((ApiCallEnvironment) value).getName());
				return c;
			}
		});
		console = new TextEditor();
		tabs2.addTab("Console", console.getView());

		toolbar.add(environmentCb);
		toolbar.add(showEnvBtn);
		toolbar.validate();
		loadWorkSpace();

		FPanel leftOptionPanel = new FPanel(new FlowLayout(FlowLayout.TOP_TO_BOTTOM, true)) {
			protected void paintComponents(com.ngeneration.furthergui.graphics.Graphics g) {
				super.paintComponents(g);
				g.setColor(Color.GRAY);
				g.drawLine(getWidth(), 0, getWidth(), getHeight());
			}
		};
		FRadioButton collectionOption = new FRadioButton("Collec");
		FRadioButton envsOption = new FRadioButton("Envs");
		leftOptionPanel.add(collectionOption);
		leftOptionPanel.add(envsOption);
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(collectionOption);
		buttonGroup.add(envsOption);
		FPanel leftPanelTop = new FPanel(new BorderLayout());
		FPanel leftPanelTopRight = new FPanel(new FlowLayout(FlowLayout.RIGHT), new Padding(5));
		FButton newBtn = new FButton("New");
		newBtn.addActionListener(getActions().get("new"));
		FButton importBtn = new FButton("Import");
		leftPanelTopRight.add(importBtn);
		leftPanelTopRight.add(newBtn);
		importBtn.addActionListener(e -> {
			File file = FFileChooserNative.openDialog();
			if (file == null)
				return;
			try {
				var text = Util.readText(file);
				if (text.contains("\"_postman_variable_scope\": \"environment\",")) {
					ApiCallEnvironment environment = Postman.loadApiCallEnvironmentFromPostman(text);
					importEnronment(environment);
				} else
					importPostmanCollection(file);
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		});
//		FPanel leftPanelTopLeft = new FPanel(new FlowLayout());
//		leftPanelTopLeft.add(workspaceLb);
		FPanel leftPanelTopRR = new FPanel(new BorderLayout());
		workspaceLb.setPadding(new Padding(20, 5));
		leftPanelTopRR.add(workspaceLb, BorderLayout.CENTER);
		leftPanelTopRR.add(leftPanelTopRight, BorderLayout.EAST);

		// add views resolvers
		getViewResolverManager().addViewResolver(new ClassViewResolver<RestCollectionRequest>(
				RestCollectionRequest.class, (model) -> new RequestNodeEditor(model)));

		explorerView = new ApiExplorerView(this);
		explorerView.setCellRenderer(new DefaultExplorerCellRenderer() {
			public FComponent getExplorerCellRendererComponent(ExplorerView explorer, ExplorerItemUnit unit,
					boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
				super.getExplorerCellRendererComponent(explorer, unit, selected, expanded, leaf, row, hasFocus);
				if (unit.getModel() instanceof RestCollectionRequest req) {
					switch (req.getMethod()) {
					case "GET":
						setForeground(Color.GREEN);
						break;
					case "POST":
						setForeground(Color.YELLOW);
						break;
					case "DELETE":
						setForeground(Color.RED);
						break;
					case "PUT":
						setForeground(Color.BLUE);
						break;
					case "OPTION":
						setForeground(Color.MAGENTA);
						break;
					default:
						setForeground(Color.DARK_GRAY);
					}
				}
				return this;
			};
		});
		explorerView.getComponent().setBackground(Color.RED);
		leftPanelTop.add(leftPanelTopRR, BorderLayout.NORTH);
		leftPanelTop.add(leftOptionPanel, BorderLayout.WEST);

		FPanel optionContentPanel = new FPanel(new OneVisibleLayout());
		optionContentPanel.add(explorerView.getComponent());
		for (ApiCallEnvironment env : getEnvironments())
			collectionList.addItem(env);
		collectionList.addSelectionChangeListener(new SelectionChangeListener() {
			@Override
			public void onSelectionChanged(SelectionChangeEvent event) {
				var item = collectionList.getSelectedItem();
				if (item != null) {
					if (!setActiveView(item.getId()))
						addViewAndSetActive(new EnvironmentView(item));
				}
			}
		});
		var envsView = new FScrollPane(collectionList);
		optionContentPanel.add(envsView);
		collectionOption.addItemListener(e -> {
			if (((FRadioButton) e.getSource()).isSelected())
				((OneVisibleLayout) optionContentPanel.getLayout()).setVisible(optionContentPanel,
						explorerView.getComponent());
		});
		envsOption.addActionListener(e -> {
			if (((FRadioButton) e.getSource()).isSelected())
				((OneVisibleLayout) optionContentPanel.getLayout()).setVisible(optionContentPanel, envsView);
		});
//		((OneVisibleLayout) optionContentPanel.getLayout()).setVisible(optionContentPanel, explorerView.getView());
		leftPanelTop.add(optionContentPanel, BorderLayout.CENTER);
		buttonGroup.setSelected(collectionOption, true);

		mainSplit.setLeftComponent(leftPanelTop);
	}

	private void loadWorkSpace() {
		if (!WORKSPACE_DIR.exists())
			WORKSPACE_DIR.mkdir();
		if (!WORKSPACE_FILE.exists()) {
			try {
				Util.writeText(WORKSPACE_FILE, new Gson().toJson(new WorkspaceContainer()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			workspaceContainer = Util.loadJson(WORKSPACE_FILE, WorkspaceContainer.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (workspaceContainer.getActiveWorkspace() != null)
			setActiveWorkspace(workspaceContainer.getActiveWorkspace());
		else if (!workspaceContainer.getWorkspaces().isEmpty())
			setActiveWorkspace(workspaceContainer.getWorkspaces().get(0));
	}

	@Override
	protected void restoreView() {
		super.restoreView();
		setActiveEnvironment(getEnvironmentById((String) getProperty(ACTIVE_ENVIRONMENT)));
	}

	private ApiCallEnvironment getEnvironmentById(String environmentId) {
		return environmentCb.getItems().stream().filter(e -> e.getId() != null && e.getId().equals(environmentId))
				.findAny().orElse(null);
	}

	private void setActiveEnvironment(ApiCallEnvironment environment) {
		setProperty(ACTIVE_ENVIRONMENT, environment == null ? null : environment.getId());
		if (environment == null)
			environmentCb.setSelectedIndex(0);
		else
			environmentCb.setSelectedItem(environment);
	}

	public ApiCallEnvironment getSelectedEnvironment() {
		return environmentCb.getSelectedIndex() == 0 ? null : (ApiCallEnvironment) environmentCb.getSelectedItem();
	}

	public void importEnronment(final ApiCallEnvironment environment) {
		while (true) {
			var f = environmentCb.getItems().stream().filter(e -> e.getName().equals(environment.getName())).findAny();
			if (f.isPresent())
				environment.setName(Util.generateName(environment.getName()));
			else
				break;
		}
//		ApiCallApplication.getInstance().getWorkspace().importEnvironment(environment);
//		environmentCb.addItem(environment);
//		collectionList.addItem(environment);
//		collectionList.revalidate();
	}

	public void importPostmanCollection(File file) throws IOException {
		importPostmanCollectionInternal(file);
	}

	private void importPostmanCollectionInternal(File file) throws IOException {
//		var collectionId = ApiCallApplication.getInstance().getWorkspace().importPostmanCollection(file);
//		var event = new ApiCallApplicationEvent(this);
//		listeners.stream().filter(l -> !event.isConsumed()).forEach(l -> l.onCollectionAdded(event, collectionId));
	}

	public void executeScript(String script, List<ApiCallEnvironmentValue> collectionVariables,
			Map<String, String> params) throws ScriptException {
		Gson gson = new Gson();
		Map<String, String> localParams = new HashMap<>(params);
		localParams.put("collectionVariables", gson.toJson(getMap(collectionVariables)));
		localParams.put("environmentVariables", gson.toJson(getMap(getSelectedEnvironmentValues())));
		localParams.put("globalVariables", gson.toJson(getMap(getGlobals().getValues())));
		ee.getContext().setWriter(new StringWriter() {
			public void write(String text) {
				ApiCallApplication.getInstance().printToLog(text);
			}

			public void write(int len) {
				write("" + len);
			}

			public void write(String str, int off, int len) {
				write(str.substring(off, off + len));
			}

			@Override
			public void write(char[] cbuf, int off, int len) {
				write(new String(cbuf, off, len));
			}
		});
		int lines = 0;
		try {
			try {
				String code = Util.readText(new FileInputStream(SCRIPT_PATH));
				lines = code.split("\n").length - 3;
				code = code.replace("scriptCode", script);
				ee.eval(code);
				javax.script.Invocable invocable = (Invocable) ee;
				Object result = invocable.invokeFunction("xdxd", localParams);
				var res = new HashMap<String, Map<String, Object>>();
				for (var s : ((Map<Object, Object>) result).entrySet()) {
					var xd = new HashMap<String, Object>();
					for (var ss : ((Map<Object, Object>) s.getValue()).entrySet())
						xd.put(ss.getKey().toString(), ss.getValue());
					res.put(s.getKey().toString(), xd);
				}

				overrideProperties(this.globals.getValues(), res.get("globalProperties"));
				overrideProperties(getSelectedEnvironment().getValues(), res.get("environmentProperties"));
				overrideProperties(collectionVariables, res.get("collectionProperties"));

				for (String varKey : params.keySet()) {
					Object x = res.get(varKey);
					if (x != null)
						params.put(varKey, gson.toJson(x));
				}

			} catch (IOException | NoSuchMethodException e) {
				e.printStackTrace();
			}
		} catch (ScriptException e) {
			var message = e.getMessage();
			if (e.getLineNumber() > -1) {
				message = message.replace("at line number " + e.getLineNumber(),
						"at line number " + (e.getLineNumber() - lines));
			}
//			e.printStackTrace();
			ApiCallApplication.getInstance().printToLog(message + System.lineSeparator());
			throw e;
		}
	}

	public List<ApiCallEnvironmentValue> getSelectedEnvironmentValues() {
		var selectedEnv = ApiCallApplication.getInstance().getSelectedEnvironment();
		return selectedEnv != null ? selectedEnv.getValues() : new LinkedList<>();
	}

	private void overrideProperties(List<ApiCallEnvironmentValue> variable, Map<String, Object> newValues) {
		final var map = new HashMap<String, ApiCallEnvironmentValue>();
		globals.getValues().forEach(v -> map.put(v.getKey(), v));

		for (Map.Entry<String, Object> entry : newValues.entrySet()) {
			var va = map.get(entry.getKey());
			if (va == null) {
				va = new ApiCallEnvironmentValue(entry.getKey(), String.valueOf(entry.getValue()));
				map.put(va.getKey(), va);
			} else
				va.setValue(String.valueOf(entry.getValue()));
		}
		globals.getValues().clear();
		map.entrySet().forEach(e -> globals.getValues().add(e.getValue()));
	}

	private Map<String, String> getMap(List<ApiCallEnvironmentValue> collectionVariables) {
		var values = new HashMap<String, String>();
		if (collectionVariables != null) {
			for (var v : collectionVariables)
				values.put(v.getKey(), v.getValue());
		}
		return values;
	}

	@Override
	public View getViewById(String viewId) {
		var view = super.getViewById(viewId);
		if (view == null && ExplorerView.class.getName().equals(viewId))
			return this.explorerView;
		return view;
	}

	public List<ApiCallEnvironment> getEnvironments() {
		List<ApiCallEnvironment> environments = new LinkedList<>();
		environments.add(globals);
		int[] i = new int[1];
		for (var env : environmentCb.getItems()) {
			if (i[0] > 0)
				environments.add(env);
			i[0]++;
		}
		return environments;
	}

	public static void main(String[] args) {

//		JFrame frame = new JFrame();
//		JRadioButton button = new JRadioButton("xd", new ImageIcon("C:\\Users\\LuisRodriguez\\OneDrive - DISRUPTING, SA DE CV\\Pictures\\Screenshots\\xd.png", "xd"), false);
//		JPanel panel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
//		panel.add(button);
//		frame.getContentPane().add(panel, java.awt.BorderLayout.NORTH);
//		frame.setSize(200, 200);
//		frame.validate();
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.setVisible(true);

		FurtherApp app = new FurtherApp();
		app.setWidth(1200);
		app.setHeight(900);

		app.run((xd) -> {
			new ApiCallApplication().getControl().setVisible(true);
//			FFrame frame = new FFrame("xd");
//			frame.setDefaultCloseOperation(FFrame.EXIT_ON_CLOSE);
//			
//			FPanel panel1 = new FPanel();
//			panel1.setDimension(100, 100);
//			panel1.setBackground(Color.RED);
//			
//			FPanel panel2 = new FPanel();
//			panel2.setLocation(100 * 0, 100 * 2);
//			panel2.setDimension(100 * 3, 100);
//			panel2.setBackground(Color.BLUE);
//			panel2.setName("blue");
//			
//			FPanel panel3 = new FPanel();
//			panel3.setLocation(100 * 1, 0);
//			panel3.setDimension(100 * 1, 100);
//			panel3.setBackground(Color.WHITE);
//			panel3.setName("white");
//			
//			panel2.setLayout(null);
//			panel2.add(panel3);
//			
//			frame.getContainerComponent().setLayout(null);
//			frame.getContainerComponent().add(panel1);
//			frame.getContainerComponent().add(panel2);
//			
//			frame.setDimension(500, 500);
//			frame.setVisible(true);
		});
	}

	@Override
	protected Map<String, Action> loadActions() {
		return Setup.getActions(new Actions());
	}

	@Override
	protected KeyContainer loadKeys() {
		try {
			return Util.loadJson(ApiCallApplication.class.getResourceAsStream("/keys.json"), KeyContainer.class);
		} catch (Exception e) {
			throw new RuntimeException("xddxdx");
		}
	}

	@Override
	protected void loadViewState() {
		if (getWorkspace() != null)
			super.loadViewState();
	}

	public WorkspaceInfo createWorkspace(String name) {
		WorkspaceInfo workspace = new WorkspaceInfo(UUID.randomUUID().toString(), name);
		workspaceContainer.getWorkspaces().add(workspace);
		workspaceContainer.getWorkspaces().sort((e1, e2) -> e1.getName().compareTo(e2.getName()));
		try {
			Util.writeText(WORKSPACE_FILE, new Gson().toJson(workspaceContainer));
			new File(WORKSPACE_DIR, workspace.getId()).mkdir();
//			getWorkspace().
			saveState();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return workspace;
	}

	@Override
	protected IWorkspace loadWorkspace(WorkspaceInfo info) {
		if (!getWorkspaceContainer().getWorkspaces().stream().filter(f -> f.getId().equals(info.getId())).findAny()
				.isPresent())
			throw new RuntimeException("Workspace not found: " + info);
		// save before xdxdxd
		getViews().forEach(v -> closeView(v.getId()));
		// validate existence

		// load workspace file!!!
		try {
			File workspaceDir = new File(WORKSPACE_DIR, info.getId());
			return (IWorkspace) PersistUtil.load(new File(workspaceDir, info.getId()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void setActiveWorkspace(WorkspaceInfo info) {
		super.setActiveWorkspace(info);
		environmentCb.removeAll();
		environmentCb.addItem(new ApiCallEnvironment(null, "NO ENVIRONMENT"));
		if (getWorkspace() instanceof Workspace workspace) {
			for (ApiCallEnvironment env : workspace.getEnvironments()) {
				environmentCb.addItem(env);
			}
			workspaceContainer.setActiveWorkspace(info);
			workspaceLb.setText(info.getName());
			try {
				Util.writeText(WORKSPACE_FILE, new Gson().toJson(workspaceContainer));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		getFrame().validate();
	}

	@Override
	protected String getViewStateFile() {
		return ((Workspace) getWorkspace()).getStateFile();
	}

}