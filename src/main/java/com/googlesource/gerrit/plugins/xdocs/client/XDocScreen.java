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

package com.googlesource.gerrit.plugins.xdocs.client;

import com.google.gerrit.plugin.client.screen.Screen;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineHyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class XDocScreen extends VerticalPanel {
  static class Factory implements Screen.EntryPoint {
    @Override
    public void onLoad(Screen screen) {
      String projectName = URL.decode(screen.getToken(1));
      String revision = URL.decode(screen.getToken(2));
      String path = URL.decode(screen.getToken(3));
      String token = screen.getTokenGroups() >= 5 ? screen.getToken(5) : null;
      screen.show(new XDocScreen(projectName, revision, path, token));
      screen.setWindowTitle(FileInfo.getFileName(path));
    }
  }

  static class HeadFactory implements Screen.EntryPoint {
    @Override
    public void onLoad(Screen screen) {
      String projectName = URL.decode(screen.getToken(1));
      String path = URL.decode(screen.getToken(2));
      String token = screen.getTokenGroups() >= 4 ? screen.getToken(4) : null;
      screen.show(new XDocScreen(projectName, "HEAD", path, token));
      screen.setWindowTitle(FileInfo.getFileName(path));
    }
  }

  XDocScreen(String projectName, String revision, String path,
      final String token) {
    setStyleName("xdocs-panel");

    HorizontalPanel p = new HorizontalPanel();
    p.setStyleName("xdocs-file-header");
    p.add(new InlineHyperlink(projectName, "/admin/projects/" + projectName));
    p.add(new Label("/"));
    p.add(new Label(path));
    p.add(new Label("(" + revision + ")"));
    add(p);

    final String url = XDocApi.getUrl(projectName, revision, path);
    XDocApi.checkHtml(url, new AsyncCallback<VoidResult>() {
      @Override
      public void onSuccess(VoidResult result) {
        String frameId = "xdoc_iframe";
        Frame frame = new Frame(url);
        frame.getElement().setId(frameId);
        resize(frame, frameId);
        insertIds(frame, frameId);
        if (token != null) {
          scrollTo(frame, frameId, token);
        }
        add(frame);
      }

      @Override
      public void onFailure(Throwable caught) {
        showError(caught.getMessage());
      }
    });
  }

  private void showError(String message) {
    Label l = new Label("Unable to load document: " + message);
    l.setStyleName("xdocs-error");
    add(l);
  }

  public static void resize(Widget w, String id) {
    StringBuilder autoResizeScript = new StringBuilder();
    autoResizeScript.append("if (document.getElementById) {");
    autoResizeScript.append("  var e = document.getElementById(\"" + id + "\"); ");
    autoResizeScript.append("  e.width = (e.contentWindow.document .body.scrollWidth) + \"px\"; ");
    autoResizeScript.append("  e.height = (e.contentWindow.document .body.scrollHeight) + \"px\"; ");
    autoResizeScript.append("}");
    appendOnLoadScript(w, autoResizeScript.toString());
  }

  /**
   * Adds an onload script to the given frame that inserts IDs on all headings
   * in the frame content. IDs will only be added on headings that don't have an
   * ID yet.
   *
   * Adding an ID to a heading makes an anchor on it available that can be
   * accessed by clicking on a link icon that is displayed next to the heading
   * when the user hovers over it. These anchors are inserted by another script,
   * but only on headings that have an ID.
   *
   * @param f the frame
   * @param frameId the ID of the frame
   */
  private static void insertIds(Frame f, String frameId) {
    StringBuilder insertIdsScript = new StringBuilder();
    insertIdsScript.append("if (document.getElementById) {");
    insertIdsScript.append("  function insertIds(a) {");
    insertIdsScript.append("    for(var i = 0; i < a.length; i++) { ");
    insertIdsScript.append("      var id = a[i].getAttribute('id'); ");
    insertIdsScript.append("      if (id == null) { ");
    insertIdsScript.append("        id = a[i].textContent; ");
    insertIdsScript.append("        id = id.replace(/ /g,'_'); ");
    insertIdsScript.append("        a[i].setAttribute('id', id); ");
    insertIdsScript.append("      } ");
    insertIdsScript.append("    } ");
    insertIdsScript.append("  } ");
    insertIdsScript.append("  var f = document.getElementById(\"" + frameId + "\"); ");
    insertIdsScript.append("  insertIds(f.contentWindow.document.getElementsByTagName('h1')); ");
    insertIdsScript.append("  insertIds(f.contentWindow.document.getElementsByTagName('h2')); ");
    insertIdsScript.append("  insertIds(f.contentWindow.document.getElementsByTagName('h3')); ");
    insertIdsScript.append("  insertIds(f.contentWindow.document.getElementsByTagName('h4')); ");
    insertIdsScript.append("} ");
    appendOnLoadScript(f, insertIdsScript.toString());
  }

  /**
   * Adds an onload script to the given frame that scrolls the frame to the
   * element with the given ID.
   *
   * @param f the frame
   * @param frameId the ID of the frame
   * @param id the ID of element to which the frame should be scrolled
   */
  private static void scrollTo(Frame f, String frameId, String id) {
    StringBuilder scrollToScript = new StringBuilder();
    scrollToScript.append("if (document.getElementById) {");
    scrollToScript.append("  var f = document.getElementById(\"" + frameId + "\"); ");
    scrollToScript.append("  var e = f.contentWindow.document.getElementById(\"" + id + "\"); ");
    scrollToScript.append("  if (e != null) {");
    scrollToScript.append("    function getOffset(d, e) {");
    scrollToScript.append("      var box = e.getBoundingClientRect(); ");
    scrollToScript.append("      var body = d.body; ");
    scrollToScript.append("      var docElem = d.documentElement; ");
    scrollToScript.append("      var scrollTop = window.pageYOffset || docElem.scrollTop || body.scrollTop; ");
    scrollToScript.append("      var scrollLeft = window.pageXOffset || docElem.scrollLeft || body.scrollLeft; ");
    scrollToScript.append("      var clientTop = docElem.clientTop || body.clientTop || 0; ");
    scrollToScript.append("      var clientLeft = docElem.clientLeft || body.clientLeft || 0; ");
    scrollToScript.append("      var top  = box.top +  scrollTop - clientTop; ");
    scrollToScript.append("      var left = box.left + scrollLeft - clientLeft; ");
    scrollToScript.append("      return { left: Math.round(left), top: Math.round(top) }; ");
    scrollToScript.append("    }");
    scrollToScript.append("    var fOffset = getOffset(document, f); ");
    scrollToScript.append("    var eOffset = getOffset(f.contentWindow.document, e); ");
    scrollToScript.append("    var x = eOffset.left; ");
    scrollToScript.append("    var y = eOffset.top + fOffset.top; ");
    scrollToScript.append("    window.scrollTo(x, y); ");
    scrollToScript.append("  } ");
    scrollToScript.append("} ");
    appendOnLoadScript(f, scrollToScript.toString());
  }

  private static void appendOnLoadScript(Widget w, String script) {
    w.getElement().setAttribute("onLoad",
        w.getElement().getAttribute("onLoad") + " " + script);
  }
}
