/* Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

decorate(document.getElementsByTagName('h1'));
decorate(document.getElementsByTagName('h2'));
decorate(document.getElementsByTagName('h3'));
decorate(document.getElementsByTagName('h4'));

var divs = document.getElementsByTagName('div');
var arr = new Array();
var excluded = getExcludedIds();
for(var i = 0; i < divs.length; i++) {
  var d = divs[i];
  var id = d.getAttribute('id');
  if (id != null && !(id in excluded)) {
    arr[arr.length] = d;
  }
}
decorate(arr);

var anchors = document.getElementsByTagName('a');
arr = new Array();
for(var i = 0; i < anchors.length; i++) {
  var a = anchors[i];
  // if the anchor has no id there is no target to
  // which we can link
  if (a.getAttribute('id') != null) {
    // if the anchor is empty there is no content which
    // can receive the mouseover event, an empty anchor
    // applies to the element that follows, move the
    // element that follows into the anchor so that there
    // is content which can receive the mouseover event
    if (a.firstChild == null) {
      var next = a.nextSibling;
      if (next != null) {
        next.parentNode.removeChild(next);
        a.appendChild(next);
      }
    }
    arr[arr.length] = a;
  }
}
decorate(arr);

function decorate(e) {
  for(var i = 0; i < e.length; i++) {
    e[i].onmouseover = function (evt) {
      var element = this;
      // do nothing if the link icon is currently showing
      var a = element.firstChild;
      if (a != null && a instanceof Element
          && a.getAttribute('id') == 'LINK') {
        return;
      }

      // if there is no id there is no target to link to
      // insert an id if needed
      var id = element.getAttribute('id');
      if (id == null) {
        id = element.textContent;
        if (id != null) {
          id = id.replace(/ /g,"_");
          element.setAttribute('id', id);
        } else {
          return;
        }
      }

      // create and show a link icon that links to this element
      a = document.createElement('a');
      a.setAttribute('id', 'LINK');
      var loc = parent.document.location.toString();
      var i = -1;
      if (loc.indexOf('@URL@' + '#') == 0) {
        i = loc.indexOf('#', loc.indexOf('#') + 1);
      } else {
        i = loc.indexOf('#');
      }
      if (i != -1) {
        loc = loc.substring(0, i);
      }
      a.setAttribute('href', loc + '#' + id);
      a.setAttribute('style', 'position: absolute;'
          + ' left: ' + (element.offsetLeft - 16 - 2 * 4) + 'px;'
          + ' padding-left: 4px; padding-right: 4px; padding-top:4px;');
      var img = document.createElement('img');
      img.setAttribute('src', '/plugins/xdocs/static/link.png');
      img.setAttribute('style', 'background-color: #FFFFFF;');
      a.appendChild(img);
      element.insertBefore(a, element.firstChild);

      // remove the link icon when the mouse is moved away,
      // but keep it shown if the mouse is over the element, the link or the icon
      hide = function(evt) {
        if (document.elementFromPoint(evt.clientX, evt.clientY) != element
            && document.elementFromPoint(evt.clientX, evt.clientY) != a
            && document.elementFromPoint(evt.clientX, evt.clientY) != img
            && element.contains(a)) {
          element.removeChild(a);
        }
      }
      element.onmouseout = hide;
      a.onmouseout = hide;
      img.onmouseout = hide;
    }
  }
}

function getExcludedIds() {
  var excluded = {};
  excluded['header'] = true;
  excluded['toc'] = true;
  excluded['toctitle'] = true;
  excluded['content'] = true;
  excluded['preamble'] = true;
  excluded['footer'] = true;
  excluded['footer-text'] = true;
  return excluded;
}
