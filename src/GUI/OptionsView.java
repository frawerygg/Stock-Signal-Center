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
 * JavaFX page for the options-income evaluator.
 *
 * Responsibilities:
 * - Explain the current options-income logic.
 * - Display candidate and breakout-risk signals.
 * - Search, filter and sort rows.
 *
 * It does not select exact option contracts.
 */
public class OptionsView extends VBox {
    private final ObservableList<OptionsRow> sourceRows =
            FXCollections.observableArrayList();

    private final FilteredList<OptionsRow> filteredRows =
            new FilteredList<>(
                    sourceRows,
                    row -> true
            );

    private final TableView<OptionsRow> table =
            new TableView<>();

    private final TextField searchInput =
            new TextField();

    private final ComboBox<String> filterComboBox =
            new ComboBox<>();

    private final Label displayedCountLabel =
            new Label("0 displayed");

    private final Label candidateCountLabel =
            new Label("Candidates: 0");

    private final Label riskCountLabel =
            new Label("Breakout risks: 0");


    public OptionsView() {
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
                        "How the options-income evaluator works"
                );

        title.setStyle(
                "-fx-text-fill: #effff5;"
                        + "-fx-font-size: 20;"
                        + "-fx-font-weight: bold;"
        );

        Label description =
                new Label(
                        "Finds covered-call and cash-secured-put candidates "
                                + "in range-bound markets without active directional signals. "
                                + "It does not choose strikes or expirations."
                );

        description.setWrapText(true);

        description.setStyle(
                "-fx-text-fill: #8fb59f;"
                        + "-fx-font-size: 13;"
        );

        VBox coveredCallRules =
                createRuleBox(
                        "Covered Call Candidate",
                        List.of(
                                "1. System 4 classifies the stock as range-bound.",
                                "2. Systems 1–3 have no active directional signal.",
                                "3. Bollinger position is between 60% and 95%.",
                                "4. Current price is at or above SMA20.",
                                "5. RSI14 is between 50 and 65.",
                                "6. Relative volume is no more than 1.20."
                        )
                );

        VBox cashPutRules =
                createRuleBox(
                        "Cash-Secured Put Candidate",
                        List.of(
                                "1. System 4 classifies the stock as range-bound.",
                                "2. Systems 1–3 have no active directional signal.",
                                "3. Bollinger position is between 5% and 40%.",
                                "4. Current price is at or below SMA20.",
                                "5. RSI14 is between 35 and 50.",
                                "6. Relative volume is no more than 1.20."
                        )
                );

        HBox ruleColumns =
                new HBox(
                        12,
                        coveredCallRules,
                        cashPutRules
                );

        HBox.setHgrow(
                coveredCallRules,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                cashPutRules,
                Priority.ALWAYS
        );

        VBox riskRules =
                createRuleBox(
                        "Overrides and warnings",
                        List.of(
                                "Compression produces Breakout Risk because a larger directional move may be forming.",
                                "False breakouts produce No Option Signal.",
                                "A non-range-bound market produces No Option Signal.",
                                "An active BUY or SHORT signal overrides the options-income candidate."
                        )
                );

        Label formulaTitle =
                new Label(
                        "Bollinger position"
                );

        formulaTitle.setStyle(
                "-fx-text-fill: #8fb59f;"
                        + "-fx-font-size: 11;"
                        + "-fx-font-weight: bold;"
        );

        Label formula =
                createFormulaLabel(
                        "Position = (Close − Lower Band) "
                                + "÷ (Upper Band − Lower Band)"
                );

        VBox panel =
                new VBox(
                        10,
                        title,
                        description,
                        ruleColumns,
                        riskRules,
                        formulaTitle,
                        formula
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
                "Search ticker, strategy, regime or explanation..."
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
                "Trade Candidates",
                "Covered Calls",
                "Cash-Secured Puts",
                "Breakout Risk"
        );

        filterComboBox.setValue(
                "All Signals"
        );

        filterComboBox.setPrefWidth(185);

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

        candidateCountLabel.setStyle(
                "-fx-text-fill: #45d6a5;"
                        + "-fx-font-size: 11;"
                        + "-fx-font-weight: bold;"
        );

        riskCountLabel.setStyle(
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
                        candidateCountLabel,
                        riskCountLabel
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
                        "Current Options-Income Signals"
                );

        title.setStyle(
                "-fx-text-fill: #effff5;"
                        + "-fx-font-size: 15;"
                        + "-fx-font-weight: bold;"
        );

