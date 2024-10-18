package com.ngeneration.apicall;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.ngeneration.furthergui.FButton;
import com.ngeneration.furthergui.FComponent;
import com.ngeneration.furthergui.FFrame;
import com.ngeneration.furthergui.FMenuBar;
import com.ngeneration.furthergui.FPanel;
import com.ngeneration.furthergui.FSplitPane;
import com.ngeneration.furthergui.FTabbedPane;
import com.ngeneration.furthergui.FToolBar;
import com.ngeneration.furthergui.event.AbstractAction;
import com.ngeneration.furthergui.event.Action;
import com.ngeneration.furthergui.event.ActionEvent;
import com.ngeneration.furthergui.event.Event;
import com.ngeneration.furthergui.event.KeyEvent;
import com.ngeneration.furthergui.event.KeyStroke;
import com.ngeneration.furthergui.event.WindowAdapter;
import com.ngeneration.furthergui.event.WindowEvent;
import com.ngeneration.furthergui.graphics.Color;
import com.ngeneration.furthergui.layout.BorderLayout;
import com.nxtr.easymng.AbstractApplication;
import com.nxtr.easymng.Bundle;
import com.nxtr.easymng.PropertyParser;
import com.nxtr.easymng.Setup;
import com.nxtr.easymng.View;

public abstract class FrameApplication extends AbstractApplication {

	private FFrame frame;
	private FMenuBar menubar = new FMenuBar();
	protected FToolBar toolbar = new FToolBar();
	private FButton undoBtn = new FButton("Undo");
	private FButton redoBtn = new FButton("Redo");
	private FButton saveBtn = new FButton("Save");

	protected FTabbedPane tabs2 = new FTabbedPane();

	protected FSplitPane mainSplit = new FSplitPane();
	protected FSplitPane secondSplit = new FSplitPane(FSplitPane.VERTICAL);

	public FFrame getFrame() {
		return frame;
	}

