package com.ngeneration.apicall.explorer.model.rich;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.ngeneration.apicall.Util;
import com.nxtr.easymng.workspace.AbstractWorkspaceItem;
import com.nxtr.easymng.workspace.IWorkspaceItem;

public class PersistUtil {

	public static void save(IWorkspaceItem node) {
		try {
			Util.writeText(new File(node.getWorkspace().getDirectory(), node.getId()),
					new Gson().toJson(getData(node)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static AbstractWorkspaceItem load(File file) {
		Gson gson = new Gson();
		AbstractWorkspaceItem item = null;
		try {
			var value = gson.fromJson(new FileReader(file), JsonObject.class);
			String type = value.get("type").getAsString();
			List<AbstractWorkspaceItem> items = new ArrayList<>();
			if (value.get("children") != null && !value.get("children").getAsString().isEmpty()) {
				items = Arrays.asList(value.get("children").getAsString().split(",")).stream()
						.map(s -> new File(file.getParent(), s)).filter(File::exists).map(PersistUtil::load).toList();
			}
			if (type.equals("request")) {
				RestCollectionRequest req = gson.fromJson(value.get("node"), RestCollectionRequest.class);
				item = req;
			} else if (type.equals("folder")) {
				RestCollectionFolder req = gson.fromJson(value.get("node"), RestCollectionFolder.class);
				item = req;
				req.addItems(0, items);
			} else if (type.equals("workspace")) {
				Workspace xd = gson.fromJson(value.get("node"), Workspace.class);
				Workspace xd2 = new Workspace(xd.getId(), xd.getName(), file.getParentFile());
				xd2.addItems(0, items);
				item = xd2;
			} else if (type.equals("project")) {
				CollectionProject req = gson.fromJson(value.get("node"), CollectionProject.class);
				item = req;
				req.addItems(0, items);
			} else
				throw new RuntimeException("unknown type: " + type);
		} catch (JsonSyntaxException | JsonIOException | FileNotFoundException e) {
			e.printStackTrace();
		}
		return item;
	}

	private static Object getData(IWorkspaceItem node) {
		Map<String, Object> data = new HashMap<>();
		data.put("type", getNodeType(node));
		data.put("node", node);
		data.put("children", node.getItemsCount() == 0 ? null
				: String.join(",", node.getItems().stream().map(IWorkspaceItem::getId).toList()));
		return data;
	}

	private static String getNodeType(IWorkspaceItem node) {
		if (node instanceof RestCollectionRequest) {
			return "request";
		} else if (node instanceof Workspace) {
			return "workspace";
		} else if (node instanceof CollectionProject) {
			return "project";
		} else if (node instanceof RestCollectionFolder) {
			return "folder";
		} else
			throw new RuntimeException("invalid node to save: " + node);
	}

}
