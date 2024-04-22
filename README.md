# CodeScene Gerrit Plugin

#### Installing

Copy plugin jar `codescene.jar` to your Gerrit `plugins` folder. Add configuration for the plugin.

If you want to build the JAR yourself, see below.

#### Configuration

Create a Bot user in CodeScene with access to projects you need.

You can configure the plugin in multiple ways. You can add configuration to global config at `/etc/gerrit.config`:

```
[plugin "codescene"]
        url = http://localhost:3333
        username = some_bot_user
        password = example
```

Or you can configure via plugin specific global configuration at `/etc/codescene.config`:

```
[server]
        url = http://localhost:3333
        username = some_bot_user
        password = example
```

Or you can use project specific plugin configuration files, same as `codescene.config` here.

Set property `ssl_verify` to `false` to skip HTTPS certificate verification.

Set property `cache` to `false` to disable cache of CodeScene's results on the server side.

#### Building and deploying

Run `npm install` to install npm packages and install Typescript compiler.

Install `esbuild` bundler.

Build the plugin with `make build`. You can find JAR in target folder.

Example:

```
make build && cp target/codescene.jar ~/docker-volumes/gerrit/plugins/codescene.jar
```

# License

Copyright 2024 CodeScene

See LICENSE file.