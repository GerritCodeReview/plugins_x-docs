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

import com.google.gerrit.plugin.client.Plugin;
import com.google.gerrit.plugin.client.screen.Screen;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
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
      String fileName = URL.decode(screen.getToken(3));
      screen.show(new XDocScreen(projectName, revision, fileName));
    }
  }

  static class HeadFactory implements Screen.EntryPoint {
    @Override
    public void onLoad(Screen screen) {
      String projectName = URL.decode(screen.getToken(1));
      String fileName = URL.decode(screen.getToken(2));
      screen.show(new XDocScreen(projectName, "HEAD", fileName));
    }
  }

  XDocScreen(String projectName, String revision, String fileName) {
    setStyleName("xdocs-panel");

    HorizontalPanel p = new HorizontalPanel();
    p.setStyleName("xdocs-header");
    p.add(new InlineHyperlink(projectName, "/admin/projects/" + projectName));
    p.add(new Label("/"));
    p.add(new Label(fileName));
    p.add(new Label("(" + revision + ")"));
    add(p);

    final String url = getUrl(projectName, revision, fileName);
    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
    try {
      builder.sendRequest(null, new RequestCallback() {
        public void onResponseReceived(Request request, Response response) {
          int status = response.getStatusCode();
          if (200 <= status && status < 300) {
            String frameId = "xdoc_iframe";
            Frame frame = new Frame(url);
            frame.getElement().setId(frameId);
            resize(frame, frameId);
            add(frame);
          } else {
            showError(status + " " + response.getStatusText());
          }
        }

        public void onError(Request request, Throwable exception) {
          showError(exception.getMessage());
        }
      });
    } catch (RequestException e) {
      showError(e.getMessage());
    }
  }

  private void showError(String message) {
    Label l = new Label("Unable to load document: " + message);
    l.setStyleName("xdocs-error");
    add(l);
  }

  public static String getUrl(String projectName, String revision, String fileName) {
    StringBuilder url = new StringBuilder();
    url.append("plugins/");
    url.append(Plugin.get().getName());
    url.append("/project/");
    url.append(URL.encodeQueryString(projectName));
    if (revision != null && !"HEAD".equals(revision)) {
      url.append("/rev/");
      url.append(URL.encodeQueryString(revision));
    }
    url.append("/");
    url.append(URL.encodeQueryString(fileName));
    return url.toString();
  }

  public static void resize(Widget w, String id) {
    StringBuilder autoResizeScript = new StringBuilder();
    autoResizeScript.append("if (document.getElementById) {");
    autoResizeScript.append("var e = document.getElementById(\"");
    autoResizeScript.append(id);
    autoResizeScript.append("\"); ");
    autoResizeScript.append("e.width = (e.contentWindow.document .body.scrollWidth) + \"px\"; ");
    autoResizeScript.append("e.height = (e.contentWindow.document .body.scrollHeight) + \"px\"; ");
    autoResizeScript.append("}");
    w.getElement().setAttribute("onLoad", autoResizeScript.toString());
  }
}
