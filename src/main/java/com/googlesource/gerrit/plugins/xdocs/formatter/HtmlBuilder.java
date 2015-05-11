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

package com.googlesource.gerrit.plugins.xdocs.formatter;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

import com.google.gerrit.server.GerritPersonIdent;
import com.google.inject.Inject;

import org.eclipse.jgit.lib.PersonIdent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HtmlBuilder {
  private final DateFormat rfc2822DateFormatter;
  private final StringBuilder html = new StringBuilder();

  @Inject
  HtmlBuilder(@GerritPersonIdent PersonIdent gerritIdent) {
    rfc2822DateFormatter =
        new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
    rfc2822DateFormatter.setCalendar(Calendar.getInstance(
        gerritIdent.getTimeZone(), Locale.US));
  }

  public HtmlBuilder startDocument() {
    return open("html");
  }

  public HtmlBuilder endDocument() {
    return close("html");
  }

  public HtmlBuilder openBody() {
    return open("body");
  }

  public HtmlBuilder closeBody() {
    return close("body");
  }

  public HtmlBuilder openHead() {
    return open("head");
  }

  public HtmlBuilder closeHead() {
    return close("head");
  }

  public HtmlBuilder openTable() {
    return open("table");
  }

  public HtmlBuilder openTable(String styleName) {
    return open("table", styleName);
  }

  public HtmlBuilder closeTable() {
    return close("table");
  }

  public HtmlBuilder openRow() {
    return open("tr");
  }

  public HtmlBuilder closeRow() {
    return close("tr");
  }

  public HtmlBuilder appendCellHeader(String s) {
    return open("th").append(s).close("th");
  }

  public HtmlBuilder appendCell(String s) {
    return open("td").append(s).close("td");
  }

  public HtmlBuilder appendCell() {
    return appendCell("");
  }

  public HtmlBuilder appendDateCell(long date) {
    return open("td").appendDate(date).close("td");
  }

  public HtmlBuilder open(String tag) {
    html.append("<").append(tag).append(">");
    return this;
  }

  public HtmlBuilder open(String tag, String styleName) {
    html.append("<")
        .append(tag)
        .append(" ")
        .append("class=")
        .append(styleName)
        .append(">");
    return this;
  }

  public HtmlBuilder close(String tag) {
    html.append("</").append(tag).append(">");
    return this;
  }

  public HtmlBuilder append(String s) {
    html.append(escapeHtml(s));
    return this;
  }

  public HtmlBuilder appendDate(long date) {
    html.append(rfc2822DateFormatter.format(new Date(date)));
    return this;
  }

  @Override
  public String toString() {
    return html.toString();
  }
}
