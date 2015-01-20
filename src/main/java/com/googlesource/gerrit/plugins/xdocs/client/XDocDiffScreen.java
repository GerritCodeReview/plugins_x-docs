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
import com.google.gerrit.plugin.client.Plugin;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineHyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import com.googlesource.gerrit.plugins.xdocs.client.ChangeInfo.EditInfo;
import com.googlesource.gerrit.plugins.xdocs.client.ChangeInfo.RevisionInfo;

import java.util.List;

public abstract class XDocDiffScreen extends VerticalPanel {
  protected final String changeId;
  protected final String path;
  protected String revisionA;
  protected String revisionB;
  protected int patchSet;
  protected Integer base;
  private FlowPanel iconPanel;
  private FlowPanel additionalIconPanel;

  XDocDiffScreen(String changeId, final String patchSet, String path) {
    setStyleName("xdocs-panel");

    this.changeId = changeId;
    this.path = path;

    ChangeApi.getChangeInfo(changeId, new AsyncCallback<ChangeInfo>() {

      @Override
      public void onSuccess(final ChangeInfo change) {
        change.revisions().copyKeysIntoChildren("name");
        ChangeApi.edit(change._number(), new AsyncCallback<EditInfo>() {
          @Override
          public void onSuccess(EditInfo edit) {
            if (edit != null) {
              change.revisions().put(edit.name(), RevisionInfo.fromEdit(edit));
            }

            parseRevisions(change, patchSet);
            if (revisionA == null) {
              ProjectApi.getCommitInfo(change.project(), change.current_revision(),
                  new AsyncCallback<CommitInfo>() {
                    @Override
                    public void onSuccess(CommitInfo commit) {
                      if (commit.parents() != null) {
                        List<CommitInfo> parents = Natives.asList(commit.parents());
                        if (!parents.isEmpty()) {
                          revisionA = parents.get(0).commit();
                        }
                      }
                      show(change);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                      // never invoked
                    }
                  });
            } else {
              show(change);
            }
          }

          @Override
          public void onFailure(Throwable caught) {
            // never invoked
          }
        });
      }

      private void show(ChangeInfo change) {
        addHeader(change);
        init();
        display(change);
      }

      @Override
      public void onFailure(Throwable caught) {
        showError("Unable to load change " + XDocDiffScreen.this.changeId
            + ": " + caught.getMessage());
      }
    });
  }

  protected abstract void display(ChangeInfo change);

  protected String getPath() {
    return path;
  }

  protected String getRevisionA() {
    return revisionA;
  }

  protected String getRevisionB() {
    return revisionB;
  }

  private void parseRevisions(ChangeInfo change, String patchSetString) {
    int i = patchSetString.indexOf("..");
    if (i > 0) {
      base = parsePatchSet(patchSetString.substring(0, i));
      revisionA = getRevision(change, base);
      if (patchSetString.length() > i + 2) {
        patchSet = parsePatchSet(patchSetString.substring(i + 2));
        revisionB = getRevision(change, patchSet);
      } else {
        throw new IllegalArgumentException("Invalid patch set: " + patchSetString);
      }
    } else {
      patchSet = parsePatchSet(patchSetString);
      revisionB = getRevision(change, patchSet);
    }
  }

  private static String getRevision(ChangeInfo change, int patchSet) {
    for (RevisionInfo rev : Natives.asList(change.revisions().values())) {
      if (rev.is_edit()) {
        return rev.commit().commit();
      }
      if (rev._number() == patchSet) {
        return rev.ref();
      }
    }
    throw new IllegalArgumentException("Patch set " + patchSet + " not found.");
  }

  private static int parsePatchSet(String patchSet) {
    try {
      return Integer.valueOf(patchSet);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid patch set: " + patchSet);
    }
  }

  private void addHeader(ChangeInfo change) {
    HorizontalPanel p = new HorizontalPanel();
    p.setStyleName("xdocs-header");
    p.add(getPathHeader(change));

    iconPanel = new FlowPanel();
    iconPanel.setStyleName("xdocs-icon-panel");
    p.add(iconPanel);
    additionalIconPanel = new FlowPanel();
    iconPanel.add(additionalIconPanel);
    addNavigationButtons(change);

    add(p);
  }

