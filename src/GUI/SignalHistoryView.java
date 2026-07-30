package GUI;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/**
 * Displays directional trigger events and market-regime changes
 * from the most recent seven trading days.
 *
 * It does not evaluate signals itself.
 */
public class SignalHistoryView extends VBox {
    private final ObservableList<SignalHistoryRow> sourceRows =
            FXCollections.observableArrayList();

    private final FilteredList<SignalHistoryRow> filteredRows =
            new FilteredList<>(
                    sourceRows,
                    row -> true
            );

    private final TableView<SignalHistoryRow> table =
            new TableView<>();

    private final TextField searchInput =
            new TextField();

    private final ComboBox<String> filterComboBox =
            new ComboBox<>();

    private final Label displayedCountLabel =
            new Label("0 displayed");

    private final Label directionalCountLabel =
            new Label("Directional: 0");

    private final Label regimeCountLabel =
            new Label("Regime changes: 0");


    public SignalHistoryView() {
        setSpacing(14);
        setFillWidth(true);

        VBox explanationPanel =
                createExplanationPanel();

        HBox toolbar =
                createToolbar();

        VBox tablePanel =
                createTablePanel();

        VBox.setVgrow(
                tablePanel,
                Priority.ALWAYS
        );

        getChildren().addAll(
                explanationPanel,
                toolbar,
                tablePanel
        );

        configureFiltering();
    }


    // ============================================================
    // Explanation
    // ============================================================

    private VBox createExplanationPanel() {
        Label title =
                new Label(
                        "Past seven trading days"
                );

        title.setStyle(
                "-fx-text-fill: #effff5;"
                        + "-fx-font-size: 20;"
                        + "-fx-font-weight: bold;"
        );

        Label description =
                new Label(
                        "The signal engine reevaluates each stock at "
                                + "the previous seven historical end indices. "
                                + "Systems 1–3 appear only when they produced "
                                + "a raw BUY or SHORT trigger. System 4 appears "
                                + "when its market-regime classification changed."
                );

        description.setWrapText(true);

        description.setStyle(
                "-fx-text-fill: #8fb59f;"
                        + "-fx-font-size: 13;"
        );

        Label note =
                new Label(
                        "Muted events remain visible because the raw system "
                                + "triggered, even though System 4 prevented "
                                + "the signal from becoming active."
                );

        note.setWrapText(true);
        note.setPadding(new Insets(10));

        note.setStyle(
                "-fx-background-color: rgba(53,208,127,0.08);"
                        + "-fx-background-radius: 8;"
                        + "-fx-text-fill: #daf7e6;"
                        + "-fx-font-size: 12;"
        );

        VBox panel =
                new VBox(
                        9,
                        title,
                        description,
                        note
                );

        panel.setPadding(
                new Insets(18)
        );

        panel.setStyle(
                "-fx-background-color: linear-gradient("
                        + "to bottom, #173b2a, #10291d);"
                        + "-fx-background-radius: 13;"
                        + "-fx-border-color: #2d5a43;"
                        + "-fx-border-radius: 13;"
        );

        return panel;
    }


    // ============================================================
    // Toolbar
    // ============================================================

