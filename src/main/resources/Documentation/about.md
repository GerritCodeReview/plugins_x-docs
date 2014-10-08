This plugin serves Markdown project documentation as HTML pages.

If projects contain documentation as Markdown files, the plugin
automatically serves the generated HTML under
`/@PLUGIN@/project/<project-name>/<file-name>`. The project name and
the file name must be URL encoded.

The file is served from the branch/commit to which `HEAD` points unless
a revision is specified in the URL as
`/@PLUGIN@/project/<project-name>/rev/<rev>/<file-name>`.

```
  /@PLUGIN@/project/external%2Fopenssl/rev/stable-1.3/docs%2Ffaq.md
```

`rev` can be any ref or commit that is visible to the calling user.

If the file name is omitted the plugin serves the `README.md` from the
project if available.

*WARNING:* All HTML blocks as well as inline HTML tags are suppressed.
Both will be accepted in the input but not be contained in the output.

Images that are stored in the project can be included into the Markdown
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
</table>
