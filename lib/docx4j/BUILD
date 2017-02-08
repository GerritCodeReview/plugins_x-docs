load("//tools/bzl:maven_jar.bzl", "maven_jar")

package(default_visibility = ["//visibility:public"])

java_library(
  name = 'docx4j_lib',
  deps = [
    '//plugins/x-docs/lib/antlr:antlr_lib',
    '//plugins/x-docs/lib/commons:commons_lib',
    ':guava',
  ],
  exported_deps = [
    '//plugins/x-docs/lib/avalon-framework:avalon-framework_lib',
    '//plugins/x-docs/lib/poi:poi_lib',
    '//plugins/x-docs/lib/plutext:plutext_lib',
    '//plugins/x-docs/lib/xalan:xalan_lib',
    '//plugins/x-docs/lib/xmlgraphics:xmlgraphics_lib',
    ':docx4j',
    ':mbassador',
    ':slf4j-api',
    ':wmf2svg',
    ':xml-apis',
  ],
)

maven_jar(
  name = 'docx4j',
  artifact = 'org.docx4j:docx4j:3.2.1',
  sha1 = '35b2ef9f7eb12efe9708b986ecf6e86cfd77a162',
  src_sha1 = '49702b88080bc22f6cfa58aadbf5422bcd904094',
)

maven_jar(
  name = 'guava',
  artifact = 'com.google.guava:guava:18.0',
  sha1 = 'cce0823396aa693798f8882e64213b1772032b09',
  attach_source = False,
)

maven_jar(
  name = 'mbassador',
  artifact = 'net.engio:mbassador:1.2.0',
  sha1 = '7aa2c6f172cc8c59983c5255faa45a16a41e173b',
  src_sha1 = 'd10cdaf71bfcb2002ab904a9d475e60c70c9b6bf',
)

maven_jar(
  name = 'slf4j-api',
  artifact = 'org.slf4j:slf4j-api:1.7.7',
  sha1 = '2b8019b6249bb05d81d3a3094e468753e2b21311',
  src_sha1 = '0acd62e31cc314266e73eebed0b6dd7ea974a0ed',
)

maven_jar(
  name = 'wmf2svg',
  artifact = 'net.arnx:wmf2svg:0.9.7',
  sha1 = '88a871f85a0e765c960af88ccc5a9a0e00384cd8',
  src_sha1 = '6948f9616032bd857c5173b204042889527a3bf3',
)

maven_jar(
  name = 'xml-apis',
  artifact = 'xml-apis:xml-apis:1.3.04',
  sha1 = '90b215f48fe42776c8c7f6e3509ec54e84fd65ef',
  src_sha1 = 'd89105ccbe3fd823865330fd964233baf2b53e88',
)
