load("//tools/bzl:maven_jar.bzl", "maven_jar")

package(default_visibility = ["//visibility:public"])

java_library(
  name = 'asciidoc_lib',
  exports = [
    ':asciidoctor',
    ':jruby',
  ],
)

maven_jar(
  name = 'asciidoctor',
  artifact = 'org.asciidoctor:asciidoctorj:1.5.4.1',
  sha1 = 'f7ddfb2bbed2f8da3f9ad0d1a5514f04b4274a5a',
  attach_source = False,
)

maven_jar(
  name = 'jruby',
  artifact = 'org.jruby:jruby-complete:1.7.25',
  sha1 = '8eb234259ec88edc05eedab05655f458a84bfcab',
  attach_source = False,
)
