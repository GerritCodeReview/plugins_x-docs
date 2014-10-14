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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.reviewdb.client.RefNames;
import com.google.gerrit.server.git.GitRepositoryManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import java.io.IOException;

@Singleton
public class FormatterUtil {
  private final GitRepositoryManager repoManager;
  private final String pluginName;

  @Inject
  FormatterUtil(@PluginName String pluginName,
      GitRepositoryManager repoManager) {
    this.pluginName = pluginName;
    this.repoManager = repoManager;
  }

  /**
   * Returns the CSS from the file "<plugin-name>/<name>.css" in the
   * refs/meta/config branch of the project.
   *
   * @param name the name of the file in the "<plugin-name>/" folder without the
   *        ".css" file extension
   * @return the CSS from the file; HTML characters are escaped;
   *         <code>null</code> if the file doesn't exist
   */
  public String getCss(String projectName, String name) {
    return escapeHtml(getMetaConfigFile(projectName, name + ".css"));
  }

  /**
   * Inserts the given CSS into the given HTML.
   *
   * @param html the HTML
   * @param css the CSS, may be <code>null</code>
   * @return the HTML that includes the CSS
   */
  public String insertCss(String html, String css) {
    if (html == null || css == null) {
      return html;
    }

    return insertCss(html, css, null);
  }

  /**
   * Inserts the given CSS's into the given HTML.
   *
   * @param html the HTML
   * @param css1 first CSS, may be <code>null</code>
   * @param css2 second CSS, may be <code>null</code>
   * @return the HTML that includes the CSS
   */
  public String insertCss(String html, String css1, String css2) {
    if (html == null || (css1 == null && css2 == null)) {
      return html;
    }

    int p = html.lastIndexOf("</head>");
    if (p > 0) {
      StringBuilder b = new StringBuilder();
      b.append(html.substring(0, p));
      if (css1 != null) {
        b.append("<style type=\"text/css\">\n");
        b.append(css1);
        b.append("</style>\n");
      }
      if (css2 != null) {
        b.append("<style type=\"text/css\">\n");
        b.append(css2);
        b.append("</style>\n");
      }
      b.append(html.substring(p));
      return b.toString();
    } else {
      return html;
    }
  }

  /**
   * Returns the content of the specified file from the "<plugin-name>/" folder
   * of the ref/meta/config branch.
   *
   * @param projectName the name of the project
   * @param fileName the name of the file in the "<plugin-name>/" folder
   * @return the file content, <code>null</code> if the file doesn't exist
   */
  public String getMetaConfigFile(String projectName, String fileName) {
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
            tw.setFilter(PathFilter.create(pluginName + "/" + fileName));
            if (!tw.next()) {
              return null;
            }
            ObjectId objectId = tw.getObjectId(0);
            ObjectLoader loader = repo.open(objectId);
            byte[] raw = loader.getBytes(Integer.MAX_VALUE);
            return new String(raw, UTF_8);
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
