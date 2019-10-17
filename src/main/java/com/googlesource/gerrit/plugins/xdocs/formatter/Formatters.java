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

import static com.googlesource.gerrit.plugins.xdocs.XDocGlobalConfig.KEY_ENABLED;
import static com.googlesource.gerrit.plugins.xdocs.XDocGlobalConfig.KEY_EXT;
import static com.googlesource.gerrit.plugins.xdocs.XDocGlobalConfig.KEY_FORMATTER;
import static com.googlesource.gerrit.plugins.xdocs.XDocGlobalConfig.KEY_MIME_TYPE;
import static com.googlesource.gerrit.plugins.xdocs.XDocGlobalConfig.KEY_PREFIX;
import static com.googlesource.gerrit.plugins.xdocs.XDocGlobalConfig.KEY_PRIO;
import static com.googlesource.gerrit.plugins.xdocs.XDocGlobalConfig.SECTION_FORMATTER;

import com.google.gerrit.common.data.PatchScript.FileMode;
import com.google.gerrit.entities.Project;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.extensions.registration.DynamicMap;
import com.google.gerrit.server.change.FileContentUtil;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.gerrit.server.mime.FileTypeRegistry;
import com.google.gerrit.server.project.ProjectCache;
import com.google.gerrit.server.project.ProjectState;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.googlesource.gerrit.plugins.xdocs.ConfigSection;
import com.googlesource.gerrit.plugins.xdocs.XDocGlobalConfig;
import eu.medsea.mimeutil.MimeType;
import java.util.Map.Entry;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.lib.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class Formatters {
  private static final Logger log = LoggerFactory.getLogger(Formatters.class);

  public static final String RAW_FORMATTER = "RAW";

  private final String pluginName;
  private final PluginConfigFactory pluginCfgFactory;
  private final FileTypeRegistry fileTypeRegistry;
  private final DynamicMap<Formatter> formatters;
  private final ProjectCache projectCache;

  @Inject
  Formatters(
      @PluginName String pluginName,
      PluginConfigFactory pluginCfgFactory,
      FileTypeRegistry fileTypeRegistry,
      DynamicMap<Formatter> formatters,
      ProjectCache projectCache) {
    this.pluginName = pluginName;
    this.pluginCfgFactory = pluginCfgFactory;
    this.fileTypeRegistry = fileTypeRegistry;
    this.formatters = formatters;
    this.projectCache = projectCache;
  }

  public FormatterProvider get(String projectName, String fileName) {
    ProjectState project = projectCache.get(new Project.NameKey(projectName));
    if (project == null) {
      return null;
    }
    return get(project, fileName);
  }

  public FormatterProvider get(ProjectState project, String fileName) {
    XDocGlobalConfig globalCfg =
        new XDocGlobalConfig(pluginCfgFactory.getGlobalPluginConfig(pluginName));
    MimeType mimeType = fileTypeRegistry.getMimeType(fileName, (byte[]) null);
    mimeType =
        new MimeType(
            FileContentUtil.resolveContentType(
                project, fileName, FileMode.FILE, mimeType.toString()));
    String extension = FilenameUtils.getExtension(fileName);
    FormatterProvider formatter = null;
    int formatterPrio = 0;
    for (String pluginName : formatters.plugins()) {
      for (Entry<String, Provider<Formatter>> e : formatters.byPlugin(pluginName).entrySet()) {
        if (!globalCfg.getFormatterConfig(e.getKey()).getBoolean(KEY_ENABLED, true)) {
          continue;
        }
        ConfigSection formatterCfg = getFormatterConfig(e.getKey(), project);
        String[] prefixes = formatterCfg.getStringList(KEY_PREFIX);
        if (prefixes.length > 0) {
          boolean match = false;
          for (String prefix : prefixes) {
            if (fileName.startsWith(prefix)) {
              match = true;
              break;
            }
          }
          if (!match) {
            continue;
          }
        }
        for (String mt : formatterCfg.getStringList(KEY_MIME_TYPE)) {
          MimeType configuredMimeType = new MimeType(mt);
          if (mimeType.equals(configuredMimeType)
              || ("*".equals(configuredMimeType.getSubType())
                  && mimeType.getMediaType().equals(configuredMimeType.getMediaType()))) {
            int prio = formatterCfg.getInt(KEY_PRIO, 0);
            if (formatter == null || prio > formatterPrio) {
              formatter = new FormatterProvider(e.getKey(), e.getValue());
              formatterPrio = prio;
            }
          }
        }
        for (String ext : formatterCfg.getStringList(KEY_EXT)) {
          if (extension.equals(ext) || "*".equals(ext)) {
            int prio = formatterCfg.getInt(KEY_PRIO, 0);
            if (formatter == null || prio > formatterPrio) {
              formatter = new FormatterProvider(e.getKey(), e.getValue());
              formatterPrio = prio;
            }
          }
        }
      }
    }
    return formatter;
  }

  public ConfigSection getFormatterConfig(String formatterName, String projectName) {
    ProjectState project = projectCache.get(new Project.NameKey(projectName));
    if (project == null) {
      return null;
    }
    return getFormatterConfig(formatterName, project);
  }

  public ConfigSection getFormatterConfig(String formatterName, ProjectState project) {
    for (ProjectState p : project.tree()) {
      Config cfg = pluginCfgFactory.getProjectPluginConfig(p, pluginName);
      if (cfg.getSubsections(SECTION_FORMATTER).contains(formatterName)) {
        return new XDocGlobalConfig(cfg).getFormatterConfig(formatterName);
      }
    }

    return new XDocGlobalConfig(pluginCfgFactory.getGlobalPluginConfig(pluginName))
        .getFormatterConfig(formatterName);
  }

  public FormatterProvider getByName(String formatterName) {
    if (formatterName.equals(RAW_FORMATTER)) {
      return resolveRawFormatter();
    }

    for (String pluginName : formatters.plugins()) {
      for (Entry<String, Provider<Formatter>> e : formatters.byPlugin(pluginName).entrySet()) {
        if (formatterName.equals(e.getKey())) {
          return new FormatterProvider(formatterName, e.getValue());
        }
      }
    }
    return null;
  }

  public FormatterProvider getRawFormatter() {
    return getByName(RAW_FORMATTER);
  }

  private FormatterProvider resolveRawFormatter() {
    XDocGlobalConfig globalCfg =
        new XDocGlobalConfig(pluginCfgFactory.getGlobalPluginConfig(pluginName));
    String formatterName =
        globalCfg
            .getFormatterConfig(RAW_FORMATTER)
            .getString(KEY_FORMATTER, PlainTextFormatter.NAME);
    if (formatterName.equals(RAW_FORMATTER)) {
      log.warn(
          String.format(
              "%s plugin: Invalid '%s' formatter configuration, '%s' formatter cannot be set to '%s', using '%s' formatter",
              pluginName, RAW_FORMATTER, RAW_FORMATTER, formatterName, PlainTextFormatter.NAME));
      formatterName = PlainTextFormatter.NAME;
    }
    FormatterProvider formatter = getByName(formatterName);
    if (formatter == null) {
      log.warn(
          String.format(
              "%s plugin: Invalid '%s' formatter configuration, formatter '%s' not found, using '%s' formatter",
              pluginName, RAW_FORMATTER, formatterName, PlainTextFormatter.NAME));
      formatter = getByName(PlainTextFormatter.NAME);
    }
    return new FormatterProvider(RAW_FORMATTER, formatter.formatter);
  }

  public static class FormatterProvider {
    private final String name;
    private final Provider<? extends Formatter> formatter;

    FormatterProvider(String name, Provider<? extends Formatter> formatter) {
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
