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
import static com.googlesource.gerrit.plugins.xdocs.XDocGlobalConfig.KEY_APPEND_CSS;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.inject.Inject;

import com.googlesource.gerrit.plugins.xdocs.ConfigSection;

import java.io.UnsupportedEncodingException;

public class MarkdownFormatter implements Formatter {
  public final static String NAME = "MARKDOWN";

  private final FormatterUtil util;
  private final Formatters formatters;

  @Inject
  MarkdownFormatter(FormatterUtil formatterUtil, Formatters formatters) {
    this.util = formatterUtil;
    this.formatters = formatters;
  }

  @Override
  public String format(String projectName, String revision,
      ConfigSection globalCfg, String raw) throws UnsupportedEncodingException {
    ConfigSection projectCfg =
        formatters.getFormatterConfig(globalCfg.getSubsection(), projectName);
    com.google.gerrit.server.documentation.MarkdownFormatter f =
        new com.google.gerrit.server.documentation.MarkdownFormatter();
    if (!globalCfg.getBoolean(KEY_ALLOW_HTML, false)) {
      f.suppressHtml();
    }
    String projectCss = util.getCss(projectName, "markdown");
    if (projectCfg.getBoolean(KEY_APPEND_CSS, true)) {
      byte[] b = f.markdownToDocHtml(raw, UTF_8.name());
      return util.insertCss(new String(b, UTF_8), projectCss);
    } else {
      f.setCss(projectCss);
      byte[] b = f.markdownToDocHtml(raw, UTF_8.name());
      return new String(b, UTF_8);
    }
  }
}
