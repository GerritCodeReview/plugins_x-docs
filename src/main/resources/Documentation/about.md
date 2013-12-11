This plugin allows to extend the Gerrit documentation by custom
content.

Custom documentation content is developed in a Gerrit project that is
registered as documentation extension in the
[plugin configuration](config.html). The @PLUGIN@ plugin registers a
menu item under the 'Documentation' menu entry for it and serves the
content directly from the configured project. The content in the Gerrit
project must be available as Markdown files. As entry point an
`index.md` must exist.

With this plugin custom documentation (e.g. company specific FAQ's,
site specific instructions, custom tutorials etc.) can be plugged in
into the Gerrit WebUI. The custom documentation is versioned in a Git
repository and the normal Gerrit workflows like code review can be used
to develop the custom content.
