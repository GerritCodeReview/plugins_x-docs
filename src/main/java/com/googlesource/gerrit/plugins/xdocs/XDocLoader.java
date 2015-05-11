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

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.Weigher;
import com.google.common.collect.Maps;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.extensions.restapi.MethodNotAllowedException;
import com.google.gerrit.extensions.restapi.ResourceNotFoundException;
import com.google.gerrit.httpd.resources.Resource;
import com.google.gerrit.httpd.resources.SmallResource;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.cache.CacheModule;
import com.google.gerrit.server.config.CanonicalWebUrl;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.gerrit.server.git.GitRepositoryManager;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import com.googlesource.gerrit.plugins.xdocs.formatter.Formatter;
import com.googlesource.gerrit.plugins.xdocs.formatter.Formatters;
import com.googlesource.gerrit.plugins.xdocs.formatter.Formatters.FormatterProvider;
import com.googlesource.gerrit.plugins.xdocs.formatter.StreamFormatter;
import com.googlesource.gerrit.plugins.xdocs.formatter.StringFormatter;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.outerj.daisy.diff.HtmlCleaner;
import org.outerj.daisy.diff.XslFilter;
import org.outerj.daisy.diff.html.HTMLDiffer;
import org.outerj.daisy.diff.html.HtmlSaxDiffOutput;
import org.outerj.daisy.diff.html.TextNodeComparator;
import org.outerj.daisy.diff.html.dom.DomTreeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

@Singleton
public class XDocLoader extends CacheLoader<String, Resource> {
  private static final Logger log = LoggerFactory.getLogger(XDocLoader.class);

  private static final String DEFAULT_HOST = "review.example.com";

  private final GitRepositoryManager repoManager;
  private final Provider<String> webUrl;
  private final String pluginName;
  private final PluginConfigFactory cfgFactory;
  private final Formatters formatters;

  @Inject
  XDocLoader(GitRepositoryManager repoManager,
      @CanonicalWebUrl Provider<String> webUrl,
      @PluginName String pluginName,
      PluginConfigFactory cfgFactory,
      Formatters formatters) {
    this.repoManager = repoManager;
    this.webUrl = webUrl;
    this.pluginName = pluginName;
    this.cfgFactory = cfgFactory;
    this.formatters = formatters;
  }

  @Override
  public Resource load(String strKey) throws Exception {
    XDocResourceKey key = XDocResourceKey.fromString(strKey);
    try (Repository repo = repoManager.openRepository(key.getProject())) {
      FormatterProvider formatter = getFormatter(key.getFormatter());
      try (RevWalk rw = new RevWalk(repo)) {
        String html = null;
        if (key.getRevId() != null) {
          html = loadHtml(formatter, repo, rw, key, key.getRevId());
        }

        if (key.getDiffMode() != DiffMode.NO_DIFF) {
          String htmlB =
              loadHtml(formatter, repo, rw, key, checkRevId(key.getRevIdB()));
          if (html == null && htmlB == null) {
            throw new ResourceNotFoundException();
          }
          html = diffHtml(html, htmlB, key.getDiffMode());
        } else {
          if (html == null) {
            throw new ResourceNotFoundException();
          }
        }

        RevCommit commit = rw.parseCommit(
            MoreObjects.firstNonNull(key.getRevIdB(), key.getRevId()));
        return getAsHtmlResource(html, commit.getCommitTime());
      }
    } catch (ResourceNotFoundException e) {
      return Resource.NOT_FOUND;
    } catch (MethodNotAllowedException e) {
      return Resources.METHOD_NOT_ALLOWED;
    }
  }

  private FormatterProvider getFormatter(String formatterName)
      throws ResourceNotFoundException {
    FormatterProvider formatter = formatters.getByName(formatterName);
    if (formatter == null) {
      throw new ResourceNotFoundException();
    }
    return formatter;
  }

  private static ObjectId checkRevId(ObjectId revId)
      throws ResourceNotFoundException {
    if (revId == null) {
      throw new ResourceNotFoundException();
    }
    return revId;
  }

