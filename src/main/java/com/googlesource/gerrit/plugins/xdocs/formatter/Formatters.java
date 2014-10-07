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

package com.googlesource.gerrit.plugins.xdocs.formatter;

import static com.googlesource.gerrit.plugins.xdocs.XDocGlobalConfig.KEY_MIME_TYPE;

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.extensions.registration.DynamicMap;
import com.google.gerrit.server.FileTypeRegistry;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import com.googlesource.gerrit.plugins.xdocs.XDocGlobalConfig;

import eu.medsea.mimeutil.MimeType;

import java.util.Map.Entry;

@Singleton
public class Formatters {
  private final String pluginName;
  private final PluginConfigFactory pluginCfgFactory;
  private final FileTypeRegistry fileTypeRegistry;
  private final DynamicMap<Formatter> formatters;

  @Inject
  Formatters(
      @PluginName String pluginName,
      PluginConfigFactory pluginCfgFactory,
      FileTypeRegistry fileTypeRegistry,
      DynamicMap<Formatter> formatters) {
    this.pluginName = pluginName;
    this.pluginCfgFactory = pluginCfgFactory;
    this.fileTypeRegistry = fileTypeRegistry;
    this.formatters = formatters;
  }

  public FormatterProvider get(String fileName) {
    XDocGlobalConfig pluginCfg =
        new XDocGlobalConfig(pluginCfgFactory.getGlobalPluginConfig(pluginName));
    MimeType mimeType = fileTypeRegistry.getMimeType(fileName, null);
    for (String pluginName : formatters.plugins()) {
      for (Entry<String, Provider<Formatter>> e :
          formatters.byPlugin(pluginName).entrySet()) {
        for (String configuredMimeType :
          pluginCfg.getFormatterConfig(e.getKey()).getStringList(KEY_MIME_TYPE)) {
          if (mimeType.equals(new MimeType(configuredMimeType))) {
            return new FormatterProvider(e.getKey(), e.getValue());
          }
        }
      }
    }
    return null;
  }

  public FormatterProvider getByName(String formatterName) {
    for (String pluginName : formatters.plugins()) {
      for (Entry<String, Provider<Formatter>> e :
          formatters.byPlugin(pluginName).entrySet()) {
        if (formatterName.equals(e.getKey())) {
          return new FormatterProvider(formatterName, e.getValue());
        }
      }
    }
    return null;
  }

  public static class FormatterProvider {
    private final String name;
    private final Provider<Formatter> formatter;

    FormatterProvider(String name,
        Provider<Formatter> formatter) {
      this.name = name;
      this.formatter = formatter;
    }

    public Formatter get() {
      return formatter.get();
    }

    public String getName() {
      return name;
    }
  }
}
