load("//tools/bzl:maven_jar.bzl", "maven_jar")

def external_plugin_deps():
  maven_jar(
    name = 'antlr',
    artifact = 'org.antlr:antlr:3.2',
    sha1 = '6b0acabea7bb3da058200a77178057e47e25cb69',
    deps = [
      '@antlr-runtime//jar',
      '@stringtemplate//jar',
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

  maven_jar(
    name = 'codec',
    artifact = 'commons-codec:commons-codec:1.4',
    sha1 = '4216af16d38465bbab0f3dff8efa14204f7a399a',
    exclude = [
      'META-INF/LICENSE.txt',
      'META-INF/NOTICE.txt'
    ]
  )

  maven_jar(
    name = 'io',
    artifact = 'commons-io:commons-io:1.4',
    sha1 = 'a8762d07e76cfde2395257a5da47ba7c1dbd3dce',
    exclude = [
      'META-INF/LICENSE.txt',
      'META-INF/NOTICE.txt'
    ]
  )

  maven_jar(
    name = 'lang',
    artifact = 'commons-lang:commons-lang:2.5',
    sha1 = 'b0236b252e86419eef20c31a44579d2aee2f0a69',
    exclude = [
      'META-INF/LICENSE.txt',
      'META-INF/NOTICE.txt'
    ]
  )

  maven_jar(
    name = 'daisydiff',
    artifact = 'org.outerj.daisy:daisydiff:1.1',
    sha1 = 'a67ed5147dd164e0614f1efc0145d06851c90d4f',
    repository = 'https://maven.atlassian.com/content/repositories/atlassian-3rdparty',
    attach_source = False,
  )

  maven_jar(
    name = 'docx4j',
    artifact = 'org.docx4j:docx4j:3.2.1',
    sha1 = '35b2ef9f7eb12efe9708b986ecf6e86cfd77a162',
    src_sha1 = '49702b88080bc22f6cfa58aadbf5422bcd904094',
  )

  maven_jar(
    name = 'guava',
    artifact = 'com.google.guava:guava:18.0',
    sha1 = 'cce0823396aa693798f8882e64213b1772032b09',
    attach_source = False,
  )

  maven_jar(
    name = 'mbassador',
    artifact = 'net.engio:mbassador:1.2.0',
    sha1 = '7aa2c6f172cc8c59983c5255faa45a16a41e173b',
    src_sha1 = 'd10cdaf71bfcb2002ab904a9d475e60c70c9b6bf',
  )

  maven_jar(
    name = 'slf4j-api',
    artifact = 'org.slf4j:slf4j-api:1.7.7',
    sha1 = '2b8019b6249bb05d81d3a3094e468753e2b21311',
    src_sha1 = '0acd62e31cc314266e73eebed0b6dd7ea974a0ed',
  )

  maven_jar(
    name = 'wmf2svg',
    artifact = 'net.arnx:wmf2svg:0.9.7',
    sha1 = '88a871f85a0e765c960af88ccc5a9a0e00384cd8',
    src_sha1 = '6948f9616032bd857c5173b204042889527a3bf3',
  )

  maven_jar(
    name = 'xml-apis',
    artifact = 'xml-apis:xml-apis:1.3.04',
    sha1 = '90b215f48fe42776c8c7f6e3509ec54e84fd65ef',
    src_sha1 = 'd89105ccbe3fd823865330fd964233baf2b53e88',
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

  maven_jar(
    name = 'batik-anim',
    artifact = 'org.apache.xmlgraphics:batik-anim:1.7',
    sha1 = 'a45dd2ff8e4ecd56a4fc64dc668b53bee90bf601',
    attach_source = False,
  )

  maven_jar(
    name = 'batik-awt-util',
    artifact = 'org.apache.xmlgraphics:batik-awt-util:1.7',
    sha1 = '67605a29d49bf33f3c1d7832f490b0a007e7a6e2',
    attach_source = False,
  )

  maven_jar(
    name = 'batik-bridge',
    artifact = 'org.apache.xmlgraphics:batik-bridge:1.7',
    sha1 = '8e0cde3830e0f17704cd392b0a09b13944987a51',
    attach_source = False,
  )
  maven_jar(
    name = 'batik-css',
    artifact = 'org.apache.xmlgraphics:batik-css:1.7',
    sha1 = 'e6bb5c85753331534593f33fb9236acb41a0ab79',
    attach_source = False,
  )

  maven_jar(
    name = 'batik-dom',
    artifact = 'org.apache.xmlgraphics:batik-dom:1.7',
    sha1 = '710d559bd1df52581b57b75a99ed5fd2e2918bb7',
    attach_source = False,
  )

  maven_jar(
    name = 'batik-ext',
    artifact = 'org.apache.xmlgraphics:batik-ext:1.7',
    sha1 = '4784302b44a0336166fef6153a5e3d73e861aecc',
    attach_source = False,
  )

  maven_jar(
    name = 'batik-extension',
    artifact = 'org.apache.xmlgraphics:batik-extension:1.7',
    sha1 = '8e810d9ce0499beca541e3b1a144557cf06f8b19',
    attach_source = False,
  )

  maven_jar(
    name = 'batik-gvt',
    artifact = 'org.apache.xmlgraphics:batik-gvt:1.7',
    sha1 = '03d315e60d72c761c52946b4acaa4b86239ef938',
    attach_source = False,
  )

  maven_jar(
    name = 'batik-js',
    artifact = 'org.apache.xmlgraphics:batik-js:1.7',
    sha1 = '688eb1bf13b7a54491fcb3405068fc5092589884',
    attach_source = False,
  )

  maven_jar(
    name = 'batik-parser',
    artifact = 'org.apache.xmlgraphics:batik-parser:1.7',
    sha1 = '5d756cc4f6bf891793e6c7590773859c33a8609f',
    attach_source = False,
  )

  maven_jar(
    name = 'batik-script',
    artifact = 'org.apache.xmlgraphics:batik-script:1.7',
    sha1 = '4ea7906724bfefc1fca3e5b28229a458523e1fbf',
    attach_source = False,
  )

  maven_jar(
    name = 'batik-svg-dom',
    artifact = 'org.apache.xmlgraphics:batik-svg-dom:1.7',
    sha1 = '5b3b1fea480fabbd3e0c44540af25b9fda0587ae',
    attach_source = False,
  )

  maven_jar(
    name = 'batik-svggen',
    artifact = 'org.apache.xmlgraphics:batik-svggen:1.7',
    sha1 = 'baa58d0f5bfd2a28142e222cee126eb71bd0a938',
    attach_source = False,
  )

  maven_jar(
    name = 'batik-transcoder',
    artifact = 'org.apache.xmlgraphics:batik-transcoder:1.7',
    sha1 = 'aa2eb6300cb50bbb9dbf52daf7d625aa0df1d930',
    attach_source = False,
  )

  maven_jar(
    name = 'batik-util',
    artifact = 'org.apache.xmlgraphics:batik-util:1.7',
    sha1 = '5c4dd0dd9a86a2fba2c6ea26fb62b32b21b2a61e',
    attach_source = False,
  )

  maven_jar(
    name = 'batik-xml',
    artifact = 'org.apache.xmlgraphics:batik-xml:1.7',
    sha1 = '17e3da8bd9d4a131350a7835f5cc0d93ba199c89',
    attach_source = False,
  )

  maven_jar(
    name = 'fop',
    artifact = 'org.apache.xmlgraphics:fop:1.1',
    sha1 = '95978100a6cde324078947a2d476cf2f207a7e5a',
    attach_source = False,
  )

  maven_jar(
    name = 'xmlgraphics-commons',
    artifact = 'org.apache.xmlgraphics:xmlgraphics-commons:1.5',
    sha1 = '7fb5c2b2c18f0e87fbe9bded16429a5d7cc2dc2b',
    attach_source = False,
  )

