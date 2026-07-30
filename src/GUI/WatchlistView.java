package GUI;

import GUI.WatchlistRow;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Watchlist Manager page.
 *
 * This class is responsible only for displaying and filtering
 * WatchlistRow objects.
 *
 * It does not download prices, calculate indicators or evaluate signals.
 */
public class WatchlistView extends VBox
{
    private final ObservableList<WatchlistRow> sourceRows =
            FXCollections.observableArrayList();

    private final FilteredList<WatchlistRow> filteredRows =
            new FilteredList<>(
                    sourceRows,
                    row -> true
            );

    private final TableView<WatchlistRow> table =
            new TableView<>();

    private final TextField tickerInput =
            new TextField();

    private final TextField searchInput =
            new TextField();

    private final ComboBox<String> filterComboBox =
            new ComboBox<>();

    private final Button addButton =
            new Button("Add Stock");

    private final Button deleteButton =
            new Button("Delete Selected");

    private final TextArea consoleArea =
            new TextArea();

    private final Label emptyTablePlaceholder =
            createEmptyTableLabel();

    private final VBox loadingTablePlaceholder =
            createLoadingTablePlaceholder();

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss");


    public WatchlistView()
    {
        setSpacing(14);
        setFillWidth(true);

        HBox toolbar = createToolbar();
        VBox tablePanel = createTablePanel();
        VBox consolePanel = createConsolePanel();

        VBox.setVgrow(
                tablePanel,
                Priority.ALWAYS
        );

        getChildren().addAll(
                toolbar,
                tablePanel,
                consolePanel
        );

        configureFiltering();
    }


    // ============================================================
    // Toolbar
    // ============================================================

    private HBox createToolbar()
    {
        tickerInput.setPromptText("Ticker");
        tickerInput.setPrefWidth(125);

        tickerInput.setStyle(
                "-fx-background-color: #0a2117;"
                        + "-fx-background-radius: 8;"
                        + "-fx-border-color: #2d5a43;"
                        + "-fx-border-radius: 8;"
                        + "-fx-text-fill: #effff5;"
                        + "-fx-prompt-text-fill: #6f9981;"
        );

        searchInput.setPromptText(
                "Search ticker or system status..."
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

        stylePrimaryButton(addButton);
        styleDangerButton(deleteButton);

        filterComboBox.getItems().addAll(
                "All Stocks",
                "Any Active Signal",
                "Any Muted Signal",
                "Warning Regime",
                "Missing / Invalid Data"
        );

        filterComboBox.setValue("All Stocks");
        filterComboBox.setPrefWidth(175);

        filterComboBox.setStyle(
                "-fx-background-color: #0a2117;"
                        + "-fx-background-radius: 8;"
                        + "-fx-border-color: #2d5a43;"
                        + "-fx-border-radius: 8;"
        );

        HBox toolbar = new HBox(
                9,
                tickerInput,
                addButton,
                deleteButton,
                searchInput,
                filterComboBox
        );

        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(14));

        toolbar.setStyle(
                "-fx-background-color: linear-gradient("
                        + "to bottom, #173b2a, #10291d);"
                        + "-fx-background-radius: 13;"
                        + "-fx-border-color: #2d5a43;"
                        + "-fx-border-radius: 13;"
        );

        return toolbar;
    }


    // ============================================================
    // Table
    // ============================================================

