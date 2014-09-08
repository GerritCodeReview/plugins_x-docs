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
