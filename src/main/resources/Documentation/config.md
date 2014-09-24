Configuration
=============

The configuration of the @PLUGIN@ plugin is done on project level in
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
