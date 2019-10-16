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

import com.google.common.base.Strings;
import com.google.gerrit.extensions.restapi.IdString;
import com.google.gerrit.reviewdb.client.Project;
import java.util.Objects;
import org.eclipse.jgit.lib.ObjectId;

public class XDocResourceKey {
  private final String formatter;
  private final Project.NameKey project;
  private final String resource;
  private final ObjectId revId;
  private final ObjectId metaConfigRevId;
  private final String parentsHash;
  private final ObjectId revIdB;
  private final DiffMode diffMode;

  XDocResourceKey(
      String formatter,
      Project.NameKey project,
      String r,
      ObjectId revId,
      ObjectId metaConfigRevId,
      String parentsHash,
      ObjectId revIdB,
      DiffMode diffMode) {
    this.formatter = formatter;
    this.project = project;
    this.resource = r;
    this.revId = revId;
    this.metaConfigRevId = metaConfigRevId;
    this.parentsHash = parentsHash;
    this.revIdB = revIdB;
    this.diffMode = diffMode != null ? diffMode : DiffMode.NO_DIFF;
  }

  public String getFormatter() {
    return formatter;
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

  public ObjectId getRevIdB() {
    return revIdB;
  }

  public DiffMode getDiffMode() {
    return diffMode;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        formatter, project, resource, revId, metaConfigRevId, parentsHash, revIdB, diffMode);
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof XDocResourceKey) {
      XDocResourceKey rk = (XDocResourceKey) other;
      return Objects.equals(formatter, rk.formatter)
          && Objects.equals(project, rk.project)
          && Objects.equals(resource, rk.resource)
          && Objects.equals(revId, rk.revId)
          && Objects.equals(metaConfigRevId, rk.metaConfigRevId)
          && Objects.equals(parentsHash, rk.parentsHash)
          && Objects.equals(revIdB, rk.revIdB)
          && Objects.equals(diffMode, rk.diffMode);
    }
    return false;
  }

  public String asString() {
    StringBuilder b = new StringBuilder();
    b.append(IdString.fromDecoded(formatter).encoded());
    b.append("/");
    b.append(IdString.fromDecoded(project.get()).encoded());
    b.append("/");
    b.append(resource != null ? IdString.fromDecoded(resource).encoded() : "");
    b.append("/");
    b.append(revId != null ? revId.name() : "");
    b.append("/");
    b.append(metaConfigRevId != null ? metaConfigRevId.name() : "");
    b.append("/");
    b.append(Strings.nullToEmpty(parentsHash));
    b.append("/");
    b.append(revIdB != null ? revIdB.name() : "");
    b.append("/");
    b.append(diffMode.name());
    return b.toString();
  }

  public static XDocResourceKey fromString(String str) {
    String[] s = str.split("/");
    String formatter = null;
    String project = null;
    String file = null;
    String revision = null;
    String metaConfigRevision = null;
    String parentsHash = null;
    String revisionB = null;
    String diffMode = null;
    if (s.length > 0) {
      formatter = IdString.fromUrl(s[0]).get();
    }
    if (s.length > 1) {
      project = IdString.fromUrl(s[1]).get();
    }
    if (s.length > 2) {
      file = IdString.fromUrl(s[2]).get();
    }
    if (s.length > 3) {
      revision = s[3];
    }
    if (s.length > 4) {
      metaConfigRevision = s[4];
    }
    if (s.length > 5) {
      parentsHash = s[5];
    }
    if (s.length > 6) {
      revisionB = s[6];
    }
    if (s.length > 7) {
      diffMode = s[7];
    }
    return new XDocResourceKey(
        formatter,
        new Project.NameKey(project),
        file,
        toObjectId(revision),
        toObjectId(metaConfigRevision),
        parentsHash,
        toObjectId(revisionB),
        DiffMode.valueOf(diffMode));
  }

  private static ObjectId toObjectId(String id) {
    return !Strings.isNullOrEmpty(id) ? ObjectId.fromString(id) : null;
  }
}
