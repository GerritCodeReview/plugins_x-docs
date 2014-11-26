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

import com.google.gerrit.client.rpc.NativeMap;
import com.google.gwt.core.client.JavaScriptObject;

public class ChangeInfo extends JavaScriptObject {
  public final native String project() /*-{ return this.project; }-*/;
  public final native NativeMap<RevisionInfo> revisions() /*-{ return this.revisions; }-*/;
  public final native int _number() /*-{ return this._number; }-*/;

  protected ChangeInfo() {
  }

  public static class RevisionInfo extends JavaScriptObject {
    public final native int _number() /*-{ return this._number; }-*/;
    public final native String ref() /*-{ return this.ref; }-*/;

    protected RevisionInfo () {
    }
  }
}
