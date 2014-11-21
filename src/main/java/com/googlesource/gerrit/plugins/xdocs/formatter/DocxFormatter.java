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

  @Override
  public String format(String projectName, String revision, ConfigSection cfg,
      InputStream raw) throws IOException {
    try {
      WordprocessingMLPackage p = Docx4J.load(raw);
      HTMLSettings htmlSettings = Docx4J.createHTMLSettings();
      htmlSettings.setWmlPackage(p);
      String userCSS = "html, body, div, span, h1, h2, h3, h4, h5, h6, p, a, img, ol, ul, li, table, caption, tbody, tfoot, thead, tr, th, td " +
          "{ margin: 0; padding: 0; border: 0;}" +
          "body {line-height: 1;} ";
      htmlSettings.setUserCSS(userCSS);
      Docx4jProperties.setProperty("docx4j.Convert.Out.HTML.OutputMethodXML", true);
      try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        Docx4J.toHTML(htmlSettings, out, Docx4J.FLAG_EXPORT_PREFER_XSL);
        return out.toString(UTF_8.name());
      }
    } catch (Docx4JException e) {
      throw new IOException(e);
    }
  }
}
