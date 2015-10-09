## hawtio-core

The core plugin loading and bootstrapping mechanism for the **[hawtio](http://hawt.io)** web console, now available in a handy bower package.

### Basic usage
* `bower install --save hawtio-core`

* Remove any ng-app annotation from your HTML template, hawtio-core manually bootstraps Angular

### Registering inline plugins
Register any Angular modules you want to load using `hawtioPluginLoader` in your script(s), for example:  `hawtioPluginLoader.addModule('MyAwesomeApp');`.  By default hawtio-core already adds `ng`, `ng-route` and `ng-sanitize`.


### Registering plugins dynamically
* Plugins can also be dynamically discovered by registering URLs to check with `hawtioPluginLoader.addUrl()`.  The URL should return a map of json objects that contain scripts for the plugin loader to load, for example:

```
{
  some_plugin: {
    "Name": "dummy",
    "Context": "/hawtio",
    "Scripts": [
      "test.js"
    ]
  }
}
```

Where we've the following attributes:

* **Name** - The name of the plugin
* **Scripts** - An array of script files that should be loaded
* **Context** - the top level context under which all scripts reside

