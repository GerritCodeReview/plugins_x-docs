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

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.gerrit.extensions.restapi.AuthException;
import com.google.gerrit.extensions.restapi.IdString;
import com.google.gerrit.extensions.restapi.ResourceNotFoundException;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.reviewdb.server.ReviewDb;
import com.google.gerrit.server.config.CanonicalWebUrl;
import com.google.gerrit.server.documentation.MarkdownFormatter;
import com.google.gerrit.server.git.GitRepositoryManager;
import com.google.gerrit.server.project.GetHead;
import com.google.gerrit.server.project.NoSuchProjectException;
import com.google.gerrit.server.project.ProjectCache;
import com.google.gerrit.server.project.ProjectControl;
import com.google.gerrit.server.project.ProjectResource;
import com.google.gwtexpui.server.CacheHeaders;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
public class XDocServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  public  static final String PATH_PREFIX = "/project/";

  private static final String DEFAULT_HOST = "review.example.com";

  private final Provider<ReviewDb> db;
  private final ProjectControl.Factory projectControlFactory;
  private final ProjectCache projectCache;
  private final Provider<GetHead> getHead;
  private final GitRepositoryManager repoManager;
  private final Provider<String> webUrl;

  @Inject
  XDocServlet(Provider<ReviewDb> db,
      ProjectControl.Factory projectControlFactory,
      ProjectCache projectCache,
      Provider<GetHead> getHead,
      GitRepositoryManager repoManager,
      @CanonicalWebUrl Provider<String> webUrl) {
    this.db = db;
    this.projectControlFactory = projectControlFactory;
    this.projectCache = projectCache;
    this.getHead = getHead;
    this.repoManager = repoManager;
    this.webUrl = webUrl;
  }

  @Override
  public void service(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    if (!"GET".equals(req.getMethod()) && !"HEAD".equals(req.getMethod())) {
      CacheHeaders.setNotCacheable(res);
      res.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
      return;
    }

    ResourceKey key = ResourceKey.fromPath(req.getPathInfo());
    if (projectCache.get(key.project) == null) {
      notFound(res);
      return;
    }
    if (key.file == null) {
      res.sendRedirect(getRedirectUrl(req, key));
      return;
    }
    if (!key.file.endsWith(".md")) {
      notFound(res);
      return;
    }

    try {
      ProjectControl projectControl = projectControlFactory.validateFor(key.project);
      String rev = key.revision;
      if (rev == null || Constants.HEAD.equals(rev)) {
        rev = getHead.get().apply(new ProjectResource(projectControl));
      } else  {
        if (!ObjectId.isId(rev)) {
          if (!rev.startsWith(Constants.R_REFS)) {
            rev = Constants.R_HEADS + rev;
          }
          if (!projectControl.controlForRef(rev).isVisible()) {
            notFound(res);
            return;
          }
        }
      }

      Repository repo = repoManager.openRepository(key.project);
      try {
        RevWalk rw = new RevWalk(repo);
        try {
          ObjectId revId = repo.resolve(rev);
          if (revId == null) {
            notFound(res);
            return;
          }
          RevCommit commit = rw.parseCommit(repo.resolve(rev));

          if (ObjectId.isId(rev)
              && !projectControl.canReadCommit(db.get(), rw, commit)) {
            notFound(res);
            return;
          }

          RevTree tree = commit.getTree();
          TreeWalk tw = new TreeWalk(repo);
          try {
            tw.addTree(tree);
            tw.setRecursive(true);
            tw.setFilter(PathFilter.create(key.file));
            if (!tw.next()) {
              notFound(res);
              return;
            }
            ObjectId objectId = tw.getObjectId(0);
            ObjectLoader loader = repo.open(objectId);
            byte[] md = loader.getBytes(Integer.MAX_VALUE);
            sendMarkdownAsHtml(new String(md, UTF_8),
                commit.getCommitTime(), res);
          } finally {
            tw.release();
          }
        } finally {
          rw.release();
        }
      } finally {
        repo.close();
      }
    } catch (RepositoryNotFoundException | NoSuchProjectException
        | ResourceNotFoundException | AuthException | RevisionSyntaxException e) {
      notFound(res);
      return;
    }
  }

  private void sendMarkdownAsHtml(String md, int lastModified, HttpServletResponse res)
      throws IOException {
    byte[] html = new MarkdownFormatter().suppressHtml()
        .markdownToDocHtml(replaceMacros(md), UTF_8.name());
    res.setDateHeader("Last-Modified", lastModified);
    res.setContentType("text/html");
    res.setCharacterEncoding(UTF_8.name());
    res.setContentLength(html.length);
    res.getOutputStream().write(html);
  }

  private String replaceMacros(String md) {
    Map<String, String> macros = Maps.newHashMap();
    String url = webUrl.get();
    if (Strings.isNullOrEmpty(url)) {
      url = "http://" + DEFAULT_HOST + "/";
    }
    macros.put("URL", url);

    Matcher m = Pattern.compile("(\\\\)?@([A-Z_]+)@").matcher(md);
    StringBuffer sb = new StringBuffer();
    while (m.find()) {
      String key = m.group(2);
      String val = macros.get(key);
      if (m.group(1) != null) {
        m.appendReplacement(sb, "@" + key + "@");
      } else if (val != null) {
        m.appendReplacement(sb, val);
      } else {
        m.appendReplacement(sb, "@" + key + "@");
      }
    }
    m.appendTail(sb);
    return sb.toString();
  }

  private static void notFound(HttpServletResponse res) throws IOException {
    CacheHeaders.setNotCacheable(res);
    res.sendError(HttpServletResponse.SC_NOT_FOUND);
  }

  private String getRedirectUrl(HttpServletRequest req, ResourceKey key) {
    StringBuilder redirectUrl = new StringBuilder();
    redirectUrl.append(req.getRequestURL().substring(0,
        req.getRequestURL().length() - req.getRequestURI().length()));
    redirectUrl.append(req.getContextPath());
    redirectUrl.append(PATH_PREFIX);
    redirectUrl.append(key.project);
    redirectUrl.append("/");
    if (key.revision != null) {
      redirectUrl.append("rev/");
      redirectUrl.append(key.revision);
      redirectUrl.append("/");
    }
    redirectUrl.append("README.md");
    return redirectUrl.toString();
  }

  private static class ResourceKey {
    final Project.NameKey project;
    final String file;
    final String revision;

    static ResourceKey fromPath(String path) {
      String project;
      String file = null;
      String revision = null;

      if (!path.startsWith(PATH_PREFIX)) {
        // should not happen since this servlet is only registered to handle
        // paths that start with this prefix
        throw new IllegalStateException("path must start with '" + PATH_PREFIX + "'");
      }
      path = path.substring(PATH_PREFIX.length());

      int i = path.indexOf('/');
      if (i != -1 && i != path.length() - 1) {
        project = IdString.fromUrl(path.substring(0, i)).get();
        String rest = path.substring(i + 1);

        if (rest.startsWith("rev/")) {
          if (rest.length() > 4) {
            rest = rest.substring(4);
            i = rest.indexOf('/');
            if (i != -1 && i != path.length() - 1) {
              revision = IdString.fromUrl(rest.substring(0, i)).get();
              file = IdString.fromUrl(rest.substring(i + 1)).get();
            } else {
              revision = IdString.fromUrl(rest).get();
            }
          }
        } else {
          file = IdString.fromUrl(rest).get();
        }

      } else {
        project = CharMatcher.is('/').trimTrailingFrom(path);
      }

      return new ResourceKey(project, file, revision);
    }

    private ResourceKey(String p, String f, String r) {
      project = new Project.NameKey(p);
      file = f;
      revision = r;
    }
  }
}
