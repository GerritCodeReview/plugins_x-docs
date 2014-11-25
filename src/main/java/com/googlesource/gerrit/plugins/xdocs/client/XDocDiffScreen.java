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

import com.google.gerrit.client.rpc.Natives;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineHyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import com.googlesource.gerrit.plugins.xdocs.client.ChangeInfo.RevisionInfo;

public abstract class XDocDiffScreen extends VerticalPanel {
  private final String file;
  private String revisionA;
  private String revisionB;

  XDocDiffScreen(final String change, final String patchSet, String file) {
    setStyleName("xdocs-panel");

    this.file = file;

    ChangeApi.getChangeInfo(change, new AsyncCallback<ChangeInfo>() {

      @Override
      public void onSuccess(ChangeInfo change) {
        addFileHeader(change);
        setRevisions(change, patchSet);
        display(change);
      }

      @Override
      public void onFailure(Throwable caught) {
        showError("Unable to load change " + change + ": " + caught.getMessage());
      }
    });
  }

  protected abstract void display(ChangeInfo change);

  protected String getFile() {
    return file;
  }

  protected String getRevisionA() {
    return revisionA;
  }

  protected String getRevisionB() {
    return revisionB;
  }

  private void setRevisions(ChangeInfo change, String patchSet) {
    int i = patchSet.indexOf("..");
    if (i > 0) {
      this.revisionA = getRevision(change, patchSet.substring(0, i));
      if (patchSet.length() > i + 2) {
        this.revisionB = getRevision(change, patchSet.substring(i + 2));
      } else {
        throw new IllegalArgumentException("Invalid patch set: " + patchSet);
      }
    } else {
      this.revisionB = getRevision(change, patchSet);
      this.revisionA = this.revisionB + "^1";
    }
  }

  private static String getRevision(ChangeInfo change, String patchSet) {
    for (RevisionInfo rev : Natives.asList(change.revisions().values())) {
      try {
        if (rev._number() == Integer.valueOf(patchSet)) {
          return rev.ref();
        }
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Invalid patch set: " + patchSet);
      }
    }
    throw new IllegalArgumentException("Patch set " + patchSet + " not found.");
  }

  private void addFileHeader(ChangeInfo change) {
    HorizontalPanel p = new HorizontalPanel();
    p.setStyleName("xdocs-header");
    p.add(new InlineHyperlink(change.project(), "/admin/projects/" + change.project()));
    p.add(new Label("/"));
    p.add(new Label(getFile()));
    add(p);
  }

  protected void showError(String message) {
    Label l = new Label(message);
    l.setStyleName("xdocs-error");
    add(l);
  }
}
