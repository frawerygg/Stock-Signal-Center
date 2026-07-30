package GUI;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Redirects System.out and System.err to GUI consumers while preserving
 * the original terminal output.
 *
 * Output is collected until a newline is reached, so println(), printf(),
 * and stack traces appear as complete lines in the JavaFX console.
 */
public final class ConsoleRedirector
{
    private final PrintStream originalOut;
    private final PrintStream originalErr;

    private final Consumer<String> standardOutputConsumer;
    private final Consumer<String> errorOutputConsumer;

    private boolean installed;


    public ConsoleRedirector(
            Consumer<String> standardOutputConsumer,
            Consumer<String> errorOutputConsumer)
    {
        this.originalOut = System.out;
        this.originalErr = System.err;

        this.standardOutputConsumer = Objects.requireNonNull(
                standardOutputConsumer,
                "Standard-output consumer cannot be null."
        );

        this.errorOutputConsumer = Objects.requireNonNull(
                errorOutputConsumer,
                "Error-output consumer cannot be null."
        );
    }


    public synchronized void install()
    {
        if (installed)
        {
            return;
        }

        System.setOut(
                createRedirectedPrintStream(
                        originalOut,
                        standardOutputConsumer
                )
        );

        System.setErr(
                createRedirectedPrintStream(
                        originalErr,
                        errorOutputConsumer
                )
        );

        installed = true;
    }


    /**
     * Restores the original terminal streams.
     */
    public synchronized void restore()
    {
        if (!installed)
        {
            return;
        }

        System.out.flush();
        System.err.flush();

        System.setOut(originalOut);
        System.setErr(originalErr);

        installed = false;
    }


    private PrintStream createRedirectedPrintStream(
            PrintStream terminalStream,
            Consumer<String> lineConsumer)
    {
        return new PrintStream(
                new LineTeeOutputStream(
                        terminalStream,
                        lineConsumer
                ),
                true,
                StandardCharsets.UTF_8
        );
    }


    /**
     * Writes each byte to the original terminal and also groups the output
     * into complete UTF-8 lines for the GUI.
     */
    private static final class LineTeeOutputStream
            extends OutputStream
    {
        private final PrintStream terminalStream;
        private final Consumer<String> lineConsumer;

        private final ByteArrayOutputStream lineBuffer =
                new ByteArrayOutputStream();


        private LineTeeOutputStream(
                PrintStream terminalStream,
                Consumer<String> lineConsumer)
        {
            this.terminalStream = terminalStream;
            this.lineConsumer = lineConsumer;
        }


        @Override
        public synchronized void write(int value)
                throws IOException
        {
            terminalStream.write(value);
            processByte(value);
        }


        @Override
        public synchronized void write(
                byte[] bytes,
                int offset,
                int length)
                throws IOException
        {
            Objects.checkFromIndexSize(
                    offset,
                    length,
                    bytes.length
            );

            terminalStream.write(
                    bytes,
                    offset,
                    length
            );

            for (int index = offset;
                 index < offset + length;
                 index++)
            {
                processByte(
                        bytes[index] & 0xFF
                );
            }
        }


        private void processByte(int value)
        {
            if (value == '\n')
            {
                publishBufferedLine();
            }
            else if (value != '\r')
            {
                lineBuffer.write(value);
            }
        }


        private void publishBufferedLine()
        {
            String line =
                    lineBuffer.toString(
                            StandardCharsets.UTF_8
                    );

            lineBuffer.reset();
            lineConsumer.accept(line);
        }


        @Override
        public synchronized void flush()
        {
            terminalStream.flush();

            if (lineBuffer.size() > 0)
            {
                publishBufferedLine();
            }
        }


        @Override
        public synchronized void close()
        {
            flush();

        }
    }
}
