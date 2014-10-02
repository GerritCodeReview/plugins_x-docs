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

import eu.medsea.mimeutil.MimeType;

import org.eclipse.jgit.lib.Config;

import java.util.HashMap;
import java.util.Map;

public class XDocGlobalConfig {
  private static final String SECTION_FORMATTER = "formatter";
  private static final String KEY_ALLOW_HTML = "allowHtml";
  private static final String KEY_MIME_TYPE = "mimeType";

  enum Formatter {
    MARKDOWN;
  }

  private final Config cfg;

  XDocGlobalConfig(Config cfg) {
    this.cfg = cfg;
  }

  boolean isHtmlAllowed(Formatter formatter) {
    return cfg.getBoolean(SECTION_FORMATTER, formatter.name(),
        KEY_ALLOW_HTML, false);
  }

  Map<MimeType, Formatter> getMimeTypes() {
    Map<MimeType, Formatter> mimeTypes = new HashMap<>();
    for (Formatter f : Formatter.values()) {
      for (String mimeType :
          cfg.getStringList(SECTION_FORMATTER, f.name(), KEY_MIME_TYPE)) {
        mimeTypes.put(new MimeType(mimeType), f);
      }
    }
    return mimeTypes;
  }

  static void initialize(Config cfg) {
    cfg.setString(SECTION_FORMATTER, Formatter.MARKDOWN.name(), KEY_MIME_TYPE,
        "text/x-markdown");
  }
}
