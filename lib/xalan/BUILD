load("//tools/bzl:maven_jar.bzl", "maven_jar")

package(default_visibility = ["//visibility:public"])

java_library(
  name = 'xalan_lib',
  exports = [
    ':xalan',
    ':serializer',
  ],
)

java_library(
  name = 'xalan',
  exports = ["@xalan//jar"],
)

java_library(
  name = 'serializer',
  exports = ["@serializer//jar"],
)
