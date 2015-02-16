/*
 * The MIT License
 *
 * Copyright (c) 2014 Red Hat, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.olivergondza.dumpling.cli;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.ProcessBuilder.Redirect;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public class CliAccessor {

    public static Set<? extends CliCommand> handlers() {
        return CliCommandOptionHandler.getAllHandlers();
    }

    public static @CheckForNull CliCommand getHandler(String className) {
        for (CliCommand handler: handlers()) {
            if (handler.getClass().getCanonicalName().equals(className)) {
                return handler;
            }
        }

        return null;
    }

    public static String usage(CliCommand handler) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(out);

        // Run just to register option handlers
        new Main().run(new String[] {"help", handler.getName()}, new ProcessStream(in(""), nullOutputStream, nullOutputStream));

        HelpCommand.printUsage(handler, ps);

        return out.toString();
    }

    public static String getGroovyBindingUsage() {
        try {
            // PWD is /target/site/apidocs
            Process process = new ProcessBuilder("../../../dumpling.sh", "groovy").redirectError(Redirect.INHERIT).start();

            new PrintStream(process.getOutputStream()).append("D\n").close();;

            assert process.waitFor() != 0;

            try (java.util.Scanner s = new java.util.Scanner(process.getInputStream())) {
                return s.useDelimiter("\\A").hasNext() ? s.next() : "";
            }
        } catch (IOException | InterruptedException ex) {
            throw new AssertionError(ex);
        }
    }

    private static @Nonnull InputStream in(String in) {
        try {
            return new ByteArrayInputStream(in.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            throw new AssertionError(ex);
        }
    }

    private static final @Nonnull PrintStream nullOutputStream = new PrintStream(new OutputStream() {
        @Override public void write(int b) throws IOException {}
    });
}
