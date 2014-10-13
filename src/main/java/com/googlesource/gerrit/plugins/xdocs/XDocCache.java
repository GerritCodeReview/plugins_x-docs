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
import com.google.gerrit.httpd.resources.Resource;
import com.google.gerrit.reviewdb.client.Project;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import com.googlesource.gerrit.plugins.xdocs.XDocLoader;
import com.googlesource.gerrit.plugins.xdocs.formatter.Formatters.FormatterProvider;

import org.eclipse.jgit.lib.ObjectId;

@Singleton
public class XDocCache {
  private final LoadingCache<String, Resource> cache;

  @Inject
  XDocCache(
      @Named(XDocLoader.Module.X_DOC_RESOURCES) LoadingCache<String, Resource> cache) {
    this.cache = cache;
  }

  public Resource get(FormatterProvider formatter, Project.NameKey project,
      String file, ObjectId revId) {
    return cache.getUnchecked((new XDocResourceKey(formatter.getName(),
        project, file, revId)).asString());
  }
}