    private HBox createToolbar() {
        searchInput.setPromptText(
                "Search ticker, system, result, transition or reason..."
        );

        searchInput.setStyle(
                "-fx-background-color: #0a2117;"
                        + "-fx-background-radius: 8;"
                        + "-fx-border-color: #2d5a43;"
                        + "-fx-border-radius: 8;"
                        + "-fx-text-fill: #effff5;"
                        + "-fx-prompt-text-fill: #6f9981;"
        );

        HBox.setHgrow(
                searchInput,
                Priority.ALWAYS
        );

        filterComboBox.getItems().addAll(
                "All Events",
                "System 1",
                "System 2",
                "System 3",
                "System 4",
                "BUY Events",
                "SHORT Events",
                "Muted Events",
                "Regime Changes"
        );

        filterComboBox.setValue(
                "All Events"
        );

        filterComboBox.setPrefWidth(170);

        filterComboBox.setStyle(
                "-fx-background-color: #0a2117;"
                        + "-fx-background-radius: 8;"
                        + "-fx-border-color: #2d5a43;"
                        + "-fx-border-radius: 8;"
        );

        displayedCountLabel.setStyle(
                "-fx-text-fill: #8fb59f;"
                        + "-fx-font-size: 11;"
        );

        directionalCountLabel.setStyle(
                "-fx-text-fill: #45d6a5;"
                        + "-fx-font-size: 11;"
                        + "-fx-font-weight: bold;"
        );

        regimeCountLabel.setStyle(
                "-fx-text-fill: #f2c75c;"
                        + "-fx-font-size: 11;"
                        + "-fx-font-weight: bold;"
        );

        HBox toolbar =
                new HBox(
                        10,
                        searchInput,
                        filterComboBox,
                        displayedCountLabel,
                        directionalCountLabel,
                        regimeCountLabel
                );

        toolbar.setAlignment(
                Pos.CENTER_LEFT
        );

        toolbar.setPadding(
                new Insets(12)
        );

        toolbar.setStyle(
                "-fx-background-color: #10291d;"
                        + "-fx-background-radius: 13;"
                        + "-fx-border-color: #2d5a43;"
                        + "-fx-border-radius: 13;"
        );

        return toolbar;
    }


    // ============================================================
    // Table
    // ============================================================

    private VBox createTablePanel() {
        configureTable();

        Label title =
                new Label(
                        "Trigger and Regime Events"
                );

        title.setStyle(
                "-fx-text-fill: #effff5;"
                        + "-fx-font-size: 15;"
                        + "-fx-font-weight: bold;"
        );

        Label note =
                new Label(
                        "Newest event first"
                );

        note.setStyle(
                "-fx-text-fill: #8fb59f;"
                        + "-fx-font-size: 11;"
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        HBox header =
                new HBox(
                        10,
                        title,
                        spacer,
                        note
                );

        header.setAlignment(
                Pos.CENTER_LEFT
        );

        header.setPadding(
                new Insets(
                        14,
                        16,
                        14,
                        16
                )
        );

        header.setStyle(
                "-fx-border-color: #2d5a43;"
                        + "-fx-border-width: 0 0 1 0;"
        );

        VBox panel =
                new VBox(
                        header,
                        table
                );

        VBox.setVgrow(
                table,
                Priority.ALWAYS
        );

        panel.setStyle(
                "-fx-background-color: linear-gradient("
                        + "to bottom, #173b2a, #10291d);"
                        + "-fx-background-radius: 13;"
                        + "-fx-border-color: #2d5a43;"
                        + "-fx-border-radius: 13;"
        );

        return panel;
    }


    private void configureTable() {
        table.setPrefHeight(520);

        table.setPlaceholder(
                createEmptyLabel()
        );

        table.setStyle(
                "-fx-background-color: transparent;"
                        + "-fx-control-inner-background: #10291d;"
                        + "-fx-control-inner-background-alt: #123023;"
                        + "-fx-table-cell-border-color: #2d5a43;"
                        + "-fx-text-background-color: #effff5;"
        );

        TableColumn<SignalHistoryRow, LocalDate> dateColumn =
                createDateColumn();

        table.getColumns().addAll(
                dateColumn,
                createTickerColumn(),

                createTextColumn(
                        "System",
                        SignalHistoryRow::systemDisplayName,
                        210
                ),

                createBadgeColumn(
                        "Result",
                        SignalHistoryRow::resultStatus,
                        150
                ),

                createTextColumn(
                        "Raw / Transition",
                        SignalHistoryRow::rawOrTransition,
                        190
                ),

                createNumberColumn(
                        "Signal Price",
                        SignalHistoryRow::signalPrice,
                        "$%,.2f",
                        105
                ),

                createNumberColumn(
                        "Stop",
                        SignalHistoryRow::stopPrice,
                        "$%,.2f",
                        95
                ),

                createTextColumn(
                        "Reason",
                        SignalHistoryRow::reason,
                        650
                )
        );

        SortedList<SignalHistoryRow> sortedRows =
                new SortedList<>(
                        filteredRows
                );

        sortedRows.comparatorProperty().bind(
                table.comparatorProperty()
        );

        table.setItems(
                sortedRows
        );

        dateColumn.setSortType(
                TableColumn.SortType.DESCENDING
        );

        table.getSortOrder().add(
                dateColumn
        );
    }


    private TableColumn<SignalHistoryRow, LocalDate>
    createDateColumn() {
        TableColumn<SignalHistoryRow, LocalDate> column =
                new TableColumn<>("Date");

        column.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(
                        data.getValue().date()
                )
        );

        column.setPrefWidth(105);

        return column;
    }


