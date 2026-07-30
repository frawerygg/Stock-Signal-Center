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
 * JavaFX page for System 4 — Market Regime.
 *
 * Responsibilities:
 * - Explain how market regimes are classified.
 * - Display the current regime for each stock.
 * - Show how S4 affected Systems 1–3.
 * - Search, filter and sort rows.
 *
 * It does not calculate market regimes itself.
 */
public class System4View extends VBox {
    private final ObservableList<System4Row> sourceRows =
            FXCollections.observableArrayList();

    private final FilteredList<System4Row> filteredRows =
            new FilteredList<>(
                    sourceRows,
                    row -> true
            );

    private final TableView<System4Row> table =
            new TableView<>();

    private final TextField searchInput =
            new TextField();

    private final ComboBox<String> filterComboBox =
            new ComboBox<>();

    private final Label displayedCountLabel =
            new Label("0 displayed");

    private final Label warningCountLabel =
            new Label("Warnings: 0");

    private final Label filteredCountLabel =
            new Label("Filtering active: 0");


    public System4View() {
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
                        "How System 4 works"
                );

        title.setStyle(
                "-fx-text-fill: #effff5;"
                        + "-fx-font-size: 20;"
                        + "-fx-font-weight: bold;"
        );

        Label description =
                new Label(
                        "Classifies market conditions and decides whether "
                                + "signals from Systems 1–3 remain active or are muted."
                );

        description.setWrapText(true);

        description.setStyle(
                "-fx-text-fill: #8fb59f;"
                        + "-fx-font-size: 13;"
        );

        VBox classificationRules =
                createRuleBox(
                        "Market-regime classifications",
                        List.of(
                                "1. False Bullish Breakout: the previous close was above resistance, but the current close returned below it.",
                                "2. False Bearish Breakdown: the previous close was below support, but the current close recovered above it.",
                                "3. Volatility Compression: current Bollinger bandwidth is no more than 75% of its recent average.",
                                "4. Range Bound: moving averages are close, SMA20 is flat, RSI is neutral, bandwidth is below average, and price remains inside its recent range and Bollinger Bands.",
                                "5. Normal: none of the stronger regime conditions were found."
                        )
                );

        VBox resolverRules =
                createRuleBox(
                        "How the regime filters signals",
                        List.of(
                                "1. Normal: Systems 1–3 remain unchanged.",
                                "2. Range Bound: directional signals from S1 and S3 are muted. S2 may remain active.",
                                "3. Compression: directional signals from S1 and S3 are muted. S2 may remain active.",
                                "4. False Bullish Breakout: BUY signals from S1, S2 and S3 are muted.",
                                "5. False Bearish Breakdown: SHORT signals from S1, S2 and S3 are muted."
                        )
                );

        HBox ruleColumns =
                new HBox(
                        12,
                        classificationRules,
                        resolverRules
                );

        HBox.setHgrow(
                classificationRules,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                resolverRules,
                Priority.ALWAYS
        );

        Label thresholdTitle =
                new Label(
                        "Main thresholds"
                );

        thresholdTitle.setStyle(
                "-fx-text-fill: #8fb59f;"
                        + "-fx-font-size: 11;"
                        + "-fx-font-weight: bold;"
        );

        HBox thresholds =
                new HBox(
                        8,
                        createFormulaLabel(
                                "MA distance <= 2.0%"
                        ),
                        createFormulaLabel(
                                "|SMA20 slope| <= 1.0%"
                        ),
                        createFormulaLabel(
                                "Range RSI = 40 to 60"
                        ),
                        createFormulaLabel(
                                "Compression ratio <= 0.75"
                        )
                );

        for (var child
                : thresholds.getChildren()) {
            HBox.setHgrow(
                    child,
                    Priority.ALWAYS
            );
        }

        VBox panel =
                new VBox(
                        10,
                        title,
                        description,
                        ruleColumns,
                        thresholdTitle,
                        thresholds
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

        label.setAlignment(
                Pos.CENTER
        );

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
                "Search ticker, regime, signal effect or explanation..."
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
                "Detected Regimes",
                "Range Bound",
                "Compression",
                "False Breakouts",
                "Filtering Active",
                "Warning Regimes"
        );

