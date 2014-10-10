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

The global formatter configuration can be overridden per project.
Configuring any parameter for a formatter overrides the complete
global formatter configuration.

<a id="ext">
formatter.<formatter>.ext
:	Extension of files that should be rendered by this formatter.

	Multiple extensions may be specified for a formatter.

	Overrides the [globally configured file extensions](#formatterExt)
	for this formatter.

<a id="mimeType">
formatter.<formatter>.mimeType
:	The mime type of files that should be rendered by this formatter.

	Multiple mime types may be specified for a formatter.

	Overrides the [globally configured mime types](#formatterMimeType)
	for this formatter.

<a id="globalConfig">
Global Configuration
--------------------

The global configuration of the @PLUGIN@ plugin is done in the
`$site_path/etc/@PLUGIN@.config` file.

The plugin contains an init step that creates the initial plugin
configuration.

```
  [formatter "ASCIIDOCTOR"]
    ext = adoc
  [formatter "MARKDOWN"]
    mimeType = text/x-markdown
  [formatter "PLAIN_TEXT"]
    mimeType = text/plain
```

Supported formatters:

* `ASCIIDOCTOR`
* `MARKDOWN`
* `PLAIN_TEXT`

<a id="formatterExt">
formatter.<formatter>.ext
:	Extension of files that will be rendered by this formatter.

	Multiple extensions may be specified for a formatter.

	Can be overridden on [project-level](#ext).

<a id="formatterMimeType">
formatter.<formatter>.mimeType
:	The mime type of files that will be rendered by this formatter.

	Multiple mime types may be specified for a formatter.

	Can be overridden on [project-level](#mimeType).

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

	*CANNOT* be overridden on project-level.

	Supported for the following formatters: `MARKDOWN`

	Default: `false`

<a id="formatterEnabled">
formatter.<formatter>.enabled
:	Whether this formatter is enabled.

	When a formatter is disabled the `xdocs-x_doc_resources` cache must
	be flushed.

	*CANNOT* be overridden on project-level.

	Default: `true`
