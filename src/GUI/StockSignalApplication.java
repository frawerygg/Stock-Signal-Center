package GUI;

import Downloader.ApiKeyManager;
import Downloader.DownloadManager;
import Downloader.StockTickerList;
import javafx.animation.Interpolator;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.concurrent.Task;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StockSignalApplication
        extends Application
{
    private static final Duration PAGE_TRANSITION_DURATION =
            Duration.millis(240);

    private static final Duration NAVIGATION_HIGHLIGHT_DURATION =
            Duration.millis(260);

    private static final List<String> API_KEYS =
            List.of(
                    "NB8OUJIP2BLN8F7E",
                    "A7IA501QU8SPYY2L",
                    "AF9ABR4MMGN8EXD9",
                    "L5RR67V7ZQ6H5GEV",
                    "XWCF8PX00DIAC883",
                    "NVQ357FLZ4B9Z3AR",
                    "XBHBDH6TOAA6Q8MD"
            );

    private final Map<String, Pane> pages =
            new LinkedHashMap<>();

    private final Map<String, Button> navigationButtons =
            new LinkedHashMap<>();

    private final StockSignalService stockSignalService =
            new StockSignalService(
                    Path.of("stock_data")
            );

    private Label pageTitleLabel;
    private Label pageSubtitleLabel;
    private Label statusLabel;
    private Label stockCountLabel;

    private Button refreshButton;
    private boolean refreshInProgress;
    private boolean pageTransitionInProgress;
    private String currentPageId;

    private StackPane pageContainer;
    private StackPane navigationArea;
    private Region navigationHighlight;
    private WatchlistView watchlistView;
    private System1View system1View;
    private System2View system2View;
    private System3View system3View;
    private System4View system4View;
    private OptionsView optionsView;
    private ExitView exitView;
    private SignalHistoryView signalHistoryView;
    private ConsoleRedirector consoleRedirector;




    @Override
    public void start(
            Stage stage)
    {
        BorderPane root =
                new BorderPane();

        root.setLeft(
                createSidebar()
        );

        root.setCenter(
                createMainArea()
        );

        createPages();
        installConsoleRedirect();
        showPage("watchlist");
        loadRealWatchlistRows();

        Scene scene =
                new Scene(
                        root,
                        1400,
                        850
                );

        var themeUrl = StockSignalApplication.class.getResource(
                "dark-green-theme.css"
        );

        if (themeUrl != null)
        {
            scene.getStylesheets().add(
                    themeUrl.toExternalForm()
            );
        }

        stage.setTitle(
                "Stock Signal Center"
        );

        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.setScene(scene);
        stage.show();
        StartupSplash.closeSplash();
    }


    /**
     * Sends all System.out and System.err lines to the fake console while
     * preserving the normal IntelliJ/terminal output.
     */
    private void installConsoleRedirect()
    {
        consoleRedirector =
                new ConsoleRedirector(
                        line -> watchlistView.appendSystemOutput(
                                line,
                                false
                        ),
                        line -> watchlistView.appendSystemOutput(
                                line,
                                true
                        )
                );

        consoleRedirector.install();
    }


    @Override
    public void stop()
    {
        if (consoleRedirector != null)
        {
            consoleRedirector.restore();
        }
    }


    // ============================================================
    // Sidebar
    // ============================================================

    private VBox createSidebar()
    {
        VBox sidebar =
                new VBox(6);

        sidebar.setPrefWidth(235);

        sidebar.setPadding(
                new Insets(
                        22,
                        15,
                        22,
                        15
                )
        );

        sidebar.setStyle(
                "-fx-background-color: #071b12;"
                        + "-fx-border-color: #2d5a43;"
                        + "-fx-border-width: 0 1 0 0;"
        );

        VBox navigationList =
                new VBox(6);

        navigationList.getChildren().addAll(
                createSectionLabel("MAIN"),

                createNavigationButton(
                        "watchlist",
                        "▦  Watchlist Manager"
                ),

                createNavigationButton(
                        "history",
                        "◷  7-Day Signal History"
                ),

                createSectionLabel(
                        "SIGNAL SYSTEMS"
                ),

                createNavigationButton(
                        "system1",
                        "↗  S1 — SMA Crossover"
                ),

                createNavigationButton(
                        "system2",
                        "⚡  S2 — Breakout"
                ),

                createNavigationButton(
                        "system3",
                        "↺  S3 — Pullback Recovery"
                ),

                createNavigationButton(
                        "system4",
                        "⌁  S4 — Market Regime"
                ),

                createSectionLabel("TRADING"),

                createNavigationButton(
                        "options",
                        "◇  Options Income"
                ),

                createNavigationButton(
                        "exits",
                        "⇩  Exit / Sell Points"
                )
        );

        navigationHighlight =
                new Region();

        navigationHighlight.setManaged(false);
        navigationHighlight.setMouseTransparent(true);
        navigationHighlight.setPrefHeight(42);
        navigationHighlight.setStyle(
                "-fx-background-color: rgba(53,208,127,0.18);"
                        + "-fx-background-radius: 9;"
                        + "-fx-border-color: rgba(53,208,127,0.35);"
                        + "-fx-border-radius: 9;"
        );

        navigationArea =
                new StackPane(
                        navigationHighlight,
                        navigationList
                );

        navigationArea.setAlignment(
                Pos.TOP_LEFT
        );

        Region spacer =
                new Region();

        VBox.setVgrow(
                spacer,
                Priority.ALWAYS
        );

        sidebar.getChildren().addAll(
                createBrand(),
                navigationArea,
                spacer,
                createEnginePanel()
        );

        return sidebar;
    }


    private HBox createBrand()
    {
        Label logo =
                new Label("FG");

        logo.setAlignment(
                Pos.CENTER
        );

        logo.setPrefSize(
                42,
                42
        );

        logo.setStyle(
                "-fx-background-color: linear-gradient("
                        + "to bottom right, #35d07f, #1fa866);"
                        + "-fx-background-radius: 12;"
                        + "-fx-text-fill: #02140b;"
                        + "-fx-font-size: 15;"
                        + "-fx-font-weight: bold;"
        );

        Label title =
                new Label(
                        "Signal Center"
                );

        title.setStyle(
                "-fx-text-fill: #effff5;"
                        + "-fx-font-size: 16;"
                        + "-fx-font-weight: bold;"
        );

        Label subtitle =
                new Label(
                        "JavaFX Stock Scanner"
                );

        subtitle.setStyle(
                "-fx-text-fill: #8fb59f;"
                        + "-fx-font-size: 10;"
        );

        VBox textBox =
                new VBox(
                        3,
                        title,
                        subtitle
                );

        HBox brand =
                new HBox(
                        11,
                        logo,
                        textBox
                );

        brand.setAlignment(
                Pos.CENTER_LEFT
        );

        brand.setPadding(
                new Insets(
                        0,
                        7,
                        18,
                        7
                )
        );

        return brand;
    }


    private Label createSectionLabel(
            String text)
    {
        Label label =
                new Label(text);

        label.setPadding(
                new Insets(
                        12,
                        10,
                        5,
                        10
                )
        );

        label.setStyle(
                "-fx-text-fill: #8fb59f;"
                        + "-fx-font-size: 10;"
                        + "-fx-font-weight: bold;"
        );

        return label;
    }


    private Button createNavigationButton(
            String pageId,
            String text)
    {
        Button button =
                new Button(text);

        button.setMaxWidth(
                Double.MAX_VALUE
        );

        button.setPrefHeight(42);

        button.setAlignment(
                Pos.CENTER_LEFT
        );

        applyInactiveNavigationStyle(
                button
        );

        button.setOnAction(event ->
                showPage(pageId)
        );

        navigationButtons.put(
                pageId,
                button
        );

        return button;
    }




    private VBox createEnginePanel()
    {
        Label engineLabel =
                new Label(
                        "●  Signal engine ready"
                );

        engineLabel.setStyle(
                "-fx-text-fill: #effff5;"
                        + "-fx-font-weight: bold;"
        );

        stockCountLabel =
                new Label(
                        "Stocks: 0"
                );

        stockCountLabel.setStyle(
                "-fx-text-fill: #8fb59f;"
        );

        Label historyLabel =
                new Label(
                        "History window: 7 days"
                );

        historyLabel.setStyle(
                "-fx-text-fill: #8fb59f;"
        );

        VBox panel =
                new VBox(
                        9,
                        engineLabel,
                        stockCountLabel,
                        historyLabel
                );

        panel.setPadding(
                new Insets(14)
        );

        panel.setStyle(
                "-fx-background-color: #10291d;"
                        + "-fx-background-radius: 11;"
                        + "-fx-border-color: #2d5a43;"
                        + "-fx-border-radius: 11;"
        );

        return panel;
    }


    // ============================================================
    // Main area
    // ============================================================

    private BorderPane createMainArea()
    {
        BorderPane mainArea =
                new BorderPane();

        mainArea.setStyle(
                "-fx-background-color: #06170f;"
        );

        mainArea.setTop(
                createHeader()
        );

        pageContainer =
                new StackPane();

        pageContainer.setPadding(
                new Insets(
                        0,
                        24,
                        24,
                        24
                )
        );

        /*
         * Keep all page effects contained inside the content area.
         */
        Rectangle pageClip =
                new Rectangle();

        pageClip.widthProperty().bind(
                pageContainer.widthProperty()
        );

        pageClip.heightProperty().bind(
                pageContainer.heightProperty()
        );

        pageContainer.setClip(pageClip);

        mainArea.setCenter(
                pageContainer
        );

        return mainArea;
    }


    private HBox createHeader()
    {
        pageTitleLabel =
                new Label(
                        "Watchlist Manager"
                );

        pageTitleLabel.setStyle(
                "-fx-text-fill: #effff5;"
                        + "-fx-font-size: 27;"
                        + "-fx-font-weight: bold;"
        );

        pageSubtitleLabel =
                new Label(
                        "Review all tracked stocks and signals."
                );

        pageSubtitleLabel.setStyle(
                "-fx-text-fill: #8fb59f;"
                        + "-fx-font-size: 13;"
        );

        VBox titleBox =
                new VBox(
                        5,
                        pageTitleLabel,
                        pageSubtitleLabel
                );

        statusLabel =
                new Label(
                        "Data ready"
                );

        statusLabel.setPadding(
                new Insets(
                        5,
                        10,
                        5,
                        10
                )
        );

        statusLabel.setStyle(
                "-fx-background-color: rgba(53,208,127,0.14);"
                        + "-fx-background-radius: 20;"
                        + "-fx-text-fill: #b8f5d0;"
                        + "-fx-font-size: 11;"
                        + "-fx-font-weight: bold;"
        );

        refreshButton =
                new Button(
                        "Refresh Data"
                );

        refreshButton.setPrefHeight(36);

        refreshButton.setStyle(
                "-fx-background-color: #10291d;"
                        + "-fx-background-radius: 8;"
                        + "-fx-border-color: #2d5a43;"
                        + "-fx-border-radius: 8;"
                        + "-fx-text-fill: #effff5;"
        );

        refreshButton.setOnAction(event ->
                handleRefresh()
        );

        HBox actions =
                new HBox(
                        10,
                        statusLabel,
                        refreshButton
                );

        actions.setAlignment(
                Pos.CENTER_RIGHT);

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        HBox header =
                new HBox(
                        15,
                        titleBox,
                        spacer,
                        actions
                );

        header.setAlignment(
                Pos.CENTER_LEFT
        );

        header.setPadding(
                new Insets(
                        24,
                        24,
                        18,
                        24
                )
        );

        return header;
    }


    // ============================================================
    // Pages
    // ============================================================

    private void createPages()
    {
        watchlistView =
                new WatchlistView();

        watchlistView.setOnAddStock(
                event -> handleAddStock()
        );

        watchlistView.setOnTickerSubmit(
                event -> handleAddStock()
        );

        watchlistView.setOnDeleteStock(
                event -> handleDeleteStock()
        );

        pages.put(
                "watchlist",
                watchlistView
        );

        system1View =
                new System1View();

        pages.put(
                "system1",
                system1View
        );

        system2View =
                new System2View();

        pages.put(
                "system2",
                system2View
        );

        system3View =
                new System3View();

        pages.put(
                "system3",
                system3View
        );

        system4View =
                new System4View();

        pages.put(
                "system4",
                system4View
        );

        optionsView =
                new OptionsView();

        pages.put(
                "options",
                optionsView
        );

        exitView =
                new ExitView();

        pages.put(
                "exits",
                exitView
        );

        signalHistoryView =
                new SignalHistoryView();

        pages.put(
                "history",
                signalHistoryView
        );
    }


    private Pane createPlaceholderPage(
            String title,
            String message)
    {
        Label titleLabel =
                new Label(title);

        titleLabel.setStyle(
                "-fx-text-fill: #effff5;"
                        + "-fx-font-size: 20;"
                        + "-fx-font-weight: bold;"
        );

        Label messageLabel =
                new Label(message);

        messageLabel.setStyle(
                "-fx-text-fill: #8fb59f;"
                        + "-fx-font-size: 13;"
        );

        VBox content =
                new VBox(
                        10,
                        titleLabel,
                        messageLabel
                );

        content.setPadding(
                new Insets(24)
        );

        content.setStyle(
                "-fx-background-color: linear-gradient("
                        + "to bottom, #173b2a, #10291d);"
                        + "-fx-background-radius: 13;"
                        + "-fx-border-color: #2d5a43;"
                        + "-fx-border-radius: 13;"
        );

        StackPane wrapper =
                new StackPane(content);

        StackPane.setAlignment(
                content,
                Pos.TOP_LEFT
        );

        return wrapper;
    }


    private void showPage(
            String pageId)
    {
        Pane nextPage =
                pages.get(pageId);

        if (nextPage == null)
        {
            throw new IllegalArgumentException(
                    "Unknown page: " + pageId
            );
        }

        if (pageId.equals(currentPageId))
        {
            return;
        }

        /*
         * The first page is installed immediately because the stage has not
         * been laid out yet. Later page changes use a restrained cross-fade.
         */
        if (currentPageId == null
                || pageContainer.getChildren().isEmpty())
        {
            resetPageTransform(nextPage);
            pageContainer.getChildren().setAll(nextPage);

            updateHeader(pageId);
            updateNavigationStyle(
                    pageId,
                    false
            );

            currentPageId = pageId;
            return;
        }

        if (pageTransitionInProgress)
        {
            return;
        }

        Pane currentPage =
                pages.get(currentPageId);

        pageTransitionInProgress = true;
        setNavigationButtonsLocked(true);

        resetPageTransform(currentPage);
        resetPageTransform(nextPage);
        nextPage.setOpacity(0);

        pageContainer.getChildren().setAll(
                currentPage,
                nextPage
        );

        updateHeader(pageId);
        updateNavigationStyle(
                pageId,
                true
        );

        FadeTransition currentPageFadeOut =
                new FadeTransition(
                        PAGE_TRANSITION_DURATION,
                        currentPage
                );

        currentPageFadeOut.setFromValue(1);
        currentPageFadeOut.setToValue(0);
        currentPageFadeOut.setInterpolator(
                Interpolator.EASE_BOTH
        );

        FadeTransition nextPageFadeIn =
                new FadeTransition(
                        PAGE_TRANSITION_DURATION,
                        nextPage
                );

        nextPageFadeIn.setFromValue(0);
        nextPageFadeIn.setToValue(1);
        nextPageFadeIn.setInterpolator(
                Interpolator.EASE_BOTH
        );

        ParallelTransition transition =
                new ParallelTransition(
                        currentPageFadeOut,
                        nextPageFadeIn
                );

        transition.setOnFinished(event ->
        {
            resetPageTransform(currentPage);
            resetPageTransform(nextPage);

            pageContainer.getChildren().setAll(
                    nextPage
            );

            currentPageId = pageId;
            pageTransitionInProgress = false;
            setNavigationButtonsLocked(false);
        });

        transition.play();
    }


    private void resetPageTransform(
            Pane page)
    {
        page.setTranslateX(0);
        page.setTranslateY(0);
        page.setOpacity(1);
    }


    private void setNavigationButtonsLocked(
            boolean locked)
    {
        navigationButtons.values().forEach(
                button -> button.setMouseTransparent(locked)
        );
    }


    private void updateHeader(
            String pageId)
    {
        switch (pageId)
        {
            case "watchlist" ->
                    setHeader(
                            "Watchlist Manager",
                            "Add, remove, sort and review tracked stocks."
                    );

            case "system1" ->
                    setHeader(
                            "S1 — SMA Crossover",
                            "Bullish and bearish SMA20/SMA50 crossover signals."
                    );

            case "system2" ->
                    setHeader(
                            "S2 — Momentum Breakout",
                            "Twenty-day price breakouts confirmed by volume and RSI."
                    );

            case "system3" ->
                    setHeader(
                            "S3 — Extreme Pullback Recovery",
                            "Recovery signals following severe 21-day drawdowns."
                    );

            case "system4" ->
                    setHeader(
                            "S4 — Market Regime",
                            "Classifies the environment and filters raw signals."
                    );

            case "options" ->
                    setHeader(
                            "Options Income",
                            "Covered-call and cash-secured-put evaluations."
                    );

            case "exits" ->
                    setHeader(
                            "Exit / Sell Points",
                            "Protective stops produced by active systems."
                    );

            case "history" ->
                    setHeader(
                            "Seven-Day Signal History",
                            "Directional triggers and market-regime changes "
                                    + "from the latest seven trading days."
                    );

            default ->
                    throw new IllegalArgumentException(
                            "Unknown page: " + pageId
                    );
        }
    }


    private void setHeader(
            String title,
            String subtitle)
    {
        pageTitleLabel.setText(title);
        pageSubtitleLabel.setText(subtitle);
    }


    private void updateNavigationStyle(
            String activePageId,
            boolean animateHighlight)
    {
        for (Map.Entry<String, Button> entry
                : navigationButtons.entrySet())
        {
            if (entry.getKey()
                    .equals(activePageId))
            {
                applyActiveNavigationStyle(
                        entry.getValue()
                );
            }
            else
            {
                applyInactiveNavigationStyle(
                        entry.getValue()
                );
            }
        }

        moveNavigationHighlight(
                activePageId,
                animateHighlight
        );
    }


    private void moveNavigationHighlight(
            String pageId,
            boolean animate)
    {
        Button targetButton =
                navigationButtons.get(pageId);

        if (targetButton == null
                || navigationArea == null
                || navigationHighlight == null)
        {
            return;
        }

        /*
         * localToScene requires a completed layout pass. The first call is
         * therefore repeated on the JavaFX pulse after the stage is shown.
         */
        if (targetButton.getScene() == null
                || targetButton.getWidth() <= 0)
        {
            Platform.runLater(() ->
                    moveNavigationHighlight(
                            pageId,
                            false
                    )
            );
            return;
        }

        Bounds buttonBounds =
                targetButton.localToScene(
                        targetButton.getBoundsInLocal()
                );

        Bounds areaBounds =
                navigationArea.localToScene(
                        navigationArea.getBoundsInLocal()
                );

        double targetX =
                buttonBounds.getMinX()
                        - areaBounds.getMinX();

        double targetY =
                buttonBounds.getMinY()
                        - areaBounds.getMinY();

        navigationHighlight.setLayoutX(targetX);
        navigationHighlight.setPrefWidth(
                buttonBounds.getWidth()
        );
        navigationHighlight.setPrefHeight(
                buttonBounds.getHeight()
        );

        if (!animate)
        {
            navigationHighlight.setTranslateY(targetY);
            return;
        }

        TranslateTransition highlightSlide =
                new TranslateTransition(
                        NAVIGATION_HIGHLIGHT_DURATION,
                        navigationHighlight
                );

        highlightSlide.setToY(targetY);
        highlightSlide.setInterpolator(
                Interpolator.EASE_BOTH
        );
        highlightSlide.play();
    }


    private void applyActiveNavigationStyle(
            Button button)
    {
        button.setStyle(
                "-fx-background-color: transparent;"
                        + "-fx-background-radius: 9;"
                        + "-fx-border-color: transparent;"
                        + "-fx-border-radius: 9;"
                        + "-fx-text-fill: #effff5;"
                        + "-fx-font-size: 12;"
                        + "-fx-font-weight: bold;"
        );
    }


    private void applyInactiveNavigationStyle(
            Button button)
    {
        button.setStyle(
                "-fx-background-color: transparent;"
                        + "-fx-background-radius: 9;"
                        + "-fx-border-color: transparent;"
                        + "-fx-border-radius: 9;"
                        + "-fx-text-fill: #8fb59f;"
                        + "-fx-font-size: 12;"
        );
    }


    // ============================================================
    // Download and table refresh
    // ============================================================

    private void clearAllTables()
    {
        watchlistView.clearRows();
        signalHistoryView.clearRows();

        system1View.clearRows();
        system2View.clearRows();
        system3View.clearRows();
        system4View.clearRows();

        optionsView.clearRows();
        exitView.clearRows();

        stockCountLabel.setText(
                "Stocks: 0"
        );
    }


    private void downloadAndRefreshData()
    {
        if (refreshInProgress)
        {
            watchlistView.log(
                    "A data refresh is already running."
            );

            return;
        }

        refreshInProgress = true;
        refreshButton.setDisable(true);
        watchlistView.setLoading(true);

        clearAllTables();

        statusLabel.setText(
                "Downloading"
        );

        watchlistView.log(
                "Downloading market data for the active watchlist..."
        );

        Task<Void> downloadTask =
                new Task<>()
                {
                    @Override
                    protected Void call()
                            throws Exception
                    {
                        StockTickerList tickerList =
                                new StockTickerList();

                        ApiKeyManager apiKeyManager =
                                new ApiKeyManager(
                                        API_KEYS
                                );

                        DownloadManager downloadManager =
                                new DownloadManager(
                                        tickerList.getQuote(),
                                        apiKeyManager
                                );

                        downloadManager.downloadAll();
                        downloadManager.printResults();

                        return null;
                    }
                };

        downloadTask.setOnSucceeded(event ->
        {
            try
            {
                watchlistView.log(
                        "Market-data download finished."
                );

                watchlistView.log(
                        "Reloading saved data and recalculating signals..."
                );

                loadRealWatchlistRows();
            }
            finally
            {
                finishRefreshState();
            }
        });

        downloadTask.setOnFailed(event ->
        {
            try
            {
                Throwable exception =
                        downloadTask.getException();

                String message =
                        exception == null
                                ? "Unknown download error"
                                : exception.getMessage();

                statusLabel.setText(
                        "Download failed"
                );

                watchlistView.log(
                        "Market-data download failed: "
                                + message
                );

                watchlistView.log(
                        "Reloading any saved data that is currently available..."
                );

                /*
                 * Some earlier ticker downloads may have succeeded before
                 * the failure occurred, so reload the local files anyway.
                 */
                loadRealWatchlistRows();

                if (exception != null)
                {
                    exception.printStackTrace();
                }
            }
            finally
            {
                finishRefreshState();
            }
        });

        Thread downloadThread =
                new Thread(
                        downloadTask,
                        "stock-data-download"
                );

        downloadThread.setDaemon(true);
        downloadThread.start();
    }


    private void finishRefreshState()
    {
        refreshInProgress = false;
        refreshButton.setDisable(false);
        watchlistView.setLoading(false);
    }


    // ============================================================
    // Backend loading
    // ============================================================

    private void loadRealWatchlistRows()
    {
        statusLabel.setText(
                "Evaluating"
        );

        watchlistView.log(
                "Loading active watchlist..."
        );

        try
        {
            StockSignalService.LoadResult result =
                    stockSignalService.loadLatest();

            watchlistView.setRows(
                    result.rows()
            );

            system1View.setRows(
                    stockSignalService
                            .getLatestResults()
                            .stream()
                            .map(System1Row::from)
                            .filter(row ->
                                    row.hasActiveSignal()
                                            || row.isMuted()
                            )
                            .toList()
            );

            system2View.setRows(
                    stockSignalService
                            .getLatestResults()
                            .stream()
                            .map(System2Row::from)
                            .filter(row ->
                                    row.hasActiveSignal()
                                            || row.isMuted()
                            )
                            .toList()
            );

            system3View.setRows(
                    stockSignalService
                            .getLatestResults()
                            .stream()
                            .map(System3Row::from)
                            .filter(row ->
                                    row.hasActiveSignal()
                                            || row.isMuted()
                            )
                            .toList()
            );

            system4View.setRows(
                    stockSignalService
                            .getLatestResults()
                            .stream()
                            .map(System4Row::from)
                            .filter(row -> !row.isNormal())
                            .toList()
            );

            optionsView.setRows(
                    stockSignalService
                            .getLatestResults()
                            .stream()
                            .map(OptionsRow::from)
                            .filter(row -> !row.hasNoSignal())
                            .toList()
            );

            exitView.setRows(
                    stockSignalService
                            .getLatestResults()
                            .stream()
                            .flatMap(aaa ->
                                    ExitRow.from(aaa)
                                            .stream()
                            )
                            .toList()
            );

            signalHistoryView.setRows(
                    stockSignalService.buildHistory(
                            7
                    )
            );

            stockCountLabel.setText(
                    "Stocks: "
                            + result.trackedTickerCount()
            );

            watchlistView.log(
                    "Tracked tickers: "
                            + result.trackedTickerCount()
            );

            watchlistView.log(
                    "Tickers with saved data: "
                            + result.availableDataCount()
            );

            watchlistView.log(
                    "Successfully evaluated: "
                            + result.evaluatedStockCount()
            );

            if (result.missingDataCount() > 0)
            {
                watchlistView.log(
                        result.missingDataCount()
                                + " ticker(s) have no saved JSON data yet."
                );
            }

            if (result.skippedEvaluationCount() > 0)
            {
                watchlistView.log(
                        result.skippedEvaluationCount()
                                + " stock(s) could not produce complete metrics."
                );
            }

            if (result.rows().isEmpty())
            {
                statusLabel.setText(
                        "No stocks"
                );

                watchlistView.log(
                        "Ticker.txt does not currently contain any stocks."
                );
            }
            else if (result.evaluatedStockCount() == 0)
            {
                statusLabel.setText(
                        "Check tickers"
                );

                watchlistView.log(
                        "No tracked ticker has usable data. Check the red N/A cells."
                );
            }
            else if (result.evaluatedStockCount()
                    < result.trackedTickerCount())
            {
                statusLabel.setText(
                        "Data ready - warnings"
                );

                watchlistView.log(
                        "Watchlist evaluation complete. Unavailable metrics are shown as red N/A cells."
                );
            }
            else
            {
                statusLabel.setText(
                        "Data ready"
                );

                watchlistView.log(
                        "Watchlist evaluation complete."
                );
            }
        }
        catch (Exception exception)
        {
            if (exception instanceof IOException)
            {
                statusLabel.setText(
                        "Load failed"
                );
            }
            else
            {
                statusLabel.setText(
                        "Evaluation failed"
                );
            }

            stockCountLabel.setText(
                    "Stocks: 0"
            );

            clearAllTables();

            watchlistView.log(
                    "Could not load the watchlist: "
                            + exception.getMessage()
            );

            exception.printStackTrace();
        }
    }


    // ============================================================
    // Button actions
    // ============================================================

    private void handleRefresh()
    {
        watchlistView.log(
                "Manual refresh requested."
        );

        downloadAndRefreshData();
    }


    private void handleAddStock()
    {
        String ticker =
                watchlistView.getTickerInput();

        if (!isValidTicker(ticker))
        {
            watchlistView.log(
                    "Invalid ticker. Use 1–10 letters, numbers, "
                            + "periods or hyphens."
            );

            return;
        }

        try
        {
            boolean added =
                    stockSignalService.addTicker(
                            ticker
                    );

            if (!added)
            {
                watchlistView.log(
                        ticker
                                + " is already in the watchlist."
                );

                return;
            }

            watchlistView.clearTickerInput();

            watchlistView.log(
                    ticker
                            + " was added to Ticker.txt."
            );

            watchlistView.log(
                    "Downloading data for the updated watchlist..."
            );

            downloadAndRefreshData();
        }
        catch (IOException
               | IllegalArgumentException exception)
        {
            watchlistView.log(
                    "Could not add ticker: "
                            + exception.getMessage()
            );

            exception.printStackTrace();
        }
    }


    private void handleDeleteStock()
    {
        WatchlistRow selectedRow =
                watchlistView.getSelectedRow();

        if (selectedRow == null)
        {
            watchlistView.log(
                    "Select a stock before deleting it."
            );

            return;
        }

        String ticker =
                selectedRow.ticker();

        Alert confirmation =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirmation.setTitle(
                "Remove Stock"
        );

        confirmation.setHeaderText(
                "Remove "
                        + ticker
                        + " from the watchlist?"
        );

        confirmation.setContentText(
                "The ticker will be removed from Ticker.txt. "
                        + "Its saved JSON price history will be kept."
        );

        ButtonType selectedButton =
                confirmation.showAndWait()
                        .orElse(
                                ButtonType.CANCEL
                        );

        if (selectedButton
                != ButtonType.OK)
        {
            watchlistView.log(
                    "Removal cancelled."
            );

            return;
        }

        try
        {
            boolean removed =
                    stockSignalService.removeTicker(
                            ticker
                    );

            if (!removed)
            {
                watchlistView.log(
                        ticker
                                + " was not found in Ticker.txt."
                );

                return;
            }

            watchlistView.log(
                    ticker
                            + " was removed from the watchlist."
            );

            watchlistView.log(
                    "Its saved price history was kept."
            );

            loadRealWatchlistRows();
        }
        catch (IOException exception)
        {
            watchlistView.log(
                    "Could not remove ticker: "
                            + exception.getMessage()
            );

            exception.printStackTrace();
        }
    }


    private boolean isValidTicker(
            String ticker)
    {
        return ticker != null
                && ticker.matches(
                "[A-Z0-9.-]{1,10}"
        );
    }


    public static void main(
            String[] args)
    {
        launch(args);
    }
}