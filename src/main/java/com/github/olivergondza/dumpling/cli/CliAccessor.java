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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CliAccessor {

    public static Map<String, String> getCommandUsages() {
        String handlers = runDumplingCli("CliCommandOptionHandler.getAllHandlers().collect { it.getClass().simpleName }.join(',')", "groovy");

        Map<String, String> usage = new HashMap<>();
        for (String s : handlers.trim().split(",")) {
            usage.put(s, runDumplingCli(null, "help", s));
        }
        return usage;
    }

    public static String getGroovyBindingUsage() {
        return runDumplingCli("D", "groovy");
    }

    private static String runDumplingCli(String stdin, String... args) {
        List<String> argList = new ArrayList<>();
        argList.add(dumplingSh().getAbsolutePath());
        argList.addAll(Arrays.asList(args));
        try {
            Process process = new ProcessBuilder(argList).redirectError(Redirect.INHERIT).start();

            if (stdin != null) {
                new PrintStream(process.getOutputStream()).append(stdin).append('\n').close();
            }

            assert process.waitFor() != 0;

            try (java.util.Scanner s = new java.util.Scanner(process.getInputStream())) {
                return s.useDelimiter("\\A").hasNext() ? s.next() : "";
            }
        } catch (IOException | InterruptedException ex) {
            throw new AssertionError(ex);
        }
    }

    private static File dumplingSh() {
        File current = new File(".").getAbsoluteFile();
        do {
            File script = new File(current, "dumpling.sh");
            if (script.exists()) return script;

            current = current.getParentFile();
        } while (current != null);

        throw new Error(new File(".").getAbsoluteFile() + " does not seem to be dumpling subdirectory");
    }
}
