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
package com.github.olivergondza.dumpling;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import com.github.olivergondza.dumpling.cli.CliAccessor;
import com.github.olivergondza.dumpling.cli.CliCommand;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Type;

public class DumplingDoclet {

    //private static final Map<String, Doc> scopesToClass = new HashMap<String, Doc>();

    public static boolean start(RootDoc root) {
        final File target = new File(".");

        final List<ClassDoc> cliCommands = new ArrayList<ClassDoc>();
        final List<ClassDoc> factories = new ArrayList<ClassDoc>();
        final List<ClassDoc> queries = new ArrayList<ClassDoc>();
        final List<MethodDoc> threadPredicates = new ArrayList<MethodDoc>();

        for (ClassDoc cls: root.classes()) {
            // Sort classes
            for (Type iface: cls.interfaceTypes()) {
                if ("CliCommand".equals(iface.typeName())) {
                    cliCommands.add(cls);
                } else if ("SingleThreadSetQuery".equals(iface.typeName())) {
                    queries.add(cls);
                } else if ("CliRuntimeFactory".equals(iface.typeName())) {
                    factories.add(cls);
                }
            }

            // special case as there is no reasonable superclass
            if ("JvmRuntimeFactory".equals(cls.typeName())) {
                factories.add(cls);
            }

            for (MethodDoc mthd: cls.methods()) {
                if ("ProcessThread.Predicate".equals(mthd.returnType().typeName())) {
                    threadPredicates.add(mthd);
                }
            }
        }

        printCli(new File(target, "cliCommands.md"), cliCommands, "CLI Commands");
        printDoc(new File(target, "factories.md"), factories, "Runtime factories");
        printDoc(new File(target, "queries.md"), queries, "Queries");
        printDoc(new File(target, "threadPredicates.md"), threadPredicates, "Thread predicates");

        return true;
    }

    private static void printDoc(File out, List<? extends Doc> docs, String title) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(out);
            header(writer, title);
            for (Doc d: docs) {
                writer.write("### "); writer.write(javadocLink(d, d.name())); writer.write('\n');
                writer.write(d.commentText()); writer.write('\n');
            }
        } catch (IOException ex) {
            throw new AssertionError(ex);
        } finally {

            try {
                if (writer != null) writer.close();
            } catch (IOException ex) {
                // TODO Auto-generated catch block
                ex.printStackTrace();
            }
        }
    }

    private static void printCli(File out, List<ClassDoc> docs, String title) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(out);
            header(writer, title);
            for (ClassDoc d: docs) {
                CliCommand command = instantiateComand(d);
                writer.write("### "); writer.write(javadocLink(d, "`" + command.getName() + "`")); writer.write("\n");
                writer.write(command.getDescription()); writer.write('\n');
            }
        } catch (IOException ex) {
            throw new AssertionError(ex);
        } finally {

            try {
                if (writer != null) writer.close();
            } catch (IOException ex) {
                // TODO Auto-generated catch block
                ex.printStackTrace();
            }
        }
    }

    private static CliCommand instantiateComand(ClassDoc d) {
        return CliAccessor.getHandler(d.qualifiedTypeName());
    }

    private static String javadocLink(Doc element, String title) {
        if (element instanceof ClassDoc) {

            ClassDoc cd = (ClassDoc) element;
            String packageName = cd.containingPackage().name();
            return String.format("[%s](./apidocs/%s/%s.html)", title, packageName.replace(".", "/"), cd.name());
        } else if (element instanceof MethodDoc) {

            MethodDoc md = ((MethodDoc) element);
            String packageName = md.containingPackage().name();
            String className = md.containingClass().name();
            return String.format("[%s](./apidocs/%s/%s.html#%s%s)",
                    title, packageName.replace(".", "/"), className, md.name(), md.signature()
            );
        } else {

            throw new AssertionError();
        }
    }

    private static void header(Writer writer, String title) throws IOException {
        writer.write("---\n");
        writer.write("title: "); writer.write(title); writer.write('\n');
        writer.write("layout: default\n");
        writer.write("---\n");
        writer.write("[Reference documentation for TAG](.)\n");
        writer.write("# {{page.title}}\n");
    }
}
