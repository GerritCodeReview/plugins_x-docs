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
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class XDocApi {
  public static String getUrl(String projectName, String revision,
      String path) {
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
    url.append(path);
    return url.toString();
  }

  public static void checkHtml(String url,
      final AsyncCallback<VoidResult> callback) {
    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
    try {
      builder.sendRequest(null, new RequestCallback() {
        public void onResponseReceived(Request request, Response response) {
          int status = response.getStatusCode();
          if (200 <= status && status < 300) {
            callback.onSuccess(VoidResult.create());
          } else {
            callback.onFailure(new RequestException(status + " "
                + response.getStatusText()));
          }
        }

        public void onError(Request request, Throwable caught) {
          callback.onFailure(caught);
        }
      });
    } catch (RequestException e) {
      callback.onFailure(e);
    }
  }
}
