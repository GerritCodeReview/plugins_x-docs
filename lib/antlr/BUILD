load("//tools/bzl:maven_jar.bzl", "maven_jar")

package(default_visibility = ["//visibility:public"])

java_library(
  name = 'antlr_lib',
  deps = [
    ':antlr',
  ],
)

maven_jar(
  name = 'antlr',
  artifact = 'org.antlr:antlr:3.1',
  sha1 = '6b0acabea7bb3da058200a77178057e47e25cb69',
  deps = [
    ':antlr-runtime',
    ':stringtemplate',
  ],
)

maven_jar(
  name = 'antlr-runtime',
  artifact = 'org.antlr:antlr-runtime:3.2',
  sha1 = '31c746001016c6226bd7356c9f87a6a084ce3715',
)

maven_jar(
  name = 'stringtemplate',
  artifact = 'org.antlr:stringtemplate:3.2',
  sha1 = '6fe2e3bb57daebd1555494818909f9664376dd6c',
  attach_source = False,
)