    private TableColumn<SignalHistoryRow, String>
    createTickerColumn() {
        TableColumn<SignalHistoryRow, String> column =
                new TableColumn<>("Ticker");

        column.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().ticker()
                )
        );

        column.setCellFactory(tableColumn ->
                new TableCell<>() {
                    @Override
                    protected void updateItem(
                            String value,
                            boolean empty
                    ) {
                        super.updateItem(value, empty);

                        if (empty || value == null) {
                            setText(null);
                            setStyle("");
                            return;
                        }

                        setText(value);

                        setStyle(
                                "-fx-text-fill: #effff5;"
                                        + "-fx-font-weight: bold;"
                        );
                    }
                }
        );

        column.setPrefWidth(85);

        return column;
    }


    private TableColumn<SignalHistoryRow, String>
    createTextColumn(
            String title,
            TextExtractor extractor,
            double width
    ) {
        TableColumn<SignalHistoryRow, String> column =
                new TableColumn<>(title);

        column.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        extractor.getValue(
                                data.getValue()
                        )
                )
        );

        column.setPrefWidth(width);

        return column;
    }


    private TableColumn<SignalHistoryRow, String>
    createBadgeColumn(
            String title,
            TextExtractor extractor,
            double width
    ) {
        TableColumn<SignalHistoryRow, String> column =
                new TableColumn<>(title);

        column.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        extractor.getValue(
                                data.getValue()
                        )
                )
        );

        column.setCellFactory(tableColumn ->
                new TableCell<>() {
                    private final Label badge =
                            new Label();

                    {
                        badge.setPadding(
                                new Insets(
                                        4,
                                        8,
                                        4,
                                        8
                                )
                        );
                    }

                    @Override
                    protected void updateItem(
                            String value,
                            boolean empty
                    ) {
                        super.updateItem(value, empty);

                        if (empty || value == null) {
                            setText(null);
                            setGraphic(null);
                            return;
                        }

                        badge.setText(value);
                        badge.setStyle(
                                getBadgeStyle(value)
                        );

                        setText(null);
                        setGraphic(badge);
                    }
                }
        );

        column.setPrefWidth(width);

        return column;
    }


    private TableColumn<SignalHistoryRow, Number>
    createNumberColumn(
            String title,
            NumberExtractor extractor,
            String format,
            double width
    ) {
        TableColumn<SignalHistoryRow, Number> column =
                new TableColumn<>(title);

        column.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(
                        extractor.getValue(
                                data.getValue()
                        )
                )
        );

        column.setCellFactory(tableColumn ->
                new TableCell<>() {
                    @Override
                    protected void updateItem(
                            Number value,
                            boolean empty
                    ) {
                        super.updateItem(value, empty);

                        if (empty || value == null) {
                            setText(null);
                            setStyle("");
                            return;
                        }

                        double number =
                                value.doubleValue();

                        if (!Double.isFinite(number)) {
                            setText("—");

                            setStyle(
                                    "-fx-text-fill: #8fb59f;"
                            );

                            return;
                        }

                        setText(
                                String.format(
                                        Locale.US,
                                        format,
                                        number
                                )
                        );

                        setStyle(
                                "-fx-text-fill: #d8f4e2;"
                        );
                    }
                }
        );

        column.setPrefWidth(width);

        return column;
    }


    private Label createEmptyLabel() {
        Label label =
                new Label(
                        "No trigger or regime-change event occurred "
                                + "during the past seven trading days."
                );

        label.setStyle(
                "-fx-text-fill: #8fb59f;"
                        + "-fx-font-size: 13;"
        );

        return label;
    }


    // ============================================================
    // Filtering
    // ============================================================

    private void configureFiltering() {
        searchInput.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        updateFilter()
        );

        filterComboBox.valueProperty().addListener(
                (observable, oldValue, newValue) ->
                        updateFilter()
        );
    }


    private void updateFilter() {
        String searchText =
                searchInput.getText()
                        .trim()
                        .toLowerCase(Locale.ROOT);

        String selectedFilter =
                filterComboBox.getValue();

        filteredRows.setPredicate(row -> {
            boolean matchesSearch =
                    searchText.isEmpty()
                            || containsSearchText(
                            row,
                            searchText
                    );

            boolean matchesCategory =
                    matchesFilter(
                            row,
                            selectedFilter
                    );

            return matchesSearch
                    && matchesCategory;
        });

        updateSummary();
    }


    private boolean containsSearchText(
            SignalHistoryRow row,
            String searchText
    ) {
        String searchableText =
                row.ticker()
                        + " "
                        + row.systemDisplayName()
                        + " "
                        + row.resultStatus()
                        + " "
                        + row.rawOrTransition()
                        + " "
                        + row.reason();

        return searchableText
                .toLowerCase(Locale.ROOT)
                .contains(searchText);
    }


    private boolean matchesFilter(
            SignalHistoryRow row,
            String selectedFilter
    ) {
        if (selectedFilter == null) {
            return true;
        }

        return switch (selectedFilter) {
            case "System 1" ->
                    row.systemNumber() == 1;

            case "System 2" ->
                    row.systemNumber() == 2;

            case "System 3" ->
                    row.systemNumber() == 3;

            case "System 4",
                 "Regime Changes" ->
                    row.systemNumber() == 4;

            case "BUY Events" ->
                    row.isBuy();

            case "SHORT Events" ->
                    row.isShort();

            case "Muted Events" ->
                    row.isMuted();

            default -> true;
        };
    }


    private void updateSummary() {
        long directionalCount =
                sourceRows.stream()
                        .filter(
                                SignalHistoryRow::isDirectionalEvent
                        )
                        .count();

        long regimeCount =
                sourceRows.stream()
                        .filter(
                                SignalHistoryRow::isRegimeEvent
                        )
                        .count();

        displayedCountLabel.setText(
                filteredRows.size()
                        + " displayed"
        );

        directionalCountLabel.setText(
                "Directional: "
                        + directionalCount
        );

        regimeCountLabel.setText(
                "Regime changes: "
                        + regimeCount
        );
    }


    // ============================================================
    // Public input
    // ============================================================

    public void setRows(
            List<SignalHistoryRow> rows
    ) {
        if (rows == null) {
            sourceRows.clear();
        } else {
            sourceRows.setAll(rows);
        }

        updateFilter();
    }


    public void clearRows() {
        sourceRows.clear();
        updateSummary();
    }


    // ============================================================
    // Badge styling
    // ============================================================

    private String getBadgeStyle(
            String value
    ) {
        String upper =
                value.toUpperCase(Locale.ROOT);

        String background;
        String textColor;

        if (upper.equals("BUY")) {
            background =
                    "rgba(69,214,165,0.14)";

            textColor = "#aaf7db";
        } else if (upper.equals("SHORT")) {
            background =
                    "rgba(255,113,129,0.14)";

            textColor = "#ffd0d6";
        } else if (upper.equals("MUTED")
                || upper.contains("COMPRESSION")) {
            background =
                    "rgba(242,199,92,0.14)";

            textColor = "#ffe5a3";
        } else if (upper.contains("FALSE")) {
            background =
                    "rgba(255,157,92,0.15)";

            textColor = "#ffe0c5";
        } else if (upper.contains("RANGE")) {
            background =
                    "rgba(155,134,255,0.15)";

            textColor = "#d8cffd";
        } else if (upper.equals("NORMAL")) {
            background =
                    "rgba(53,208,127,0.14)";

            textColor = "#b8f5d0";
        } else {
            background =
                    "rgba(111,153,129,0.14)";

            textColor = "#c0d8c8";
        }

        return "-fx-background-color: "
                + background
                + ";"
                + "-fx-background-radius: 20;"
                + "-fx-text-fill: "
                + textColor
                + ";"
                + "-fx-font-size: 10;"
                + "-fx-font-weight: bold;";
    }


    @FunctionalInterface
    private interface NumberExtractor {
        double getValue(
                SignalHistoryRow row
        );
    }


    @FunctionalInterface
    private interface TextExtractor {
        String getValue(
                SignalHistoryRow row
        );
    }
}