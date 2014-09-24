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

import com.google.common.cache.LoadingCache;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.extensions.webui.FileWebLink;
import com.google.gerrit.httpd.resources.Resource;
import com.google.gerrit.server.git.GitRepositoryManager;
import com.google.gerrit.server.project.ProjectCache;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class XDocPatchWebLink extends XDocWebLink implements FileWebLink {

  @Inject
  XDocPatchWebLink(
      @PluginName String pluginName,
      GitRepositoryManager repoManager,
      @Named(XDocLoader.Module.X_DOC_RESOURCES) LoadingCache<String, Resource> cache,
      XDocConfig.Factory cfgFactory,
      ProjectCache projectCache) {
    super(pluginName, repoManager, cache, cfgFactory, projectCache);
  }

  @Override
  public String getLinkName() {
    return "preview";
  }

  @Override
  public String getFileUrl(String projectName, String revision,
      String fileName) {
    return super.getPatchUrl(projectName, revision, fileName);
  }
}
