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
import com.google.gerrit.client.rpc.Natives;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

import java.util.Collections;
import java.util.Comparator;

public class ChangeInfo extends JavaScriptObject {
  public final native String project() /*-{ return this.project; }-*/;
  public final native NativeMap<RevisionInfo> revisions() /*-{ return this.revisions; }-*/;
  public final native int _number() /*-{ return this._number; }-*/;
  public final native String current_revision() /*-{ return this.current_revision; }-*/;

  protected ChangeInfo() {
  }

  public static class RevisionInfo extends JavaScriptObject {
    public final native int _number() /*-{ return this._number; }-*/;
    public final native String name() /*-{ return this.name; }-*/;
    public final native String ref() /*-{ return this.ref; }-*/;
    public final native boolean is_edit() /*-{ return this._number == 0; }-*/;
    public final native String edit_base() /*-{ return this.edit_base; }-*/;

    public static int findEditParent(JsArray<RevisionInfo> list) {
      for (int i = 0; i < list.length(); i++) {
        // edit under revisions?
        RevisionInfo editInfo = list.get(i);
        if (editInfo.is_edit()) {
          String parentRevision = editInfo.edit_base();
          // find parent
          for (int j = 0; j < list.length(); j++) {
            RevisionInfo parentInfo = list.get(j);
            String name = parentInfo.name();
            if (name.equals(parentRevision)) {
              // found parent patch set number
              return parentInfo._number();
            }
          }
        }
      }
      return -1;
    }

    public static void sortRevisionInfoByNumber(JsArray<RevisionInfo> list) {
      final int editParent = findEditParent(list);
      Collections.sort(Natives.asList(list), new Comparator<RevisionInfo>() {
        @Override
        public int compare(RevisionInfo a, RevisionInfo b) {
          return num(a) - num(b);
        }

        private int num(RevisionInfo r) {
          return !r.is_edit() ? 2 * (r._number() - 1) + 1 : 2 * editParent;
        }
      });
    }

    protected RevisionInfo () {
    }
  }
}
