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
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class XDocSideBySideDiffScreen extends XDocDiffScreen {
  static class Factory implements Screen.EntryPoint {
    @Override
    public void onLoad(Screen screen) {
      String change = URL.decode(screen.getToken(1));
      String patchSet = URL.decode(screen.getToken(2));
      String path = URL.decode(screen.getToken(4));
      screen.show(new XDocSideBySideDiffScreen(change, patchSet, path));
    }
  }

  XDocSideBySideDiffScreen(String change, String patchSet, String path) {
    super(change, patchSet, path);
  }

  @Override
  protected void display(ChangeInfo change) {
    String frameIdA = "xdoc_sidebyside_diff_a_iframe";
    Frame frameA =
        new Frame(XDocScreen.getUrl(change.project(), getRevisionSideA(), getPath()));
    frameA.getElement().setId(frameIdA);
    XDocScreen.resize(frameA, frameIdA);

    String frameIdB = "xdoc_sidebyside_diff_b_iframe";
    Frame frameB =
        new Frame(XDocScreen.getUrl(change.project(), getRevisionSideB(), getPath()));
    frameB.getElement().setId(frameIdB);
    XDocScreen.resize(frameB, frameIdB);

    HorizontalPanel p = new HorizontalPanel();
    p.setVerticalAlignment(ALIGN_MIDDLE);
    p.setStyleName("xdocs-sidebyside-panel");
    p.add(frameA);
    p.add(frameB);
    add(p);
  }

  private String getRevisionSideA() {
    return getRevisionA() + "<-" + getRevisionB();
  }

  private String getRevisionSideB() {
    return getRevisionA() + "->" + getRevisionB();
  }
}
