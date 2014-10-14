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

import com.google.common.collect.Lists;
import com.google.gerrit.extensions.annotations.Exports;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.extensions.registration.DynamicMap;
import com.google.gerrit.extensions.registration.DynamicSet;
import com.google.gerrit.extensions.webui.BranchWebLink;
import com.google.gerrit.extensions.webui.FileWebLink;
import com.google.gerrit.extensions.webui.GerritTopMenu;
import com.google.gerrit.extensions.webui.ProjectWebLink;
import com.google.gerrit.extensions.webui.TopMenu;
import com.google.gerrit.server.config.FactoryModule;
import com.google.inject.Inject;

import com.googlesource.gerrit.plugins.xdocs.formatter.AsciidoctorFormatter;
import com.googlesource.gerrit.plugins.xdocs.formatter.Formatter;
import com.googlesource.gerrit.plugins.xdocs.formatter.MarkdownFormatter;
import com.googlesource.gerrit.plugins.xdocs.formatter.PlainTextFormatter;

import java.util.List;

public class Module extends FactoryModule {
  private final String pluginName;

  @Inject
  Module(@PluginName String pluginName) {
    this.pluginName = pluginName;
  }

  @Override
  protected void configure() {
    install(new XDocLoader.Module());
    factory(XDocProjectConfig.Factory.class);

    DynamicMap.mapOf(binder(), Formatter.class);
    bind(Formatter.class)
        .annotatedWith(Exports.named(AsciidoctorFormatter.NAME))
        .to(AsciidoctorFormatter.class);
    bind(Formatter.class)
        .annotatedWith(Exports.named(MarkdownFormatter.NAME))
        .to(MarkdownFormatter.class);
    bind(Formatter.class)
        .annotatedWith(Exports.named(PlainTextFormatter.NAME))
        .to(PlainTextFormatter.class);

    DynamicSet.bind(binder(), ProjectWebLink.class)
        .to(XDocWebLink.class);
    DynamicSet.bind(binder(), BranchWebLink.class)
        .to(XDocWebLink.class);
    DynamicSet.bind(binder(), FileWebLink.class)
        .to(XDocFileWebLink.class);

    DynamicSet.bind(binder(), TopMenu.class).toInstance(new TopMenu() {
      @Override
      public List<MenuEntry> getEntries() {
        StringBuilder url = new StringBuilder();
        url.append("/plugins/");
        url.append(pluginName);
        url.append(XDocServlet.PATH_PREFIX);
        url.append("${projectName}");
        return Lists.newArrayList(new MenuEntry(GerritTopMenu.PROJECTS,
            Lists.newArrayList(new MenuItem("Readme", url.toString()))));
      }
    });
  }
}