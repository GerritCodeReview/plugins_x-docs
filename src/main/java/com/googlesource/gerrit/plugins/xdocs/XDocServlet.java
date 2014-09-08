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

import static javax.servlet.http.HttpServletResponse.SC_NOT_MODIFIED;

import com.google.common.base.CharMatcher;
import com.google.common.base.MoreObjects;
import com.google.common.cache.LoadingCache;
import com.google.common.hash.Hashing;
import com.google.common.net.HttpHeaders;
import com.google.gerrit.extensions.restapi.AuthException;
import com.google.gerrit.extensions.restapi.IdString;
import com.google.gerrit.extensions.restapi.ResourceNotFoundException;
import com.google.gerrit.httpd.resources.Resource;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.reviewdb.server.ReviewDb;
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
import com.google.inject.name.Named;

import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
public class XDocServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  public  static final String PATH_PREFIX = "/project/";

  private final Provider<ReviewDb> db;
  private final ProjectControl.Factory projectControlFactory;
  private final ProjectCache projectCache;
  private final Provider<GetHead> getHead;
  private final GitRepositoryManager repoManager;
  private final LoadingCache<String, Resource> docCache;

  @Inject
  XDocServlet(Provider<ReviewDb> db,
      ProjectControl.Factory projectControlFactory,
      ProjectCache projectCache,
      Provider<GetHead> getHead,
      GitRepositoryManager repoManager,
      @Named(XDocLoader.Module.X_DOC_RESOURCES) LoadingCache<String, Resource> cache) {
    this.db = db;
    this.projectControlFactory = projectControlFactory;
    this.projectCache = projectCache;
    this.getHead = getHead;
    this.repoManager = repoManager;
    this.docCache = cache;
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
      Resource.NOT_FOUND.send(req, res);
      return;
    }
    if (key.file == null) {
      res.sendRedirect(getRedirectUrl(req, key));
      return;
    }
    if (!key.file.endsWith(".md")) {
      Resource.NOT_FOUND.send(req, res);
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
            Resource.NOT_FOUND.send(req, res);
            return;
          }
        }
      }

      Repository repo = repoManager.openRepository(key.project);
      try {
        ObjectId revId =
            repo.resolve(MoreObjects.firstNonNull(rev, Constants.HEAD));
        if (revId == null) {
          Resource.NOT_FOUND.send(req, res);
          return;
        }

        if (ObjectId.isId(rev)) {
          RevWalk rw = new RevWalk(repo);
          try {
            RevCommit commit = rw.parseCommit(repo.resolve(rev));
            if (!projectControl.canReadCommit(db.get(), rw, commit)) {
              Resource.NOT_FOUND.send(req, res);
              return;
            }
          } finally {
            rw.release();
          }
        }

        String eTag = null;
        String receivedETag = req.getHeader(HttpHeaders.IF_NONE_MATCH);
        if (receivedETag != null) {
          eTag = computeETag(key.project, revId, key.file);
          if (eTag.equals(receivedETag)) {
            res.sendError(SC_NOT_MODIFIED);
            return;
          }
        }

        Resource rsc = docCache.getUnchecked(
            (new XDocResourceKey(key.project, key.file, revId)).asString());

        if (rsc != Resource.NOT_FOUND) {
          res.setHeader(
              HttpHeaders.ETAG,
              MoreObjects.firstNonNull(eTag,
                  computeETag(key.project, revId, key.file)));
        }
        CacheHeaders.setCacheablePrivate(res, 7, TimeUnit.DAYS, false);
        rsc.send(req, res);
        return;
      } finally {
        repo.close();
      }
    } catch (RepositoryNotFoundException | NoSuchProjectException
        | ResourceNotFoundException | AuthException | RevisionSyntaxException e) {
      Resource.NOT_FOUND.send(req, res);
      return;
    }
  }

  private static String computeETag(Project.NameKey project, ObjectId revId,
      String file) {
    return Hashing.md5().newHasher()
        .putUnencodedChars(project.get())
        .putUnencodedChars(revId.getName())
        .putUnencodedChars(file)
        .hash().toString();
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
