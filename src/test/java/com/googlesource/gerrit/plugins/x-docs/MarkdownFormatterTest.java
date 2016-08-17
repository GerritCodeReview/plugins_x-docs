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

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class MarkdownFormatterTest {

  private static final String PROLOG = "<html><head><style type=\"text/css\">\n\n</style></head><body>\n<p>";
  private static final String EPILOG = "</p>\n</body></html>";

  private ConfigSection cfg;
  private MarkdownFormatter formatter;

  @Before
  public void setUp() throws IOException {
    FormatterUtil util = createNiceMock(FormatterUtil.class);

    // For easier result comparison, avoid the internal MarkdownFormatter to apply the default CSS.
    expect(util.getInheritedCss(anyString(), anyString(), anyString(), anyString())).andReturn("");

    replay(util);

    cfg = createNiceMock(ConfigSection.class);

    // Do not expect any behavior from the ConfigSection itself.
    replay(cfg);

    Formatters formatters = createNiceMock(Formatters.class);

    // Avoid a NPE by just returning the ConfigSection mock object.
    expect(formatters.getFormatterConfig(anyString(), anyString())).andReturn(cfg);

    replay(formatters);

    formatter = new MarkdownFormatter(util, formatters);
  }

  @Test
  public void basicTextFormattingWorks() throws IOException {
    String raw = "*italic* **bold** `monospace`";
    String formatted = PROLOG + "<em>italic</em> <strong>bold</strong> <code>monospace</code>" + EPILOG;
    assertEquals(formatted, formatter.format(null, null, null, null, cfg, raw));
  }
}
