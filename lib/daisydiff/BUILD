load("//tools/bzl:maven_jar.bzl", "maven_jar")

package(default_visibility = ["//visibility:public"])

java_library(
  name = 'daisydiff_lib',
  exports = [
    ':daisydiff',
  ],
)

maven_jar(
  name = 'daisydiff',
  artifact = 'org.outerj.daisy:daisydiff:1.1',
  sha1 = 'a67ed5147dd164e0614f1efc0145d06851c90d4f',
  repository = 'https://maven.atlassian.com/content/repositories/atlassian-3rdparty',
  attach_source = False,
)
