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

import static com.google.gerrit.pgm.init.api.InitUtil.extract;

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.pgm.init.api.ConsoleUI;
import com.google.gerrit.pgm.init.api.InitStep;
import com.google.gerrit.server.config.SitePaths;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.File;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.util.FS;

@Singleton
public class XDocInit implements InitStep {
  private final String pluginName;
  private final SitePaths sitePaths;
  private final ConsoleUI ui;

  @Inject
  XDocInit(@PluginName String pluginName, SitePaths sitePaths, ConsoleUI ui) {
    this.pluginName = pluginName;
    this.sitePaths = sitePaths;
    this.ui = ui;
  }

  @Override
  public void run() throws Exception {
    File pluginConfig = new File(sitePaths.etc_dir.toFile(), pluginName + ".config");
    if (!pluginConfig.exists()) {
      ui.message("\n");
      ui.header("%s plugin", pluginName);

      FileBasedConfig cfg = new FileBasedConfig(pluginConfig, FS.DETECTED);
      XDocGlobalConfig.initialize(cfg);
      cfg.save();

      ui.message("Initialized %s plugin: %s", pluginName, pluginConfig.getAbsolutePath());
    }

    extract(
        new File(sitePaths.static_dir.toFile(), "xdocs/css/unified.css").toPath(),
        XDocInit.class,
        "diff/unified.css");
    extract(
        new File(sitePaths.static_dir.toFile(), "xdocs/css/sidebyside-a.css").toPath(),
        XDocInit.class,
        "diff/sidebyside-a.css");
    extract(
        new File(sitePaths.static_dir.toFile(), "xdocs/css/sidebyside-b.css").toPath(),
        XDocInit.class,
        "diff/sidebyside-b.css");
  }

  @Override
  public void postRun() throws Exception {}
}