    private VBox createTablePanel()
    {
        configureTable();

        Label title = new Label("Tracked Stocks");

        title.setStyle(
                "-fx-text-fill: #effff5;"
                        + "-fx-font-size: 15;"
                        + "-fx-font-weight: bold;"
        );

        Label description = new Label(
                "Click a column header to sort"
        );

        description.setStyle(
                "-fx-text-fill: #8fb59f;"
                        + "-fx-font-size: 11;"
        );

        Region spacer = new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        HBox header = new HBox(
                10,
                title,
                spacer,
                description
        );

        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(
                new Insets(14, 16, 14, 16)
        );

        header.setStyle(
                "-fx-border-color: #2d5a43;"
                        + "-fx-border-width: 0 0 1 0;"
        );

        VBox panel = new VBox(
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


    private void configureTable()
    {
        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        table.setPrefHeight(500);

        table.setPlaceholder(
                emptyTablePlaceholder
        );

        table.setStyle(
                "-fx-background-color: transparent;"
                        + "-fx-control-inner-background: #10291d;"
                        + "-fx-control-inner-background-alt: #123023;"
                        + "-fx-table-cell-border-color: #2d5a43;"
                        + "-fx-text-background-color: #effff5;"
        );

        TableColumn<WatchlistRow, String> tickerColumn =
                createTickerColumn();

        TableColumn<WatchlistRow, Number> priceColumn =
                createPriceColumn();

        TableColumn<WatchlistRow, Number> changeColumn =
                createChangeColumn();

        TableColumn<WatchlistRow, Number> atrColumn =
                createAtrColumn();

        TableColumn<WatchlistRow, Number> volumeColumn =
                createRelativeVolumeColumn();

        TableColumn<WatchlistRow, String> system1Column =
                createStatusColumn(
                        "S1",
                        WatchlistRow::system1Status
                );

        TableColumn<WatchlistRow, String> system2Column =
                createStatusColumn(
                        "S2",
                        WatchlistRow::system2Status
                );

        TableColumn<WatchlistRow, String> system3Column =
                createStatusColumn(
                        "S3",
                        WatchlistRow::system3Status
                );

        TableColumn<WatchlistRow, String> regimeColumn =
                createStatusColumn(
                        "S4",
                        WatchlistRow::marketRegimeStatus
                );

        table.getColumns().addAll(
                tickerColumn,
                priceColumn,
                changeColumn,
                atrColumn,
                volumeColumn,
                system1Column,
                system2Column,
                system3Column,
                regimeColumn
        );

        SortedList<WatchlistRow> sortedRows =
                new SortedList<>(filteredRows);

        sortedRows.comparatorProperty().bind(
                table.comparatorProperty()
        );

        table.setItems(sortedRows);
    }


    private TableColumn<WatchlistRow, String>
    createTickerColumn()
    {
        TableColumn<WatchlistRow, String> column =
                new TableColumn<>("Ticker");

        column.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().ticker()
                )
        );

        column.setCellFactory(tableColumn ->
                new TableCell<>()
                {
                    @Override
                    protected void updateItem(
                            String value,
                            boolean empty)
                    {
                        super.updateItem(value, empty);

                        if (empty || value == null)
                        {
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

        column.setPrefWidth(90);

        return column;
    }


    private TableColumn<WatchlistRow, Number>
    createPriceColumn()
    {
        TableColumn<WatchlistRow, Number> column =
                new TableColumn<>("Price");

        column.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(
                        data.getValue().price()
                )
        );

        column.setCellFactory(tableColumn ->
                createNumberCell(
                        "$%,.2f"
                )
        );

        column.setPrefWidth(100);

        return column;
    }


    private TableColumn<WatchlistRow, Number>
    createChangeColumn()
    {
        TableColumn<WatchlistRow, Number> column =
                new TableColumn<>("Change %");

        column.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(
                        data.getValue()
                                .changePercent()
                )
        );

        column.setCellFactory(tableColumn ->
                new TableCell<>()
                {
                    @Override
                    protected void updateItem(
                            Number value,
                            boolean empty)
                    {
                        super.updateItem(value, empty);

                        if (empty || value == null)
                        {
                            setText(null);
                            setStyle("");
                            return;
                        }

                        double percentage =
                                value.doubleValue();

                        if (!Double.isFinite(percentage))
                        {
                            setText("N/A");
                            setStyle(
                                    "-fx-text-fill: #ff495d;"
                                            + "-fx-font-weight: bold;"
                            );
                            return;
                        }

                        setText(
                                String.format(
                                        Locale.US,
                                        "%+.2f%%",
                                        percentage
                                )
                        );

                        if (percentage > 0.0)
                        {
                            setStyle(
                                    "-fx-text-fill: #45d6a5;"
                                            + "-fx-font-weight: bold;"
                            );
                        }
                        else if (percentage < 0.0)
                        {
                            setStyle(
                                    "-fx-text-fill: #ff7181;"
                                            + "-fx-font-weight: bold;"
                            );
                        }
                        else
                        {
                            setStyle(
                                    "-fx-text-fill: #8fb59f;"
                            );
                        }
                    }
                }
        );

        column.setPrefWidth(105);

        return column;
    }


    private TableColumn<WatchlistRow, Number>
    createAtrColumn()
    {
        TableColumn<WatchlistRow, Number> column =
                new TableColumn<>("ATR14");

        column.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(
                        data.getValue().atr14()
                )
        );

        column.setCellFactory(tableColumn ->
                createNumberCell("%.2f")
        );

        column.setPrefWidth(90);

        return column;
    }


    private TableColumn<WatchlistRow, Number>
    createRelativeVolumeColumn()
    {
        TableColumn<WatchlistRow, Number> column =
                new TableColumn<>("Relative Volume");

        column.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(
                        data.getValue()
                                .relativeVolume()
                )
        );

        column.setCellFactory(tableColumn ->
                createNumberCell("%.2fx")
        );

        column.setPrefWidth(135);

        return column;
    }


