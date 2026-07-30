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
 * JavaFX page for System 3 — Extreme Pullback Recovery.
 *
 * Responsibilities:
 * - Explain the current System 3 rules.
 * - Display System3Row objects.
 * - Search, filter and sort results.
 *
 * It does not calculate metrics or evaluate signals.
 */
public class System3View extends VBox {
    private final ObservableList<System3Row> sourceRows =
            FXCollections.observableArrayList();

    private final FilteredList<System3Row> filteredRows =
            new FilteredList<>(
                    sourceRows,
                    row -> true
            );

    private final TableView<System3Row> table =
            new TableView<>();

    private final TextField searchInput =
            new TextField();

    private final ComboBox<String> filterComboBox =
            new ComboBox<>();

    private final Label displayedCountLabel =
            new Label("0 displayed");

    private final Label activeCountLabel =
            new Label("Active: 0");

    private final Label severeCountLabel =
            new Label("Severe pullbacks: 0");


    public System3View() {
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
                        "How System 3 works"
                );

        title.setStyle(
                "-fx-text-fill: #effff5;"
                        + "-fx-font-size: 20;"
                        + "-fx-font-weight: bold;"
        );

        Label description =
                new Label(
                        "Finds stocks recovering after an extreme 21-day "
                                + "decline. The current system produces BUY signals only."
                );

        description.setWrapText(true);

        description.setStyle(
                "-fx-text-fill: #8fb59f;"
                        + "-fx-font-size: 13;"
        );

        VBox drawdownRules =
                createRuleBox(
                        "Extreme decline requirements",
                        List.of(
                                "1. The 21-day peak-to-trough close drawdown is at least 20%.",
                                "2. The same drawdown is at least 4.0 times the ATR measured at the peak.",
                                "3. These two tests confirm that the decline was genuinely severe."
                        )
                );

        VBox recoveryRules =
                createRuleBox(
                        "Recovery confirmation",
                        List.of(
                                "1. Price has recovered at least 1.0 current ATR from the trough close.",
                                "2. Previous RSI14 was at or below 35.",
                                "3. Current RSI14 has crossed above 35.",
                                "4. Today's price change is positive.",
                                "5. Today's price change is stronger than the previous day's change."
                        )
                );

        HBox ruleColumns =
                new HBox(
                        12,
                        drawdownRules,
                        recoveryRules
                );

        HBox.setHgrow(
                drawdownRules,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                recoveryRules,
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

        Label buyStop =
                createFormulaLabel(
                        "BUY Stop = "
                                + "21-day Trough Low − 0.5 × ATR14"
                );

        VBox panel =
                new VBox(
                        10,
                        title,
                        description,
                        ruleColumns,
                        stopTitle,
                        buyStop
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
                "Active BUY Signals",
                "Muted BUY Signals"
        );

        filterComboBox.setValue(
                "All Signals"
        );

        filterComboBox.setPrefWidth(180);

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

        severeCountLabel.setStyle(
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
                        severeCountLabel
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
                        "Current System 3 Results"
                );

        title.setStyle(
                "-fx-text-fill: #effff5;"
                        + "-fx-font-size: 15;"
                        + "-fx-font-weight: bold;"
        );

        Label note =
                new Label(
                        "Horizontal scrolling shows all recovery metrics"
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
                        System3Row::status
                ),

                createStatusColumn(
                        "Raw Signal",
                        System3Row::rawDirection
                ),

                createNumberColumn(
                        "Price",
                        System3Row::price,
                        "$%,.2f",
                        90
                ),

                createNumberColumn(
                        "Previous Close",
                        System3Row::previousClose,
                        "$%,.2f",
                        110
                ),

                createNumberColumn(
                        "21D Peak Close",
                        System3Row::peakClose21,
                        "$%,.2f",
                        110
                ),

                createNumberColumn(
                        "Peak ATR",
                        System3Row::peakAtr14,
                        "%.2f",
                        85
                ),

                createNumberColumn(
                        "21D Trough Close",
                        System3Row::troughClose21,
                        "$%,.2f",
                        120
                ),

                createNumberColumn(
                        "Trough Low",
                        System3Row::troughLow21,
                        "$%,.2f",
                        100
                ),

                createNumberColumn(
                        "Drawdown %",
                        System3Row::drawdownPercent21,
                        "%.2f%%",
                        100
                ),

                createNumberColumn(
                        "Drawdown ATR",
                        System3Row::drawdownAtrMultiple21,
                        "%.2fx",
                        105
                ),

                createNumberColumn(
                        "Recovery ATR",
                        System3Row::recoveryAtrMultiple21,
                        "%.2fx",
                        105
                ),

                createNumberColumn(
                        "Previous RSI",
                        System3Row::previousRsi14,
                        "%.2f",
                        100
                ),

                createNumberColumn(
                        "RSI14",
                        System3Row::rsi14,
                        "%.2f",
                        75
                ),

                createSignedPercentColumn(
                        "Previous Change",
                        System3Row::previousPriceChangePercent,
                        115
                ),

                createSignedPercentColumn(
                        "Current Change",
                        System3Row::priceChangePercent,
                        110
                ),

                createSignedPercentColumn(
                        "Acceleration",
                        System3Row::accelerationPercent,
                        100
                ),

                createNumberColumn(
                        "ATR14",
                        System3Row::atr14,
                        "%.2f",
                        75
                ),

                createNumberColumn(
                        "Stop",
                        System3Row::stopPrice,
                        "$%,.2f",
                        90
                ),

                createReasonColumn()
        );

