// Copyright (C) 2016 The Android Open Source Project
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

package com.googlesource.gerrit.plugins.xdocs.formatter;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;

import com.googlesource.gerrit.plugins.xdocs.ConfigSection;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;

public class AsciidoctorFormatterTest {

  private ConfigSection cfg;
  private AsciidoctorFormatter formatter;

  @Before
  public void setUp() throws IOException {
    FormatterUtil util = createNiceMock(FormatterUtil.class);

    // To simplify things, make applyCss() a no-op and return the HTML code as-is.
    expect(util.applyCss(anyString(), anyString(), anyString())).andAnswer(
      new IAnswer<String>() {
        @Override
        public String answer() throws Throwable {
          // The first argument is the HTML code.
          return (String) getCurrentArguments()[0];
        }
      }
    );

    replay(util);

    cfg = createNiceMock(ConfigSection.class);

    // Do not expect any behavior from the ConfigSection itself.
    replay(cfg);

    Formatters formatters = createNiceMock(Formatters.class);

    // Avoid a NPE by just returning the ConfigSection mock object.
    expect(formatters.getFormatterConfig(anyString(), anyString())).andReturn(cfg);

    replay(formatters);

    formatter = new AsciidoctorFormatter(util, formatters);
  }

  @Test
  public void basicTextFormattingWorks() throws IOException {
    String raw = "_italic_ *bold* `monospace`";
    String formatted = "<em>italic</em> <strong>bold</strong> <code>monospace</code>";
    assertEquals(1, StringUtils.countMatches(formatter.format(null, null, null, null, cfg, raw), formatted));
  }
}