        filterComboBox.setValue(
                "Detected Regimes"
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

        warningCountLabel.setStyle(
                "-fx-text-fill: #ff9d5c;"
                        + "-fx-font-size: 11;"
                        + "-fx-font-weight: bold;"
        );

        filteredCountLabel.setStyle(
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
                        warningCountLabel,
                        filteredCountLabel
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
                        "Current Market Regime"
                );

        title.setStyle(
                "-fx-text-fill: #effff5;"
                        + "-fx-font-size: 15;"
                        + "-fx-font-weight: bold;"
        );

        Label note =
                new Label(
                        "Horizontal scrolling shows all regime and resolver data"
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
                        "Regime",
                        System4Row::regimeDisplayName,
                        160
                ),

                createBadgeColumn(
                        "Overall",
                        System4Row::overallSignal,
                        110
                ),

                createNumberColumn(
                        "Price",
                        System4Row::price,
                        "$%,.2f",
                        90
                ),

                createNumberColumn(
                        "Previous Close",
                        System4Row::previousClose,
                        "$%,.2f",
                        110
                ),

                createNumberColumn(
                        "MA Distance",
                        System4Row::movingAverageDistancePercent,
                        "%.2f%%",
                        105
                ),

                createSignedPercentColumn(
                        "SMA20 Slope",
                        System4Row::sma20SlopePercent,
                        105
                ),

                createNumberColumn(
                        "RSI14",
                        System4Row::rsi14,
                        "%.2f",
                        75
                ),

                createNumberColumn(
                        "Bandwidth",
                        System4Row::bandwidth20,
                        "%.4f",
                        95
                ),

                createNumberColumn(
                        "Average BW",
                        System4Row::averageBandwidth20,
                        "%.4f",
                        95
                ),

                createNumberColumn(
                        "BW Ratio",
                        System4Row::bandwidthRatio,
                        "%.2fx",
                        90
                ),

                createNumberColumn(
                        "20D High",
                        System4Row::previousHighestHigh20,
                        "$%,.2f",
                        95
                ),

                createNumberColumn(
                        "20D Low",
                        System4Row::previousLowestLow20,
                        "$%,.2f",
                        95
                ),

                createNumberColumn(
                        "Bollinger Upper",
                        System4Row::bollingerUpper20,
                        "$%,.2f",
                        115
                ),

                createNumberColumn(
                        "Bollinger Lower",
                        System4Row::bollingerLower20,
                        "$%,.2f",
                        115
                ),

                createBadgeColumn(
                        "S1 Effect",
                        System4Row::system1Effect,
                        115
                ),

                createBadgeColumn(
                        "S2 Effect",
                        System4Row::system2Effect,
                        115
                ),

                createBadgeColumn(
                        "S3 Effect",
                        System4Row::system3Effect,
                        115
                ),

                createIntegerColumn(
                        "Bullish",
                        System4Row::bullishCount,
                        70
                ),

                createIntegerColumn(
                        "Bearish",
                        System4Row::bearishCount,
                        70
                ),

                createTextColumn(
                        "Regime Explanation",
                        System4Row::regimeExplanation,
                        360
                ),

                createTextColumn(
                        "Final Resolution",
                        System4Row::resolutionExplanation,
                        500
                )
        );

        SortedList<System4Row> sortedRows =
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


    private TableColumn<System4Row, String>
    createTickerColumn() {
        TableColumn<System4Row, String> column =
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


    private TableColumn<System4Row, LocalDate>
    createDateColumn() {
        TableColumn<System4Row, LocalDate> column =
                new TableColumn<>("Date");

        column.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(
                        data.getValue().date()
                )
        );

        column.setPrefWidth(105);

        return column;
    }


