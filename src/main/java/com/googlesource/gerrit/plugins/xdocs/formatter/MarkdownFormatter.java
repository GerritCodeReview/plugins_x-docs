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
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.inject.Inject;

import com.googlesource.gerrit.plugins.xdocs.ConfigSection;

import java.io.UnsupportedEncodingException;

public class MarkdownFormatter implements Formatter {
  public final static String NAME = "MARKDOWN";

  private final FormatterUtil util;

  @Inject
  MarkdownFormatter(FormatterUtil formatterUtil) {
    this.util = formatterUtil;
  }

  @Override
  public String format(String projectName, String revision, ConfigSection cfg,
      String raw) throws UnsupportedEncodingException {
    com.google.gerrit.server.documentation.MarkdownFormatter f =
        new com.google.gerrit.server.documentation.MarkdownFormatter();
    if (!cfg.getBoolean(KEY_ALLOW_HTML, false)) {
      f.suppressHtml();
    }
    // if there is no project-specific CSS and f.setCss(null) is invoked
    // com.google.gerrit.server.documentation.MarkdownFormatter applies the
    // default CSS
    f.setCss(util.getCss(projectName, "markdown"));
    byte[] b = f.markdownToDocHtml(raw, UTF_8.name());
    return new String(b, UTF_8);
  }
}
