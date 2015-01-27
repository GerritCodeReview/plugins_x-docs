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

import com.google.gerrit.extensions.common.DiffWebLinkInfo;
import com.google.gerrit.extensions.webui.DiffWebLink;
import com.google.inject.Inject;

import com.googlesource.gerrit.plugins.xdocs.formatter.Formatters;
import com.googlesource.gerrit.plugins.xdocs.formatter.Formatters.FormatterProvider;

public class SideBySideDiffPreviewWebLink implements DiffWebLink {
  private static final String SIDE_BY_SIDE_PREVIEW_DIFF = "side-by-side preview diff";

  private final PreviewDiffUrl previewDiffUrl;
  private final Formatters formatters;

  @Inject
  SideBySideDiffPreviewWebLink(
      PreviewDiffUrl previewDiffUrl,
      Formatters formatters) {
    this.previewDiffUrl = previewDiffUrl;
    this.formatters = formatters;
  }

  @Override
  public DiffWebLinkInfo getDiffLink(String projectName, int changeId,
      Integer patchSetIdA, String revisionA, String pathA, int patchSetIdB,
      String revisionB, String pathB) {
    FormatterProvider formatter = formatters.get(projectName, pathB);
    if (formatter == null) {
      return null;
    }

    return DiffWebLinkInfo.forSideBySideDiffView(SIDE_BY_SIDE_PREVIEW_DIFF,
        previewDiffUrl.getSideBySideIconUrl(),
        previewDiffUrl.getSideBySideUrl(changeId, patchSetIdA, patchSetIdB, pathB),
        Target.SELF);
  }
}
