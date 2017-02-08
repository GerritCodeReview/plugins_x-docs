package(default_visibility = ["//visibility:public"])

java_library(
  name = 'docx4j_lib',
  runtime_deps = [
    '//plugins/x-docs/lib/antlr:antlr_lib',
    '//plugins/x-docs/lib/commons:commons_lib',
    ':guava',
  ],
  exports = [
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

java_library(
  name = 'docx4j',
  exports = ["@docx4j//jar"],
)

java_library(
  name = 'guava',
  exports = ["@guava//jar"],
)

java_library(
  name = 'mbassador',
  exports = ["@mbassador//jar"],
)

java_library(
  name = 'slf4j-api',
  exports = ["@slf4j-api//jar"],
)

java_library(
  name = 'wmf2svg',
  exports = ["@wmf2svg//jar"],
)

java_library(
  name = 'xml-apis',
  exports = ["@xml-apis//jar"],
)
