package(default_visibility = ["//visibility:public"])

java_library(
  name = 'antlr_lib',
  runtime_deps = [
    ':antlr',
  ],
)

java_library(
  name = 'antlr',
  runtime_deps = [
    ':antlr-runtime',
    ':stringtemplate',
  ],
  exports = ["@antlr//jar"],
)

java_library(
  name = 'antlr-runtime',
  exports = ["@antlr-runtime//jar"],
)

java_library(
  name = 'stringtemplate',
  exports = ["@stringtemplate//jar"],
)
