load("//tools/bzl:maven_jar.bzl", "maven_jar")

package(default_visibility = ["//visibility:public"])

java_library(
  name = 'poi_lib',
  exports = [
    ':poi',
    ':poi-scratchpad',
  ],
)

maven_jar(
  name = 'poi',
  artifact = 'org.apache.poi:poi:3.10.1',
  sha1 = '95174823e13aa828cb715b542e647e56096ffcb2',
  src_sha1 = 'dcbcab74270ee3f68da61ef13c6f0858fb532e52',
)

maven_jar(
  name = 'poi-scratchpad',
  artifact = 'org.apache.poi:poi-scratchpad:3.10.1',
  sha1 = 'f40da8984b7a9bdf81270d7ecd2639548361fccd',
  src_sha1 = '88df5b9afa00070902f9bb100c554dba289afc7a',
)