  private String loadHtml(FormatterProvider formatter, Repository repo,
      RevWalk rw, XDocResourceKey key, ObjectId revId) throws IOException,
      ResourceNotFoundException, MethodNotAllowedException, GitAPIException {
    RevCommit commit = rw.parseCommit(revId);
    RevTree tree = commit.getTree();
    try (TreeWalk tw = new TreeWalk(repo)) {
      tw.addTree(tree);
      tw.setRecursive(true);
      tw.setFilter(PathFilter.create(key.getResource()));
      if (!tw.next()) {
        return null;
      }
      ObjectId objectId = tw.getObjectId(0);
      ObjectLoader loader = repo.open(objectId);
      return getHtml(formatter, repo, loader, key.getProject(),
          key.getResource(), revId);
    }
  }

  private String getHtml(FormatterProvider formatter, Repository repo,
      ObjectLoader loader, Project.NameKey project, String path, ObjectId revId)
      throws MethodNotAllowedException, IOException, GitAPIException,
      ResourceNotFoundException {
    Formatter f = formatter.get();
    if (f instanceof StringFormatter) {
      return getHtml(formatter.getName(), (StringFormatter) f, repo, loader,
          project, path, revId);
    } else if (f instanceof StreamFormatter) {
      return getHtml(formatter.getName(), (StreamFormatter) f, repo, loader,
          project, path, revId);
    } else {
      log.error(String.format("Unsupported formatter: %s", formatter.getName()));
      throw new ResourceNotFoundException();
    }
  }

  private String getHtml(String formatterName, StringFormatter f,
      Repository repo, ObjectLoader loader, Project.NameKey project,
      String path, ObjectId revId) throws MethodNotAllowedException,
      IOException, GitAPIException {
    byte[] bytes = loader.getBytes(Integer.MAX_VALUE);
    boolean isBinary = RawText.isBinary(bytes);
    if (formatterName.equals(Formatters.RAW_FORMATTER) && isBinary) {
      throw new MethodNotAllowedException();
    }
    String raw = new String(bytes, UTF_8);
    String abbrRevId = getAbbrRevId(repo, revId);
    if (!isBinary) {
      raw = replaceMacros(repo, project, revId, abbrRevId, raw);
    }
    return f.format(project.get(), path, revId.getName(), abbrRevId,
        getFormatterConfig(formatterName), raw);
  }

  private String getHtml(String formatterName, StreamFormatter f,
      Repository repo, ObjectLoader loader, Project.NameKey project,
      String path, ObjectId revId) throws IOException {
    try (InputStream raw = loader.openStream()) {
      return ((StreamFormatter) f).format(project.get(), path, revId.getName(),
          getAbbrRevId(repo, revId), getFormatterConfig(formatterName), raw);
    }
  }

  private String diffHtml(String htmlA, String htmlB, DiffMode diffMode)
      throws IOException, TransformerConfigurationException, SAXException,
      ResourceNotFoundException {
    ByteArrayOutputStream htmlDiff = new ByteArrayOutputStream();

    SAXTransformerFactory tf =
        (SAXTransformerFactory) TransformerFactory.newInstance();
    TransformerHandler result = tf.newTransformerHandler();
    result.setResult(new StreamResult(htmlDiff));

    String htmlHeader = "com/googlesource/gerrit/plugins/xdocs/diff/htmlheader-";
    switch (diffMode) {
      case SIDEBYSIDE_A:
        htmlHeader += "sidebyside-a.xsl";
        break;
      case SIDEBYSIDE_B:
        htmlHeader += "sidebyside-b.xsl";
        break;
      case UNIFIED:
        htmlHeader += "unified.xsl";
        break;
      default:
        log.error(String.format("Unsupported diff mode: %s", diffMode.name()));
        throw new ResourceNotFoundException();
    }

    ContentHandler postProcess = new XslFilter().xsl(result, htmlHeader);
    postProcess.startDocument();
    postProcess.startElement("", "diffreport", "diffreport",
            new AttributesImpl());
    postProcess.startElement("", "diff", "diff",
            new AttributesImpl());

    HtmlSaxDiffOutput output = new HtmlSaxDiffOutput(postProcess, "diff");
    HTMLDiffer differ = new HTMLDiffer(output);
    differ.diff(getComparator(htmlA), getComparator(htmlB));

    postProcess.endElement("", "diff", "diff");
    postProcess.endElement("", "diffreport", "diffreport");
    postProcess.endDocument();

    return fixStyles(htmlDiff.toString(UTF_8.name()));
  }

