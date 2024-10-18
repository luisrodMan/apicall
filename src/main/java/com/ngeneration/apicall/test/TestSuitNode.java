package com.ngeneration.apicall.test;

import java.io.File;
import java.io.IOException;

import com.google.gson.Gson;
import com.ngeneration.apicall.ApiCallApplication;
import com.ngeneration.apicall.Util;
import com.ngeneration.apicall.explorer.AbstractExplorerNode;
import com.nxtr.easymng.Application;
import com.nxtr.easymng.ExecutableView;
import com.nxtr.easymng.View;

public class TestSuitNode extends AbstractExplorerNode implements ExecutableView {

	private TestSuit suit;

	public TestSuitNode(TestSuit suit) {
		this(null, suit);
	}

	public TestSuitNode(String id, TestSuit values) {
		super(id);
		this.suit = values;
	}

	@Override
	public String getName() {
		return suit.getName();
	}

	@Override
	public View getView() {
		var view = Application.getInstance().getViewById(getId());
		return view != null ? view : new TestView(this);
	}

	public void save() throws IOException {
		if (getParent() == null)
			throw new RuntimeException("Not added to workspace");
//		File file = ApiCallApplication.getInstance().getWorkspace().getNodeFile(this);
//		Util.writeText(file, new Gson().toJson(suit));
	}

	public TestSuit getModel() {
		return suit;
	}

	@Override
	public void execute() {

	}

}
