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

import static com.googlesource.gerrit.plugins.xdocs.XDocGlobalConfig.KEY_ALLOW_HTML;
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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Properties;

@Singleton
public class AsciidoctorFormatter implements StringFormatter {
  public static final String NAME = "ASCIIDOCTOR";

  private static final String BACKEND = "html5";
  private static final String DOCTYPE = "article";
  private static final String ERUBY = "erb";

  private final File baseDir;
  private final Properties attributes;
  private final FormatterUtil util;
  private final Formatters formatters;

  @Inject
  public AsciidoctorFormatter(@PluginData File baseDir,
      FormatterUtil formatterUtil, Formatters formatters) throws IOException {
    this.baseDir = baseDir;
    this.attributes = readAttributes();
    this.util = formatterUtil;
    this.formatters = formatters;
  }

  @Override
  public String format(String projectName, String revision,
      ConfigSection globalCfg, String raw) throws IOException {
    if (!globalCfg.getBoolean(KEY_ALLOW_HTML, false)) {
      raw = suppressHtml(raw);
    }

    ConfigSection projectCfg =
        formatters.getFormatterConfig(NAME, projectName);
    // asciidoctor ignores all attributes if no output file is specified,
    // this is why we must specify an output file and then read its content
    File tmpFile =
        new File(baseDir, "tmp/asciidoctor-" + TimeUtil.nowTs().getNanos() + ".tmp");
    try {
      Asciidoctor.Factory.create(AsciidoctorFormatter.class.getClassLoader())
          .render(raw, createOptions(projectCfg, revision, tmpFile));
      try (FileInputStream input = new FileInputStream(tmpFile)) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteStreams.copy(input, out);
        String html = out.toString(UTF_8.name());
        return util.applyInsertAnchorsScript(
            util.applyCss(html, NAME, projectName));
      }
    } finally {
      if (!tmpFile.delete()) {
        tmpFile.deleteOnExit();
      }
    }
  }

  private String suppressHtml(String raw) throws IOException {
    try (BufferedReader br = new BufferedReader(new StringReader(raw))) {
      StringBuilder sb = new StringBuilder();
      boolean embeddedHtml = false;
      String line;
      while ((line = br.readLine()) != null) {
        if (line.startsWith("++++")) {
          embeddedHtml = !embeddedHtml;
        }
        if (!embeddedHtml && !line.startsWith("++++")) {
          sb.append(line);
          sb.append("\n");
        }
      }
      return sb.toString();
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

  private static Properties readAttributes() throws IOException {
    Properties attributes = new Properties();
    try (InputStream in = AsciidoctorFormatter.class
        .getResourceAsStream("asciidoctor.properties")) {
      attributes.load(in);
    }
    return attributes;
  }
}