	public FrameApplication(String name) {
		frame = new FFrame(name);
//		frame.setFocusCycleRoot(true);

		secondSplit.setLeftComponent(tabs);
		secondSplit.setRightComponent(tabs2);

		mainSplit.setRightComponent(secondSplit);

		Setup.setMenu(menubar, ApiCallApplication.class.getResourceAsStream("/menu.json"), getActions());

		final Map<String, Action> commands = getActions();
		getKeys().forEach(k -> {
			Action action = commands.get(k.getCommand());
			FPanel panel = (FPanel) frame.getContainerComponent();
			if (action != null && k.getBinding() != null && !k.getBinding().isEmpty()) {
				panel.getActionMap().put(k.getCommand(), action);
				KeyStroke keyStroke = KeyStroke.getKeyStroke(k.getBinding());
				panel.getInputMap(FComponent.WHEN_IN_FOCUSED_WINDOWS).put(keyStroke, k.getCommand());
			}
		});

		KeyStroke saveKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK);
		frame.getInputMap(FComponent.WHEN_IN_FOCUSED_WINDOWS).put(saveKeyStroke, "save");
		frame.getActionMap().put("save", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					if (saveBtn.isEnabled())
						saveBtn.doClick();
				} catch (Exception e) {
				}
			}
		});
		saveBtn.setEnabled(true);
		saveBtn.addActionListener(getActions().get("save"));
		toolbar.add(saveBtn);
		frame.addWindowsListener(new WindowAdapter() {
			@Override
			public void windowOpenned(WindowEvent event) {
				System.out.println("opened xddxdx");
				setupView();
				loadViewState();
			}

			@Override
			public void windowClossin(WindowEvent event) {
				try {
					saveState();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		frame.setMenuBar(menubar);
		frame.getContainerComponent().add(toolbar, BorderLayout.NORTH);
		frame.getContainerComponent().add(mainSplit);

		frame.setDefaultCloseOperation(FFrame.EXIT_ON_CLOSE);
		frame.setDimension(1100, 850);
//		frame.setPrefferedSize(new Dimension(200, 200));
		frame.getContainerComponent().setBackground(Color.RED);
		frame.setLocationRelativeTo(null);
		frame.validate();
	}

	protected void saveState() throws IOException {
		JsonObject rootObject = new JsonObject();
		JsonArray viewsArray = new JsonArray();
		for (int i = 0; i < tabs.getTabCount(); i++) {
			int[] ii = new int[] { i };
			View view = getViews().stream().filter(f -> f.getComponent() == tabs.getComponentAt(ii[0])).findAny()
					.orElse(null);
			JsonObject viewObject = new JsonObject();
			Bundle bundle = new Bundle(FrameApplication.this);
			view.save(bundle);
			if (bundle.getPropertieCount() > 0) {
				viewObject.addProperty("class", view.getClass().getName());
				JsonObject propertiesObject = new JsonObject();
				bundle.getProperties().entrySet().forEach(entry -> {
					propertiesObject.addProperty(entry.getKey(), entry.getValue());
				});
				viewObject.add("properties", propertiesObject);
				viewsArray.add(viewObject);
			}
		}
		rootObject.add("views", viewsArray);
		final JsonObject propertiesObject = new JsonObject();
		final JsonObject appPropertiesObject = new JsonObject();
		rootObject.add("view-properties", propertiesObject);
		rootObject.add("app-properties", appPropertiesObject);
		if (tabs.getSelectedIndex() != -1)
			propertiesObject.add("mainSelectedTab", new JsonPrimitive(tabs.getSelectedIndex()));
		propertiesObject.addProperty("mainSplitValue", mainSplit.getDividerLocation());
		propertiesObject.addProperty("secondSplitValue", secondSplit.getDividerLocation());
		getProperties().entrySet()
				.forEach(e -> appPropertiesObject.addProperty(e.getKey(), String.valueOf(e.getValue())));
		Gson gson = new Gson();
		String json = gson.toJson(rootObject);
		try {
//			if (new File(getViewStateFile()).exists())
			System.out.println("savepath: " + getViewStateFile());
				Util.writeText(new File(getViewStateFile()), json);
		} catch (Exception e2) {
			e2.printStackTrace();
		}
	}

	protected abstract void setupView();

	@Override
	public FComponent getControl() {
		return frame;
	}

	protected void loadViewState() {
		Gson gson = new Gson();
		System.out.println("path: " + getViewStateFile());
		try (FileReader reader = new FileReader(getViewStateFile())) {
			JsonObject root = gson.fromJson(reader, JsonObject.class);
			root.get("views").getAsJsonArray().forEach(viewE -> {
				JsonObject viewData = viewE.getAsJsonObject();
				try {
					View view = (View) Class.forName(viewData.get("class").getAsString()).newInstance();
					Map<String, String> properties = new HashMap<>();
					if (viewData.get("properties") != null) {
						viewData.get("properties").getAsJsonObject().entrySet().forEach(entry -> {
							properties.put(entry.getKey(), entry.getValue().getAsString());
						});
					}
					Bundle bundle = new Bundle(this, properties);
					view.restore(bundle);
					addView(view);
					view.onAttached(bundle);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			PropertyParser mainProperties = PropertyParser.getParser(root.get("view-properties"));
			PropertyParser appProperties = PropertyParser.getParser(root.get("app-properties"));
			int mainTab = mainProperties.getInt("mainSelectedTab", 0);
			if (tabs.getTabCount() > mainTab)
				tabs.setSelectedIndex(mainTab);
			mainSplit.setDividerLocation(mainProperties.getInt("mainSplitValue", 0));
			secondSplit.setDividerLocation(mainProperties.getInt("secondSplitValue", 0));
			if (appProperties != null)
				appProperties.getProperties().entrySet().forEach(e -> setProperty(e.getKey(), e.getValue()));
			restoreView();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected abstract String getViewStateFile();

	protected void restoreView() {

	}

}
