load("//tools/bzl:junit.bzl", "junit_tests")
load(
    "//tools/bzl:plugin.bzl",
    "gerrit_plugin",
    "PLUGIN_DEPS",
    "PLUGIN_TEST_DEPS",
)

gerrit_plugin(
    name = "x-docs",
    srcs = glob(["src/main/java/**/*.java"]),
    resources = glob(["src/main/**/*"]),
    gwt_module = "com.googlesource.gerrit.plugins.xdocs.XDocs",
    manifest_entries = [
        "Gerrit-PluginName: x-docs",
        "Gerrit-HttpModule: com.googlesource.gerrit.plugins.xdocs.HttpModule",
        "Gerrit-Module: com.googlesource.gerrit.plugins.xdocs.Module",
        "Gerrit-InitStep: com.googlesource.gerrit.plugins.xdocs.XDocInit",
    ],
    deps = [
        "@asciidoctor//jar",
        "@jruby//jar",
        "@org_antlr//jar:neverlink",
        "@java_runtime//jar:neverlink",
        "@stringtemplate//jar:neverlink",
        "@daisydiff//jar",
        "@commons_codec//jar:neverlink",
        "@commons_io//jar:neverlink",
        "@commons_lang//jar:neverlink",
        "@guava//jar:neverlink",
        "@avalon_framework_api//jar",
        "@avalon_framework_impl//jar",
        "@poi//jar",
        "@poi_scratchpad//jar",
        "@jaxb_svg11//jar",
        "@jaxb_xmldsig_core//jar",
        "@jaxb_xslfo//jar",
        "@xalan//jar",
        "@docx4j//jar",
        "@mbassador//jar",
        "@log_api//jar:neverlink",
        "@wmf2svg//jar",
        "@xml_apis//jar",
        "@serializer//jar",
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

junit_tests(
    name = "x-docs_tests",
    srcs = glob(["src/test/java/**/*.java"]),
    tags = ["xdocs"],
    deps = PLUGIN_DEPS + PLUGIN_TEST_DEPS + [
        ":x-docs__plugin",
    ],
)