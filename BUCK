include_defs('//bucklets/gerrit_plugin.bucklet')

gerrit_plugin(
  name = 'codescene',
  srcs = glob(['src/main/java/**/*.java']),
  resources = glob(['src/main/resources/**/*']),
  manifest_entries = [
    'Gerrit-PluginName: codescene',
    'Gerrit-ApiType: plugin',
    'Gerrit-ApiVersion: 2.13.9',
    'Gerrit-Module: com.codescene.gerrit.Module',
    'Gerrit-SshModule: com.codescene.gerrit.SshModule',
    'Gerrit-HttpModule: com.codescene.gerrit.HttpModule',
  ],
)

# this is required for bucklets/tools/eclipse/project.py to work
java_library(
  name = 'classpath',
  deps = [':codescene__plugin'],
)

