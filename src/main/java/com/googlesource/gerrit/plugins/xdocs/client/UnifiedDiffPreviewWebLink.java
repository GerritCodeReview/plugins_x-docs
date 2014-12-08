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

package com.googlesource.gerrit.plugins.xdocs.client;

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.extensions.common.DiffWebLinkInfo;
import com.google.gerrit.extensions.webui.DiffWebLink;
import com.google.inject.Inject;

import com.googlesource.gerrit.plugins.xdocs.formatter.Formatters;
import com.googlesource.gerrit.plugins.xdocs.formatter.Formatters.FormatterProvider;

public class UnifiedDiffPreviewWebLink implements DiffWebLink {
  private static final String UNIFIED_PREVIEW_DIFF = "unified preview diff";

  private final String pluginName;
  private final Formatters formatters;

  @Inject
  UnifiedDiffPreviewWebLink(
      @PluginName String pluginName,
      Formatters formatters) {
    this.pluginName = pluginName;
    this.formatters = formatters;
  }

  @Override
  public DiffWebLinkInfo getDiffLink(String projectName, int changeId,
      Integer patchSetIdA, String revisionA, String fileNameA, int patchSetIdB,
      String revisionB, String fileNameB) {
    FormatterProvider formatter = formatters.get(projectName, fileNameB);
    if (formatter == null) {
      return null;
    }

    return DiffWebLinkInfo.forUnifiedDiffView(UNIFIED_PREVIEW_DIFF,
        "plugins/" + pluginName + "/static/unifiedDiffPreview.png",
        getUrl(pluginName, changeId, patchSetIdA, patchSetIdB, fileNameB),
        Target.SELF);
  }

  private static String getUrl(String pluginName, int changeId,
      Integer patchSetIdA, int patchSetIdB, String fileName) {
    return SideBySideDiffPreviewWebLink.getUrl(pluginName, changeId,
        patchSetIdA, patchSetIdB, fileName) + ",unified";
  }
}
