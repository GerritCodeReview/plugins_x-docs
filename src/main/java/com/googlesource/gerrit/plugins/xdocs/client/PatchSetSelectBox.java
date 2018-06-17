// Copyright (C) 2015 The Android Open Source Project
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
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ImageResourceRenderer;
import com.google.gwt.user.client.ui.InlineHyperlink;
import com.google.gwt.user.client.ui.Label;
import com.googlesource.gerrit.plugins.xdocs.client.ChangeInfo.RevisionInfo;

public class PatchSetSelectBox extends FlowPanel {

  public enum DisplaySide {
    A,
    B
  }

  public static enum DiffView {
    SIDE_BY_SIDE,
    UNIFIED_DIFF
  }

  private final DiffView diffView;
  private final DisplaySide side;
  private final ChangeInfo change;
  private final Integer basePatchSet;
  private final int patchSet;
  private final String path;

  public PatchSetSelectBox(
      DiffView diffView,
      DisplaySide side,
      ChangeInfo change,
      Integer basePatchSet,
      int patchSet,
      String path) {
    this.diffView = diffView;
    this.side = side;
    this.change = change;
    this.path = path;
    this.basePatchSet = basePatchSet;
    this.patchSet = patchSet;

    init();
  }

  private void init() {
    setStyleName("xdocs-patch-set-select-box");
    if (isSideBySideDiff()) {
      addStyleName("xdocs-patch-set-select-box-side-by-side");
    }

    addPatchSetLabel();

    if (sideA()) {
      add(createBaseLink());
    } else if (isUnifiedDiff()) {
      // create hidden 'Base' link to align the patch set links with side A
      add(createHiddenBaseLink());
    }

    JsArray<RevisionInfo> list = change.revisions().values();
    RevisionInfo.sortRevisionInfoByNumber(list);
    for (int i = 0; i < list.length(); i++) {
      add(createLink(list.get(i)));
    }

    if (!FileInfo.COMMIT_MSG.equals(path)) {
      add(createDownloadLink());
    }

    if (showEditIcon()) {
      add(createEditIcon());
    }
  }

  private void addPatchSetLabel() {
    add(new Label("Patch Set"));
    if (isUnifiedDiff()) {
      Label l = new Label(sideA() ? "(-)" : "(+)");
      l.addStyleName("xdocs-monospace");
      add(l);
    }
  }

  private boolean isSideBySideDiff() {
    return diffView == DiffView.SIDE_BY_SIDE;
  }

  private boolean isUnifiedDiff() {
    return diffView == DiffView.UNIFIED_DIFF;
  }

  private boolean sideA() {
    return side == DisplaySide.A;
  }

  private InlineHyperlink createBaseLink() {
    InlineHyperlink link =
        new InlineHyperlink("Base", getUrl(change._number(), null, patchSet, path, diffView));
    if (isBaseSelected()) {
      link.setStyleName("xdocs-patch-set-select-box-selected");
    }
    return link;
  }

  private InlineHyperlink createHiddenBaseLink() {
    InlineHyperlink link = new InlineHyperlink("Base", null);
    link.addStyleName("xdocs-hidden");
    return link;
  }

  private InlineHyperlink createLink(RevisionInfo r) {
    String label = r.is_edit() ? "edit" : Integer.toString(r._number());
    Integer patchSetIdA = sideA() ? Integer.valueOf(r._number()) : basePatchSet;
    int patchSetIdB = sideA() ? patchSet : r._number();
    InlineHyperlink link =
        new InlineHyperlink(
            label, getUrl(change._number(), patchSetIdA, patchSetIdB, path, diffView));
    if (isSelected(r._number())) {
      link.setStyleName("xdocs-patch-set-select-box-selected");
    }
    return link;
  }

  private Anchor createDownloadLink() {
    String base = GWT.getHostPageBaseURL() + "cat/";
    String sideUrl = isBaseSelected() ? "1" : "0";
    int ps =
        isBaseSelected()
            ? change.revision(change.current_revision())._number()
            : getSelectedPatchSet();
    Anchor anchor =
        new Anchor(
            new ImageResourceRenderer().render(XDocsPlugin.RESOURCES.downloadIcon()),
            base + URL.encode(change._number() + "," + ps + "," + path) + "^" + sideUrl);
    anchor.setTitle("Download");
    return anchor;
  }

  private boolean showEditIcon() {
    if (sideA() || !change.isOpen() || !Plugin.get().isSignedIn()) {
      return false;
    }

    if (change.has_edit()) {
      return patchSet == 0;
    }
    return patchSet == change.revision(change.current_revision())._number();
  }

  private Anchor createEditIcon() {
    Anchor anchor =
        new Anchor(
            new ImageResourceRenderer().render(XDocsPlugin.RESOURCES.edit()),
            "#" + getEditUrl(change._number(), patchSet, path));
    anchor.setTitle("Edit");
    return anchor;
  }

  private Integer getSelectedPatchSet() {
    return sideA() ? basePatchSet : Integer.valueOf(patchSet);
  }

  private boolean isBaseSelected() {
    return getSelectedPatchSet() == null;
  }

  private boolean isSelected(int ps) {
    return getSelectedPatchSet() != null && getSelectedPatchSet().intValue() == ps;
  }

  private static String getUrl(
      int changeId, Integer patchSetIdA, int patchSetIdB, String path, DiffView diffView) {
    StringBuilder url = new StringBuilder();
    url.append("/x/");
    url.append(Plugin.get().getName());
    url.append("/c/");
    url.append(changeId);
    url.append("/");
    if (patchSetIdA != null) {
      url.append(patchSetIdA);
      url.append("..");
    }
    url.append(patchSetIdB);
    url.append("/");
    url.append(path);
    if (diffView == DiffView.UNIFIED_DIFF) {
      url.append(",unified");
    }
    return url.toString();
  }

  private static String getEditUrl(int changeId, int patchSetId, String path) {
    StringBuilder url = new StringBuilder();
    url.append("/c/");
    url.append(changeId);
    url.append("/");
    url.append(patchSetId);
    url.append("/");
    url.append(path);
    url.append(",edit");
    return url.toString();
  }
}
