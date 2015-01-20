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
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Frame;

import com.googlesource.gerrit.plugins.xdocs.client.PatchSetSelectBox.DiffView;
import com.googlesource.gerrit.plugins.xdocs.client.PatchSetSelectBox.DisplaySide;

public class XDocSideBySideDiffScreen extends XDocDiffScreen {
  static class Factory implements Screen.EntryPoint {
    @Override
    public void onLoad(Screen screen) {
      String change = URL.decode(screen.getToken(1));
      String patchSet = URL.decode(screen.getToken(2));
      String path = URL.decode(screen.getToken(4));
      screen.show(new XDocSideBySideDiffScreen(change, patchSet, path));
      screen.setWindowTitle(FileInfo.getFileName(path));
    }
  }

  XDocSideBySideDiffScreen(String change, String patchSet, String path) {
    super(change, patchSet, path);
  }

  @Override
  protected void display(ChangeInfo change) {
    String frameIdA = "xdoc_sidebyside_diff_a_iframe";
    Frame frameA = getRevisionA() != null
        ? new Frame(XDocApi.getUrl(change.project(), getRevisionSideA(), getPath()))
        : new Frame();
    frameA.getElement().setId(frameIdA);
    XDocScreen.resize(frameA, frameIdA);

    String frameIdB = "xdoc_sidebyside_diff_b_iframe";
    Frame frameB =
        new Frame(XDocApi.getUrl(change.project(), getRevisionSideB(), getPath()));
    frameB.getElement().setId(frameIdB);
    XDocScreen.resize(frameB, frameIdB);

    FlexTable t = new FlexTable();
    t.setStyleName("xdocs-diff-table");
    int row = addRow(t);
    t.setWidget(row, 0, new PatchSetSelectBox(
        DiffView.SIDE_BY_SIDE, DisplaySide.A, change, base, patchSet, path));
    t.setWidget(row, 1, new PatchSetSelectBox(
        DiffView.SIDE_BY_SIDE,  DisplaySide.B, change, base, patchSet, path));
    row = addRow(t);
    t.setWidget(row, 0, frameA);
    t.setWidget(row, 1, frameB);
    add(t);
  }

  private String getRevisionSideA() {
    return getRevisionA() + "<-" + getRevisionB();
  }

  private String getRevisionSideB() {
    return (getRevisionA() != null ? getRevisionA() : "") + "->" + getRevisionB();
  }

  @Override
  protected void init() {
    addIcon(createIcon(
        XDocsPlugin.RESOURCES.sideBySideDiff(),
        "side-by-side text diff",
        XDocsPlugin.getSideBySideDiffUrl(changeId, base, patchSet, path)));
    addIcon(createIcon(
        XDocsPlugin.RESOURCES.unifiedDiffPreview(),
        "unified preview diff",
        XDocsPlugin.getUnifiedPreviewDiffUrl(changeId, base, patchSet, path)));
  }
}
