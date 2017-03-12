/*
 * Open Source Software published under the Apache Licence, Version 2.0.
 */

package io.github.vocabhunter.gui.controller;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.vocabhunter.analysis.grid.GridCell;
import io.github.vocabhunter.analysis.grid.GridLine;
import io.github.vocabhunter.analysis.grid.TextGrid;
import io.github.vocabhunter.analysis.grid.TextGridManager;
import io.github.vocabhunter.gui.common.ColumnNameTool;
import io.github.vocabhunter.gui.dialogues.FileDialogue;
import io.github.vocabhunter.gui.dialogues.FileDialogueFactory;
import io.github.vocabhunter.gui.dialogues.FileDialogueType;
import io.github.vocabhunter.gui.dialogues.FileFormatType;
import io.github.vocabhunter.gui.model.FilterFileMode;
import io.github.vocabhunter.gui.model.FilterFileModel;
import io.github.vocabhunter.gui.model.FilterGridModel;
import io.github.vocabhunter.gui.view.FilterGridWordTableCell;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;
import javax.inject.Inject;

import static java.util.stream.Collectors.toList;

@SuppressFBWarnings({"NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"})
public class FilterGridController extends AbstractFilterController<FilterGridModel> {
    private final TextGridManager textGridManager;

    public TableView<GridLine> tableWords;

    public VBox columnSelectionBox;

    private List<CheckBox> checkBoxes;

    @Inject
    public FilterGridController(final FileDialogueFactory factory, final TextGridManager textGridManager) {
        super(factory);
        this.textGridManager = textGridManager;
    }

    @Override
    protected FilterGridModel buildFilterModel(final FilterFileModel model) {
        Path file = model.getFile();
        FilterFileMode mode = model.getMode();
        TextGrid grid = readGrid(file, mode);

        return new FilterGridModel(file, grid, mode, model.getColumns());
    }

    @Override
    protected void exit(final Stage stage, final FilterGridModel filterModel, final Runnable onSave, final FilterFileModel parentModel, final boolean isSaveRequested) {
        if (isSaveRequested) {
            parentModel.setMode(filterModel.getMode());
            parentModel.setFile(filterModel.getFile());
            parentModel.setColumns(filterModel.getColumns());
            onSave.run();
        }
        stage.close();
    }

    @Override
    protected void initialiseInternal(final FilterFileModel parentModel, final FilterGridModel filterModel) {
        tableWords.setItems(filterModel.getLines());
        tableWords.setSelectionModel(null);
        setupColumnsAndCheckBoxes(filterModel);
    }

    private ObservableValue<GridCell> extractValue(final CellDataFeatures<GridLine, GridCell> features, final int index) {
        List<GridCell> cells = features.getValue().getCells();

        if (index < cells.size()) {
            return new ReadOnlyObjectWrapper<>(cells.get(index));
        } else {
            return new ReadOnlyObjectWrapper<>(GridCell.EMPTY_CELL);
        }
    }

    @Override
    protected void changeFile(final Stage stage, final FileDialogueFactory factory, final FilterGridModel filterModel) {
        FileDialogue dialogue = factory.create(FileDialogueType.OPEN_WORD_LIST, stage);

        dialogue.showChooser();

        if (dialogue.isFileSelected()) {
            Path file = dialogue.getSelectedFile();
            FileFormatType format = dialogue.getFileFormatType();
            FilterFileMode mode = FileFormatTypeTool.getMode(format);
            TextGrid grid = readGrid(file, mode);

            unbindCheckboxes(filterModel);
            filterModel.replaceContent(file, grid, mode, FilterGridModel.DEFAULT_COLUMNS);
            setupColumnsAndCheckBoxes(filterModel);
        }
    }

    private TextGrid readGrid(final Path file, final FilterFileMode mode) {
        if (mode == FilterFileMode.DOCUMENT) {
            return textGridManager.readDocument(file);
        } else {
            return textGridManager.readExcel(file);
        }
    }

    private void unbindCheckboxes(final FilterGridModel filterModel) {
        IntStream.range(0, checkBoxes.size())
            .forEach(i -> unbindCheckbox(filterModel, i));
    }

    private void unbindCheckbox(final FilterGridModel filterModel, final int column) {
        checkBoxes.get(column).selectedProperty().unbindBidirectional(getColumnSelection(filterModel, column));
    }

    private void setupColumnsAndCheckBoxes(final FilterGridModel filterModel) {
        tableWords.getColumns().setAll(buildColumns(filterModel));
        checkBoxes = buildAndBindCheckBoxes(filterModel);
        columnSelectionBox.getChildren().setAll(checkBoxes);
    }

    private List<CheckBox> buildAndBindCheckBoxes(final FilterGridModel filterModel) {
        return IntStream.range(0, filterModel.getColumnCount())
            .mapToObj(i -> buildAndBindCheckBox(filterModel, i))
            .collect(toList());
    }

    private CheckBox buildAndBindCheckBox(final FilterGridModel filterModel, final int columnNo) {
        String name = ColumnNameTool.columnName(columnNo);
        CheckBox box = new CheckBox(name);
        BooleanProperty property = getColumnSelection(filterModel, columnNo);

        box.selectedProperty().bindBidirectional(property);
        box.setId("checkBoxColumn" + columnNo);

        return box;
    }

    private List<TableColumn<GridLine, GridCell>> buildColumns(final FilterGridModel filterModel) {
        return IntStream.range(0, filterModel.getColumnCount())
            .mapToObj(this::buildColumn)
            .collect(toList());
    }

    private TableColumn<GridLine, GridCell> buildColumn(final int index) {
        TableColumn<GridLine, GridCell> column = new TableColumn<>(ColumnNameTool.columnName(index));

        column.setCellValueFactory(features -> extractValue(features, index));
        column.setCellFactory(c -> new FilterGridWordTableCell());

        return column;
    }

    private BooleanProperty getColumnSelection(final FilterGridModel filterModel, final int columnNo) {
        return filterModel.getColumnSelections().get(columnNo);
    }
}
