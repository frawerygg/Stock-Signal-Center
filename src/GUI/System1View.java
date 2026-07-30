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
 * Frontend page for System 1.
 *
 * This class:
 * - Explains the System 1 rules.
 * - Displays System1Row objects.
 * - Provides search, filtering and sorting.
 *
 * It does not evaluate System 1 itself.
 */
public class System1View extends VBox {
    private final ObservableList<System1Row> sourceRows =
            FXCollections.observableArrayList();

    private final FilteredList<System1Row> filteredRows =
            new FilteredList<>(
                    sourceRows,
                    row -> true
            );

    private final TableView<System1Row> table =
            new TableView<>();

    private final TextField searchInput =
            new TextField();

    private final ComboBox<String> filterComboBox =
            new ComboBox<>();

    private final Label resultCountLabel =
            new Label("0 stocks");

    private final Label activeCountLabel =
            new Label("Active: 0");

    private final Label mutedCountLabel =
            new Label("Muted: 0");

    public System1View() {
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
    // Explanation panel
    // ============================================================

    private VBox createExplanationPanel() {
        Label title =
                new Label(
                        "How System 1 works"
                );

        title.setStyle(
                "-fx-text-fill: #effff5;"
                        + "-fx-font-size: 20;"
                        + "-fx-font-weight: bold;"
        );

        Label description =
                new Label(
                        "Detects fresh SMA20/SMA50 crossovers confirmed "
                                + "by price direction, SMA20 slope, and RSI."
                );

        description.setWrapText(true);

        description.setStyle(
                "-fx-text-fill: #8fb59f;"
                        + "-fx-font-size: 13;"
        );

        VBox bullishRules =
                createRuleBox(
                        "Bullish BUY conditions",
                        List.of(
                                "1. Yesterday SMA20 was at or below SMA50.",
                                "2. Today SMA20 is above SMA50.",
                                "3. Close is above SMA20.",
                                "4. SMA20 is above its value five trading days ago.",
                                "5. Close is above the previous close.",
                                "6. RSI14 is between 50 and 70."
                        )
                );

        VBox bearishRules =
                createRuleBox(
                        "Bearish SHORT conditions",
                        List.of(
                                "1. Yesterday SMA20 was at or above SMA50.",
                                "2. Today SMA20 is below SMA50.",
                                "3. Close is below SMA20.",
                                "4. SMA20 is below its value five trading days ago.",
                                "5. Close is below the previous close.",
                                "6. RSI14 is between 30 and 50."
                        )
                );

        HBox ruleColumns =
                new HBox(
                        12,
                        bullishRules,
                        bearishRules
                );

        HBox.setHgrow(
                bullishRules,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                bearishRules,
                Priority.ALWAYS
        );

        Label stopTitle =
                new Label(
                        "Protective-stop formulas"
                );

        stopTitle.setStyle(
                "-fx-text-fill: #8fb59f;"
                        + "-fx-font-size: 11;"
                        + "-fx-font-weight: bold;"
        );

        Label longStop =
                createFormulaLabel(
                        "BUY stop = MIN(SMA50, previous 7-day low) "
                                + "− 0.5 × ATR14"
                );

        Label shortStop =
                createFormulaLabel(
                        "SHORT stop = MAX(SMA50, previous 7-day high) "
                                + "+ 0.5 × ATR14"
                );

        VBox panel =
                new VBox(
                        10,
                        title,
                        description,
                        ruleColumns,
                        stopTitle,
                        longStop,
                        shortStop
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

            ruleContainer.getChildren().add(
                    ruleLabel
            );
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

    private Label createFormulaLabel(
            String text
    ) {
        Label label =
                new Label(text);

        label.setMaxWidth(
                Double.MAX_VALUE
        );

        label.setWrapText(true);

        label.setPadding(
                new Insets(9)
        );

        label.setStyle(
                "-fx-background-color: rgba(53,208,127,0.08);"
                        + "-fx-background-radius: 8;"
                        + "-fx-text-fill: #daf7e6;"
                        + "-fx-font-family: Consolas;"
                        + "-fx-font-size: 11;"
        );

        return label;
    }

    // ============================================================
    // Toolbar
    // ============================================================

    private HBox createToolbar() {
        searchInput.setPromptText(
                "Search ticker, status or reason..."
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
                "All Signals",
                "Active Signals",
                "Muted Signals"
        );

        filterComboBox.setValue(
                "All Signals"
        );

        filterComboBox.setPrefWidth(170);

        filterComboBox.setStyle(
                "-fx-background-color: #0a2117;"
                        + "-fx-background-radius: 8;"
                        + "-fx-border-color: #2d5a43;"
                        + "-fx-border-radius: 8;"
        );

        resultCountLabel.setStyle(
                "-fx-text-fill: #8fb59f;"
                        + "-fx-font-size: 11;"
        );

        activeCountLabel.setStyle(
                "-fx-text-fill: #45d6a5;"
                        + "-fx-font-size: 11;"
                        + "-fx-font-weight: bold;"
        );

        mutedCountLabel.setStyle(
                "-fx-text-fill: #f2c75c;"
                        + "-fx-font-size: 11;"
                        + "-fx-font-weight: bold;"
        );

        HBox toolbar =
                new HBox(
                        10,
                        searchInput,
                        filterComboBox,
                        resultCountLabel,
                        activeCountLabel,
                        mutedCountLabel
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
    // Results table
    // ============================================================

    private VBox createTablePanel() {
        configureTable();

        Label title =
                new Label(
                        "Current System 1 Results"
                );

        title.setStyle(
                "-fx-text-fill: #effff5;"
                        + "-fx-font-size: 15;"
                        + "-fx-font-weight: bold;"
        );

        Label note =
                new Label(
                        "The table shows the latest evaluated trading day"
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
                createStatusColumn(),
                createRawDirectionColumn(),

                createNumberColumn(
                        "Price",
                        System1Row::price,
                        "$%,.2f",
                        90
                ),

                createNumberColumn(
                        "Previous Close",
                        System1Row::previousClose,
                        "$%,.2f",
                        115
                ),

                createNumberColumn(
                        "Previous SMA20",
                        System1Row::previousSma20,
                        "$%,.2f",
                        120
                ),

                createNumberColumn(
                        "Previous SMA50",
                        System1Row::previousSma50,
                        "$%,.2f",
                        120
                ),

                createNumberColumn(
                        "SMA20",
                        System1Row::sma20,
                        "$%,.2f",
                        90
                ),

                createNumberColumn(
                        "SMA50",
                        System1Row::sma50,
                        "$%,.2f",
                        90
                ),

                createNumberColumn(
                        "SMA20 5D Ago",
                        System1Row::sma20FiveDaysAgo,
                        "$%,.2f",
                        115
                ),

                createNumberColumn(
                        "RSI14",
                        System1Row::rsi14,
                        "%.2f",
                        75
                ),

                createNumberColumn(
                        "ATR14",
                        System1Row::atr14,
                        "%.2f",
                        75
                ),

                createNumberColumn(
                        "Stop",
                        System1Row::stopPrice,
                        "$%,.2f",
                        90
                ),

                createReasonColumn()
        );

        SortedList<System1Row> sortedRows =
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

    private TableColumn<System1Row, String>
    createTickerColumn() {
        TableColumn<System1Row, String> column =
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

    private TableColumn<System1Row, LocalDate>
    createDateColumn() {
        TableColumn<System1Row, LocalDate> column =
                new TableColumn<>("Date");

        column.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(
                        data.getValue().date()
                )
        );

        column.setPrefWidth(105);

        return column;
    }

    private TableColumn<System1Row, String>
    createStatusColumn() {
        TableColumn<System1Row, String> column =
                new TableColumn<>("Result");

        column.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().status()
                )
        );

        column.setCellFactory(tableColumn ->
                createStatusCell()
        );

        column.setPrefWidth(95);

        return column;
    }

    private TableColumn<System1Row, String>
    createRawDirectionColumn() {
        TableColumn<System1Row, String> column =
                new TableColumn<>("Raw Signal");

        column.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().rawDirection()
                )
        );

        column.setCellFactory(tableColumn ->
                createStatusCell()
        );

        column.setPrefWidth(100);

        return column;
    }

    private TableCell<System1Row, String>
    createStatusCell() {
        return new TableCell<>() {
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
        };
    }

    private TableColumn<System1Row, Number>
    createNumberColumn(
            String title,
            NumberExtractor extractor,
            String format,
            double width
    ) {
        TableColumn<System1Row, Number> column =
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

    private TableColumn<System1Row, String>
    createReasonColumn() {
        TableColumn<System1Row, String> column =
                new TableColumn<>("Explanation");

        column.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().reason()
                )
        );

        column.setPrefWidth(470);

        return column;
    }

    private Label createEmptyLabel() {
        Label label =
                new Label(
                        "No current System 1 signals."
                );

        label.setStyle(
                "-fx-text-fill: #8fb59f;"
                        + "-fx-font-size: 13;"
        );

        return label;
    }

    // ============================================================
    // Search and filtering
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
            System1Row row,
            String searchText
    ) {
        String searchableText =
                row.ticker()
                        + " "
                        + row.status()
                        + " "
                        + row.rawDirection()
                        + " "
                        + row.reason();

        return searchableText
                .toLowerCase(Locale.ROOT)
                .contains(searchText);
    }

