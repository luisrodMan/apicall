package com.ngeneration.apicall.explorer.ui;

import java.util.List;

import com.ngeneration.apicall.explorer.CollectionHeader;
import com.ngeneration.apicall.explorer.CollectionQuery;
import com.ngeneration.apicall.model.ApiCallEnvironment;
import com.ngeneration.furthergui.Cursor;
import com.ngeneration.furthergui.DefaultTableModel;
import com.ngeneration.furthergui.FComponent;
import com.ngeneration.furthergui.FRadioButton;
import com.ngeneration.furthergui.FTable;
import com.ngeneration.furthergui.FTextField;
import com.ngeneration.furthergui.event.MouseAdapter;
import com.ngeneration.furthergui.event.MouseEvent;
import com.ngeneration.furthergui.graphics.Color;
import com.ngeneration.furthergui.text.FTextComponent;

public class PropertiesTable extends FTable {

	public PropertiesTable(PropertiesDataModel model) {
		super(model);
		setRowHeight(30);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent event) {
				var model = viewToModel(event.getLocation());
				if (model != null) {
					if (model.getCol() == 0 && getModel().isEditable(model.getRow(), model.getCol())) {
						setValue(model.getRow(), model.getCol(),
								!((boolean) getValueAt(model.getRow(), model.getCol())));
					}
					setToEditMode(model.getRow(), model.getCol());
					event.consume();
				}
			}
		});
		setCellRenderer(new FTable.DefaultTableCellRenderer() {
			private FRadioButton button = new FRadioButton("");

			@Override
			public FComponent getRendererComponent(FTable table, Object value, boolean isSelected, boolean cellHasFocus,
					int row, int col) {
				if (col == 0 && row < table.getRowCount() - 1) {
					button.setSelected((boolean) value);
					button.setAlign(FRadioButton.CENTER_ALIGN);
					return button;
				} else {
					var c = super.getRendererComponent(table, value, isSelected, cellHasFocus, row, col);
					if (col > 0 && (row == table.getRowCount() - 1 || !(boolean) table.getValueAt(row, 0)))
						setForeground(Color.LIGTH_GRAY);
					setText(col == 0 ? "" : (value == null ? "" : value.toString()));
					return c;
				}
			}
		});
		setCellEditor(new FTable.DefaultCellEditor(this) {

			@Override
			public FComponent getEditorComponent(int row, int col, Object value, boolean selected) {
				if (col == 0) {
					FRadioButton radio = new FRadioButton("");
					radio.setAlign(FRadioButton.CENTER_ALIGN);
					radio.setSelected((boolean) value);
					return radio;
				} else {
					FTextField field = (FTextField) super.getEditorComponent(row, col, value, selected);
					// reset last column
					for (int i = 1; i < getTable().getColumsCount(); i++)
						getTable().setValue(getTable().getRowCount() - 1, i, "xd,Key,Value,Description".split(",")[i]);
					if (row == getTable().getRowCount() - 1) {
						// when text change
						field.setText("");
						field.addPropertyListener(e -> {
							if (!"added".equals(field.getName())
									&& e.getProperty().equals(FTextComponent.TEXT_PROPERTY)) {
								for (int i = 1; i < getTable().getColumsCount(); i++)
									if (i != col)
										getTable().setValue(row, i, "");
								getTable().getModel().addRow(new Object[] { true, "Key", "Value", "Description" });
								field.setName("added");
							}
						});

					} else if (value == null)
						field.setText("");
					return field;
				}
			}

			@Override
			public Object getValue(FComponent component, int row, int col) {
				if (component instanceof FRadioButton)
					return ((FRadioButton) component).isSelected();
				return super.getValue(component, row, col);
			}
		});

		addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent event) {
				var model = viewToModel(event.getLocation());
				setCursor(Cursor.getStandardCursor(
						model == null || model.getCol() < 1 ? Cursor.ARROW_CURSOR : Cursor.TEXT_CURSOR));
			}
		});
	}

	public static class PropertiesDataModel extends DefaultTableModel {

		private Object[] defaultRow;

		public static PropertiesDataModel buildForQueries(List<CollectionQuery> queries) {
			PropertiesDataModel model = new PropertiesDataModel();
			queries.forEach(row -> model
					.addRow(new Object[] { row.isEnabled(), row.getKey(), row.getValue(), row.getDescription() }));
			model.addRow(model.defaultRow = new Object[] { true, "Key", "Value", "Description" });
			return model;
		}

		public static PropertiesDataModel buildForHeaders(List<CollectionHeader> queries) {
			PropertiesDataModel model = new PropertiesDataModel();
			queries.forEach(row -> model
					.addRow(new Object[] { row.isEnabled(), row.getKey(), row.getValue(), row.getDescription() }));
			model.addRow(model.defaultRow = new Object[] { true, "Key", "Value", "Description" });
			return model;
		}

		private PropertiesDataModel() {
			super(new String[] { "", "Key", "Value", "Description" }, 0);
		}

		public boolean isEditable(int row, int col) {
			return row < getRowCount() - 1 || col > 0;
		}

		public static PropertiesDataModel buildForEnvironment(ApiCallEnvironment environment) {
			PropertiesDataModel model = new PropertiesDataModel();
			environment.getValues().forEach(
					row -> model.addRow(new Object[] { row.isEnabled(), row.getKey(), row.getValue(), row.getType() }));
			model.addRow(model.defaultRow = new Object[] { true, "Add variable", "Value", "Type" });
			return model;
		}

	}

}
