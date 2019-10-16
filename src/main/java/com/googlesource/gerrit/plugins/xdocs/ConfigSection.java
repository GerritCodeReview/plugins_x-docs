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
import java.util.Set;
import org.eclipse.jgit.lib.Config;

public class ConfigSection {
  private final Config cfg;
  private final String section;
  private final String subsection;

  ConfigSection(Config cfg, String section) {
    this(cfg, section, null);
  }

  ConfigSection(Config cfg, String section, String subsection) {
    this.cfg = cfg;
    this.section = section;
    this.subsection = subsection;
  }

  public String getSubsection() {
    return subsection;
  }

  public String getString(String name) {
    return cfg.getString(section, subsection, name);
  }

  public String getString(String name, String defaultValue) {
    if (defaultValue == null) {
      return cfg.getString(section, subsection, name);
    } else {
      return MoreObjects.firstNonNull(cfg.getString(section, subsection, name), defaultValue);
    }
  }

  public String[] getStringList(String name) {
    return cfg.getStringList(section, subsection, name);
  }

  public int getInt(String name, int defaultValue) {
    return cfg.getInt(section, subsection, name, defaultValue);
  }

  public long getLong(String name, long defaultValue) {
    return cfg.getLong(section, subsection, name, defaultValue);
  }

  public boolean getBoolean(String name, boolean defaultValue) {
    return cfg.getBoolean(section, subsection, name, defaultValue);
  }

  public <T extends Enum<?>> T getEnum(String name, T defaultValue) {
    return cfg.getEnum(section, subsection, name, defaultValue);
  }

  public <T extends Enum<?>> T getEnum(T[] all, String name, T defaultValue) {
    return cfg.getEnum(all, section, subsection, name, defaultValue);
  }

  public Set<String> getNames() {
    return cfg.getNames(section, subsection);
  }
}
