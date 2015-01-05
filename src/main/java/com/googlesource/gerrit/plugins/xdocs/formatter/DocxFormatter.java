// Copyright (C) 2014 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.googlesource.gerrit.plugins.xdocs.formatter;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.inject.Inject;

import com.googlesource.gerrit.plugins.xdocs.ConfigSection;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DocxFormatter implements StreamFormatter {
  public static final String NAME = "DOCX";

  private final FormatterUtil util;

  @Inject
  DocxFormatter(FormatterUtil formatterUtil) {
    this.util = formatterUtil;
  }

  @Override
  public String format(String projectName, String revision, ConfigSection cfg,
      InputStream raw) throws IOException {
    // Docx4J tries to load some resources dynamically. This fails if the Gerrit
    // core classloader is used since it doesn't see the resources that are
    // contained in the plugin jar. To make the resource loading work we
    // must set a context classloader on the current thread.
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
      WordprocessingMLPackage p = Docx4J.load(raw);
      HTMLSettings htmlSettings = Docx4J.createHTMLSettings();
      htmlSettings.setWmlPackage(p);
      Docx4jProperties.setProperty("docx4j.Convert.Out.HTML.OutputMethodXML", true);
      try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        Docx4J.toHTML(htmlSettings, out, Docx4J.FLAG_EXPORT_PREFER_XSL);
        String html = out.toString(UTF_8.name());
        return util.applyCss(html, NAME, projectName);
      }
    } catch (Docx4JException e) {
      throw new IOException(e);
    } finally {
      Thread.currentThread().setContextClassLoader(loader);
    }
  }
}
