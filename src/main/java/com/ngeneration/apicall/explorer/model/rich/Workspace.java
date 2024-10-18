package com.ngeneration.apicall.explorer.model.rich;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ngeneration.apicall.Util;
import com.ngeneration.apicall.explorer.AbstractExplorerNode;
import com.ngeneration.apicall.explorer.CollectionFolderNode;
import com.ngeneration.apicall.explorer.CollectionRequestNode;
import com.ngeneration.apicall.model.ApiCallEnvironment;
import com.ngeneration.apicall.model.ApiCallEnvironmentValue;
import com.ngeneration.apicall.test.TestSuit;
import com.ngeneration.apicall.test.TestSuitNode;
import com.ngeneration.apicall.util.ApiCallUtil;
import com.nxtr.easymng.workspace.AbstractWorkspace;
import com.nxtr.easymng.workspace.AbstractWorkspaceItem;
import com.nxtr.easymng.workspace.IProject;
import com.nxtr.easymng.workspace.IWorkspaceItem;
import com.nxtr.easymng.workspace.WorkspaceInfo;

public class Workspace extends AbstractWorkspace {

	private static File WORKBENCH_STATE_FILE;

	private static final String TEST_PREFIX = "~";
	private static final String COLLECTION_PREFIX = "@";
	private static final String INFO_FILE_NAME = ".__settings";
	private static final String ENV_FILE_PREFIX = "env_";
	private transient File workspaceFolder;

	private transient long id = 0;
	private transient Gson gson = new Gson();
	private transient AbstractExplorerNode collections;
	private List<ApiCallEnvironment> environments;

	private transient final List<CollectionProject> collections2 = new LinkedList<>();
	
	public Workspace(String id, String name, File workspaceDir) {
		super(id, name);
		workspaceFolder = workspaceDir;
		try {
			collections = load();
			environments = loadEnvironments();
		} catch (IOException e) {
			e.printStackTrace();
			collections = getDefaultWorkspace();
		}
		WORKBENCH_STATE_FILE = new File(workspaceFolder, ".state");

	}

	public List<CollectionProject> getCollections2() {
		return new LinkedList<>(collections2);
	}

	public void createCollection(String collectionName) {

	}

	public void createCollection(int index, String collectionName) {

	}

	public AbstractExplorerNode getCollections() {
		return collections;
	}

	public List<ApiCallEnvironment> getEnvironments() {
		return environments;
	}

