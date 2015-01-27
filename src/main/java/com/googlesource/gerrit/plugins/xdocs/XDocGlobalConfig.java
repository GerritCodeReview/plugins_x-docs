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

package com.googlesource.gerrit.plugins.xdocs;

import com.googlesource.gerrit.plugins.xdocs.formatter.AsciidoctorFormatter;
import com.googlesource.gerrit.plugins.xdocs.formatter.DocxFormatter;
import com.googlesource.gerrit.plugins.xdocs.formatter.ImageFormatter;
import com.googlesource.gerrit.plugins.xdocs.formatter.MarkdownFormatter;
import com.googlesource.gerrit.plugins.xdocs.formatter.PlainTextFormatter;
import com.googlesource.gerrit.plugins.xdocs.formatter.ZipFormatter;

import org.eclipse.jgit.lib.Config;

import java.util.Arrays;

public class XDocGlobalConfig {
  public static final String SECTION_FORMATTER = "formatter";
  public static final String KEY_ALLOW_HTML = "allowHtml";
  public static final String KEY_CSS_THEME = "cssTheme";
  public static final String KEY_ENABLED = "enabled";
  public static final String KEY_EXT = "ext";
  public static final String KEY_FORMATTER = "formatter";
  public static final String KEY_INCLUDE_TOC = "includeToc";
  public static final String KEY_INHERIT_CSS = "inheritCss";
  public static final String KEY_MIME_TYPE = "mimeType";
  public static final String KEY_PREFIX = "prefix";
  public static final String KEY_PRIO = "prio";

  private final Config cfg;

  public XDocGlobalConfig(Config cfg) {
    this.cfg = cfg;
  }

  public ConfigSection getFormatterConfig(String formatterName) {
    return new ConfigSection(cfg, SECTION_FORMATTER, formatterName);
  }

  static void initialize(Config cfg) {
    cfg.setString(SECTION_FORMATTER, AsciidoctorFormatter.NAME, KEY_EXT, "adoc");
    cfg.setStringList(SECTION_FORMATTER, DocxFormatter.NAME, KEY_EXT,
        Arrays.asList("docx"));
    cfg.setString(SECTION_FORMATTER, ImageFormatter.NAME, KEY_MIME_TYPE,
        "image/*");
    cfg.setString(SECTION_FORMATTER, MarkdownFormatter.NAME, KEY_MIME_TYPE,
        "text/x-markdown");
    cfg.setString(SECTION_FORMATTER, PlainTextFormatter.NAME, KEY_MIME_TYPE,
        "text/plain");
    cfg.setStringList(SECTION_FORMATTER, ZipFormatter.NAME, KEY_EXT,
        Arrays.asList("jar", "war", "zip"));
  }
}
