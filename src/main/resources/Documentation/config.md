Configuration
=============

<a id="projectConfig">
Project-Specific Configuration
------------------------------

The project-specific configuration of the @PLUGIN@ plugin is done in
the `@PLUGIN@.config` file in the `refs/meta/config` branch of the
project.

```
  [web]
    indexFile = Documentation/README.md
```

<a id="webIndexFile">
web.indexFile
:	The documentation file that serves as entry point for the project
	documentation.

	The documentation links in web UI will link to this file.

	Default: `README.md`

<a id="globalConfig">
Global Configuration
--------------------

The global configuration of the @PLUGIN@ plugin is done in the
`$site_path/etc/@PLUGIN@.config` file.

The plugin contains an init step that creates the initial plugin
configuration.

```
  [formatter "MARKDOWN"]
    mimeType = text/x-markdown
  [formatter "PLAIN_TEXT"]
    mimeType = text/plain
```

Supported formatters:

* `ASCIIDOCTOR`
* `MARKDOWN`
* `PLAIN_TEXT`


<a id="formatterMimeType">
formatter.<formatter>.mimeType
:	The mime type of files that you be rendered by this formatter.

	Multiple mime types may be specified for a formatter.

<a id="formatterAllowHtml">
formatter.<formatter>.allowHtml
:	Whether inline HTML blocks and inline HTML tags are allowed for
    this formatter.

	If `false` inline HTML blocks as well as inline HTML tags are
	suppressed. Both will be accepted in the input but not be contained
	in the output.

	When this option is changed the `xdocs-x_doc_resources` cache must
	be flushed.

	**WARNING:** Allowing HTML for user-provided input is a security
	risk, e.g. code for XSS attacks may be contained in the HTML.

	Supported for the following formatters: `MARKDOWN`

	Default: `false`
