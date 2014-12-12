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

import static com.googlesource.gerrit.plugins.xdocs.XDocGlobalConfig.KEY_CSS_THEME;
import static com.googlesource.gerrit.plugins.xdocs.XDocGlobalConfig.KEY_INHERIT_CSS;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.gerrit.extensions.annotations.PluginData;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.reviewdb.client.RefNames;
import com.google.gerrit.server.config.CanonicalWebUrl;
import com.google.gerrit.server.git.GitRepositoryManager;
import com.google.gerrit.server.project.ProjectCache;
import com.google.gerrit.server.project.ProjectState;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import com.googlesource.gerrit.plugins.xdocs.ConfigSection;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.TemporaryBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Singleton
public class FormatterUtil {
  private static final Logger log = LoggerFactory.getLogger(FormatterUtil.class);

  private final String pluginName;
  private final File baseDir;
  private final GitRepositoryManager repoManager;
  private final ProjectCache projectCache;
  private final Formatters formatters;
  private final Map<String, String> defaultCss;
  private final Map<String, String> resources;
  private final String webUrl;

  @Inject
  FormatterUtil(@PluginName String pluginName,
      @PluginData File baseDir,
      GitRepositoryManager repoManager,
      ProjectCache projectCache,
      Formatters formatters,
      @CanonicalWebUrl Provider<String> webUrl) {
    this.pluginName = pluginName;
    this.baseDir = baseDir;
    this.repoManager = repoManager;
    this.projectCache = projectCache;
    this.formatters = formatters;
    this.defaultCss = new HashMap<>();
    this.resources = new HashMap<>();
    this.webUrl = webUrl.get();
  }