	@Deprecated
	public List<ApiCallEnvironment> loadEnvironments() {
		var envs = new LinkedList<ApiCallEnvironment>();
		for (var env : workspaceFolder.listFiles(f -> f.isFile() && f.getName().startsWith(ENV_FILE_PREFIX))) {
			try {
				envs.add(ApiCallUtil.loadEnvironment(env));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return envs;
	}

	public ApiCallEnvironment importEnvironment(ApiCallEnvironment environment) {
		environment.setId(UUID.randomUUID().toString());
		// better clone object xd
		saveEnvironment(environment);
		return environment;
	}

	public ApiCallEnvironment saveEnvironment(ApiCallEnvironment environment) {
		try {
			Util.writeText(new File(workspaceFolder, ENV_FILE_PREFIX + environment.getId()),
					new Gson().toJson(environment));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return environment;
	}

	public File getWorkspaceFolder() {
		return workspaceFolder;
	}

	public void newCollection(String name) throws IOException {
		createFolder(workspaceFolder, name);
	}

	private String createId() {
		return String.valueOf(id == 0 ? (id = System.currentTimeMillis()) : ++id);
	}

	public String importPostmanCollection(File file) throws IOException {
		return importPostmanFile(file);
	}

	private String importPostmanFile(File file) throws IOException {
		Gson gson = new Gson();
		try (var reader = new FileReader(file)) {
			var root = gson.fromJson(reader, JsonObject.class);
			var id = appendId(workspaceFolder, root.get("info").getAsJsonObject().get("name").getAsString(), "");
			File rootFolder = new File(workspaceFolder, id);
			rootFolder.mkdir();
			appendNode(rootFolder, root);
			return id;
		}
	}

	private void appendNode(File rootFolder, JsonObject object) throws IOException {
		JsonElement ee = object.get("item");
		if (ee != null) {
			for (var e : ee.getAsJsonArray()) {
				var itemObject = e.getAsJsonObject();
				var requestElement = itemObject.get("request");
				if (requestElement != null)
					createRequest(rootFolder, itemObject);
				else {
					var newFolder = createFolder(rootFolder, itemObject.get("name").getAsString());
					appendNode(newFolder, itemObject);
				}
			}
		}
	}

	private void createRequest(File folder, JsonObject itemObject) throws IOException {
		var id = appendId(folder, itemObject.get("name").getAsString(), "");
		File file = new File(folder, id);
		file.createNewFile();

		JsonObject requestData = itemObject.get("request").getAsJsonObject();
		JsonObject object = new JsonObject();
		object.addProperty("method", requestData.get("method").getAsString());
		object.addProperty("endpoint", requestData.get("url").getAsJsonObject().get("raw").getAsString());
		JsonElement bodyElement = requestData.get("body");
		if (bodyElement != null)
			object.add("body", bodyElement);
		add(object, "header", requestData.get("header"));
		add(object, "query", requestData.get("url").getAsJsonObject().get("query"));
		add(object, "event", itemObject.get("event"));
		try (var writer = new FileWriter(file)) {
			writer.write(gson.toJson(object));
		}
	}

	private void add(JsonObject object, String name, JsonElement headerE) {
		if (headerE != null) {
			var array = new JsonArray();
			object.add(name, array);
			headerE.getAsJsonArray().forEach(h -> array.add(h));
		}
	}

	private File createFolder(File rootFolder, String name) throws IOException {
		String id = appendId(rootFolder, name, "");
		File file = new File(rootFolder, id);
		file.mkdir();
		return file;
	}

	public String registrerNodeInWorkspace(AbstractExplorerNode container, TestSuit suit) throws IOException {
		return appendId(getNodeFile(container), suit.getName(), TEST_PREFIX);
	}

	private String appendId(File parentDirectory, String name, String idPrefix) throws IOException {
		File conf = new File(parentDirectory, INFO_FILE_NAME);
		if (!conf.exists())
			conf.createNewFile();
		String id = createId();
		try (var writer = new FileWriter(conf, true)) {
			writer.append(idPrefix + id + " " + name).append(System.lineSeparator());
		}
		return id;
	}

	@Deprecated
	public AbstractExplorerNode load() throws IOException {
		List<ApiCallEnvironmentValue> collectionVariables = new LinkedList<>();
		return createNode(workspaceFolder, workspaceFolder.getName(), workspaceFolder.getName(), collectionVariables);
	}

	public AbstractExplorerNode getDefaultWorkspace() {
		List<ApiCallEnvironmentValue> collectionVariables = new LinkedList<>();
		return new CollectionFolderNode(workspaceFolder.getName(), workspaceFolder.getName());
	}

	private AbstractExplorerNode createNode(File directory, String id, String name,
			List<ApiCallEnvironmentValue> collectionVariables) throws IOException {
		CollectionFolderNode folder = new CollectionFolderNode(id, name);
		File infoFile = new File(directory, Workspace.INFO_FILE_NAME);
		if (!infoFile.exists())
			return folder;
		for (var line : Files.readAllLines(infoFile.toPath())) {
			if (line.isEmpty())
				continue;
			int i = line.indexOf(" ");
			String childId = line.substring(0, i);
			String childName = line.substring(i + 1);
			String prefix = null;
			if (childId.startsWith(TEST_PREFIX) || childId.startsWith(COLLECTION_PREFIX))
				prefix = childId.substring(0, 1);
			if (prefix != null)
				childId = line.substring(i + 1);
			else
				prefix = COLLECTION_PREFIX;
			File childFile = new File(directory, childId);
			if (childFile.isDirectory())
				folder.addNode(folder.getNodeCount(), createNode(childFile, childId, childName, collectionVariables));
			else {
				try (InputStream stream = new FileInputStream(childFile)) {
					if (prefix.equals(COLLECTION_PREFIX))
						folder.addNode(folder.getNodeCount(), new CollectionRequestNode(childId, childName,
								gson.fromJson(new InputStreamReader(stream), JsonObject.class)));
					else if (prefix.equals(TEST_PREFIX))
						folder.addNode(folder.getNodeCount(),
								new TestSuitNode(childId, Util.loadJson(stream, TestSuit.class)));
					else
						throw new RuntimeException("Invalid prefix: " + prefix);
				}
			}
		}
		return folder;
	}

	public File getNodeFile(AbstractExplorerNode collectionNode) {
		return new File(workspaceFolder.getParent(), String.join(File.separator, collectionNode.getPath()));
	}

	public String getStateFile() {
		return WORKBENCH_STATE_FILE.getAbsolutePath();
	}

	public static void initialize(WorkspaceInfo workspace) {

	}

	@Override
	public void importProject(IProject project) {
		if (project instanceof CollectionProject collectionProject) {
			addItem(0, (CollectionProject) project);
			save();
			saveCollectionItemRecursive(project);
		} else
			throw new RuntimeException("project type not supported: " + project);
	}

	@Override
	protected void addItems(int index, List<AbstractWorkspaceItem> items) {
		super.addItems(index, items);
	}

	@Override
	public void save() {
		PersistUtil.save(this);
	}

	private void saveCollectionItemRecursive(IWorkspaceItem iworkspaceitem1) {
		iworkspaceitem1.save();
		iworkspaceitem1.getItems().forEach(this::saveCollectionItemRecursive);
	}

	@Override
	public void importProject(File file) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public File getDirectory() {
		return workspaceFolder;
	}

}
