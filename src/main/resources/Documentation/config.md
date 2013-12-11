Configuration
=============

The configuration of the @PLUGIN@ plugin is done in
`$review_site/etc/@PLUGIN@.config`.

```
  [doc "faq"]
    name = FAQ
    project = docs/faq
```

<a id="name">
`doc.<id>.name`
:	The name of the menu item under the 'Documentation' menu entry.
	By default same as `id`.

<a id="project">
`doc.<id>.project`
:	The name of the project from which the documentation should be
	served.

<a id="branch">
`doc.<id>.branch`
:	The name of the branch from which the documentation should be
	served.
	By default `refs/heads/master`.
