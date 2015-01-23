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

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

import com.googlesource.gerrit.plugins.xdocs.ConfigSection;

public class PlainTextFormatter implements StringFormatter {
  public final static String NAME = "PLAIN_TEXT";

  @Override
  public String format(String projectName, String path, String revision,
      String abbrRev, ConfigSection cfg, String raw) {
    return "<pre>" + escapeHtml(raw) + "</pre>";
  }
}
