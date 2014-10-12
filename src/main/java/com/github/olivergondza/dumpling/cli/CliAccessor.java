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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Set;

import javax.annotation.CheckForNull;

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
        new Main().run(new String[] {"help", handler.getName()}, ProcessStream.system());

        HelpCommand.printUsage(handler, ps);

        return out.toString();
    }
}
