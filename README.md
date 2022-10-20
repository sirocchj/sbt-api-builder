[//]: # (# returning soon)
[//]: # ([![Download]&#40;https://api.bintray.com/packages/sirocchj/sbt-plugins/sbt-api-builder/images/download.svg&#41;]&#40;https://bintray.com/sirocchj/sbt-plugins/sbt-api-builder/_latestVersion&#41;)
[//]: # ([![Build Status]&#40;https://travis-ci.org/sirocchj/sbt-api-builder.svg?branch=master&#41;]&#40;https://travis-ci.org/sirocchj/sbt-api-builder&#41;)

# ApiBuilder SBT Plugin

A plugin that liberates you from ever needing to use the CLI for updating
ApiBuilder definitions.

## Install

This plugin requires sbt 1.0.0+

Add the following line to your `./project/plugins.sbt` file:
```sbtshell
addSbtPlugin("io.laserdisc" % "sbt-api-builder" % latestVersion)
```

That's it! This plugin is automatically installed into every module your project
is made of. To disable it for some module(s) you need to add the following line
to your project module(s)' definition:
```sbtshell
.disablePlugins(ApiBuilderPlugin)
```
e.g.
```sbtshell
lazy val example = project.in(file("."))
  .disablePlugins(ApiBuilderPlugin)
```

## Usage

From within an SBT shell you can now access the task:
```sbtshell
> apiBuilderUpdate
```
This task will use the `default` profile's token in `~/.apibuilder/config`
to fetch from `https://api.apibuilder.io/` all the resources defined in
`src/main/apibuilder/config` and `src/test/apibuilder/config`.  All files
are copied in `target/SCALA_BIN_VERSION/src_managed/main` and
`target/SCALA_BIN_VERSION/src_managed/test`, respectively, maintaining
the directory structure and file naming suggested by ApiBuilder.

The above task is also added to the `sourceGenerators` setting. This allows
sbt to know that it needs to compile the `src_managed` directory also, which
in turn implies that any task that triggers `compile` will also trigger
`apiBuilderUpdate`.

### Global configuration

The expected format of the global config file is:
```
[default]
token = some token value
[profile name]
token = some other token value
```
that is, the profile name is either `default` or it comes after the keyword
`profile`.

The location where to expect this file defaults to `~/.apibuilder` but it
can be changed via:
```sbtshell
> set Global/apiBuilderGlobalConfigDirectory := file("/foo/bar/somedir")
```
Likewise, the name of the file defaults to `config` but it can be changed via:
```sbtshell
> set Global/apiBuilderGlobalConfigFilename := "my_config"
```
Within this file the `default` profile is the one that will be used, unless
instructed otherwise via:
```sbtshell
> set Global/apiBuilderProfile := Some("other")
```
or with the `APIBUILDER_PROFILE` environment variable.

Otherwise, the token can be specified with the `APIBUILDER_TOKEN` environment variable.

Finally, the ApiBuilder files are expected to be found hitting the endpoint
`https://api.apibuilder.io`. If that is not the case (e.g. you host an internal
repository), just set the plugin to fetch from a different endpoint:
```sbtshell
> set Global/apiBuilderUrl := url("https://acme.org")
```

### Model/API configuration

The expected format of the local config file follows the convention of one
of the flavours of the YAML files that can be defined for the
[ApiBuilder CLI](https://github.com/apicollective/apibuilder-cli), specifically:
```yaml
code:
  org:
    project:
      version: <version>
      generators:
        - generator: <generator name>:
          files:
            - <file name or file pattern>
        - generator: <generator name>:
          files:
            - <file name or file pattern>
```
The `files` key is interpreted as a GLOB pattern that files for the
(`org` / `project` / `version` / `generator`) tuple must match for them to be
saved.

For example, to build [API Builder's api](https://app.apibuilder.io/apicollective/apibuilder-api/latest),
the following config file is required.
```yaml
code:
  apicollective:
    apibuilder-api:
      version: latest
      generators:
        - generator: http4s_0_18
          files:
            - '*ModelsJson.scala'
            - '*Server.scala'
```

As with the global configuration, the directory and the filename of where this
local configuration YAML file is to be found can be changed too.
Since these settings are scoped at _Compile_ and _Test_ configurations
(see [sbt documentation](https://www.scala-sbt.org/1.0/docs/Scopes.html#Scoping+by+the+configuration+axis)),
in order to override the predefined values to `project_root_dir/.apibuilder/config`
and `project_root_dir/.apibuilder/test_config` (for tests), all it's needed is
the following:
```sbtshell
> set Compile/apiBuilderCLIConfigDirectory := baseDirectory.value / ".apibuilder"
> set Test/apiBuilderCLIConfigDirectory    := baseDirectory.value / ".apibuilder"
```
and
```sbtshell
> set Compile/apiBuilderCLIConfigFilename := "config" // really unchanged, no need to override
> set Test/apiBuilderCLIConfigFilename    := "test_config"
```

## License

sbt-api-builder is licensed under the **[MIT License](LICENSE)** (the
"License"); you may not use this software except in compliance with the License.

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
