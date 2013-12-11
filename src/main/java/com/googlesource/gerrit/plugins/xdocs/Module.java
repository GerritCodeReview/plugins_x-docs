// Copyright (C) 2013 The Android Open Source Project
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

import com.google.gerrit.extensions.annotations.Exports;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.extensions.registration.DynamicMap;
import com.google.gerrit.extensions.registration.DynamicSet;
import com.google.gerrit.extensions.webui.GerritTopMenu;
import com.google.gerrit.extensions.webui.TopMenu;
import com.google.gerrit.extensions.webui.TopMenu.MenuItem;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import org.eclipse.jgit.lib.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Module extends AbstractModule {
  private static final Logger log = LoggerFactory.getLogger(Module.class);

  public static final String DOC = "doc";
  private static final String KEY_NAME = "name";
  private static final String KEY_PROJECT = "project";
  private static final String KEY_BRANCH = "branch";

  private final String pluginName;
  private final Config cfg;

  @Inject
  Module(@PluginName String pluginName, PluginConfigFactory cfgFactory) {
    this.pluginName = pluginName;
    this.cfg = cfgFactory.getGlobalPluginConfig(pluginName);
  }

  @Override
  protected void configure() {
    DynamicMap.mapOf(binder(), DocumentationExtension.class);

    final List<MenuItem> menuItems = new ArrayList<>();
    for (String id : cfg.getSubsections(DOC)) {
      String name = cfg.getString(DOC, id, KEY_NAME);
      if (name == null) {
        name = id;
      }

      String project = cfg.getString(DOC, id, KEY_PROJECT);
      if (project == null) {
        log.warn("Ignoring documentation extension '" + id + "': No project specified");
        continue;
      }

      String branch = cfg.getString(DOC, id, KEY_BRANCH);
      if (branch == null) {
        branch = "refs/heads/master";
      }

      bind(DocumentationExtension.class).annotatedWith(Exports.named(id))
          .toInstance(new DocumentationExtension(
              name, new Project.NameKey(project), branch));

      menuItems.add(new MenuItem(name, "plugins/" + pluginName + "/" + id));
    }
    DynamicSet.bind(binder(), TopMenu.class).toInstance(new TopMenu() {
      @Override
      public List<MenuEntry> getEntries() {
        return Collections.singletonList(new MenuEntry(GerritTopMenu.DOCUMENTATION, menuItems));
      }
    });
  }
}
