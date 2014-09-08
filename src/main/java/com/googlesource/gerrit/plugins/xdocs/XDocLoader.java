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

import com.google.common.base.Strings;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.Weigher;
import com.google.common.collect.Maps;
import com.google.gerrit.httpd.resources.Resource;
import com.google.gerrit.httpd.resources.ResourceKey;
import com.google.gerrit.httpd.resources.SmallResource;
import com.google.gerrit.server.cache.CacheModule;
import com.google.gerrit.server.config.CanonicalWebUrl;
import com.google.gerrit.server.documentation.MarkdownFormatter;
import com.google.gerrit.server.git.GitRepositoryManager;
import com.google.inject.Inject;
import com.google.inject.Provider;
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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class XDocLoader extends CacheLoader<XDocResourceKey, Resource> {
  private static final String DEFAULT_HOST = "review.example.com";

  private final GitRepositoryManager repoManager;
  private final Provider<String> webUrl;

  @Inject
  XDocLoader(GitRepositoryManager repoManager,
      @CanonicalWebUrl Provider<String> webUrl) {
    this.repoManager = repoManager;
    this.webUrl = webUrl;
  }

  @Override
  public Resource load(XDocResourceKey key) throws Exception {
    Repository repo = repoManager.openRepository(key.getProject());
    try {
      RevWalk rw = new RevWalk(repo);
      try {
        RevCommit commit = rw.parseCommit(key.getRevId());
        RevTree tree = commit.getTree();
        TreeWalk tw = new TreeWalk(repo);
        try {
          tw.addTree(tree);
          tw.setRecursive(true);
          tw.setFilter(PathFilter.create(key.getResource()));
          if (!tw.next()) {
            return Resource.NOT_FOUND;
          }
          ObjectId objectId = tw.getObjectId(0);
          ObjectLoader loader = repo.open(objectId);
          byte[] md = loader.getBytes(Integer.MAX_VALUE);
          return getMarkdownAsHtmlResource(new String(md, UTF_8),
              commit.getCommitTime(), key);
        } finally {
          tw.release();
        }
      } finally {
        rw.release();
      }
    } finally {
      repo.close();
    }
  }

  private Resource getMarkdownAsHtmlResource(String md, int lastModified,
      ResourceKey cacheKey) throws IOException {
    byte[] html = new MarkdownFormatter().suppressHtml()
        .markdownToDocHtml(replaceMacros(md), UTF_8.name());
    return new SmallResource(html)
        .setContentType("text/html")
        .setCharacterEncoding(UTF_8.name())
        .setLastModified(lastModified);
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
      if (m.group(1) != null || val == null) {
        m.appendReplacement(sb, "@" + key + "@");
      } else {
        m.appendReplacement(sb, val);
      }
    }
    m.appendTail(sb);
    return sb.toString();
  }

  public static class Module extends CacheModule {
    static final String X_DOC_RESOURCES = "x_doc_resources";

    @Override
    protected void configure() {
      install(new CacheModule() {
        @Override
        protected void configure() {
          persist(X_DOC_RESOURCES, XDocResourceKey.class, Resource.class)
            .maximumWeight(2 << 20)
            .weigher(XDocResourceWeigher.class)
            .loader(XDocLoader.class);
        }
      });
    }
  }

  private static class XDocResourceWeigher implements
      Weigher<XDocResourceKey, Resource> {
    @Override
    public int weigh(XDocResourceKey key, Resource value) {
      return key.weigh() + value.weigh();
    }
  }
}
