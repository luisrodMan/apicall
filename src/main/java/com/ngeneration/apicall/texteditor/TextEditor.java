package com.ngeneration.apicall.texteditor;

import java.awt.Font;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.ngeneration.furthergui.Cursor;
import com.ngeneration.furthergui.FComponent;
import com.ngeneration.furthergui.FPanel;
import com.ngeneration.furthergui.FScrollPane;
import com.ngeneration.furthergui.FTextPane;
import com.ngeneration.furthergui.event.AbstractAction;
import com.ngeneration.furthergui.event.Action;
import com.ngeneration.furthergui.event.ActionEvent;
import com.ngeneration.furthergui.event.DocumentEvent;
import com.ngeneration.furthergui.event.DocumentListener;
import com.ngeneration.furthergui.graphics.FFont;
import com.ngeneration.furthergui.layout.BorderLayout;
import com.ngeneration.furthergui.math.Padding;
import com.nxtr.easymng.UndoRedoManager;

public class TextEditor {

	public static final String JAVA_LANG = "java";
	public static final String JSON_LANG = "json";

	private final Font defaultFont = new Font("Consolas", Font.PLAIN, 13);
	private static FFont font;
	private FTextPane editor;
	private FPanel panel;

	private UndoRedoManager manager = new UndoRedoManager();
	private FScrollPane scroll;

	public TextEditor() {
		Action action2 = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent event) {
				new Actions().duplicateSelectedLines(TextEditor.this.editor);
			}
		};

		if (font == null)
			font = new FFont(defaultFont);

		editor = new FTextPane();
		editor.setFont(font);

		panel = new FPanel(new BorderLayout());
		panel.setPadding(new Padding());
//		Action action = new SelectWordAction

		scroll = new FScrollPane(editor);
//		LinesRuler numberRuler = new LinesRuler(editor);
//		numberRuler.setForeGroundColor
		scroll.setRowHeaderView(new FPanel()/* numberRuler */);
		panel.add(scroll);
		editor.addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent event) {
				// TODO Auto-generated method stub
//				numberRuler.repaint();
			}

			@Override
			public void insertUpdate(DocumentEvent event) {
				// TODO Auto-generated method stub
//				numberRuler.repaint();
			}

			@Override
			public void chanedUpdate(DocumentEvent event) {
				// TODO Auto-generated method stub
//				numberRuler.repaint();
			}
		});
	}

	public FComponent getView() {
		return panel;
	}

	private String language = JAVA_LANG;

	private static Map<String, String> keywords = Arrays.asList("try,catch".split(",")).stream()
			.collect(Collectors.toMap(v -> v, v -> v));
	private static Map<String, String> words1 = Arrays.asList("print,postman,,JSON".split(",")).stream()
			.collect(Collectors.toMap(v -> v, v -> v));
//	private static Patter javascriptPatter  = Pattern.comp

	public void append(String text) {
		editor.insertString(editor.getLength(), text, null);
	}

	public void steEditable(boolean b) {
	}

	public void setLanguage(String jsonLang) {
		language = jsonLang;
	}

	public void setInitialText(String testScript) {
		setText(testScript);
	}

	public void setText(String string) {
		editor.setText(string);
	}

	public String getText() {
		return editor.getText();
	}

	public void setEditable(boolean selected) {
	}

	public void setCursor(Cursor cursor) {

	}

}
