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

import static com.googlesource.gerrit.plugins.xdocs.XDocGlobalConfig.KEY_INCLUDE_TOC;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.io.ByteStreams;
import com.google.gerrit.common.TimeUtil;
import com.google.gerrit.extensions.annotations.PluginData;
import com.google.inject.Inject;
import com.google.inject.Singleton;

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
import java.util.Properties;

@Singleton
public class AsciidoctorFormatter implements Formatter {
  public static final String NAME = "ASCIIDOCTOR";

  private static final String BACKEND = "html5";
  private static final String DOCTYPE = "article";
  private static final String ERUBY = "erb";

  private final File baseDir;
  private final String css;
  private final Properties attributes;

  @Inject
  public AsciidoctorFormatter(@PluginData File baseDir) throws IOException {
    this.baseDir = baseDir;
    this.css = readCss();
    this.attributes = readAttributes();
  }

  @Override
  public String format(String projectName, String revision, ConfigSection cfg,
      String raw) throws IOException {
    // asciidoctor ignores all attributes if no output file is specified,
    // this is why we must specified an output file and then read its content
    File tmpFile =
        new File(baseDir, "tmp/asciidoctor-" + TimeUtil.nowTs().getNanos() + ".tmp");
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
    AttributesBuilder ab = AttributesBuilder.attributes()
        .tableOfContents(cfg.getBoolean(KEY_INCLUDE_TOC, true))
        .sourceHighlighter("prettify");
    for (String name : attributes.stringPropertyNames()) {
      ab.attribute(name, attributes.getProperty(name));
    }
    ab.attribute("last-update-label!");
    ab.attribute("revnumber", revision);
    return ab.get();
  }

  private String insertCss(String html) {
    int p = html.lastIndexOf("</head>");
    if (p > 0) {
      StringBuilder b = new StringBuilder();
      b.append(html.substring(0, p));
      b.append("<style type=\"text/css\">\n");
      b.append(css);
      b.append("</style>\n");
      b.append(html.substring(p));
      return b.toString();
    } else {
      return html;
    }
  }

  private static String readCss() throws IOException {
    String name = "asciidoctor.css";
    URL url = AsciidoctorFormatter.class.getResource(name);
    if (url == null) {
      throw new FileNotFoundException("Resource " + name);
    }
    try (InputStream in = url.openStream();
        TemporaryBuffer.Heap tmp = new TemporaryBuffer.Heap(128 * 1024)) {
      tmp.copy(in);
      return new String(tmp.toByteArray(), UTF_8);
    }
  }

  private static Properties readAttributes() throws IOException {
    Properties attributes = new Properties();
    try (InputStream in = AsciidoctorFormatter.class
        .getResourceAsStream("asciidoctor.properties")) {
      attributes.load(in);
    }
    return attributes;
  }
}