  protected void init() {
  }

  private Widget getPathHeader(ChangeInfo change) {
    HorizontalPanel p = new HorizontalPanel();
    p.setStyleName("xdocs-file-header");
    p.add(new InlineHyperlink(change.project(), "/admin/projects/" + change.project()));
    p.add(new Label("/"));
    p.add(new Label(path));
    return p;
  }

  private void addNavigationButtons(final ChangeInfo change) {
    DiffApi.list(changeId, patchSet, base,
        new AsyncCallback<NativeMap<FileInfo>>() {
      @Override
      public void onSuccess(NativeMap<FileInfo> result) {
        JsArray<FileInfo> files = result.values();
        FileInfo.sortFileInfoByPath(files);
        int index = 0;
        for (int i = 0; i < files.length(); i++) {
          if (path.equals(files.get(i).path())) {
            index = i;
            break;
          }
        }

        FileInfo prevInfo = index == 0 ? null : files.get(index - 1);
        if (prevInfo != null) {
          iconPanel.add(createNavLink(XDocsPlugin.RESOURCES.goPrev(),
              change, patchSet, base, prevInfo));
        }

        iconPanel.add(createIcon(XDocsPlugin.RESOURCES.goUp(),
            "Up to change", toChange(change)));

        FileInfo nextInfo = index == files.length() - 1
            ? null
            : files.get(index + 1);
        if (nextInfo != null) {
          iconPanel.add(createNavLink(XDocsPlugin.RESOURCES.goNext(),
              change, patchSet, base, nextInfo));
        }
      }

      @Override
      public void onFailure(Throwable caught) {
        showError("Unable to load files of change " + changeId + ": "
            + caught.getMessage());
      }
    });
  }

  private InlineHyperlink createNavLink(ImageResource res,
      final ChangeInfo change, final int patchSet, final Integer base,
      final FileInfo file) {
    final InlineHyperlink link = createIcon(
        res, FileInfo.getFileName(file.path()),
        toFile(change, patchSet, base, file));
    XDocApi.checkHtml(XDocApi.getUrl(change.project(),
        getRevision(change, patchSet), file.path()),
        new AsyncCallback<VoidResult>() {
      @Override
      public void onSuccess(VoidResult result) {
        link.setTargetHistoryToken(
            toPreview(change, patchSet, base, file));
      }

      @Override
      public void onFailure(Throwable caught) {
      }
    });
    return link;
  }

  protected static InlineHyperlink createIcon(ImageResource res, String tooltip, String target) {
    InlineHyperlink l = new InlineHyperlink(
        AbstractImagePrototype.create(res).getHTML(), true, target);
    if (tooltip != null) {
      l.setTitle(tooltip);
    }
    return l;
  }

  protected void addIcon(InlineHyperlink icon) {
    additionalIconPanel.add(icon);
  }

  private String toPreview(ChangeInfo change, int patchSet,
      Integer base, FileInfo file) {
    String panel = getPanel();
    return "/x/" + Plugin.get().getName()
        + toPatchSet(change, patchSet, base)
        + file.path()
        + (panel != null ? "," + panel : "");
  }

  private String toFile(ChangeInfo change, int patchSet, Integer base,
      FileInfo file) {
    String panel = file.binary() ? "unified" : getPanel();
    return toPatchSet(change, patchSet, base)
        + file.path()
        + (panel != null ? "," + panel : "");
  }

  protected String getPanel() {
    return null;
  }

  private static String toPatchSet(ChangeInfo change, int patchSet, Integer base) {
    return toChange(change)
        + (base != null ? patchSet + ".." + base : patchSet) + "/";
  }

  private static String toChange(ChangeInfo change) {
    return "/c/" + change._number() + "/";
  }

  protected void showError(String message) {
    Label l = new Label(message);
    l.setStyleName("xdocs-error");
    add(l);
  }

  protected static int addRow(FlexTable table) {
    int row = table.getRowCount();
    table.insertRow(row);
    return row;
  }
}
