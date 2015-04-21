/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apiman.tools.i18n;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Used to scan all of the UI templates looking for strings that require
 * translation. The output from this scanner is a messages.properties file with
 * all of the strings that require translation. This should then be compared
 * with the messages.properties file included in the API Manager UI project to
 * see if anything is missing.
 * 
 * TODO: integrate this into a maven plugin of some kind to automatically detect
 * missing strings during the build
 */
@SuppressWarnings("nls")
public class TemplateScanner {

    public static void main(String[] args) throws IOException {
        if (args == null || args.length != 1) {
            System.out.println("Template directory not provided (no path provided).");
            System.exit(1);
        }
        File templateDir = new File(args[0]);
        if (!templateDir.isDirectory()) {
            System.out.println("Template directory not provided (provided path is not a directory).");
            System.exit(1);
        }
        
        if (!new File(templateDir, "dash.html").isFile()) {
            System.out.println("Template directory not provided (dash.html not found).");
            System.exit(1);
        }
        
        File outputDir = new File(templateDir, "../../../../../../tools/i18n/target");
        if (!outputDir.isDirectory()) {
            System.out.println("Output directory not found: " + outputDir);
            System.exit(1);
        }
        File outputFile = new File(outputDir, "scanner-messages.properties");
        if (outputFile.isFile() && !outputFile.delete()) {
            System.out.println("Couldn't delete the old messages.properties: " + outputFile);
            System.exit(1);
        }

        System.out.println("Starting scan.");
        System.out.println("Scanning template directory: " + templateDir.getAbsolutePath());
        
        String[] extensions = { "html", "include" };
        Collection<File> files = FileUtils.listFiles(templateDir, extensions, true);
        
        TreeMap<String, String> strings = new TreeMap<>();
        
        for (File file : files) {
            System.out.println("\tScanning file: " + file);
            scanFile(file, strings);
        }

        outputMessages(strings, outputFile);
        
        System.out.println("Scan complete.  Scanned " + files.size() + " files and discovered " + strings.size() + " translation strings.");
    }

    /**
     * Scan the given html template using jsoup and find all strings that require translation.  This is
     * done by finding all elements with a "apiman-i18n-key" attribute.
     * @param file
     * @param strings
     * @throws IOException 
     */
    private static void scanFile(File file, TreeMap<String, String> strings) throws IOException {
        Document doc = Jsoup.parse(file, "UTF-8");
        Elements elements = doc.select("*[apiman-i18n-key]");
        for (Element element : elements) {
            String i18nKey = element.attr("apiman-i18n-key");
            String elementVal = element.text();
            if (strings.containsKey(i18nKey)) {
                String currentValue = strings.get(i18nKey);
                if (currentValue.equals(elementVal)) {
                    strings.put(i18nKey, elementVal);
                } else {
                    throw new IOException("Duplicate i18n key found with different default values.  Key=" + i18nKey + "  Value1=" + elementVal + "  Value2=" + currentValue);
                }
            } else {
                strings.put(i18nKey, elementVal);
            }
        }
    }

    /**
     * Output the sorted map of strings to the specified output file.
     * @param strings
     * @param outputFile
     * @throws FileNotFoundException 
     */
    private static void outputMessages(TreeMap<String, String> strings, File outputFile) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(new FileOutputStream(outputFile));
        for (Entry<String, String> entry : strings.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue();
            writer.append(key);
            writer.append('=');
            writer.append(val);
            writer.append("\n");
        }
        writer.flush();
        writer.close();
    }

}