    private TableCell<WatchlistRow, Number>
    createNumberCell(String format)
    {
        return new TableCell<>()
        {
            @Override
            protected void updateItem(
                    Number value,
                    boolean empty)
            {
                super.updateItem(value, empty);

                if (empty || value == null)
                {
                    setText(null);
                    setStyle("");
                    return;
                }

                double number =
                        value.doubleValue();

                if (!Double.isFinite(number))
                {
                    setText("N/A");
                    setStyle(
                            "-fx-text-fill: #ff495d;"
                                    + "-fx-font-weight: bold;"
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
        };
    }


    private TableColumn<WatchlistRow, String>
    createStatusColumn(
            String title,
            StatusExtractor extractor)
    {
        TableColumn<WatchlistRow, String> column =
                new TableColumn<>(title);

        column.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        extractor.getStatus(
                                data.getValue()
                        )
                )
        );

        column.setCellFactory(tableColumn ->
                new TableCell<>()
                {
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

                        badge.setStyle(
                                "-fx-background-radius: 20;"
                                        + "-fx-font-size: 10;"
                                        + "-fx-font-weight: bold;"
                        );
                    }

                    @Override
                    protected void updateItem(
                            String value,
                            boolean empty)
                    {
                        super.updateItem(value, empty);

                        if (empty || value == null)
                        {
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

        column.setPrefWidth(115);

        return column;
    }


    private Label createEmptyTableLabel()
    {
        Label label = new Label(
                "No stocks are currently displayed."
        );

        label.setStyle(
                "-fx-text-fill: #8fb59f;"
                        + "-fx-font-size: 13;"
        );

        return label;
    }


    private VBox createLoadingTablePlaceholder()
    {
        ProgressIndicator spinner =
                new ProgressIndicator(
                        ProgressIndicator.INDETERMINATE_PROGRESS
                );

        spinner.setPrefSize(
                48,
                48
        );

        spinner.setMaxSize(
                48,
                48
        );

        spinner.getStyleClass().add(
                "stock-loading-spinner"
        );

        Label title = new Label(
                "Loading stocks..."
        );

        title.getStyleClass().add(
                "stock-loading-title"
        );

        Label detail = new Label(
                "Downloading market data and recalculating signals"
        );

        detail.getStyleClass().add(
                "stock-loading-detail"
        );

        VBox placeholder = new VBox(
                10,
                spinner,
                title,
                detail
        );

        placeholder.setAlignment(
                Pos.CENTER
        );

        placeholder.setPadding(
                new Insets(30)
        );

        placeholder.getStyleClass().add(
                "stock-loading-placeholder"
        );

        return placeholder;
    }


    // ============================================================
    // Console
    // ============================================================

    private VBox createConsolePanel()
    {
        Label title = new Label(
                "Application Console"
        );

        title.setStyle(
                "-fx-text-fill: #effff5;"
                        + "-fx-font-size: 15;"
                        + "-fx-font-weight: bold;"
        );

        Button clearButton =
                new Button("Clear");

        styleNormalButton(clearButton);

        clearButton.setOnAction(event ->
                consoleArea.clear()
        );

        Region spacer = new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        HBox header = new HBox(
                10,
                title,
                spacer,
                clearButton
        );

        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(
                new Insets(12, 16, 12, 16)
        );

        header.setStyle(
                "-fx-border-color: #2d5a43;"
                        + "-fx-border-width: 0 0 1 0;"
        );

        consoleArea.setEditable(false);
        consoleArea.setWrapText(false);
        consoleArea.setPrefHeight(150);

        consoleArea.setStyle(
                "-fx-control-inner-background: #03110b;"
                        + "-fx-text-fill: #b8d8c4;"
                        + "-fx-font-family: Consolas;"
                        + "-fx-font-size: 12;"
                        + "-fx-highlight-fill: #2d5a43;"
        );

        VBox panel = new VBox(
                header,
                consoleArea
        );

        panel.setStyle(
                "-fx-background-color: #10291d;"
                        + "-fx-background-radius: 13;"
                        + "-fx-border-color: #2d5a43;"
                        + "-fx-border-radius: 13;"
        );

        return panel;
    }


    // ============================================================
    // Search and filtering
    // ============================================================

    private void configureFiltering()
    {
        searchInput.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        updateFilter()
        );

        filterComboBox.valueProperty().addListener(
                (observable, oldValue, newValue) ->
                        updateFilter()
        );
    }


    private void updateFilter()
    {
        String searchText =
                searchInput.getText()
                        .trim()
                        .toLowerCase(Locale.ROOT);

        String selectedFilter =
                filterComboBox.getValue();

        filteredRows.setPredicate(row ->
        {
            boolean matchesSearch =
                    searchText.isEmpty()
                            || containsSearchText(
                            row,
                            searchText
                    );

            boolean matchesCategory =
                    matchesSelectedFilter(
                            row,
                            selectedFilter
                    );

            return matchesSearch
                    && matchesCategory;
        });
    }


    private boolean containsSearchText(
            WatchlistRow row,
            String searchText)
    {
        String combinedText =
                row.ticker()
                        + " "
                        + row.system1Status()
                        + " "
                        + row.system2Status()
                        + " "
                        + row.system3Status()
                        + " "
                        + row.marketRegimeStatus()
                        + " "
                        + row.dataStatus();

        return combinedText
                .toLowerCase(Locale.ROOT)
                .contains(searchText);
    }


    private boolean matchesSelectedFilter(
            WatchlistRow row,
            String selectedFilter)
    {
        if (selectedFilter == null)
        {
            return true;
        }

        return switch (selectedFilter)
        {
            case "Any Active Signal" ->
                    row.hasActiveSignal();

            case "Any Muted Signal" ->
                    row.hasMutedSignal();

            case "Warning Regime" ->
                    row.hasWarningRegime();

            case "Missing / Invalid Data" ->
                    !row.dataAvailable();

            default -> true;
        };
    }


    // ============================================================
    // Public input and output methods
    // ============================================================

    /**
     * Input:
     * A list of GUI-ready WatchlistRow objects.
     *
     * Output:
     * The table immediately displays those rows.
     */
    public void setRows(
            List<WatchlistRow> rows)
    {
        if (rows == null)
        {
            sourceRows.clear();
            return;
        }

        sourceRows.setAll(rows);
        updateFilter();
    }


    public void clearRows()
    {
        sourceRows.clear();
    }


    /**
     * Switches the empty table area between the normal empty message
     * and an animated loading indicator.
     */
    public void setLoading(
            boolean loading)
    {
        Runnable update = () ->
                table.setPlaceholder(
                        loading
                                ? loadingTablePlaceholder
                                : emptyTablePlaceholder
                );

        if (Platform.isFxApplicationThread())
        {
            update.run();
        }
        else
        {
            Platform.runLater(update);
        }
    }


    /**
     * Output:
     * The row currently selected by the user.
     *
     * Returns null when nothing is selected.
     */
    public WatchlistRow getSelectedRow()
    {
        return table
                .getSelectionModel()
                .getSelectedItem();
    }


    /**
     * Output:
     * The ticker typed into the Add Stock field.
     */
    public String getTickerInput()
    {
        return tickerInput
                .getText()
                .trim()
                .toUpperCase(Locale.ROOT);
    }


    public void clearTickerInput()
    {
        tickerInput.clear();
    }


    public void setOnAddStock(
            EventHandler<ActionEvent> handler)
    {
        addButton.setOnAction(handler);
    }


    public void setOnDeleteStock(
            EventHandler<ActionEvent> handler)
    {
        deleteButton.setOnAction(handler);
    }


    /**
     * Adds an application-status line to the GUI console.
     */
    public void log(String message)
    {
        if (message == null || message.isBlank())
        {
            return;
        }

        appendConsoleLine(
                message,
                false
        );
    }


    /**
     * Receives one complete line redirected from System.out or System.err.
     *
     * This method is safe to call from downloader tasks and other
     * non-JavaFX threads.
     */
    public void appendSystemOutput(
            String message,
            boolean errorOutput)
    {
        appendConsoleLine(
                message == null ? "null" : message,
                errorOutput
        );
    }


    private void appendConsoleLine(
            String message,
            boolean errorOutput)
    {
        Runnable appendAction = () ->
        {
            /* Preserve blank lines printed by System.out.println(). */
            if (message.isEmpty())
            {
                consoleArea.appendText(
                        System.lineSeparator()
                );

                return;
            }

            String time =
                    LocalTime.now()
                            .format(TIME_FORMAT);

            String outputType =
                    errorOutput
                            ? "[ERROR] "
                            : "";

            consoleArea.appendText(
                    "[" + time + "] "
                            + outputType
                            + message
                            + System.lineSeparator()
            );

            /* Keep the newest output visible automatically. */
            consoleArea.positionCaret(
                    consoleArea.getLength()
            );
        };

        if (Platform.isFxApplicationThread())
        {
            appendAction.run();
        }
        else
        {
            Platform.runLater(
                    appendAction
            );
        }
    }


    // ============================================================
    // Styling
    // ============================================================

    private String getBadgeStyle(String value)
    {
        String upper =
                value.toUpperCase(Locale.ROOT);

        String background;
        String textColor;

        if (upper.equals("N/A"))
        {
            background =
                    "rgba(255,73,93,0.18)";

            textColor = "#ff495d";
        }
        else if (upper.equals("BUY"))
        {
            background =
                    "rgba(69,214,165,0.14)";

            textColor = "#aaf7db";
        }
        else if (upper.equals("SHORT"))
        {
            background =
                    "rgba(255,113,129,0.14)";

            textColor = "#ffd0d6";
        }
        else if (upper.equals("MUTED")
                || upper.contains("COMPRESSION"))
        {
            background =
                    "rgba(242,199,92,0.14)";

            textColor = "#ffe5a3";
        }
        else if (upper.equals("NORMAL"))
        {
            background =
                    "rgba(53,208,127,0.14)";

            textColor = "#b8f5d0";
        }
        else if (upper.contains("RANGE"))
        {
            background =
                    "rgba(155,134,255,0.15)";

            textColor = "#d8cffd";
        }
        else if (upper.contains("FALSE"))
        {
            background =
                    "rgba(255,157,92,0.15)";

            textColor = "#ffe0c5";
        }
        else
        {
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


    private void stylePrimaryButton(
            Button button)
    {
        button.setStyle(
                "-fx-background-color: linear-gradient("
                        + "to bottom right, #35d07f, #1fa866);"
                        + "-fx-background-radius: 8;"
                        + "-fx-text-fill: #02140b;"
                        + "-fx-font-weight: bold;"
        );
    }


    private void styleDangerButton(
            Button button)
    {
        button.setStyle(
                "-fx-background-color: rgba(255,113,129,0.10);"
                        + "-fx-background-radius: 8;"
                        + "-fx-border-color: rgba(255,113,129,0.35);"
                        + "-fx-border-radius: 8;"
                        + "-fx-text-fill: #ffd7dc;"
        );
    }


    private void styleNormalButton(
            Button button)
    {
        button.setStyle(
                "-fx-background-color: #10291d;"
                        + "-fx-background-radius: 8;"
                        + "-fx-border-color: #2d5a43;"
                        + "-fx-border-radius: 8;"
                        + "-fx-text-fill: #effff5;"
        );
    }

    public void setOnTickerSubmit(
            EventHandler<ActionEvent> handler)
    {
        tickerInput.setOnAction(handler);
    }


    @FunctionalInterface
    private interface StatusExtractor
    {
        String getStatus(
                WatchlistRow row);
    }
}