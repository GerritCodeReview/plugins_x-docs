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
import static javax.servlet.http.HttpServletResponse.SC_NOT_MODIFIED;

import com.google.common.base.CharMatcher;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.net.HttpHeaders;
import com.google.gerrit.common.data.PatchScript.FileMode;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.extensions.restapi.AuthException;
import com.google.gerrit.extensions.restapi.IdString;
import com.google.gerrit.extensions.restapi.MethodNotAllowedException;
import com.google.gerrit.extensions.restapi.ResourceNotFoundException;
import com.google.gerrit.httpd.resources.Resource;
import com.google.gerrit.httpd.resources.SmallResource;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.reviewdb.server.ReviewDb;
import com.google.gerrit.server.mime.FileTypeRegistry;
import com.google.gerrit.server.change.FileContentUtil;
import com.google.gerrit.server.git.GitRepositoryManager;
import com.google.gerrit.server.project.GetHead;
import com.google.gerrit.server.project.NoSuchProjectException;
import com.google.gerrit.server.project.ProjectCache;
import com.google.gerrit.server.project.ProjectControl;
import com.google.gerrit.server.project.ProjectResource;
import com.google.gerrit.server.project.ProjectState;
import com.google.gwtexpui.server.CacheHeaders;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import com.googlesource.gerrit.plugins.xdocs.formatter.Formatters;
import com.googlesource.gerrit.plugins.xdocs.formatter.Formatters.FormatterProvider;