    private TableColumn<System4Row, String>
    createBadgeColumn(
            String title,
            TextExtractor extractor,
            double width
    ) {
        TableColumn<System4Row, String> column =
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


    private TableColumn<System4Row, Number>
    createNumberColumn(
            String title,
            NumberExtractor extractor,
            String format,
            double width
    ) {
        TableColumn<System4Row, Number> column =
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


    private TableColumn<System4Row, Number>
    createSignedPercentColumn(
            String title,
            NumberExtractor extractor,
            double width
    ) {
        TableColumn<System4Row, Number> column =
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

                        setText(
                                String.format(
                                        Locale.US,
                                        "%+.2f%%",
                                        number
                                )
                        );

                        if (number > 0.0) {
                            setStyle(
                                    "-fx-text-fill: #45d6a5;"
                                            + "-fx-font-weight: bold;"
                            );
                        } else if (number < 0.0) {
                            setStyle(
                                    "-fx-text-fill: #ff7181;"
                                            + "-fx-font-weight: bold;"
                            );
                        } else {
                            setStyle(
                                    "-fx-text-fill: #8fb59f;"
                            );
                        }
                    }
                }
        );

        column.setPrefWidth(width);

        return column;
    }


    private TableColumn<System4Row, Number>
    createIntegerColumn(
            String title,
            IntegerExtractor extractor,
            double width
    ) {
        TableColumn<System4Row, Number> column =
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


    private TableColumn<System4Row, String>
    createTextColumn(
            String title,
            TextExtractor extractor,
            double width
    ) {
        TableColumn<System4Row, String> column =
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


    private Label createEmptyLabel() {
        Label label =
                new Label(
                        "No abnormal market regimes detected."
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
            System4Row row,
            String searchText
    ) {
        String searchableText =
                row.ticker()
                        + " "
                        + row.regimeDisplayName()
                        + " "
                        + row.overallSignal()
                        + " "
                        + row.system1Effect()
                        + " "
                        + row.system2Effect()
                        + " "
                        + row.system3Effect()
                        + " "
                        + row.regimeExplanation()
                        + " "
                        + row.resolutionExplanation();

        return searchableText
                .toLowerCase(Locale.ROOT)
                .contains(searchText);
    }


    private boolean matchesFilter(
            System4Row row,
            String selectedFilter
    ) {
        if (selectedFilter == null) {
            return true;
        }

        return switch (selectedFilter) {

            case "Range Bound" ->
                    row.isRangeBound();

            case "Compression" ->
                    row.isCompression();

            case "False Breakouts" ->
                    row.isFalseBreakout();

            case "Filtering Active" ->
                    row.hasFilteringEffect();

            case "Warning Regimes" ->
                    row.isWarningRegime();

            default -> true;
        };
    }


    private void updateSummary() {
        long warningCount =
                sourceRows.stream()
                        .filter(
                                System4Row::isWarningRegime
                        )
                        .count();

        long filteredCount =
                sourceRows.stream()
                        .filter(
                                System4Row::hasFilteringEffect
                        )
                        .count();

        displayedCountLabel.setText(
                filteredRows.size()
                        + " displayed"
        );

        warningCountLabel.setText(
                "Warnings: "
                        + warningCount
        );

        filteredCountLabel.setText(
                "Filtering active: "
                        + filteredCount
        );
    }


    // ============================================================
    // Public input
    // ============================================================

    public void setRows(
            List<System4Row> rows
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
    // Badge colors
    // ============================================================

    private String getBadgeStyle(
            String value
    ) {
        String upper =
                value.toUpperCase(Locale.ROOT);

        String background;
        String textColor;

        if (upper.contains("MUTED")) {
            background =
                    "rgba(242,199,92,0.14)";

            textColor =
                    "#ffe5a3";
        } else if (upper.contains("STRONG BUY")
                || upper.equals("BUY")
                || upper.contains("WEAK BUY")) {
            background =
                    "rgba(69,214,165,0.14)";

            textColor =
                    "#aaf7db";
        } else if (upper.contains("STRONG SHORT")
                || upper.equals("SHORT")
                || upper.contains("WEAK SHORT")) {
            background =
                    "rgba(255,113,129,0.14)";

            textColor =
                    "#ffd0d6";
        } else if (upper.contains("FALSE")) {
            background =
                    "rgba(255,157,92,0.15)";

            textColor =
                    "#ffe0c5";
        } else if (upper.contains("COMPRESSION")) {
            background =
                    "rgba(242,199,92,0.14)";

            textColor =
                    "#ffe5a3";
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
                System4Row row
        );
    }


    @FunctionalInterface
    private interface IntegerExtractor {
        int getValue(
                System4Row row
        );
    }


    @FunctionalInterface
    private interface TextExtractor {
        String getValue(
                System4Row row
        );
    }
}