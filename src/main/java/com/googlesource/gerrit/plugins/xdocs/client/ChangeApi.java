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

import com.google.gerrit.plugin.client.rpc.RestApi;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.EnumSet;

public class ChangeApi {
  public static void getChangeInfo(String id, AsyncCallback<ChangeInfo> callback) {
    RestApi call = ChangeApi.detail(id);
    addOptions(call, EnumSet.of(
      ListChangesOption.ALL_REVISIONS));
    call.get(callback);
  }

  public static RestApi detail(String id) {
    return call(id, "detail");
  }

  private static RestApi call(String id, String action) {
    return change(id).view(action);
  }

  public static RestApi change(String id) {
    return new RestApi("/changes/").id(id);
  }

  public static void addOptions(RestApi call, EnumSet<ListChangesOption> s) {
    call.addParameterRaw("O", Integer.toHexString(ListChangesOption.toBits(s)));
  }
}
