load("//tools/bzl:maven_jar.bzl", "maven_jar")

package(default_visibility = ["//visibility:public"])

java_library(
  name = 'commons_lib',
  deps = [
    ':codec',
    ':io',
    ':lang',
  ],
)

EXCLUDE = [
  'META-INF/LICENSE.txt',
  'META-INF/NOTICE.txt'
]

maven_jar(
  name = 'codec',
  artifact = 'commons-codec:commons-codec:1.4',
  sha1 = '4216af16d38465bbab0f3dff8efa14204f7a399a',
  exclude = EXCLUDE,
)

maven_jar(
  name = 'io',
  artifact = 'commons-io:commons-io:1.4',
  sha1 = 'a8762d07e76cfde2395257a5da47ba7c1dbd3dce',
  exclude = EXCLUDE,
)

maven_jar(
  name = 'lang',
  artifact = 'commons-lang:commons-lang:2.5',
  sha1 = 'b0236b252e86419eef20c31a44579d2aee2f0a69',
  exclude = EXCLUDE,
)
