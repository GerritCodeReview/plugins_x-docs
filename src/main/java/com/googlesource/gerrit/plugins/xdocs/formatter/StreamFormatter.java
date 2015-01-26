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

import com.googlesource.gerrit.plugins.xdocs.ConfigSection;

import java.io.IOException;
import java.io.InputStream;

public interface StreamFormatter extends Formatter {
  /**
   * Formats the given raw text as html.
   *
   * @param projectName the name of the project that contains the file to be
   *        formatted
   * @param path the file path
   * @param revision the revision from which the file is loaded
   * @param abbrRev the abbreviated revision from which the file is loaded
   * @param cfg the global configuration for this formatter
   * @param raw the raw stream
   * @return the content from the given stream formatted as html
   * @throws IOException thrown if the formatting fails
   */
  public String format(String projectName, String path, String revision,
      String abbrRev, ConfigSection cfg, InputStream raw) throws IOException;
}
