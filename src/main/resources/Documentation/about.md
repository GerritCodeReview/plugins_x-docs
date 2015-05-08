This plugin serves project documentation as HTML pages.

If projects contain documentation, e.g. as Markdown files, the plugin
automatically serves the generated HTML under
`/plugins/@PLUGIN@/project/<project-name>/<file-name>`. The project name must
be URL encoded.

The file is served from the branch/commit to which `HEAD` points unless
a revision is specified in the URL as
`/plugins/@PLUGIN@/project/<project-name>/rev/<rev>/<file-name>`. The revision
must be URL encoded.

```
  /plugins/@PLUGIN@/project/external%2Fopenssl/rev/stable-1.3/docs/faq.md
```

`rev` can be any ref or commit that is visible to the calling user.

If the file name is omitted, by default the plugin serves the
`README.md` from the project if available.

By setting the URL parameter `raw` the document will be returned as raw
unformatted text.

```
  /plugins/@PLUGIN@/project/external%2Fopenssl/rev/stable-1.3/docs/faq.md?raw
```

The `raw` parameter cannot be used for binary files.

*WARNING:* By default inline HTML blocks as well as inline HTML tags
may be [suppressed](config.html#formatterAllowHtml) by formatters. If
suppressed both will be accepted in the input but not be contained in
the output.

Images that are stored in the project can be included into the project
documentation, but they are only rendered if the image mimetype is
configured as a
[safe mimetype](../../../Documentation/config-gerrit.html#mimetype).

<a id="formatters">
Formatters
----------

The plugin includes several formatters for rendering documentation.

<table>
  <tr>
    <th>Formatter</th>
    <th>Name</th>
    <th>Description</th>
    <th>License</th>
    <th>Homepage</th>
  </tr>
  <tr>
    <td><tt>AsciidoctorFormatter</tt></td>
    <td><tt>ASCIIDOCTOR</tt></td>
    <td>Asciidoctor formatter for documentation that is written in
      <a href="http://www.methods.co.nz/asciidoc/userguide.html">Aciidoc</a>
    </td>
    <td>
      asciidoctorj: <a href="../../../Documentation/licenses.html#Apache2_0">Apache2.0</a><br/>
      jruby: <a href="licenses.html#EPL1_0">Eclipse Public License 1.0</a>
    </td>
    <td>
      <a href="http://asciidoctor.org/docs/asciidoctorj/">http://asciidoctor.org/docs/asciidoctorj/</a></br>
      <a href="https://github.com/jruby/jruby">https://github.com/jruby/jruby</a></br>
    </td>
  </tr>
  <tr>
    <td><tt>DocxFormatter</tt></td>
    <td><tt>DOCX</tt></td>
    <td>Formatter for <tt>.docx</tt> files.</td>
    <td><a href="../../../Documentation/licenses.html#Apache2_0">Apache2.0</a><br/></td>
    <td>
      <a href="http://www.docx4java.org/trac/docx4j">http://www.docx4java.org/trac/docx4j</a></br>
    </td>
  </tr>
  <tr>
    <td><tt>ImageFormatter</tt></td>
    <td><tt>IMAGE</tt></td>
    <td>Formatter for images that were configured as safe. Only used if `?formatImage` is appended to the URL.</td>
    <td><a href="../../../Documentation/licenses.html#Apache2_0">Apache2.0</a></td>
    <td><a href="http://commons.apache.org">http://commons.apache.org</a></td>
  </tr>
  <tr>
    <td><tt>MarkdownFormatter</tt></td>
    <td><tt>MARKDOWN</tt></td>
    <td>Formatter for documentation that is written in
      <a href="http://daringfireball.net/projects/markdown/">Markdown</a>
    </td>
    <td><a href="../../../Documentation/licenses.html#Apache2_0">Apache2.0</a></td>
    <td><a href="https://github.com/sirthias/pegdown">https://github.com/sirthias/pegdown</a></td>
  </tr>
  <tr>
    <td><tt>PlainTextFormatter</tt></td>
    <td><tt>PLAIN_TEXT</tt></td>
    <td>Formatter for documentation that is written in plain text.</td>
    <td><a href="../../../Documentation/licenses.html#Apache2_0">Apache2.0</a></td>
    <td><a href="http://commons.apache.org">http://commons.apache.org</a></td>
  </tr>
  <tr>
    <td><tt>ZipFormatter</tt></td>
    <td><tt>ZIP</tt></td>
    <td>Formatter for zip files.</td>
    <td></td>
    <td></td>
  </tr>
</table>

<a id="htmlDiff">
HTML Diff
---------

To generate the HTML diff [daisydiff](http://code.google.com/p/daisydiff/)
is used which is licensed under the
[Apache2.0](../../../Documentation/licenses.html#Apache2_0) license.
