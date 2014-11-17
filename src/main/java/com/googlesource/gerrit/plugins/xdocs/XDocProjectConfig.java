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

import com.google.common.base.MoreObjects;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.gerrit.server.project.ProjectState;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.jgit.lib.Config;

public class XDocProjectConfig {
  interface Factory {
    XDocProjectConfig create(ProjectState project);
  }

  private static final String SECTION_WEB = "web";
  private static final String KEY_INDEX_BRANCH = "indexBranch";
  private static final String KEY_INDEX_FILE = "indexFile";
  private static final String DEFAULT_INDEX_FILE = "README.md";

  private final Config cfg;

  @Inject
  XDocProjectConfig(@PluginName String pluginName, PluginConfigFactory cfgFactory,
      @Assisted ProjectState project) {
    this.cfg = cfgFactory.getProjectPluginConfigWithInheritance(project, pluginName);
  }

  String getIndexBranch() {
    return cfg.getString(SECTION_WEB, null, KEY_INDEX_BRANCH);
  }

  String getIndexFile() {
    return MoreObjects.firstNonNull(
        cfg.getString(SECTION_WEB, null, KEY_INDEX_FILE),
        DEFAULT_INDEX_FILE);
  }
}
