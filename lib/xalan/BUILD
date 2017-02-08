load("//tools/bzl:maven_jar.bzl", "maven_jar")

package(default_visibility = ["//visibility:public"])

java_library(
  name = 'xalan_lib',
  exports = [
    ':xalan',
    ':serializer',
  ],
)

maven_jar(
  name = 'xalan',
  artifact = 'xalan:xalan:2.7.2',
  sha1 = 'd55d3f02a56ec4c25695fe67e1334ff8c2ecea23',
  src_sha1 = 'fe9c3d37a49238fac9d4d6c4f5bbd8c334da787a',
)

maven_jar(
  name = 'serializer',
  artifact = 'xalan:serializer:2.7.2',
  sha1 = '24247f3bb052ee068971393bdb83e04512bb1c3c',
  src_sha1 = 'fe9c3d37a49238fac9d4d6c4f5bbd8c334da787a',
)
