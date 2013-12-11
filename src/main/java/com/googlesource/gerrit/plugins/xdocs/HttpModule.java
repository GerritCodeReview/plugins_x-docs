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

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.inject.Inject;
import com.google.inject.servlet.ServletModule;

import org.eclipse.jgit.lib.Config;

class HttpModule extends ServletModule {
  private final Config cfg;

  @Inject
  HttpModule(@PluginName String pluginName, PluginConfigFactory cfgFactory) {
    this.cfg = cfgFactory.getGlobalPluginConfig(pluginName);
  }

  @Override
  protected void configureServlets() {
    for (String id : cfg.getSubsections(Module.DOC)) {
      serveRegex("^/" + id + "(/.*)?$").with(XDocServlet.class);
    }
  }
}
