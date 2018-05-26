load("//tools/bzl:junit.bzl", "junit_tests")
load(
    "//tools/bzl:plugin.bzl",
    "PLUGIN_DEPS",
    "PLUGIN_TEST_DEPS",
    "gerrit_plugin",
)

gerrit_plugin(
    name = "x-docs",
    srcs = glob(["src/main/java/**/*.java"]),
    gwt_module = "com.googlesource.gerrit.plugins.xdocs.XDocs",
    manifest_entries = [
        "Gerrit-PluginName: x-docs",
        "Gerrit-HttpModule: com.googlesource.gerrit.plugins.xdocs.HttpModule",
        "Gerrit-Module: com.googlesource.gerrit.plugins.xdocs.Module",
        "Gerrit-InitStep: com.googlesource.gerrit.plugins.xdocs.XDocInit",
    ],
    resources = glob(["src/main/**/*"]),
    provided_deps = [
        "@org_antlr//jar:neverlink",
        "@commons_codec//jar:neverlink",
        "@guava//jar:neverlink",
        "@log_api//jar:neverlink",
        "@commons_lang//jar:neverlink",
    ],
    deps = [
        ":avalon_framework_lib",
        ":xmlgraphics_lib",
        "@asciidoctor//jar",
        "@commons_io//jar",
        "@daisydiff//jar",
        "@docx4j//jar",
        "@jaxb_svg11//jar",
        "@jaxb_xmldsig_core//jar",
        "@jaxb_xslfo//jar",
        "@jruby//jar",
        "@mbassador//jar",
        "@poi//jar",
        "@poi_scratchpad//jar",
        "@serializer//jar",
        "@wmf2svg//jar",
        "@xalan//jar",
        "@xml_apis//jar",
    ],
)

junit_tests(
    name = "x_docs_tests",
    srcs = glob(["src/test/java/**/*.java"]),
    tags = ["xdocs"],
    deps = PLUGIN_DEPS + PLUGIN_TEST_DEPS + [
        ":x-docs__plugin",
    ],
)

java_library(
    name = "avalon_framework_lib",
    exports = [
        "@avalon_framework_api//jar",
        "@avalon_framework_impl//jar",
    ],
)

java_library(
    name = "xmlgraphics_lib",
    exports = [
        "@batik_anim//jar",
        "@batik_awt_util//jar",
        "@batik_bridge//jar",
        "@batik_css//jar",
        "@batik_dom//jar",
        "@batik_ext//jar",
        "@batik_extension//jar",
        "@batik_gvt//jar",
        "@batik_js//jar",
        "@batik_parser//jar",
        "@batik_script//jar",
        "@batik_svg_dom//jar",
        "@batik_svggen//jar",
        "@batik_transcoder//jar",
        "@batik_util//jar",
        "@batik_xml//jar",
        "@fop//jar",
        "@xmlgraphics_commons//jar",
    ],
)