  /**
   * The daisydiff formatting may make inlined styles unparsable. Fix it:
   * <ul>
   *   <li>Remove span element to highlight addition/deletion inside style elements.</li>
   *   <li>Replace '&gt;' with '>'.</li>
   * </ul>
   */
  private String fixStyles(String html) {
    Matcher m =
        Pattern.compile("(<style[a-zA-Z -=/\"]+>\n)<[a-zA-Z -=\"]+>(.*)</[a-z]+>(\n</style>)")
               .matcher(html);
    StringBuffer sb = new StringBuffer();
    while (m.find()) {
      m.appendReplacement(sb, m.group(1) + m.group(2).replaceAll("&gt;", ">")
          + m.group(3));
    }
    m.appendTail(sb);
    return sb.toString();
  }

  private TextNodeComparator getComparator(String html) throws IOException,
      SAXException {
    InputSource source =
        new InputSource(new ByteArrayInputStream(
            Strings.nullToEmpty(html).getBytes(UTF_8)));
    DomTreeBuilder handler = new DomTreeBuilder();
    new HtmlCleaner().cleanAndParse(source, handler);
    return new TextNodeComparator(handler, Locale.US);
  }

  private ConfigSection getFormatterConfig(String formatterName) {
    XDocGlobalConfig cfg =
        new XDocGlobalConfig(cfgFactory.getGlobalPluginConfig(pluginName));
    return cfg.getFormatterConfig(formatterName);
  }

  private static String getAbbrRevId(Repository repo, ObjectId revId)
      throws IOException {
    try (ObjectReader reader = repo.newObjectReader()) {
      return reader.abbreviate(revId).name();
    }
  }

  private String replaceMacros(Repository repo, Project.NameKey project,
      ObjectId revId, String abbrRevId, String raw) throws GitAPIException,
      IOException {
    Map<String, String> macros = Maps.newHashMap();

    String url = webUrl.get();
    if (Strings.isNullOrEmpty(url)) {
      url = "http://" + DEFAULT_HOST + "/";
    }
    macros.put("URL", url);

    macros.put("PROJECT", project.get());
    macros.put("PROJECT_URL", url + "#/admin/projects/" + project.get());
    macros.put("REVISION", abbrRevId);
    try (Git git = new Git(repo)) {
      macros.put("GIT_DESCRIPTION", MoreObjects.firstNonNull(
          git.describe().setTarget(revId).call(), abbrRevId));
    }

    Matcher m = Pattern.compile("(\\\\)?@([A-Z_]+)@").matcher(raw);
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

  private Resource getAsHtmlResource(String html, int lastModified) {
    return new SmallResource(html.getBytes(UTF_8))
        .setContentType("text/html")
        .setCharacterEncoding(UTF_8.name())
        .setLastModified(lastModified);
  }

  public static class Module extends CacheModule {
    static final String X_DOC_RESOURCES = "x_doc_resources";

    @Override
    protected void configure() {
      install(new CacheModule() {
        @Override
        protected void configure() {
          persist(X_DOC_RESOURCES, String.class, Resource.class)
            .maximumWeight(2 << 20)
            .weigher(XDocResourceWeigher.class)
            .loader(XDocLoader.class);
        }
      });
    }
  }

  private static class XDocResourceWeigher implements
      Weigher<String, Resource> {
    @Override
    public int weigh(String key, Resource value) {
      return key.length() * 2 + value.weigh();
    }
  }
}
