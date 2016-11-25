include_defs('//bucklets/gerrit_plugin.bucklet')

MODULE = 'com.googlesource.gerrit.plugins.xdocs.XDocs'

if STANDALONE_MODE:
  ASCIIDOCTOR = '//lib/asciidoctor:asciidoc_lib'
  DAISYDIFF = '//lib/daisydiff:daisydiff_lib'
  DOCX4J = '//lib/docx4j:docx4j_lib'
  COMMONS_IO = '//lib/commons:io'
else:
  ASCIIDOCTOR = '//plugins/x-docs/lib/asciidoctor:asciidoc_lib'
  DAISYDIFF = '//plugins/x-docs/lib/daisydiff:daisydiff_lib'
  DOCX4J = '//plugins/x-docs/lib/docx4j:docx4j_lib'
  COMMONS_IO = '//plugins/x-docs/lib/commons:io'

gerrit_plugin(
  name = 'x-docs',
  srcs = glob(['src/main/java/**/*.java']),
  resources = glob(['src/main/**/*']),
  gwt_module = MODULE,
  manifest_entries = [
    'Gerrit-ApiType: plugin',
    'Gerrit-HttpModule: com.googlesource.gerrit.plugins.xdocs.HttpModule',
    'Gerrit-Module: com.googlesource.gerrit.plugins.xdocs.Module',
    'Gerrit-InitStep: com.googlesource.gerrit.plugins.xdocs.XDocInit',
  ],
  deps = [
    ASCIIDOCTOR,
    DAISYDIFF,
    DOCX4J,
    COMMONS_IO,
  ],
)

# this is required for bucklets/tools/eclipse/project.py to work
java_library(
  name = 'classpath',
  deps = [':x-docs__plugin'],
)

java_test(
  name = 'x-docs_tests',
  srcs = glob(['src/test/java/**/*.java']),
  labels = ['xdocs'],
  deps = GERRIT_PLUGIN_API + GERRIT_TESTS + [
    ':x-docs__plugin',
  ],
)
