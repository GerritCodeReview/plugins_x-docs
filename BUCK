include_defs('//bucklets/gerrit_plugin.bucklet')

MODULE = 'com.googlesource.gerrit.plugins.xdocs.XDocs'

if __standalone_mode__:
  PROV_DEPS = ['//lib/gerrit:gwtexpui']
  ASCIIDOCTOR = '//lib/asciidoctor:asciidoc_lib'
else:
  PROV_DEPS = [
    '//gerrit-gwtexpui:Clippy',
    '//gerrit-gwtexpui:GlobalKey',
    '//gerrit-gwtexpui:SafeHtml',
    '//gerrit-gwtexpui:UserAgent',
  ]
  ASCIIDOCTOR = '//plugins/x-docs/lib/asciidoctor:asciidoc_lib'

gerrit_plugin(
  name = 'x-docs',
  srcs = glob(['src/main/java/**/*.java']),
  resources = glob(['src/main/**/*']),
  gwt_module = MODULE,
  manifest_entries = [
    'Gerrit-PluginName: xdocs',
    'Gerrit-ApiType: plugin',
    'Gerrit-ApiVersion: 2.11-SNAPSHOT',
    'Gerrit-HttpModule: com.googlesource.gerrit.plugins.xdocs.HttpModule',
    'Gerrit-Module: com.googlesource.gerrit.plugins.xdocs.Module',
    'Gerrit-InitStep: com.googlesource.gerrit.plugins.xdocs.XDocInit',
  ],
  deps = [ASCIIDOCTOR],
  provided_deps = PROV_DEPS,
)

# this is required for bucklets/tools/eclipse/project.py to work
java_library(
  name = 'classpath',
  deps = [':x-docs__plugin'],
)

