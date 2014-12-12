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

import com.google.gerrit.plugin.client.Plugin;
import com.google.gerrit.plugin.client.PluginEntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;

public class XDocsPlugin extends PluginEntryPoint {
  public static final Resources RESOURCES = GWT.create(Resources.class);

  @Override
  public void onPluginLoad() {
    Plugin.get().screenRegex("project/(.*)/rev/(.*)/([^#]*)(#(.*))?",
        new XDocScreen.Factory());
    Plugin.get().screenRegex("project/(.*)/([^#]*)(#(.*))?",
        new XDocScreen.HeadFactory());
    Plugin.get().screenRegex("c/(.*)/([0-9]+(\\.{2}[0-9]+)?)/(.*),unified",
        new XDocUnifiedDiffScreen.Factory());
    Plugin.get().screenRegex("c/(.*)/([0-9]+(\\.{2}[0-9]+)?)/(.*)",
        new XDocSideBySideDiffScreen.Factory());
  }

  public static String getSideBySideDiffUrl(String changeId,
      Integer patchSetIdA, int patchSetIdB, String fileName) {
    StringBuilder url = new StringBuilder();
    url.append("/c/");
    url.append(changeId);
    url.append("/");
    if (patchSetIdA != null) {
      url.append(patchSetIdA);
      url.append("..");
    }
    url.append(patchSetIdB);
    url.append("/");
    url.append(URL.encode(fileName));
    return url.toString();
  }

  public static String getUnifiedDiffUrl(String changeId, Integer patchSetIdA,
      int patchSetIdB, String fileName) {
    return getSideBySideDiffUrl(changeId, patchSetIdA, patchSetIdB, fileName)
        + ",unified";
  }

  public static String getSideBySidePreviewDiffUrl(String changeId,
      Integer patchSetIdA, int patchSetIdB, String fileName) {
    StringBuilder url = new StringBuilder();
    url.append("/x/");
    url.append(Plugin.get().getPluginName());
    url.append(getSideBySideDiffUrl(changeId, patchSetIdA, patchSetIdB, fileName));
    return url.toString();
  }

  public static String getUnifiedPreviewDiffUrl(String changeId,
      Integer patchSetIdA, int patchSetIdB, String fileName) {
    return getSideBySidePreviewDiffUrl(changeId, patchSetIdA, patchSetIdB,
        fileName) + ",unified";
  }
}