    private boolean matchesFilter(
            System1Row row,
            String selectedFilter
    ) {
        if (selectedFilter == null) {
            return true;
        }

        return switch (selectedFilter) {
            case "Active Signals" ->
                    row.hasActiveSignal();

            case "Muted Signals" ->
                    row.isMuted();


            default -> true;
        };
    }

    private void updateSummary() {
        long activeCount =
                sourceRows.stream()
                        .filter(
                                System1Row::hasActiveSignal
                        )
                        .count();

        long mutedCount =
                sourceRows.stream()
                        .filter(
                                System1Row::isMuted
                        )
                        .count();

        resultCountLabel.setText(
                filteredRows.size()
                        + " displayed"
        );

        activeCountLabel.setText(
                "Active: "
                        + activeCount
        );

        mutedCountLabel.setText(
                "Muted: "
                        + mutedCount
        );
    }

    // ============================================================
    // Public input methods
    // ============================================================

    /**
     * Input:
     * GUI-ready System1Row objects.
     *
     * Output:
     * The table displays the rows immediately.
     */
    public void setRows(
            List<System1Row> rows
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

        switch (upper) {
            case "BUY" -> {
                background =
                        "rgba(69,214,165,0.14)";

                textColor =
                        "#aaf7db";
            }

            case "SHORT" -> {
                background =
                        "rgba(255,113,129,0.14)";

                textColor =
                        "#ffd0d6";
            }

            case "MUTED" -> {
                background =
                        "rgba(242,199,92,0.14)";

                textColor =
                        "#ffe5a3";
            }

            default -> {
                background =
                        "rgba(111,153,129,0.14)";

                textColor =
                        "#c0d8c8";
            }
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
                System1Row row
        );
    }
}