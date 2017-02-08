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
        "//plugins/x-docs/lib/asciidoctor:asciidoc_lib",
        "//plugins/x-docs/lib/daisydiff:daisydiff_lib",
        "//plugins/x-docs/lib/docx4j:docx4j_lib",
        "//plugins/x-docs/lib/commons:io",
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