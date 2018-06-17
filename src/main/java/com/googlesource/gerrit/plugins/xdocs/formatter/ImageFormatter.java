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

package com.googlesource.gerrit.plugins.xdocs.formatter;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.extensions.restapi.Url;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.googlesource.gerrit.plugins.xdocs.ConfigSection;
import com.googlesource.gerrit.plugins.xdocs.XDocServlet;
import java.io.IOException;
import java.io.InputStream;
import org.eclipse.jgit.lib.Constants;

@Singleton
public class ImageFormatter implements StreamFormatter {
  public static final String NAME = "IMAGE";

  private final String pluginName;

  @Inject
  public ImageFormatter(@PluginName String pluginName) {
    this.pluginName = pluginName;
  }

  @Override
  public String format(
      String projectName,
      String path,
      String revision,
      String abbrRev,
      ConfigSection cfg,
      InputStream raw)
      throws IOException {
    return "<img src=\"" + escapeHtml(getUrl(projectName, path, revision)) + "\"/>";
  }

  private String getUrl(String projectName, String path, String revision) {
    StringBuilder url = new StringBuilder();
    url.append("/plugins/");
    url.append(pluginName);
    url.append(XDocServlet.PATH_PREFIX);
    url.append(Url.encode(projectName));
    if (revision != null && !Constants.HEAD.equals(revision)) {
      url.append("/rev/");
      url.append(Url.encode(revision));
    }
    url.append("/");
    url.append(path);
    return url.toString();
  }
}
