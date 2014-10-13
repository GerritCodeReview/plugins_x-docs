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

import static com.googlesource.gerrit.plugins.xdocs.XDocGlobalConfig.KEY_ALLOW_HTML;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.reviewdb.client.RefNames;
import com.google.gerrit.server.git.GitRepositoryManager;
import com.google.inject.Inject;

import com.googlesource.gerrit.plugins.xdocs.ConfigSection;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class MarkdownFormatter implements Formatter {
  public final static String NAME = "MARKDOWN";

  private final GitRepositoryManager repoManager;
  private final String pluginName;

  @Inject
  MarkdownFormatter(@PluginName String pluginName,
      GitRepositoryManager repoManager) {
    this.pluginName = pluginName;
    this.repoManager = repoManager;
  }

  @Override
  public String format(String projectName, ConfigSection cfg, String raw)
      throws UnsupportedEncodingException {
    com.google.gerrit.server.documentation.MarkdownFormatter f =
        new com.google.gerrit.server.documentation.MarkdownFormatter();
    if (!cfg.getBoolean(KEY_ALLOW_HTML, false)) {
      f.suppressHtml();
    }
    // if there is no project-specific CSS and f.setCss(null) is invoked
    // com.google.gerrit.server.documentation.MarkdownFormatter applies the
    // default CSS
    f.setCss(getCss(projectName));
    byte[] b = f.markdownToDocHtml(raw, UTF_8.name());
    return new String(b, UTF_8);
  }

  private String getCss(String projectName) {
    try {
      Repository repo =
          repoManager.openRepository(new Project.NameKey(projectName));
      try {
        RevWalk rw = new RevWalk(repo);
        try {
          RevCommit commit = rw.parseCommit(repo.resolve(RefNames.REFS_CONFIG));
          RevTree tree = commit.getTree();
          TreeWalk tw = new TreeWalk(repo);
          try {
            tw.addTree(tree);
            tw.setRecursive(true);
            tw.setFilter(PathFilter.create(pluginName + "/markdown.css"));
            if (!tw.next()) {
              return null;
            }
            ObjectId objectId = tw.getObjectId(0);
            ObjectLoader loader = repo.open(objectId);
            byte[] raw = loader.getBytes(Integer.MAX_VALUE);
            return escapeHtml(new String(raw, UTF_8));
          } finally {
            tw.release();
          }
        } finally {
          rw.release();
        }
      } finally {
        repo.close();
      }
    } catch (IOException e) {
      return null;
    }
  }
}
