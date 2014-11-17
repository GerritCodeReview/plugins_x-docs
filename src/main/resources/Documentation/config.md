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
    indexRef = master
    indexFile = Documentation/README.md
```

<a id="webIndexRef">
web.indexRef
:	The reference from which the documentation should be loaded if a
	revision is not specified.

	The documentation links in the project list will link to the index
	file in this reference.

	For branches the `refs/heads/` prefix may be omitted, all other
	refs must be fully specified.

	Inherited from the parent project if not specified.

	Default: `HEAD`

<a id="webIndexFile">
web.indexFile
:	The documentation file that serves as entry point for the project
	documentation.

	The documentation links in web UI will link to this file.

	Inherited from the parent project if not specified.

	Default: `README.md`

The global formatter configuration can be overridden per project.
Child projects inherit the formatter configuration from the parent
projects. Configuring any parameter for a formatter overrides the
complete inherited/global formatter configuration.

<a id="ext">
formatter.<formatter>.ext
:	Extension of files that should be rendered by this formatter.

	If set to `*` all file extensions are handled by this formatter.

	Multiple extensions may be specified for a formatter.

	Overrides the [globally configured file extensions](#formatterExt)
	for this formatter.

<a id="mimeType">
formatter.<formatter>.mimeType
:	The mime type of files that should be rendered by this formatter.

	Multiple mime types may be specified for a formatter.

	Overrides the [globally configured mime types](#formatterMimeType)
	for this formatter.

<a id="prefix">
formatter.<formatter>.prefix
:	The prefix that a file must match to be handled by this formatter.

	Multiple prefixes may be specified for a formatter.

	Overrides the [globally configured prefixes](#formatterPrefix)
	for this formatter.

<a id="includeToc">
formatter.<formatter>.includeToc
:	Whether a Table Of Contents should be included into each document.

	Overrides the [global configuration of `includeToc`](#formatterIncludeToc)
	for this formatter.

	Supported for the following formatters: `ASCIIDOCTOR`

	Default: `true`

<a id="appendCss">
formatter.<formatter>.appendCss
:	Whether the project-specific CSS should be appended to the default CSS.

	If `false` the default CSS is completely replaced by the
	project-specific CSS.

	Overrides the [global configuration of `appendCss`](#formatterAppendCss)
	for this formatter.

	Supported for the following formatters: `ASCIIDOCTOR`, `MARKDOWN`

	Default: `true` (project-specific CSS is appended to the default CSS)

<a id="prio">
formatter.<formatter>.prio
:	The priority of this formatter.

	If several formatters can handle a file, the formatter with the
	higher priority is taken. If formatters have the same priority it
	is undefined which formatter is used.

	Overrides the [global configured priority](#formatterPrio) for this
	formatter.

	Default: `0`

<a id="projectCss">
Project-Specific CSS
--------------------

For some formatters a custom CSS file for the rendering can be
provided in the `refs/meta/config` branch of the project:

* `ASCIIDOCTOR`: `@PLUGIN@/asciidoctor.css`
* `MARKDOWN`: `@PLUGIN@/markdown.css`

Custom CSS files are *NOT* inherited from parent projects.

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

	If set to `*` all file extensions are handled by this formatter.

	Multiple extensions may be specified for a formatter.

	Can be overridden on [project-level](#ext).

<a id="formatterMimeType">
formatter.<formatter>.mimeType
:	The mime type of files that will be rendered by this formatter.

	Multiple mime types may be specified for a formatter.

	Can be overridden on [project-level](#mimeType).

<a id="formatterPrefix">
formatter.<formatter>.prefix
:	The prefix that a file must match to be handled by this formatter.

	Multiple prefixes may be specified for a formatter.

	Can be overridden on [project-level](#prefix).

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

	Supported for the following formatters: `ASCIIDOCTOR`, `MARKDOWN`

	Default: `false`

<a id="formatterEnabled">
formatter.<formatter>.enabled
:	Whether this formatter is enabled.

	When a formatter is disabled the `xdocs-x_doc_resources` cache must
	be flushed.

	*CANNOT* be overridden on project-level.

	Default: `true`

<a id="formatterIncludeToc">
formatter.<formatter>.includeToc
:	Whether a Table Of Contents should be included into each document.

	Can be overridden on [project-level](#includeToc).

	Supported for the following formatters: `ASCIIDOCTOR`

	Default: `true`

<a id="formatterAppendCss">
formatter.<formatter>.appendCss
:	Whether project-specific CSS should be appended to the default CSS.

	If `false` the default CSS is completely replaced by the
	project-specific CSS.

	Can be overridden on [project-level](#appendCss).

	Supported for the following formatters: `ASCIIDOCTOR`, `MARKDOWN`

	Default: `true` (project-specific CSS is appended to the default CSS)

<a id="formatterPrio">
formatter.<formatter>.prio
:	The priority of this formatter.

	If several formatters can handle a file, the formatter with the
	higher priority is taken. If formatters have the same priority it
	is undefined which formatter is used.

	Can be overridden on [project-level](#prio).

	Default: `0`

<a id="rawFormatter">
formatter.RAW.formatter
:	The name of the formatter that should be used as RAW formatter.

	The RAW formatter is used when the `raw` URL parameter is appended
	to the project documentation URL.

	When this option is changed the `xdocs-x_doc_resources` cache must
	be flushed.

	*CANNOT* be overridden on project-level.

	Default: `PLAIN_TEXT`

<a id="globalDefaultCss">
Global Default CSS
------------------

Gerrit administrators can override the built-in default CSS by
providing CSS files in `<review-site>/data/@PLUGIN@/css/`:

* `ASCIIDOCTOR`: `asciidoctor.css`
* `MARKDOWN`: `markdown.css`
