// Copyright (C) 2015 The Android Open Source Project
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

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.inject.Inject;

public class PreviewDiffUrl {
  private final String pluginName;

  @Inject
  PreviewDiffUrl(@PluginName String pluginName) {
    this.pluginName = pluginName;
  }

  public String getSideBySideUrl(int changeId, Integer patchSetIdA, int patchSetIdB, String path) {
    return getUrl(changeId, patchSetIdA, patchSetIdB, path, false);
  }

  public String getSideBySideIconUrl() {
    return "plugins/" + pluginName + "/static/sideBySideDiffPreview.png";
  }

  public String getUnifiedUrl(int changeId, Integer patchSetIdA, int patchSetIdB, String path) {
    return getUrl(changeId, patchSetIdA, patchSetIdB, path, true);
  }

  public String getUnifiedIconUrl() {
    return "plugins/" + pluginName + "/static/unifiedDiffPreview.png";
  }

  private String getUrl(
      int changeId, Integer patchSetIdA, int patchSetIdB, String path, boolean unified) {
    StringBuilder url = new StringBuilder();
    url.append("#/x/");
    url.append(pluginName);
    url.append("/c/");
    url.append(changeId);
    url.append("/");
    if (patchSetIdA != null) {
      url.append(patchSetIdA);
      url.append("..");
    }
    url.append(patchSetIdB);
    url.append("/");
    url.append(path);
    if (unified) {
      url.append(",unified");
    }
    return url.toString();
  }
}