  /**
   * Returns the CSS from the file "<plugin-name>/<name>-<theme>.css" in the
   * refs/meta/config branch of the project.
   *
   * If theme is <code>null</code> or empty, the CSS from the file
   * "<plugin-name>/<name>.css" is returned.
   *
   * @param name the name of the file in the "<plugin-name>/" folder without
   *        theme and without the ".css" file extension
   * @param theme the name of the CSS theme, may be <code>null</code>, if given
   *        it is included into the CSS file name: '<name>-<theme>.css'
   * @return the CSS from the file; HTML characters are escaped;
   *         <code>null</code> if the file doesn't exist
   */
  public String getCss(String projectName, String name, String theme) {
    return Strings.isNullOrEmpty(theme)
        ? getCss(projectName, name)
        : getCss(projectName, name + "-" + theme);
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
   * Returns the inherited CSS.
   *
   * If the project has a parent project the CSS of the parent project is
   * returned; if there is no parent project the global CSS is returned.
   *
   * @param projectName the name of the project
   * @param formatterName the name of the formatter for which the CSS should be
   *        returned
   * @param name the name of the CSS file without theme and without the ".css"
   *        file extension
   * @param theme the name of the CSS theme, may be <code>null</code>, if given
   *        it is included into the CSS file name: '<name>-<theme>.css'
   * @return the inherited CSS; HTML characters are escaped; <code>null</code>
   *         if there is no inherited CSS
   * @throws IOException thrown in case of an I/O Error while reading the global
   *         CSS file
   */
  public String getInheritedCss(String projectName, String formatterName,
      String name, String theme) throws IOException {
    return getInheritedCss(projectCache.get(new Project.NameKey(projectName)),
        formatterName, name, theme);
  }

  private String getInheritedCss(ProjectState project, String formatterName,
    String name, String theme) throws IOException {
    for (ProjectState parent : project.parents()) {
      String css = getCss(parent.getProject().getName(), name, theme);
      ConfigSection cfg =
          formatters.getFormatterConfig(formatterName, parent);
      if (cfg.getBoolean(KEY_INHERIT_CSS, true)) {
        return joinCss(getInheritedCss(parent, formatterName, name, theme), css);
      } else {
        return css;
      }
    }
    return getGlobalCss(name, theme);
  }

  private String joinCss(String css1, String css2) {
    if (css1 == null) {
      return css2;
    }
    if (css2 == null) {
      return css1;
    }
    return Joiner.on('\n').join(css1, css2);
  }

  /**
   * Returns the CSS from the file
   * "<review-site>/data/<plugin-name>/css/<name>-<theme>.css".
   *
   * If theme is <code>null</code> or empty, the CSS from the file
   * "<review-site>/data/<plugin-name>/css/<name>.css" is returned.
   *
   * @param name the name of the CSS file without theme and without the ".css"
   *        file extension
   * @param theme the name of the CSS theme, may be <code>null</code>, if given
   *        it is included into the CSS file name: '<name>-<theme>.css'
   * @return the CSS from the file; HTML characters are escaped;
   *         <code>null</code> if the file doesn't exist
   * @throws IOException thrown in case of an I/O Error while reading the CSS
   *         file
   */
  public String getGlobalCss(String name, String theme) throws IOException {
    return Strings.isNullOrEmpty(theme)
        ? getGlobalCss(name)
        : getGlobalCss(name + "-" + theme);
  }

  /**
   * Returns the CSS from the file
   * "<review-site>/data/<plugin-name>/css/<name>.css".
   *
   * @param name the name of the CSS file without the ".css" file extension
   * @return the CSS from the file; HTML characters are escaped;
   *         <code>null</code> if the file doesn't exist
   * @throws IOException thrown in case of an I/O Error while reading the CSS
   *         file
   */
  public String getGlobalCss(String name) throws IOException {
    Path p = Paths.get(baseDir.getAbsolutePath(), "css", name + ".css");
    if (Files.exists(p)) {
      byte[] css = Files.readAllBytes(p);
      return escapeHtml(new String(css, UTF_8));
    }
    return null;
  }

  public String applyCss(String html, String formatterName, String projectName)
      throws IOException {
    ConfigSection projectCfg =
        formatters.getFormatterConfig(formatterName, projectName);
    String cssName = formatterName.toLowerCase(Locale.US);
    String cssTheme = projectCfg.getString(KEY_CSS_THEME);
    String defaultCss = getDefaultCss(formatterName);
    String inheritedCss =
        getInheritedCss(projectName, formatterName, cssName, cssTheme);
    String projectCss = getCss(projectName, cssName, cssTheme);
    if (projectCfg.getBoolean(KEY_INHERIT_CSS, true)) {
      return insertCss(html,
          MoreObjects.firstNonNull(inheritedCss, defaultCss), projectCss);
    } else {
      return insertCss(html,
          MoreObjects.firstNonNull(projectCss,
              MoreObjects.firstNonNull(inheritedCss, defaultCss)));
    }
  }

  private String getDefaultCss(String formatterName) throws IOException {
    String css = defaultCss.get(formatterName) ;
    if (css == null) {
      URL url = FormatterUtil.class.getResource(
          formatterName.toLowerCase(Locale.US) + ".css");
      if (url != null) {
        try (InputStream in = url.openStream();
            TemporaryBuffer.Heap tmp = new TemporaryBuffer.Heap(128 * 1024)) {
          tmp.copy(in);
          css = new String(tmp.toByteArray(), UTF_8);
        }
      } else {
        log.info(String.format("No default CSS for formatter '%s' found.",
            formatterName));
        css = "";
      }
      defaultCss.put(formatterName, css);
    }
    return css;
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

  public String applyInsertAnchorsScript(String html) throws IOException {
    return insertJavaScript(html, getResource("insert-anchors.js"));
  }

  public String getResource(String resourceName) throws IOException {
     String resource = resources.get(resourceName);
     if (resource == null) {
       resource = readResource(resourceName);
       resources.put(resourceName, resource);
     }
     return resource;
  }

  private String readResource(String resourceName) throws IOException {
    URL url = FormatterUtil.class.getResource(resourceName);
    try (InputStream in = url.openStream();
        TemporaryBuffer.Heap tmp = new TemporaryBuffer.Heap(128 * 1024)) {
      tmp.copy(in);
      String resource = new String(tmp.toByteArray(), UTF_8);
      return resource.replaceAll("@URL@", webUrl);
    }
  }

  public String insertJavaScript(String html, String script) {
    return insertScript(html, script, "text/javascript");
  }

  public String insertScript(String html, String script, String type) {
    if (html == null || script == null) {
      return html;
    }

    int p = html.lastIndexOf("</html>");
    if (p > 0) {
      StringBuilder b = new StringBuilder();
      b.append(html.substring(0, p));
      b.append("<script type=\"" + type + "\">\n");
      b.append(script);
      b.append("</script>\n");
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
          ObjectId id = repo.resolve(RefNames.REFS_CONFIG);
          if (id == null) {
            return null;
          }
          RevCommit commit = rw.parseCommit(id);
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
