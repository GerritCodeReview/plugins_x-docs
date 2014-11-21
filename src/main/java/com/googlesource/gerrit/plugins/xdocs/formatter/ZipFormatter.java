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

import static com.googlesource.gerrit.plugins.xdocs.XDocGlobalConfig.KEY_APPEND_CSS;
import static com.googlesource.gerrit.plugins.xdocs.XDocGlobalConfig.KEY_CSS_THEME;

import com.google.common.base.MoreObjects;
import com.google.inject.Inject;

import com.googlesource.gerrit.plugins.xdocs.ConfigSection;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipFormatter implements StreamFormatter {
  public static final String NAME = "ZIP";

  private final Formatters formatters;
  private final FormatterUtil util;
  private final HtmlBuilder html;

  @Inject
  ZipFormatter(
      Formatters formatters,
      FormatterUtil formatterUtil,
      HtmlBuilder html) {
    this.formatters = formatters;
    this.util = formatterUtil;
    this.html = html;
  }

  @Override
  public String format(String projectName, String revision, ConfigSection globalCfg,
      InputStream raw) throws IOException {
    html.startDocument()
        .openHead()
        .closeHead()
        .openBody()
        .openTable()
        .appendCellHeader("name")
        .appendCellHeader("size")
        .appendCellHeader("last modified");
    try (ZipInputStream zip = new ZipInputStream(raw)) {
      ZipEntry entry;
      while ((entry = zip.getNextEntry()) != null) {
        html.openRow()
            .appendCell(entry.getName());
        if (!entry.isDirectory()) {
          if (entry.getSize() != -1) {
            html.appendCell(FileUtils.byteCountToDisplaySize(entry.getSize()));
          } else {
            html.appendCell("n/a");
          }
        } else {
          html.appendCell();
        }
        html.appendDateCell(entry.getTime())
            .closeRow();
      }
    }
    html.closeTable()
        .closeBody()
        .endDocument();

    ConfigSection projectCfg =
        formatters.getFormatterConfig(globalCfg.getSubsection(), projectName);
    String cssTheme = projectCfg.getString(KEY_CSS_THEME);
    String globalCss = util.getGlobalCss("zip", cssTheme);
    String projectCss = util.getCss(projectName, "zip", cssTheme);
    if (projectCfg.getBoolean(KEY_APPEND_CSS, true)) {
      return util.insertCss(html.toString(), globalCss, projectCss);
    } else if (projectCss != null || globalCss != null) {
      return util.insertCss(html.toString(),
          MoreObjects.firstNonNull(projectCss, globalCss));
    } else {
      return html.toString();
    }
  }
}
