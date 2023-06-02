package darkdustry.components;

import arc.func.Cons;
import darkdustry.DarkdustryPlugin;
import org.jline.reader.*;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.TerminalBuilder;
import reactor.util.annotation.NonNull;

import java.io.*;
import java.util.concurrent.CompletableFuture;

import static arc.Core.*;
import static mindustry.Vars.*;
import static mindustry.server.ServerControl.*;

public class Console {

    public static void load() {
        try {
            var terminal = TerminalBuilder.builder()
                    .jna(true)
                    .build();

            var completer = new StringsCompleter(instance.handler.getCommandList()
                    .map(command -> command.text));

            var reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(completer)
                    .build();

            terminal.enterRawMode();

            instance.serverInput = () -> {};
            System.setOut(new BlockingPrintStream(reader::printAbove));

            handleInput(reader);
            DarkdustryPlugin.info("JLine console loaded.");
        } catch (Exception e) {
            DarkdustryPlugin.error("Failed to load JLine console.");
        }
    }

    public static void handleInput(LineReader reader) {
        read(reader, "> ").thenAccept(text -> {
            if (!text.isEmpty() && !text.startsWith("#"))
                app.post(() -> instance.handleCommandString(text));

            handleInput(reader);
        });
    }

    public static CompletableFuture<String> read(LineReader reader, String prompt) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return reader.readLine(prompt);
            } catch (Exception e) {
                System.exit(0);
                return null;
            }
        }, mainExecutor);
    }

    public static class BlockingPrintStream extends PrintStream {
        public final Cons<String> cons;
        public int last = -1;

        public BlockingPrintStream(Cons<String> cons) {
            super(new ByteArrayOutputStream());
            this.cons = cons;
        }

        @Override
        public void write(int sign) {
            if (last == '\r' && sign == '\n') {
                last = -1;
                return;
            }

            last = sign;

            if (sign == '\n' || sign == '\r') flush();
            else super.write(sign);
        }

        @Override
        public void write(@NonNull byte[] buf, int off, int len) {
            for (int i = 0; i < len; i++)
                write(buf[off + i]);
        }

        @Override
        public void flush() {
            cons.get(out.toString());
            ((ByteArrayOutputStream) out).reset();
        }
    }
}