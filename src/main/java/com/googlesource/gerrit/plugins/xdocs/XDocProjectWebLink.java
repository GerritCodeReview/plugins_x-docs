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
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.extensions.restapi.Url;
import com.google.gerrit.extensions.webui.ProjectWebLink;
import com.google.gerrit.httpd.resources.Resource;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.git.GitRepositoryManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Singleton
public class XDocProjectWebLink implements ProjectWebLink {
  private static final Logger log = LoggerFactory
      .getLogger(XDocProjectWebLink.class);

  private final String pluginName;
  private final GitRepositoryManager repoManager;
  private final LoadingCache<String, Resource> docCache;

  @Inject
  XDocProjectWebLink(
      @PluginName String pluginName,
      GitRepositoryManager repoManager,
      @Named(XDocLoader.Module.X_DOC_RESOURCES) LoadingCache<String, Resource> cache) {
    this.pluginName = pluginName;
    this.repoManager = repoManager;
    this.docCache = cache;
  }

  @Override
  public String getLinkName() {
    return "readme";
  }

  @Override
  public String getProjectUrl(String projectName) {
    Project.NameKey p = new Project.NameKey(projectName);
    try {
      Repository repo = repoManager.openRepository(p);
      try {
        ObjectId revId = repo.resolve(Constants.HEAD);
        if (revId == null) {
          return null;
        }
        Resource rsc = docCache.getUnchecked(
           (new XDocResourceKey(p, "README.md", revId)).asString());
        if (rsc != Resource.NOT_FOUND) {
          StringBuilder url = new StringBuilder();
          url.append("plugins/");
          url.append(pluginName);
          url.append(XDocServlet.PATH_PREFIX);
          url.append(Url.encode(projectName));
          url.append("/README.md");
          return url.toString();
        } else {
          return null;
        }
      } finally {
        repo.close();
      }
    } catch (IOException e) {
      log.error("Failed to check for project documentation", e);
      return null;
    }
  }

  @Override
  public String getImageUrl() {
    return "plugins/" + pluginName + "/static/readme.png";
  }
}
