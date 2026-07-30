package GUI;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Shared layout rules for result pages.
 *
 * Keeps every result table at the same height while allowing the entire
 * page (explanation, filters, and table) to scroll vertically.
 */
final class ResultPageLayout {
    private static final double RESULT_TABLE_HEIGHT =
            440.0;

    private ResultPageLayout() {
    }

    static void installScrollablePage(
            VBox page,
            Node... sections
    ) {
        VBox content =
                new VBox(14);

        content.setFillWidth(true);
        content.setPadding(
                new Insets(
                        0,
                        8,
                        18,
                        0
                )
        );
        content.getChildren().addAll(sections);

        ScrollPane scrollPane =
                new ScrollPane(content);

        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );
        scrollPane.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED
        );
        scrollPane.setPannable(true);
        scrollPane.getStyleClass().add(
                "result-page-scroll"
        );

        page.setFillWidth(true);
        VBox.setVgrow(
                scrollPane,
                Priority.ALWAYS
        );
        page.getChildren().setAll(scrollPane);
    }

    static void applyStandardTableHeight(
            TableView<?> table
    ) {
        table.setMinHeight(
                RESULT_TABLE_HEIGHT
        );
        table.setPrefHeight(
                RESULT_TABLE_HEIGHT
        );
        table.setMaxHeight(
                RESULT_TABLE_HEIGHT
        );
    }
}
