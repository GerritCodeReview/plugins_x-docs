load("//tools/bzl:maven_jar.bzl", "maven_jar")

package(default_visibility = ["//visibility:public"])

java_library(
  name = 'plutext_lib',
  exports = [
    ':jaxb-svg11',
    ':jaxb-xmldsig-core',
    ':jaxb-xslfo',
  ],
)
maven_jar(
  name = 'jaxb-svg11',
  artifact = 'org.plutext:jaxb-svg11:1.0.2',
  sha1 = '3c0cd54d5691f5b5f8c60ed0c06353ff1db424e1',
  src_sha1 = 'd2ecea2f14168d693bf68326ebea2ebfc6144114',
)

maven_jar(
  name = 'jaxb-xmldsig-core',
  artifact = 'org.plutext:jaxb-xmldsig-core:1.0.0',
  sha1 = '57514aa2f72111cfbc0a532ce88782735370e1e5',
  src_sha1 = 'a41559be6a94177daf0813f63d6ec81b11b3c0d3',
)

maven_jar(
  name = 'jaxb-xslfo',
  artifact = 'org.plutext:jaxb-xslfo:1.0.1',
  sha1 = '85441209652b216f61160445b399f5bc97e370c6',
  src_sha1 = 'b81e98b51687771f8a0b12406ea142a6e5dc6539',
)