        SortedList<System3Row> sortedRows =
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


    private TableColumn<System3Row, String>
    createTickerColumn() {
        TableColumn<System3Row, String> column =
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


    private TableColumn<System3Row, LocalDate>
    createDateColumn() {
        TableColumn<System3Row, LocalDate> column =
                new TableColumn<>("Date");

        column.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(
                        data.getValue().date()
                )
        );

        column.setPrefWidth(105);

        return column;
    }


    private TableColumn<System3Row, String>
    createStatusColumn(
            String title,
            StatusExtractor extractor
    ) {
        TableColumn<System3Row, String> column =
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


    private TableCell<System3Row, String>
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


    private TableColumn<System3Row, Number>
    createNumberColumn(
            String title,
            NumberExtractor extractor,
            String format,
            double width
    ) {
        TableColumn<System3Row, Number> column =
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


    private TableColumn<System3Row, Number>
    createSignedPercentColumn(
            String title,
            NumberExtractor extractor,
            double width
    ) {
        TableColumn<System3Row, Number> column =
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


    private TableColumn<System3Row, String>
    createReasonColumn() {
        TableColumn<System3Row, String> column =
                new TableColumn<>("Explanation");

        column.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().reason()
                )
        );

        column.setPrefWidth(600);

        return column;
    }


    private Label createEmptyLabel() {
        Label label =
                new Label(
                        "No current System 3 signals."
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
            System3Row row,
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
            System3Row row,
            String selectedFilter
    ) {
        if (selectedFilter == null) {
            return true;
        }

        return switch (selectedFilter) {
            case "Active BUY Signals" ->
                    row.hasActiveSignal();

            case "Muted BUY Signals" ->
                    row.isMuted();


            default -> true;
        };
    }


    private void updateSummary() {
        long activeCount =
                sourceRows.stream()
                        .filter(
                                System3Row::hasActiveSignal
                        )
                        .count();

        long severeCount =
                sourceRows.stream()
                        .filter(
                                System3Row::hasSeverePullback
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

        severeCountLabel.setText(
                "Severe pullbacks: "
                        + severeCount
        );
    }


    // ============================================================
    // Public input
    // ============================================================

    /**
     * Input:
     * List of GUI-ready System3Row objects.
     *
     * Output:
     * The System 3 table updates immediately.
     */
    public void setRows(
            List<System3Row> rows
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
                System3Row row
        );
    }


    @FunctionalInterface
    private interface StatusExtractor {
        String getValue(
                System3Row row
        );
    }
}