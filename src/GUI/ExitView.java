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
 * JavaFX page for protective stops and exit conditions.
 *
 * This page displays backend stop prices.
 *
 * It does not:
 * - Track actual broker positions.
 * - Place sell orders.
 * - Know whether the user owns the stock.
 * - Update prices continuously.
 */
public class ExitView extends VBox {
    private final ObservableList<ExitRow> sourceRows =
            FXCollections.observableArrayList();

    private final FilteredList<ExitRow> filteredRows =
            new FilteredList<>(
                    sourceRows,
                    row -> true
            );

    private final TableView<ExitRow> table =
            new TableView<>();

    private final TextField searchInput =
            new TextField();

    private final ComboBox<String> filterComboBox =
            new ComboBox<>();

    private final Label displayedCountLabel =
            new Label("0 displayed");

    private final Label activeStopCountLabel =
            new Label("Active stops: 0");

    private final Label triggeredCountLabel =
            new Label("Triggered: 0");


    public ExitView() {
        setSpacing(14);
        setFillWidth(true);

        VBox explanationPanel =
                createExplanationPanel();

        HBox toolbar =
                createToolbar();

        VBox tablePanel =
                createTablePanel();

        ResultPageLayout.installScrollablePage(
                this,
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
                        "How exit and stop points work"
                );

        title.setStyle(
                "-fx-text-fill: #effff5;"
                        + "-fx-font-size: 20;"
                        + "-fx-font-weight: bold;"
        );

        Label description =
                new Label(
                        "Combines protective stops from Systems 1–3 and "
                                + "compares each stop with the latest closing price."
                );

        description.setWrapText(true);

        description.setStyle(
                "-fx-text-fill: #8fb59f;"
                        + "-fx-font-size: 13;"
        );

        VBox longRules =
                createRuleBox(
                        "BUY / Long Position",
                        List.of(
                                "1. The protective stop is normally below the entry or current price.",
                                "2. The stop is triggered when current price is at or below the stop.",
                                "3. A lower stop gives the trade more room but increases potential loss.",
                                "4. Each system can calculate a different stop for the same stock."
                        )
                );

        VBox shortRules =
                createRuleBox(
                        "SHORT Position",
                        List.of(
                                "1. The protective stop is normally above the entry or current price.",
                                "2. The stop is triggered when current price is at or above the stop.",
                                "3. A higher stop gives the short trade more room but increases potential loss.",
                                "4. Each system can calculate a different stop for the same stock."
                        )
                );

        HBox ruleColumns =
                new HBox(
                        12,
                        longRules,
                        shortRules
                );

        HBox.setHgrow(
                longRules,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                shortRules,
                Priority.ALWAYS
        );

        VBox statusRules =
                createRuleBox(
                        "Status meanings",
                        List.of(
                                "HOLD: the latest closing price has not crossed the active stop.",
                                "EXIT TRIGGERED: the latest closing price has crossed the active stop.",
                                "INACTIVE RAW STOP: the original signal was muted by System 4, so its stop is shown for review only.",
                                "This page displays scanner calculations. It does not confirm that a real position exists."
                        )
                );

        VBox panel =
                new VBox(
                        10,
                        title,
                        description,
                        ruleColumns,
                        statusRules
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


    private VBox createRuleBox(
            String title,
            List<String> rules
    ) {
        Label titleLabel =
                new Label(title);

        titleLabel.setStyle(
                "-fx-text-fill: #d8f4e2;"
                        + "-fx-font-size: 13;"
                        + "-fx-font-weight: bold;"
        );

        VBox ruleContainer =
                new VBox(6);

        for (String rule : rules) {
            Label ruleLabel =
                    new Label(rule);

            ruleLabel.setWrapText(true);

            ruleLabel.setStyle(
                    "-fx-text-fill: #b8d8c4;"
                            + "-fx-font-size: 11;"
            );

            ruleContainer
                    .getChildren()
                    .add(ruleLabel);
        }

        VBox box =
                new VBox(
                        9,
                        titleLabel,
                        ruleContainer
                );

        box.setPadding(
                new Insets(12)
        );

        box.setMaxWidth(
                Double.MAX_VALUE
        );

        box.setStyle(
                "-fx-background-color: rgba(3,17,11,0.45);"
                        + "-fx-background-radius: 9;"
                        + "-fx-border-color: #2d5a43;"
                        + "-fx-border-radius: 9;"
        );

        return box;
    }


    // ============================================================
    // Toolbar
    // ============================================================

    private HBox createToolbar() {
        searchInput.setPromptText(
                "Search ticker, system, direction, status or reason..."
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
                "All Stops",
                "Active Stops",
                "Exit Triggered",
                "Hold",
                "Long Stops",
                "Short Stops",
                "Muted Raw Stops"
        );

        filterComboBox.setValue(
                "All Stops"
        );

        filterComboBox.setPrefWidth(175);

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

        activeStopCountLabel.setStyle(
                "-fx-text-fill: #45d6a5;"
                        + "-fx-font-size: 11;"
                        + "-fx-font-weight: bold;"
        );

        triggeredCountLabel.setStyle(
                "-fx-text-fill: #ff7181;"
                        + "-fx-font-size: 11;"
                        + "-fx-font-weight: bold;"
        );

        HBox toolbar =
                new HBox(
                        10,
                        searchInput,
                        filterComboBox,
                        displayedCountLabel,
                        activeStopCountLabel,
                        triggeredCountLabel
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
                        "Current Exit / Stop Points"
                );

        title.setStyle(
                "-fx-text-fill: #effff5;"
                        + "-fx-font-size: 15;"
                        + "-fx-font-weight: bold;"
        );

        Label note =
                new Label(
                        "One stock may have a different stop from each system"
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
        ResultPageLayout.applyStandardTableHeight(
                table
        );

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

        table.getColumns().addAll(
                createTickerColumn(),
                createDateColumn(),

                createTextColumn(
                        "System",
                        ExitRow::systemDisplayName,
                        210
                ),

                createBadgeColumn(
                        "Signal",
                        ExitRow::signalStatus,
                        100
                ),

                createBadgeColumn(
                        "Direction",
                        ExitRow::tradeDirection,
                        100
                ),

                createNumberColumn(
                        "Current Price",
                        ExitRow::currentPrice,
                        "$%,.2f",
                        110
                ),

                createNumberColumn(
                        "Stop / Exit",
                        ExitRow::stopPrice,
                        "$%,.2f",
                        105
                ),

                createNumberColumn(
                        "Distance",
                        ExitRow::distancePercent,
                        "%.2f%%",
                        95
                ),

                createBadgeColumn(
                        "Status",
                        ExitRow::exitStatus,
                        145
                ),

                createTextColumn(
                        "Signal Explanation",
                        ExitRow::reason,
                        650
                )
        );

        SortedList<ExitRow> sortedRows =
                new SortedList<>(
                        filteredRows
                );

        sortedRows.comparatorProperty().bind(
                table.comparatorProperty()
        );

        table.setItems(
                sortedRows
        );
    }


    private TableColumn<ExitRow, String>
    createTickerColumn() {
        TableColumn<ExitRow, String> column =
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

        column.setPrefWidth(80);

        return column;
    }


    private TableColumn<ExitRow, LocalDate>
    createDateColumn() {
        TableColumn<ExitRow, LocalDate> column =
                new TableColumn<>("Date");

        column.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(
                        data.getValue().date()
                )
        );

        column.setPrefWidth(105);

        return column;
    }


    private TableColumn<ExitRow, String>
    createTextColumn(
            String title,
            TextExtractor extractor,
            double width
    ) {
        TableColumn<ExitRow, String> column =
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


    private TableColumn<ExitRow, String>
    createBadgeColumn(
            String title,
            TextExtractor extractor,
            double width
    ) {
        TableColumn<ExitRow, String> column =
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


    private TableColumn<ExitRow, Number>
    createNumberColumn(
            String title,
            NumberExtractor extractor,
            String format,
            double width
    ) {
        TableColumn<ExitRow, Number> column =
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
                        "No active or muted protective stops are available."
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
            ExitRow row,
            String searchText
    ) {
        String searchableText =
                row.ticker()
                        + " "
                        + row.systemDisplayName()
                        + " "
                        + row.signalStatus()
                        + " "
                        + row.tradeDirection()
                        + " "
                        + row.exitStatus()
                        + " "
                        + row.reason();

        return searchableText
                .toLowerCase(Locale.ROOT)
                .contains(searchText);
    }


    private boolean matchesFilter(
            ExitRow row,
            String selectedFilter
    ) {
        if (selectedFilter == null) {
            return true;
        }

        return switch (selectedFilter) {
            case "Active Stops" ->
                    !row.muted();

            case "Exit Triggered" ->
                    row.isExitTriggered();

            case "Hold" ->
                    row.isHold();

            case "Long Stops" ->
                    row.isLongStop();

            case "Short Stops" ->
                    row.isShortStop();

            case "Muted Raw Stops" ->
                    row.isInactiveRawStop();

            default -> true;
        };
    }


    private void updateSummary() {
        long activeStopCount =
                sourceRows.stream()
                        .filter(row ->
                                !row.muted()
                        )
                        .count();

        long triggeredCount =
                sourceRows.stream()
                        .filter(
                                ExitRow::isExitTriggered
                        )
                        .count();

        displayedCountLabel.setText(
                filteredRows.size()
                        + " displayed"
        );

        activeStopCountLabel.setText(
                "Active stops: "
                        + activeStopCount
        );

        triggeredCountLabel.setText(
                "Triggered: "
                        + triggeredCount
        );
    }


    // ============================================================
    // Public input
    // ============================================================

    public void setRows(
            List<ExitRow> rows
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

        if (upper.equals("BUY")
                || upper.equals("HOLD")) {
            background =
                    "rgba(69,214,165,0.14)";

            textColor =
                    "#aaf7db";
        } else if (upper.equals("SHORT")
                || upper.contains("EXIT TRIGGERED")) {
            background =
                    "rgba(255,113,129,0.14)";

            textColor =
                    "#ffd0d6";
        } else if (upper.equals("MUTED")
                || upper.contains("INACTIVE")) {
            background =
                    "rgba(242,199,92,0.14)";

            textColor =
                    "#ffe5a3";
        } else {
            background =
                    "rgba(111,153,129,0.14)";

            textColor =
                    "#c0d8c8";
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
                ExitRow row
        );
    }


    @FunctionalInterface
    private interface TextExtractor {
        String getValue(
                ExitRow row
        );
    }
}