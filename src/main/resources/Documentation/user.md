User Guide
==========

The @PLUGIN@ plugin serves project documentation as HTML pages. Project
documentation can be written in different syntaxes, e.g. as
[Markdown](http://daringfireball.net/projects/markdown/) or
[Asciidoc](http://www.methods.co.nz/asciidoc/userguide.html).

By default the entry point to the project documentation is a
`README.md` file in the root directory of a project. Another index file
may be [configured](config.html#webIndexFile).

If a project contains the index file in its root directory then a link
to the project documentation is displayed for it in the
[project list](@URL@#/admin/projects/).

![Screenshot1](images/project-list-with-doc-links.png)

Clicking on the documentation icon opens the rendered HTML of the
index file in a new tab.

![Screenshot2](images/rendered-readme.png)

If a project is selected the project documentation is also available
from the `Projects` menu.

![Screenshot3](images/project-documentation-menu.png)

The documentation is served from the tip of the default branch.
The documentation for other branches is available from the project
branches screen.

![Screenshot4](images/project-branches-list-with-doc-links.png)

When reviewing documentation changes a preview of the rendered HTML is
available for project documentation files. This allows to detect any
formatting issues quickly. The preview is available from the
side-by-side diff view and it is opened in a new tab.

![Screenshot5](images/side-by-side-diff-view-with-preview.png)

The URL scheme of the plugin allows to access the HTML of all project
documentation files in the project from any revision (branch or
commit). The project name, the revision and the file name must be URL
encoded.

```
  /@PLUGIN@/project/<project>/rev/<revision>/<file>
```

E.g.:

```
  /@PLUGIN@/project/dev%2Ftools%2Fcode-checks/rev/stable-1.3/docs%2Ffaq.md
```

A project documentation file can contain links to other project
documentation files which are contained in the project.

```
  The installation is described in the [installation guide](install-guide.md).
```

Images that are stored in the project can be included into the project
documentation, but they are only rendered if the image mimetype is
configured on the Gerrit server as a
[safe mimetype](../../../Documentation/config-gerrit.html#mimetype).

```
  ![Screenshot](images/screenshot.png)
```

By default for Markdown inline HTML blocks as well as inline HTML tags
are [suppressed](config.html#formatterAllowHtml). If suppressed both
will be accepted in the input but not be contained in the output.

Project documentation files may include the following macros which are
automatically replaced when the HTML pages are generated.

* `\@URL@`: The web URL of the Gerrit server.
* `\@PROJECT@`: The name of the project from which the documentation is shown.
* `\@PROJECT_URL@`: The web URL of the project from which the documentation is shown.
* `\@REVISION@`: The abbreviated ID of the revision from which the documentation is shown.
* `\@GIT_DESCRIPTION@`: The output of `git describe` on the revision
  from which the documentation is shown, or the abbreviated ID of the
  revision if no names are found.

The plugin provides several [formatters](about.html#formatters) for
different project documentation syntaxes. The formatters can be
[configured](config.html#projectConfig) on project level and for some
formatters the CSS can be [customized](config.html#projectCss).