import eu.medsea.mimeutil.MimeType;

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
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
public class XDocServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  public  static final String PATH_PREFIX = "/project/";

  private final String pluginName;
  private final Provider<ReviewDb> db;
  private final ProjectControl.Factory projectControlFactory;
  private final ProjectCache projectCache;
  private final Provider<GetHead> getHead;
  private final GitRepositoryManager repoManager;
  private final XDocCache docCache;
  private final FileTypeRegistry fileTypeRegistry;
  private final XDocProjectConfig.Factory cfgFactory;
  private final Formatters formatters;

  @Inject
  XDocServlet(
      @PluginName String pluginName,
      Provider<ReviewDb> db,
      ProjectControl.Factory projectControlFactory,
      ProjectCache projectCache,
      Provider<GetHead> getHead,
      GitRepositoryManager repoManager,
      XDocCache cache,
      FileTypeRegistry fileTypeRegistry,
      XDocProjectConfig.Factory cfgFactory,
      Formatters formatters) {
    this.pluginName = pluginName;
    this.db = db;
    this.projectControlFactory = projectControlFactory;
    this.projectCache = projectCache;
    this.getHead = getHead;
    this.repoManager = repoManager;
    this.docCache = cache;
    this.fileTypeRegistry = fileTypeRegistry;
    this.cfgFactory = cfgFactory;
    this.formatters = formatters;
  }

  @Override
  public void service(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    try {
      validateRequestMethod(req);

      ResourceKey key = ResourceKey.fromPath(getPath(req));
      ProjectState state = getProject(key);
      XDocProjectConfig cfg = cfgFactory.create(state);

      if (key.file == null) {
        res.sendRedirect(getRedirectUrl(req, key, cfg));
        return;
      }

      MimeType mimeType = fileTypeRegistry.getMimeType(key.file, null);
      mimeType = new MimeType(FileContentUtil.resolveContentType(
          state, key.file, FileMode.FILE, mimeType.toString()));
      FormatterProvider formatter = getFormatter(req, key, mimeType);
      validateDiffMode(key);

      ProjectControl projectControl = projectControlFactory.validateFor(key.project);
      String rev = getRevision(
          key.diffMode == DiffMode.NO_DIFF
              ? MoreObjects.firstNonNull(key.revision, cfg.getIndexRef())
              : key.revision,
          projectControl);
      String revB = getRevision(key.revisionB, projectControl);

      Repository repo = repoManager.openRepository(key.project);
      try {
        ObjectId revId =
            resolveRevision(repo,
                key.diffMode == DiffMode.NO_DIFF
                    ? MoreObjects.firstNonNull(rev, Constants.HEAD)
                    : rev);
        if (revId != null && ObjectId.isId(rev)) {
          validateCanReadCommit(repo, projectControl, revId);
        }

        ObjectId revIdB = resolveRevision(repo, revB);
        if (revIdB != null && ObjectId.isId(revB)) {
          validateCanReadCommit(repo, projectControl, revIdB);
        }

        if (isResourceNotModified(req, key, revId, revIdB)) {
          res.sendError(SC_NOT_MODIFIED);
          return;
        }

        Resource rsc;
        if (formatter != null) {
          rsc = docCache.get(formatter, key.project, key.file, revId,
              revIdB, key.diffMode);
        } else if (isImage(mimeType)) {
          rsc = getImageResource(repo, key.diffMode, revId, revIdB, key.file);
        } else {
          rsc = Resource.NOT_FOUND;
        }

        if (rsc != Resource.NOT_FOUND) {
          res.setHeader(HttpHeaders.ETAG,
              computeETag(key.project, revId, key.file, revIdB, key.diffMode));
        }
        if (key.diffMode == DiffMode.NO_DIFF && rev == null) {
          // file was loaded from HEAD, since HEAD is modifiable the document
          // should only be cached for a short period
          CacheHeaders.setCacheablePrivate(res, 15, TimeUnit.MINUTES, false);
        } else {
          CacheHeaders.setCacheablePrivate(res, 7, TimeUnit.DAYS, false);
        }
        rsc.send(req, res);
        return;
      } finally {
        repo.close();
      }
    } catch (RepositoryNotFoundException | NoSuchProjectException
        | ResourceNotFoundException | AuthException | RevisionSyntaxException e) {
      Resource.NOT_FOUND.send(req, res);
    } catch (MethodNotAllowedException e) {
      CacheHeaders.setNotCacheable(res);
      res.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
  }

  private String getPath(HttpServletRequest req) {
    String path = req.getRequestURI();
    String prefix = "/plugins/" + pluginName;
    if (path.startsWith(prefix)) {
      path = path.substring(prefix.length());
    }
    return path;
  }

  private Resource getImageResource(Repository repo, DiffMode diffMode,
      ObjectId revId, ObjectId revIdB, String file) {
    ObjectId id = diffMode == DiffMode.NO_DIFF || diffMode == DiffMode.SIDEBYSIDE_A
        ? revId
        : revIdB;
    try (RevWalk rw = new RevWalk(repo)) {
      RevCommit commit = rw.parseCommit(id);
      RevTree tree = commit.getTree();
      try (TreeWalk tw = new TreeWalk(repo)) {
        tw.addTree(tree);
        tw.setRecursive(true);
        tw.setFilter(PathFilter.create(file));
        if (!tw.next()) {
          return Resource.NOT_FOUND;
        }
        ObjectId objectId = tw.getObjectId(0);
        ObjectLoader loader = repo.open(objectId);
        byte[] content = loader.getBytes(Integer.MAX_VALUE);

        MimeType mimeType = fileTypeRegistry.getMimeType(file, content);
        if (!isSafeImage(mimeType)) {
          return Resource.NOT_FOUND;
        }
        return new SmallResource(content)
            .setContentType(mimeType.toString())
            .setCharacterEncoding(UTF_8.name())
            .setLastModified(commit.getCommitTime());
      }
    } catch (IOException e) {
      return Resource.NOT_FOUND;
    }
  }

  private static void validateRequestMethod(HttpServletRequest req)
      throws MethodNotAllowedException {
    if (!("GET".equals(req.getMethod()) || "HEAD".equals(req.getMethod()))) {
      throw new MethodNotAllowedException();
    }
  }

  private static void validateDiffMode(ResourceKey key)
      throws ResourceNotFoundException {
    if (key.diffMode != DiffMode.NO_DIFF && (key.revisionB == null)) {
      throw new ResourceNotFoundException();
    }
  }

  private ProjectState getProject(ResourceKey key)
      throws ResourceNotFoundException {
    ProjectState state = projectCache.get(key.project);
    if (state == null) {
      throw new ResourceNotFoundException();
    }
    return state;
  }

  private FormatterProvider getFormatter(HttpServletRequest req,
      ResourceKey key, MimeType mimeType) throws ResourceNotFoundException {
    FormatterProvider formatter;
    if (req.getParameter("raw") != null) {
      formatter = formatters.getRawFormatter();
    } else {
      formatter = formatters.get(getProject(key), key.file);
    }
    if (isSafeImage(mimeType)) {
      if (req.getParameter("formatImage") == null) {
        // image formatting is not requested, return the plain image
        formatter = null;
      }
    } else {
      if (formatter == null) {
        throw new ResourceNotFoundException();
      }
    }
    if (formatter == null && !isSafeImage(mimeType)) {
      throw new ResourceNotFoundException();
    }
    return formatter;
  }

  private boolean isSafeImage(MimeType mimeType) {
    return isImage(mimeType) && fileTypeRegistry.isSafeInline(mimeType);
  }

  private static boolean isImage(MimeType mimeType) {
    return "image".equals(mimeType.getMediaType());
  }

  private String getRevision(String revision,
      ProjectControl projectControl) throws ResourceNotFoundException,
      AuthException, IOException {
    if (revision == null) {
      return null;
    }

    if (ObjectId.isId(revision)) {
      return revision;
    }

    if (Constants.HEAD.equals(revision)) {
      return getHead.get().apply(new ProjectResource(projectControl));
    } else {
      String rev = revision;
      if (!rev.startsWith(Constants.R_REFS)) {
        rev = Constants.R_HEADS + rev;
      }
      if (!projectControl.controlForRef(rev).isVisible()) {
        throw new ResourceNotFoundException();
      }
      return rev;
    }
  }

  private static ObjectId resolveRevision(Repository repo, String revision)
      throws ResourceNotFoundException, IOException {
    if (revision == null) {
      return null;
    }

    ObjectId revId = repo.resolve(revision);
    if (revId == null) {
      throw new ResourceNotFoundException();
    }
    return revId;
  }

  private void validateCanReadCommit(Repository repo,
      ProjectControl projectControl, ObjectId revId)
      throws ResourceNotFoundException, IOException {
    try (RevWalk rw = new RevWalk(repo)) {
      RevCommit commit = rw.parseCommit(revId);
      if (!projectControl.canReadCommit(db.get(), rw, commit)) {
        throw new ResourceNotFoundException();
      }
    }
  }

  private static boolean isResourceNotModified(HttpServletRequest req,
      ResourceKey key, ObjectId revId, ObjectId revIdB) {
    String receivedETag = req.getHeader(HttpHeaders.IF_NONE_MATCH);
    if (receivedETag != null) {
      return receivedETag.equals(computeETag(key.project, revId, key.file,
          revIdB, key.diffMode));
    }
    return false;
  }

  private static String computeETag(Project.NameKey project, ObjectId revId,
      String file, ObjectId revIdB, DiffMode diffMode) {
    Hasher hasher = Hashing.md5().newHasher();
    hasher.putUnencodedChars(project.get());
    if (revId != null) {
      hasher.putUnencodedChars(revId.getName());
    }
    hasher.putUnencodedChars(file);
    if (diffMode != DiffMode.NO_DIFF) {
      hasher.putUnencodedChars(revIdB.getName()).putUnencodedChars(
          diffMode.name());
    }
    return hasher.hash().toString();
  }

  private String getRedirectUrl(HttpServletRequest req, ResourceKey key,
      XDocProjectConfig cfg) {
    StringBuilder redirectUrl = new StringBuilder();
    redirectUrl.append(req.getRequestURL().substring(0,
        req.getRequestURL().length() - req.getRequestURI().length()));
    redirectUrl.append(req.getContextPath());
    redirectUrl.append(PATH_PREFIX);
    redirectUrl.append(IdString.fromDecoded(key.project.get()).encoded());
    redirectUrl.append("/");
    if (key.revision != null) {
      redirectUrl.append("rev/");
      redirectUrl.append(key.revision);
      redirectUrl.append("/");
    }
    redirectUrl.append(cfg.getIndexFile());
    return redirectUrl.toString();
  }

  private static class ResourceKey {
    final Project.NameKey project;
    final String file;
    final String revision;
    final String revisionB;
    final DiffMode diffMode;

    static ResourceKey fromPath(String path) {
      String project;
      String file = null;
      String revision = null;
      String revisionB = null;
      DiffMode diffMode = DiffMode.NO_DIFF;

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
              file = rest.substring(i + 1);
            } else {
              revision = IdString.fromUrl(rest).get();
            }
          }
        } else {
          file = rest;
        }

      } else {
        project = IdString.fromUrl(CharMatcher.is('/').trimTrailingFrom(path)).get();
      }

      if (revision != null) {
        if (revision.contains("<->")) {
          diffMode = DiffMode.UNIFIED;
          int p = revision.indexOf("<->");
          revisionB = revision.substring(p + 3);
          revision = Strings.emptyToNull(revision.substring(0, p));
        } else if (revision.contains("<-")) {
          diffMode = DiffMode.SIDEBYSIDE_A;
          int p = revision.indexOf("<-");
          revisionB = revision.substring(p + 2);
          revision = revision.substring(0, p);
        } else if (revision.contains("->")) {
          diffMode = DiffMode.SIDEBYSIDE_B;
          int p = revision.indexOf("->");
          revisionB = revision.substring(p + 2);
          revision = Strings.emptyToNull(revision.substring(0, p));
        }
      }

      return new ResourceKey(project, file, revision, revisionB, diffMode);
    }

    private ResourceKey(String p, String f, String r, String r2, DiffMode dm) {
      project = new Project.NameKey(p);
      file = f;
      revision = r;
      revisionB = r2;
      diffMode = dm;
    }
  }
}
