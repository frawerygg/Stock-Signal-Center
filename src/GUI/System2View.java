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
 * JavaFX page for System 2.
 *
 * Responsibilities:
 * - Explain the breakout rules.
 * - Display System2Row objects.
 * - Search, filter and sort the rows.
 *
 * It does not evaluate breakouts itself.
 */
public class System2View extends VBox {
    private final ObservableList<System2Row> sourceRows =
            FXCollections.observableArrayList();

    private final FilteredList<System2Row> filteredRows =
            new FilteredList<>(
                    sourceRows,
                    row -> true
            );

    private final TableView<System2Row> table =
            new TableView<>();

    private final TextField searchInput =
            new TextField();

    private final ComboBox<String> filterComboBox =
            new ComboBox<>();

    private final Label displayedCountLabel =
            new Label("0 displayed");

    private final Label activeCountLabel =
            new Label("Active: 0");

    private final Label mutedCountLabel =
            new Label("Muted: 0");


    public System2View() {
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
                        "How System 2 works"
                );

        title.setStyle(
                "-fx-text-fill: #effff5;"
                        + "-fx-font-size: 20;"
                        + "-fx-font-weight: bold;"
        );

        Label description =
                new Label(
                        "Detects 20-day breakouts or breakdowns confirmed "
                                + "by price, volume, SMA20, and RSI."
                );

        description.setWrapText(true);

        description.setStyle(
                "-fx-text-fill: #8fb59f;"
                        + "-fx-font-size: 13;"
        );

        VBox buyRules =
                createRuleBox(
                        "Bullish breakout — BUY",
                        List.of(
                                "1. Close is above the previous 20-day highest high.",
                                "2. Close is above SMA20.",
                                "3. Relative volume is at least 1.20.",
                                "4. RSI14 is below 80."
                        )
                );

        VBox shortRules =
                createRuleBox(
                        "Bearish breakdown — SHORT",
                        List.of(
                                "1. Close is below the previous 20-day lowest low.",
                                "2. Close is below SMA20.",
                                "3. Relative volume is at least 1.20.",
                                "4. RSI14 is above 20."
                        )
                );

        HBox ruleColumns =
                new HBox(
                        12,
                        buyRules,
                        shortRules
                );

        HBox.setHgrow(
                buyRules,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                shortRules,
                Priority.ALWAYS
        );

        Label formulaTitle =
                new Label(
                        "Protective-stop formulas"
                );

        formulaTitle.setStyle(
                "-fx-text-fill: #8fb59f;"
                        + "-fx-font-size: 11;"
                        + "-fx-font-weight: bold;"
        );

        Label longStop =
                createFormulaLabel(
                        "BUY stop = previous 20-day high − 1.0 × ATR14"
                );

        Label shortStop =
                createFormulaLabel(
                        "SHORT stop = previous 20-day low + 1.0 × ATR14"
                );

        VBox panel =
                new VBox(
                        10,
                        title,
                        description,
                        ruleColumns,
                        formulaTitle,
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
                "Search ticker, status or explanation..."
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
                "BUY Signals",
                "SHORT Signals",
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

        displayedCountLabel.setStyle(
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
                        displayedCountLabel,
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
    // Table panel
    // ============================================================

    private VBox createTablePanel() {
        configureTable();

        Label title =
                new Label(
                        "Current System 2 Results"
                );

        title.setStyle(
                "-fx-text-fill: #effff5;"
                        + "-fx-font-size: 15;"
                        + "-fx-font-weight: bold;"
        );

        Label note =
                new Label(
                        "Latest evaluated trading day"
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
                createStatusColumn(
                        "Result",
                        System2Row::status
                ),
                createStatusColumn(
                        "Raw Signal",
                        System2Row::rawDirection
                ),

                createNumberColumn(
                        "Price",
                        System2Row::price,
                        "$%,.2f",
                        95
                ),

                createNumberColumn(
                        "20D High",
                        System2Row::previousHighestHigh20,
                        "$%,.2f",
                        100
                ),

                createNumberColumn(
                        "20D Low",
                        System2Row::previousLowestLow20,
                        "$%,.2f",
                        100
                ),

                createNumberColumn(
                        "SMA20",
                        System2Row::sma20,
                        "$%,.2f",
                        95
                ),

                createNumberColumn(
                        "Relative Volume",
                        System2Row::relativeVolume20,
                        "%.2fx",
                        120
                ),

                createNumberColumn(
                        "RSI14",
                        System2Row::rsi14,
                        "%.2f",
                        80
                ),

                createNumberColumn(
                        "ATR14",
                        System2Row::atr14,
                        "%.2f",
                        80
                ),

                createNumberColumn(
                        "Stop",
                        System2Row::stopPrice,
                        "$%,.2f",
                        95
                ),

                createReasonColumn()
        );

        SortedList<System2Row> sortedRows =
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


    private TableColumn<System2Row, String>
    createTickerColumn() {
        TableColumn<System2Row, String> column =
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


    private TableColumn<System2Row, LocalDate>
    createDateColumn() {
        TableColumn<System2Row, LocalDate> column =
                new TableColumn<>("Date");

        column.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(
                        data.getValue().date()
                )
        );

        column.setPrefWidth(105);

        return column;
    }


    private TableColumn<System2Row, String>
    createStatusColumn(
            String title,
            StatusExtractor extractor
    ) {
        TableColumn<System2Row, String> column =
                new TableColumn<>(title);

        column.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        extractor.getValue(
                                data.getValue()
                        )
                )
        );

        column.setCellFactory(tableColumn ->
                createStatusCell()
        );

        column.setPrefWidth(100);

        return column;
    }


    private TableCell<System2Row, String>
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


    private TableColumn<System2Row, Number>
    createNumberColumn(
            String title,
            NumberExtractor extractor,
            String format,
            double width
    ) {
        TableColumn<System2Row, Number> column =
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


    private TableColumn<System2Row, String>
    createReasonColumn() {
        TableColumn<System2Row, String> column =
                new TableColumn<>("Explanation");

        column.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().reason()
                )
        );

        column.setPrefWidth(500);

        return column;
    }


    private Label createEmptyLabel() {
        Label label =
                new Label(
                        "No current System 2 signals."
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
            System2Row row,
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
            System2Row row,
            String selectedFilter
    ) {
        if (selectedFilter == null) {
            return true;
        }

        return switch (selectedFilter) {
            case "Active Signals" ->
                    row.hasActiveSignal();

            case "BUY Signals" ->
                    row.status().equals("BUY");

            case "SHORT Signals" ->
                    row.status().equals("SHORT");

            case "Muted Signals" ->
                    row.isMuted();


            default -> true;
        };
    }


    private void updateSummary() {
        long activeCount =
                sourceRows.stream()
                        .filter(
                                System2Row::hasActiveSignal
                        )
                        .count();

        long mutedCount =
                sourceRows.stream()
                        .filter(
                                System2Row::isMuted
                        )
                        .count();

        displayedCountLabel.setText(
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
    // Public input
    // ============================================================

    /**
     * Input:
     * List of System2Row objects.
     *
     * Output:
     * The System 2 table updates immediately.
     */
    public void setRows(
            List<System2Row> rows
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
    // Styling
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
                System2Row row
        );
    }


    @FunctionalInterface
    private interface StatusExtractor {
        String getValue(
                System2Row row
        );
    }
}