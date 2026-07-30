package GUI;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.lang.reflect.InvocationTargetException;

public final class StartupSplash
{
    private static JWindow window;
    private static LoadingSpinner spinner;

    private StartupSplash()
    {
    }

    public static void showSplash()
    {
        Runnable showAction = () ->
        {
            window = new JWindow();
            window.setAlwaysOnTop(true);

            JPanel content =
                    new JPanel();

            content.setLayout(
                    new BoxLayout(
                            content,
                            BoxLayout.Y_AXIS
                    )
            );

            content.setBackground(
                    Color.decode("#10291d")
            );

            content.setBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(
                                    Color.decode("#2d5a43"),
                                    1
                            ),
                            BorderFactory.createEmptyBorder(
                                    28,
                                    40,
                                    28,
                                    40
                            )
                    )
            );

            spinner =
                    new LoadingSpinner();

            spinner.setAlignmentX(
                    Component.CENTER_ALIGNMENT
            );

            JLabel title =
                    new JLabel(
                            "Stock Signal Center",
                            SwingConstants.CENTER
                    );

            title.setAlignmentX(
                    Component.CENTER_ALIGNMENT
            );

            title.setForeground(
                    Color.decode("#effff5")
            );

            title.setFont(
                    new Font(
                            "Segoe UI",
                            Font.BOLD,
                            21
                    )
            );

            JLabel message =
                    new JLabel(
                            "Please wait — the program is starting...",
                            SwingConstants.CENTER
                    );

            message.setAlignmentX(
                    Component.CENTER_ALIGNMENT
            );

            message.setForeground(
                    Color.decode("#8fb59f")
            );

            message.setFont(
                    new Font(
                            "Segoe UI",
                            Font.PLAIN,
                            13
                    )
            );

            JLabel detail =
                    new JLabel(
                            "Loading stock data and preparing the dashboard",
                            SwingConstants.CENTER
                    );

            detail.setAlignmentX(
                    Component.CENTER_ALIGNMENT
            );

            detail.setForeground(
                    Color.decode("#6f9980")
            );

            detail.setFont(
                    new Font(
                            "Segoe UI",
                            Font.PLAIN,
                            11
                    )
            );

            content.add(spinner);
            content.add(Box.createVerticalStrut(18));
            content.add(title);
            content.add(Box.createVerticalStrut(10));
            content.add(message);
            content.add(Box.createVerticalStrut(5));
            content.add(detail);

            window.setContentPane(content);
            window.setSize(440, 260);
            window.setLocationRelativeTo(null);

            spinner.start();
            window.setVisible(true);
        };

        try
        {
            if (SwingUtilities.isEventDispatchThread())
            {
                showAction.run();
            }
            else
            {
                /*
                 * invokeAndWait ensures the splash is visible before
                 * JavaFX begins its potentially slow startup.
                 */
                SwingUtilities.invokeAndWait(
                        showAction
                );
            }
        }
        catch (InterruptedException exception)
        {
            Thread.currentThread().interrupt();
        }
        catch (InvocationTargetException exception)
        {
            throw new IllegalStateException(
                    "Could not create startup splash.",
                    exception.getCause()
            );
        }
    }

    public static void closeSplash()
    {
        SwingUtilities.invokeLater(() ->
        {
            if (spinner != null)
            {
                spinner.stop();
            }

            if (window != null)
            {
                window.setVisible(false);
                window.dispose();
                window = null;
            }
        });
    }

    private static final class LoadingSpinner
            extends JComponent
    {
        private int angle;

        private final Timer timer =
                new Timer(
                        16,
                        event ->
                        {
                            angle =
                                    (angle + 5) % 360;

                            repaint();
                        }
                );

        private LoadingSpinner()
        {
            setPreferredSize(
                    new Dimension(
                            64,
                            64
                    )
            );

            setMinimumSize(
                    new Dimension(
                            64,
                            64
                    )
            );

            setMaximumSize(
                    new Dimension(
                            64,
                            64
                    )
            );

            setOpaque(false);
        }

        private void start()
        {
            timer.start();
        }

        private void stop()
        {
            timer.stop();
        }

        @Override
        protected void paintComponent(
                Graphics graphics)
        {
            super.paintComponent(graphics);

            Graphics2D graphics2D =
                    (Graphics2D) graphics.create();

            graphics2D.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            graphics2D.setStroke(
                    new BasicStroke(
                            6.0f,
                            BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND
                    )
            );

            int size =
                    Math.min(
                            getWidth(),
                            getHeight()
                    ) - 12;

            int x =
                    (getWidth() - size) / 2;

            int y =
                    (getHeight() - size) / 2;

            graphics2D.setColor(
                    Color.decode("#2d5a43")
            );

            graphics2D.drawArc(
                    x,
                    y,
                    size,
                    size,
                    0,
                    360
            );

            graphics2D.setColor(
                    Color.decode("#35d07f")
            );

            graphics2D.drawArc(
                    x,
                    y,
                    size,
                    size,
                    angle,
                    105
            );

            graphics2D.dispose();
        }
    }
}