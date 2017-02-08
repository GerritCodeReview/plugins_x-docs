load("//tools/bzl:maven_jar.bzl", "maven_jar")

package(default_visibility = ["//visibility:public"])

java_library(
  name = 'avalon-framework_lib',
  exports = [
    ':avalon-framework-api',
    ':avalon-framework-impl',
  ],
)

maven_jar(
  name = 'avalon-framework-api',
  artifact = 'org.apache.avalon.framework:avalon-framework-api:4.3.1',
  sha1 = '2dacadeb49bc14420990b1f28897d46f96e2181d',
  src_sha1 = '5c04230666be057c5a3ad1558575fc9a886fc1b6',
)

maven_jar(
  name = 'avalon-framework-impl',
  artifact = 'org.apache.avalon.framework:avalon-framework-impl:4.3.1',
  sha1 = '2d5f5a07fd14513ce6d7a7bfaff69419c26dbd0b',
  src_sha1 = '09f779f9b35461a7767a54bc835d21bd2a97ad6e',
)
