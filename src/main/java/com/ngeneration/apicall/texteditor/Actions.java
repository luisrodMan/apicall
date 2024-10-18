package com.ngeneration.apicall.texteditor;

import com.ngeneration.furthergui.text.FTextComponent;

public class Actions {

	private final String LB = System.lineSeparator();

	private int[] getFullSelectedLines(FTextComponent editor) {
		int[] limits = new int[] { editor.getCaretPosition(), editor.getCaretPosition() };
		if (editor.getSelectionStart() != editor.getSelectionEnd()) {
			limits[0] = Math.min(editor.getSelectionStart(), editor.getSelectionEnd());
			limits[1] = Math.max(editor.getSelectionStart(), editor.getSelectionEnd());
		}
		limits[0] = getLineStart(editor, limits[0]);
		limits[1] = getLineEnd(editor, limits[1]);
		return limits;
	}

	private int getLineEnd(FTextComponent editor, int i) {
		// TODO Auto-generated method stub
		return 0;
	}

	private int getLineStart(FTextComponent editor, int i) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void moveSelectedLinesDown(FTextComponent editor) {
		try {
			int[] selection = getFullSelectedLines(editor);
			int end = selection[1] + 1;
			if (end < editor.getLength()) {
				// remove bottom line and reinsert on top xdxdxd

			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public void duplicateSelectedLines(FTextComponent editor) {

	}

}
