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

public class XDocUnifiedDiffScreen extends XDocDiffScreen {
  static class Factory implements Screen.EntryPoint {
    @Override
    public void onLoad(Screen screen) {
      String change = URL.decode(screen.getToken(1));
      String patchSet = URL.decode(screen.getToken(2));
      String path = screen.getToken(4);
      screen.show(new XDocUnifiedDiffScreen(change, patchSet, path));
      screen.setWindowTitle(FileInfo.getFileName(path));
    }
  }

  XDocUnifiedDiffScreen(String change, String patchSet, String path) {
    super(change, patchSet, path);
  }

  @Override
  protected void display(ChangeInfo change) {
    String frameId = "xdoc_unified_diff_iframe";
    Frame frame =
        new Frame(XDocApi.getUrl(change.project(), getRevision(), getPath()));
    frame.getElement().setId(frameId);
    XDocScreen.resize(frame, frameId);

    FlexTable t = new FlexTable();
    t.setStyleName("xdocs-diff-table");
    t.addStyleName("xdocs-unified-diff-table");
    t.setWidget(addRow(t), 0, new PatchSetSelectBox(
        DiffView.UNIFIED_DIFF, DisplaySide.A, change, base, patchSet, path));
    t.setWidget(addRow(t), 0, new PatchSetSelectBox(
        DiffView.UNIFIED_DIFF, DisplaySide.B, change, base, patchSet, path));
    t.setWidget(addRow(t), 0, frame);
    add(t);
  }

  private String getRevision() {
    return (getRevisionA() != null ? getRevisionA() : "") + "<->" + getRevisionB();
  }

  @Override
  protected String getPanel() {
    return "unified";
  }

  @Override
  protected void init() {
    addIcon(createIcon(
        XDocsPlugin.RESOURCES.unifiedDiff(),
        "unified text diff",
        XDocsPlugin.getUnifiedDiffUrl(changeId, base, patchSet, path)));
    addIcon(createIcon(
        XDocsPlugin.RESOURCES.sideBySideDiffPreview(),
        "side-by-side preview diff",
        XDocsPlugin.getSideBySidePreviewDiffUrl(changeId, base, patchSet, path)));
  }
}