        Label note =
                new Label(
                        "Candidate classification only — no exact contracts"
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

                createBadgeColumn(
                        "Evaluation",
                        OptionsRow::strategyDisplayName,
                        190
                ),

                createBadgeColumn(
                        "Market Regime",
                        OptionsRow::regimeDisplayName,
                        155
                ),

                createNumberColumn(
                        "Price",
                        OptionsRow::price,
                        "$%,.2f",
                        90
                ),

                createNumberColumn(
                        "SMA20",
                        OptionsRow::sma20,
                        "$%,.2f",
                        90
                ),

                createNumberColumn(
                        "RSI14",
                        OptionsRow::rsi14,
                        "%.2f",
                        75
                ),

                createNumberColumn(
                        "Relative Volume",
                        OptionsRow::relativeVolume20,
                        "%.2fx",
                        120
                ),

                createNumberColumn(
                        "Lower Band",
                        OptionsRow::bollingerLower20,
                        "$%,.2f",
                        105
                ),

                createNumberColumn(
                        "Upper Band",
                        OptionsRow::bollingerUpper20,
                        "$%,.2f",
                        105
                ),

                createNumberColumn(
                        "Band Position",
                        OptionsRow::bollingerPositionPercent,
                        "%.1f%%",
                        105
                ),

                createIntegerColumn(
                        "Active Signals",
                        OptionsRow::activeDirectionalSignalCount,
                        100
                ),

                createExplanationColumn()
        );

        SortedList<OptionsRow> sortedRows =
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


    private TableColumn<OptionsRow, String>
    createTickerColumn() {
        TableColumn<OptionsRow, String> column =
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


    private TableColumn<OptionsRow, LocalDate>
    createDateColumn() {
        TableColumn<OptionsRow, LocalDate> column =
                new TableColumn<>("Date");

        column.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(
                        data.getValue().date()
                )
        );

        column.setPrefWidth(105);

        return column;
    }


    private TableColumn<OptionsRow, String>
    createBadgeColumn(
            String title,
            TextExtractor extractor,
            double width
    ) {
        TableColumn<OptionsRow, String> column =
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


    private TableColumn<OptionsRow, Number>
    createNumberColumn(
            String title,
            NumberExtractor extractor,
            String format,
            double width
    ) {
        TableColumn<OptionsRow, Number> column =
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


    private TableColumn<OptionsRow, Number>
    createIntegerColumn(
            String title,
            IntegerExtractor extractor,
            double width
    ) {
        TableColumn<OptionsRow, Number> column =
                new TableColumn<>(title);

        column.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(
                        extractor.getValue(
                                data.getValue()
                        )
                )
        );

        column.setPrefWidth(width);

        return column;
    }


    private TableColumn<OptionsRow, String>
    createExplanationColumn() {
        TableColumn<OptionsRow, String> column =
                new TableColumn<>("Explanation");

        column.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue()
                                .explanation()
                )
        );

        column.setPrefWidth(600);

        return column;
    }


    private Label createEmptyLabel() {
        Label label =
                new Label(
                        "No current options-income signals or breakout risks."
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
            OptionsRow row,
            String searchText
    ) {
        String searchableText =
                row.ticker()
                        + " "
                        + row.strategyDisplayName()
                        + " "
                        + row.regimeDisplayName()
                        + " "
                        + row.explanation();

        return searchableText
                .toLowerCase(Locale.ROOT)
                .contains(searchText);
    }


    private boolean matchesFilter(
            OptionsRow row,
            String selectedFilter
    ) {
        if (selectedFilter == null) {
            return true;
        }

        return switch (selectedFilter) {
            case "Trade Candidates" ->
                    row.isCandidate();

            case "Covered Calls" ->
                    row.isCoveredCallCandidate();

            case "Cash-Secured Puts" ->
                    row.isCashSecuredPutCandidate();

            case "Breakout Risk" ->
                    row.isBreakoutRisk();


            default -> true;
        };
    }


    private void updateSummary() {
        long candidateCount =
                sourceRows.stream()
                        .filter(
                                OptionsRow::isCandidate
                        )
                        .count();

        long riskCount =
                sourceRows.stream()
                        .filter(
                                OptionsRow::isBreakoutRisk
                        )
                        .count();

        displayedCountLabel.setText(
                filteredRows.size()
                        + " displayed"
        );

        candidateCountLabel.setText(
                "Candidates: "
                        + candidateCount
        );

        riskCountLabel.setText(
                "Breakout risks: "
                        + riskCount
        );
    }


    // ============================================================
    // Public input
    // ============================================================

    public void setRows(
            List<OptionsRow> rows
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

        if (upper.contains("COVERED CALL")
                || upper.contains("CASH-SECURED")) {
            background =
                    "rgba(69,214,165,0.14)";

            textColor =
                    "#aaf7db";
        } else if (upper.contains("BREAKOUT RISK")
                || upper.contains("COMPRESSION")) {
            background =
                    "rgba(242,199,92,0.14)";

            textColor =
                    "#ffe5a3";
        } else if (upper.contains("FALSE")) {
            background =
                    "rgba(255,157,92,0.15)";

            textColor =
                    "#ffe0c5";
        } else if (upper.contains("RANGE")) {
            background =
                    "rgba(155,134,255,0.15)";

            textColor =
                    "#d8cffd";
        } else if (upper.equals("NORMAL")) {
            background =
                    "rgba(53,208,127,0.14)";

            textColor =
                    "#b8f5d0";
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
                OptionsRow row
        );
    }


    @FunctionalInterface
    private interface IntegerExtractor {
        int getValue(
                OptionsRow row
        );
    }


    @FunctionalInterface
    private interface TextExtractor {
        String getValue(
                OptionsRow row
        );
    }
}