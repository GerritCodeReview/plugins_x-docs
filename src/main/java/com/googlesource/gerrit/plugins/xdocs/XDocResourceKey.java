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

package com.googlesource.gerrit.plugins.xdocs;

import com.google.common.base.Objects;
import com.google.gerrit.extensions.restapi.IdString;
import com.google.gerrit.reviewdb.client.Project;

import org.eclipse.jgit.lib.ObjectId;

public class XDocResourceKey {
  private final Project.NameKey project;
  private final String resource;
  private final ObjectId revId;

  XDocResourceKey(Project.NameKey project, String r, ObjectId revId) {
    this.project = project;
    this.resource = r;
    this.revId = revId;
  }

  public Project.NameKey getProject() {
    return project;
  }

  public String getResource() {
    return resource;
  }

  public ObjectId getRevId() {
    return revId;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(project, resource, revId);
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof XDocResourceKey) {
      XDocResourceKey rk = (XDocResourceKey) other;
      return project.equals(rk.project) && resource.equals(rk.resource)
          && revId.equals(rk.revId);
    }
    return false;
  }

  public String asString() {
    StringBuilder b = new StringBuilder();
    b.append(IdString.fromDecoded(project.get()).encoded());
    b.append("/");
    b.append(resource != null ? IdString.fromDecoded(resource).encoded() : "");
    b.append("/");
    b.append(revId != null ? revId.name() : "");
    return b.toString();
  }

  public static XDocResourceKey fromString(String str) {
    String[] s = str.split("/");
    String project = null;
    String file = null;
    String revision = null;
    if (s.length > 0) {
      project = IdString.fromUrl(s[0]).get();
    }
    if (s.length > 1) {
      file = IdString.fromUrl(s[1]).get();
    }
    if (s.length > 2) {
      revision = s[2];
    }
    return new XDocResourceKey(new Project.NameKey(project), file,
        ObjectId.fromString(revision));
  }
}
