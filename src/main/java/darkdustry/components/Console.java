package darkdustry.components;

import arc.func.Cons;
import mindustry.server.ServerControl;
import org.jline.reader.LineReaderBuilder;

import java.io.*;

import static arc.Core.app;
import static darkdustry.PluginVars.control;

public class Console {

    public static void load() {
        var reader = LineReaderBuilder.builder().build();
        System.setOut(new BlockingPrintStream(reader::printAbove));

        control = (ServerControl) app.getListeners().find(ServerControl.class::isInstance);
        control.serverInput = () -> {
            while (true) {
                try {
                    var line = reader.readLine("> ");
                    if (!line.isBlank())
                        app.post(() -> control.handleCommandString(line));
                } catch (Exception ignored) {}
            }
        };
    }

    public static class BlockingPrintStream extends PrintStream {
        private final Cons<String> cons;

        private int last = -1;

        public BlockingPrintStream(Cons<String> cons) {
            super(new ByteArrayOutputStream());
            this.cons = cons;
        }

        public ByteArrayOutputStream out() {
            return (ByteArrayOutputStream) out;
        }

        @Override
        public void write(int b) {
            if (last == 13 && b == 10) {
                last = -1;
                return;
            }

            if ((last = b) == 13 || b == 10) {
                flush();
            } else {
                super.write(b);
            }
        }

        @Override
        public void write(byte[] buf, int off, int len) {
            for (int i = 0; i < len; i++) {
                write(buf[off + i]);
            }
        }

        @Override
        public void flush() {
            var string = out().toString();
            out().reset();
            cons.get(string);
        }
    }
}