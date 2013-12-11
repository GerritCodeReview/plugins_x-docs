// Copyright (C) 2013 The Android Open Source Project
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

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.extensions.registration.DynamicMap;
import com.google.gerrit.server.config.CanonicalWebUrl;
import com.google.gerrit.server.documentation.MarkdownFormatter;
import com.google.gerrit.server.git.GitRepositoryManager;
import com.google.gerrit.server.ssh.SshInfo;
import com.google.gwtexpui.server.CacheHeaders;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
class XDocServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private final String pluginName;
  private final DynamicMap<DocumentationExtension> xDocs;
  private final GitRepositoryManager repoManager;
  private final Provider<String> webUrl;
  private final String sshHost;
  private final int sshPort;

  @Inject
  XDocServlet(@PluginName String pluginName,
      DynamicMap<DocumentationExtension> xDocs,
      GitRepositoryManager repoManager,
      @CanonicalWebUrl Provider<String> webUrl, SshInfo sshInfo) {
    this.pluginName = pluginName;
    this.xDocs = xDocs;
    this.repoManager = repoManager;
    this.webUrl = webUrl;

    String sshHost = "review.example.com";
    int sshPort = 29418;
    if (!sshInfo.getHostKeys().isEmpty()) {
      String host = sshInfo.getHostKeys().get(0).getHost();
      int c = host.lastIndexOf(':');
      if (0 <= c) {
        sshHost = host.substring(0, c);
        sshPort = Integer.parseInt(host.substring(c+1));
      } else {
        sshHost = host;
        sshPort = 22;
      }
    }
    this.sshHost = sshHost;
    this.sshPort = sshPort;
  }

  @Override
  public void service(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    if (!"GET".equals(req.getMethod()) && !"HEAD".equals(req.getMethod())) {
      CacheHeaders.setNotCacheable(res);
      res.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
      return;
    }

    String uri = req.getRequestURI();
    String ctx = req.getContextPath();
    if (uri.length() <= ctx.length()) {
      notFound(res);
      return;
    }

    String path = uri.substring(ctx.length() + 1);
    if ("".equals(path)) {
      notFound(res);
      return;
    }

    int i = path.indexOf("/");
    if (i == -1) {
      res.sendRedirect(uri + "/index.html");
      return;
    } else if (i == path.length() - 1) {
      res.sendRedirect(uri + "index.html");
      return;
    }

    String id = path.substring(0, i);
    String file = path.substring(i + 1);
    if (!file.endsWith(".html")) {
      notFound(res);
      return;
    }
    file = file.substring(0, file.length() - 4) + "md";

    // TODO the lookup should actually be done by the plugin name,
    //      but due to a bug in Gerrit it is bound for "gerrit"
    // DocumentationExtension xDoc = xDocs.get(pluginName, id);
    DocumentationExtension xDoc = xDocs.get("gerrit", id);
    if (xDoc == null) {
      notFound(res);
      return;
    }

    try {
      Repository repo = repoManager.openRepository(xDoc.project);
      try {
        RevWalk rw = new RevWalk(repo);
        try {
          RevCommit commit = rw.parseCommit(repo.resolve(xDoc.ref));
          RevTree tree = commit.getTree();
          TreeWalk tw = new TreeWalk(repo);
          try {
            tw.addTree(tree);
            tw.setRecursive(true);
            tw.setFilter(PathFilter.create(file));
            if (!tw.next()) {
              notFound(res);
              return;
            }
            ObjectId objectId = tw.getObjectId(0);
            ObjectLoader loader = repo.open(objectId);
            byte[] md = loader.getBytes(Integer.MAX_VALUE);
            sendMarkdownAsHtml(new String(md, "UTF-8"), commit.getCommitTime(), res);
          } finally {
            tw.release();
          }
        } finally {
          rw.release();
        }
      } finally {
        repo.close();
      }
    } catch (RepositoryNotFoundException e) {
      notFound(res);
      return;
    }
  }

  private void sendMarkdownAsHtml(String md, int lastModified, HttpServletResponse res)
      throws IOException {
    res.setDateHeader("Last-Modified", lastModified);
    Map<String, String> macros = Maps.newHashMap();
    macros.put("SSH_HOST", sshHost);
    macros.put("SSH_PORT", "" + sshPort);
    String url = webUrl.get();
    if (Strings.isNullOrEmpty(url)) {
      url = "http://review.example.com/";
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

    byte[] html = new MarkdownFormatter()
      .markdownToDocHtml(sb.toString(), "UTF-8");
    res.setContentType("text/html");
    res.setCharacterEncoding("UTF-8");
    res.setContentLength(html.length);
    res.getOutputStream().write(html);
  }

  private static void notFound(HttpServletResponse res) throws IOException {
    CacheHeaders.setNotCacheable(res);
    res.sendError(HttpServletResponse.SC_NOT_FOUND);
  }
}
