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
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.gerrit.httpd.resources.Resource;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.project.ProjectCache;
import com.google.gerrit.server.project.ProjectState;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import com.googlesource.gerrit.plugins.xdocs.formatter.Formatters.FormatterProvider;

import org.eclipse.jgit.lib.ObjectId;

@Singleton
public class XDocCache {
  private final LoadingCache<String, Resource> cache;
  private final ProjectCache projectCache;

  @Inject
  XDocCache(
      @Named(XDocLoader.Module.X_DOC_RESOURCES) LoadingCache<String, Resource> cache,
      ProjectCache projectCache) {
    this.cache = cache;
    this.projectCache = projectCache;
  }

  public Resource get(FormatterProvider formatter, Project.NameKey project,
      String file, ObjectId revId) {
    ProjectState p = projectCache.get(project);
    ObjectId metaConfigRevId = p != null ? p.getConfig().getRevision() : null;
    return cache.getUnchecked((new XDocResourceKey(formatter.getName(),
        project, file, revId, metaConfigRevId, getParentsHash(project)))
        .asString());
  }

  private String getParentsHash(Project.NameKey project) {
    Hasher h = Hashing.md5().newHasher();
    ProjectState p = projectCache.get(project);
    if (p != null) {
      for (ProjectState parent : p.parents()) {
        ObjectId metaConfigRevId = parent.getConfig().getRevision();
        if (metaConfigRevId != null) {
          h.putUnencodedChars(metaConfigRevId.getName());
        }
      }
    }
    return h.hash().toString();
  }
}
