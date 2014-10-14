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

import com.google.common.io.ByteStreams;
import com.google.gerrit.extensions.annotations.PluginData;
import com.google.inject.Inject;

import com.googlesource.gerrit.plugins.xdocs.ConfigSection;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.eclipse.jgit.util.TemporaryBuffer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsciidoctorFormatter implements Formatter {
  public final static String NAME = "ASCIIDOCTOR";

  private static final String KEY_INCLUDE_TOC = "includeToc";

  private static final String BACKEND = "html5";
  private static final String DOCTYPE = "article";
  private static final String ERUBY = "erb";

  private static final String css;

  static {
    AtomicBoolean file = new AtomicBoolean();
    String src;
    try {
      src = readCss(file);
    } catch (IOException err) {
      src = "";
    }
    css = file.get() ? null : src;
  }

  private final File baseDir;

  @Inject
  public AsciidoctorFormatter(@PluginData File baseDir) {
    this.baseDir = baseDir;
  }

  @Override
  public String format(String projectName, String revision, ConfigSection cfg,
      String raw) throws IOException {
    // asciidoctor ignores all attributes if no output file is specified,
    // this is why we must specified an output file and then read its content
    File tmpFile =
        new File(baseDir, "tmp/asciidoc-" + System.currentTimeMillis() + ".tmp");
    try {
      Asciidoctor.Factory.create(AsciidoctorFormatter.class.getClassLoader())
          .render(raw, createOptions(cfg, revision, tmpFile));
      try (FileInputStream input = new FileInputStream(tmpFile)) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteStreams.copy(input, out);
        return insertCss(out.toString(UTF_8.name()));
      }
    } finally {
      if (!tmpFile.delete()) {
        tmpFile.deleteOnExit();
      }
    }
  }

  private Options createOptions(ConfigSection cfg, String revision, File out) {
    return OptionsBuilder.options()
            .backend(BACKEND)
            .docType(DOCTYPE)
            .eruby(ERUBY)
            .safe(SafeMode.SECURE)
            .attributes(getAttributes(cfg, revision))
            .mkDirs(true)
            .toFile(out)
            .get();
  }

  private Attributes getAttributes(ConfigSection cfg, String revision) {
    return AttributesBuilder.attributes()
        .tableOfContents(cfg.getBoolean(KEY_INCLUDE_TOC, true))
        .sourceHighlighter("prettify")
        .attribute("newline", "\\n")
        .attribute("asterisk", "&#42;")
        .attribute("plus", "&#43;")
        .attribute("caret", "&#94;")
        .attribute("startsb", "&#91;")
        .attribute("endsb", "&#93;")
        .attribute("tilde", "&#126;")
        .attribute("last-update-label!")
        .attribute("revnumber", revision)
        .get();
  }

  private String insertCss(String html) {
    int p = html.lastIndexOf("</head>");
    if (p > 0) {
      StringBuilder b = new StringBuilder();
      b.append(html.substring(0, p));
      b.append("<style type=\"text/css\">\n");
      b.append(readCSS());
      b.append("</style>\n");
      b.append(html.substring(p));
      return b.toString();
    } else {
      return html;
    }
  }

  private static String readCSS() {
    if (css != null) {
      return css;
    }
    try {
      return readCss(new AtomicBoolean());
    } catch (IOException err) {
      return "";
    }
  }

  private static String readCss(AtomicBoolean file)
      throws IOException {
    String name = "asciidoc.css";
    URL url = AsciidoctorFormatter.class.getResource(name);
    if (url == null) {
      throw new FileNotFoundException("Resource " + name);
    }
    file.set("file".equals(url.getProtocol()));
    try (InputStream in = url.openStream()) {
      try (TemporaryBuffer.Heap tmp = new TemporaryBuffer.Heap(128 * 1024)) {
        tmp.copy(in);
        return new String(tmp.toByteArray(), UTF_8);
      }
    }
  }
}
