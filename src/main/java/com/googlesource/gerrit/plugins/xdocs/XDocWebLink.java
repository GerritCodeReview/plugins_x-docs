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
import com.google.gerrit.extensions.webui.BranchWebLink;
import com.google.gerrit.extensions.webui.ProjectWebLink;
import com.google.gerrit.httpd.resources.Resource;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.FileTypeRegistry;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.gerrit.server.git.GitRepositoryManager;
import com.google.gerrit.server.project.ProjectCache;
import com.google.gerrit.server.project.ProjectState;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import com.googlesource.gerrit.plugins.xdocs.XDocGlobalConfig.Formatter;

import eu.medsea.mimeutil.MimeType;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Singleton
public class XDocWebLink implements ProjectWebLink, BranchWebLink {
  private static final Logger log = LoggerFactory
      .getLogger(XDocWebLink.class);

  private final String pluginName;
  private final GitRepositoryManager repoManager;
  private final LoadingCache<String, Resource> docCache;
  private final XDocProjectConfig.Factory cfgFactory;
  private final ProjectCache projectCache;
  private final FileTypeRegistry fileTypeRegistry;
  private final PluginConfigFactory pluginCfgFactory;

  @Inject
  XDocWebLink(
      @PluginName String pluginName,
      GitRepositoryManager repoManager,
      @Named(XDocLoader.Module.X_DOC_RESOURCES) LoadingCache<String, Resource> cache,
      XDocProjectConfig.Factory cfgFactory,
      ProjectCache projectCache,
      FileTypeRegistry fileTypeRegistry,
      PluginConfigFactory pluginCfgFactory) {
    this.pluginName = pluginName;
    this.repoManager = repoManager;
    this.docCache = cache;
    this.cfgFactory = cfgFactory;
    this.projectCache = projectCache;
    this.fileTypeRegistry = fileTypeRegistry;
    this.pluginCfgFactory = pluginCfgFactory;
  }

  @Override
  public String getLinkName() {
    return "readme";
  }

  @Override
  public String getProjectUrl(String projectName) {
    return getBranchUrl(projectName, Constants.HEAD);
  }

  @Override
  public String getBranchUrl(String projectName, String branchName) {
    ProjectState state = projectCache.get(new Project.NameKey(projectName));
    if (state == null) {
      // project not found -> no link
      return null;
    }
    return getPatchUrl(projectName, branchName,
        cfgFactory.create(state).getIndexFile());
  }

  public String getPatchUrl(String projectName, String revision,
      String fileName) {
    XDocGlobalConfig pluginCfg =
        new XDocGlobalConfig(pluginCfgFactory.getGlobalPluginConfig(pluginName));
    MimeType mimeType = fileTypeRegistry.getMimeType(fileName, null);
    Formatter formatter = pluginCfg.getMimeTypes().get(mimeType);
    if (formatter == null) {
      return null;
    }

    Project.NameKey p = new Project.NameKey(projectName);
    try {
      Repository repo = repoManager.openRepository(p);
      try {
        ObjectId revId = repo.resolve(revision);
        if (revId == null) {
          return null;
        }
        Resource rsc = docCache.getUnchecked(
           (new XDocResourceKey(formatter, p, fileName, revId)).asString());
        if (rsc != Resource.NOT_FOUND) {
          StringBuilder url = new StringBuilder();
          url.append("plugins/");
          url.append(pluginName);
          url.append(XDocServlet.PATH_PREFIX);
          url.append(Url.encode(projectName));
          if (revision != null && !Constants.HEAD.equals(revision)) {
            url.append("/rev/");
            url.append(Url.encode(revision));
          }
          url.append("/");
          url.append(Url.encode(fileName));
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

  @Override
  public String getTarget() {
    return Target.BLANK;
  }
}
