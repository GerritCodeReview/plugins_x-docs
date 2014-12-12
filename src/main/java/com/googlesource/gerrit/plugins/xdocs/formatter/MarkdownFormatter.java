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
import static com.googlesource.gerrit.plugins.xdocs.XDocGlobalConfig.KEY_CSS_THEME;
import static com.googlesource.gerrit.plugins.xdocs.XDocGlobalConfig.KEY_INHERIT_CSS;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.inject.Inject;

import com.googlesource.gerrit.plugins.xdocs.ConfigSection;

import java.io.IOException;

public class MarkdownFormatter implements StringFormatter {
  public final static String NAME = "MARKDOWN";

  private final FormatterUtil util;
  private final Formatters formatters;

  @Inject
  MarkdownFormatter(
      FormatterUtil formatterUtil,
      Formatters formatters) {
    this.util = formatterUtil;
    this.formatters = formatters;
  }

  @Override
  public String format(String projectName, String revision,
      ConfigSection globalCfg, String raw) throws IOException {
    ConfigSection projectCfg =
        formatters.getFormatterConfig(NAME, projectName);
    com.google.gerrit.server.documentation.MarkdownFormatter f =
        new com.google.gerrit.server.documentation.MarkdownFormatter();
    if (!globalCfg.getBoolean(KEY_ALLOW_HTML, false)) {
      f.suppressHtml();
    }
    String cssTheme = projectCfg.getString(KEY_CSS_THEME);
    String inheritedCss =
        util.getInheritedCss(projectName, NAME, "markdown", cssTheme);
    String projectCss = util.getCss(projectName, "markdown", cssTheme);
    String html;
    if (projectCfg.getBoolean(KEY_INHERIT_CSS, true)) {
      // if there is no inherited CSS and f.setCss(null) is invoked
      // com.google.gerrit.server.documentation.MarkdownFormatter applies the
      // default CSS
      f.setCss(inheritedCss);
      byte[] b = f.markdownToDocHtml(raw, UTF_8.name());
      html = util.insertCss(new String(b, UTF_8), projectCss);
    } else {
      if (projectCss != null) {
        f.setCss(projectCss);
      } else {
        // if there is no inherited CSS and f.setCss(null) is invoked
        // com.google.gerrit.server.documentation.MarkdownFormatter applies the
        // default CSS
        f.setCss(inheritedCss);
      }
      byte[] b = f.markdownToDocHtml(raw, UTF_8.name());
      html = new String(b, UTF_8);
    }
    return util.applyInsertAnchorsScript(html);
  }
}
