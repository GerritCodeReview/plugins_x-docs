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

import com.google.gerrit.httpd.resources.Resource;
import com.google.gwtexpui.server.CacheHeaders;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Resources {
  public static final Resource METHOD_NOT_ALLOWED =
      new Resource() {
        private static final long serialVersionUID = 1L;

        @Override
        public int weigh() {
          return 0;
        }

        @Override
        public void send(HttpServletRequest req, HttpServletResponse res) throws IOException {
          CacheHeaders.setNotCacheable(res);
          res.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }

        @Override
        public boolean isUnchanged(long latestModifiedDate) {
          return false;
        }

        protected Object readResolve() {
          return METHOD_NOT_ALLOWED;
        }
      };
}
