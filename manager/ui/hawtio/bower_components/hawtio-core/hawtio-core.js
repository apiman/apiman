
// hawtio log initialization
/* globals Logger window console document localStorage $ angular jQuery navigator Jolokia */

Logger.setLevel(Logger.INFO);
Logger.storagePrefix = 'hawtio';

Logger.oldGet = Logger.get;
Logger.loggers = {};
Logger.get = function(name) {
  var answer = Logger.oldGet(name);
  Logger.loggers[name] = answer;
  return answer;
};

// we'll default to 100 statements I guess...
window['LogBuffer'] = 100;

if ('localStorage' in window) {
  if (!('logLevel' in window.localStorage)) {
    window.localStorage['logLevel'] = JSON.stringify(Logger.INFO);
  }
  var logLevel = Logger.DEBUG;
  try {
    logLevel = JSON.parse(window.localStorage['logLevel']);
  } catch (e) {
    console.error("Failed to parse log level setting: ", e);
  }
  // console.log("Using log level: ", logLevel);
  Logger.setLevel(logLevel);
  if ('showLog' in window.localStorage) {
    var showLog = window.localStorage['showLog'];
    // console.log("showLog: ", showLog);
    if (showLog === 'true') {
      var container = document.getElementById("log-panel");
      if (container) {
        container.setAttribute("style", "bottom: 50%;");
      }
    }
  }
  if ('logBuffer' in window.localStorage) {
    var logBuffer = window.localStorage['logBuffer'];
    window['LogBuffer'] = parseInt(logBuffer, 10);
  } else {
    window.localStorage['logBuffer'] = window['LogBuffer'];
  }
  if ('childLoggers' in window.localStorage) {
    var childLoggers = [];
    try {
      childLoggers = JSON.parse(localStorage['childLoggers']);
    } catch (e) {

    }
    childLoggers.forEach(function(child) {
      Logger.get(child.logger).setLevel(Logger[child.level]);
    });
  }
}

var consoleLogger = null;

if ('console' in window) {
  window['JSConsole'] = window.console;
  consoleLogger = function(messages, context) {
    var MyConsole = window['JSConsole'];
    var hdlr = MyConsole.log;
    // Prepend the logger's name to the log message for easy identification.
    if (context.name) {
      messages[0] = "[" + context.name + "] " + messages[0];
    }
    // Delegate through to custom warn/error loggers if present on the console.
    if (context.level === Logger.WARN && 'warn' in MyConsole) {
      hdlr = MyConsole.warn;
    } else if (context.level === Logger.ERROR && 'error' in MyConsole) {
      hdlr = MyConsole.error;
    } else if (context.level === Logger.INFO && 'info' in MyConsole) {
      hdlr = MyConsole.info;
    }
    if (hdlr && hdlr.apply) {
      try {
        hdlr.apply(MyConsole, messages);
      } catch (e) {
        MyConsole.log(messages);
      }
    }
  };
}

// keep these hidden in the Logger object
Logger.getType = function(obj) {
  return Object.prototype.toString.call(obj).slice(8, -1);
};

Logger.isError = function(obj) {
  return obj && Logger.getType(obj) === 'Error';
};

Logger.isArray = function (obj) {
  return obj && Logger.getType(obj) === 'Array';
};

Logger.isObject = function (obj) {
  return obj && Logger.getType(obj) === 'Object';
};

Logger.isString = function(obj) {
  return obj && Logger.getType(obj) === 'String';
};

window['logInterceptors'] = [];

Logger.formatStackTraceString = function(stack) {
  var lines = stack.split("\n");
  if (lines.length > 100) {
    // too many lines, let's snip the middle so the browser doesn't bail
    var start = 20;
    var amount = lines.length - start * 2;
    lines.splice(start, amount, '>>> snipped ' + amount + ' frames <<<');
  }
  var stackTrace = "<div class=\"log-stack-trace\">\n";
  for (var j = 0; j < lines.length; j++) {
    var line = lines[j];
    if (line.trim().length === 0) {
      continue;
    }
    //line = line.replace(/\s/g, "&nbsp;");
    stackTrace = stackTrace + "<p>" + line + "</p>\n";
  }
  stackTrace = stackTrace + "</div>\n";
  return stackTrace;
};


Logger.setHandler(function(messages, context) {
  // MyConsole.log("context: ", context);
  // MyConsole.log("messages: ", messages);
  var node = undefined;
  var panel = undefined;
  var container = document.getElementById("hawtio-log-panel");
  if (container) {
    panel = document.getElementById("hawtio-log-panel-statements");
    node = document.createElement("li");
  }
  var text = "";
  var postLog = [];
  // try and catch errors logged via console.error(e.toString) and reformat
  if (context['level'].name === 'ERROR' && messages.length === 1) {
    if (Logger.isString(messages[0])) {
      var message = messages[0];
      var messageSplit = message.split(/\n/);
      if (messageSplit.length > 1) {
        // we may have more cases that require normalizing, so a more flexible solution
        // may be needed
        var lookFor = "Error: Jolokia-Error: ";
        if (messageSplit[0].search(lookFor) === 0) {
          var msg = messageSplit[0].slice(lookFor.length);
          window['JSConsole'].info("msg: ", msg);
          try {
            var errorObject = JSON.parse(msg);
            var error = new Error();
            error.message = errorObject['error'];
            error.stack = errorObject['stacktrace'].replace("\\t", "&nbsp;&nbsp").replace("\\n", "\n");
            messages = [error];
          } catch (e) {
            // we'll just bail and let it get logged as a string...
          }
        } else {
          var error = new Error();
          error.message = messageSplit[0];
          error.stack = message;
          messages = [error];
        }
      }
    }
  }
  var scroll = false;
  if (node) {
    for (var i = 0; i < messages.length; i++) {
      var message = messages[i];
      if (Logger.isArray(message) || Logger.isObject(message)) {
        var obj = "" ;
        try {
          obj = '<pre data-language="javascript">' + JSON.stringify(message, null, 2) + '</pre>';
        } catch (error) {
          obj = message + " (failed to convert) ";
          // silently ignore, could be a circular object...
        }
        text = text + obj;
      } else if (Logger.isError(message)) {
        if ('message' in message) {
          text = text + message['message'];
        }
        if ('stack' in message) {
          postLog.push(function() {
            var stackTrace = Logger.formatStackTraceString(message['stack']);
            var logger = Logger;
            if (context.name) {
              logger = Logger.get(context['name']);
            }
            logger.info("Stack trace: ", stackTrace);
          });
        }
      } else {
        text = text + message;
      }
    }
    if (context.name) {
      text = '[<span class="green">' + context.name + '</span>] ' + text;
    }
    node.innerHTML = text;
    node.className = context.level.name;
    if (container) {
      if (container.scrollHeight === 0) {
        scroll = true;
      }
      if (panel.scrollTop > (panel.scrollHeight - container.scrollHeight - 200)) {
        scroll = true;
      }
    }
  }
  function onAdd() {
    if (panel && node) {
      panel.appendChild(node);
      if (panel.childNodes.length > parseInt(window['LogBuffer'])) {
        panel.removeChild(panel.firstChild);
      }
      if (scroll) {
        panel.scrollTop = panel.scrollHeight;
      }
    }
    if (consoleLogger) {
      consoleLogger(messages, context);
    }
    var interceptors = window['logInterceptors'];
    for (var i = 0; i < interceptors.length; i++) {
      interceptors[i](context.level.name, text);
    }
  }
  onAdd();
  postLog.forEach(function (func) { func(); });
});

// Catch uncaught exceptions and stuff so we can log them
/*
window.onerror = function(msg, url, line, column, errorObject) {
  if (errorObject && Logger.isObject(errorObject)) {
    Logger.get("Window").error(errorObject);
  } else {
    var href = ' (<a href="' + url + ':' + line + '">' + url + ':' + line;

    if (column) {
      href = href + ':' + column;
    }
    href = href + '</a>)';
    Logger.get("Window").error(msg, href);
  }
  return true;
};
*/

// sneaky hack to redirect console.log !
/* window.console = {
  log: Logger.debug,
  warn: Logger.warn,
  error: Logger.error,
  info: Logger.info
};
*/

/*
 * Plugin loader and discovery mechanism for hawtio
 */
var hawtioPluginLoader = (function(self) {

  var log = Logger.get('hawtio-loader');

  self.log = log;

  /**
   * List of URLs that the plugin loader will try and discover
   * plugins from
   * @type {Array}
   */
  self.urls = [];

  /**
   * Holds all of the angular modules that need to be bootstrapped
   * @type {Array}
   */
  self.modules = [];

  /**
   * Tasks to be run before bootstrapping, tasks can be async.
   * Supply a function that takes the next task to be
   * executed as an argument and be sure to call the passed
   * in function.
   *
   * @type {Array}
   */
  self.tasks = [];

  self.registerPreBootstrapTask = function(task, front) {
    var tObj = task;
    var unnamed = 'unnamed-task-' + (self.tasks.length + 1)
    if (angular.isFunction(task)) {
      log.debug("Adding legacy task");
      tObj = {
        name: unnamed,
        task: task
      }
    }
    if (!task.name) {
      task.name = unnamed;
    }
    if (task.depends && !angular.isArray(task.depends) && task.depends !== '*') {
      task.depends = [task.depends];
    }
    if (!front) {
      self.tasks.push(tObj);
    } else {
      self.tasks.unshift(tObj);
    }
  };

  self.addModule = function(module) {
    log.debug("Adding module: " + module);
    self.modules.push(module);
  };

  self.addUrl = function(url) {
    log.debug("Adding URL: " + url);
    self.urls.push(url);
  };

  self.getModules = function() {
    return self.modules;
  };

  self.loaderCallback = null;

  self.setLoaderCallback = function(cb) {
    self.loaderCallback = cb;
    // log.debug("Setting callback to : ", self.loaderCallback);
  };

  function intersection(search, needle) {
    if (!angular.isArray(needle)) {
      needle = [needle];
    }
    //self.log.debug("Search: ", search);
    //self.log.debug("Needle: ", needle);
    var answer = [];
    needle.forEach(function(n) {
      search.forEach(function(s) {
        if (n === s) {
          answer.push(s);
        }
      });
    });
    return answer;
  }


  self.loadPlugins = function(callback) {

    var lcb = self.loaderCallback;

    var plugins = {};

    var urlsToLoad = self.urls.length;
    var totalUrls = urlsToLoad;

    var bootstrap = function() {
      var executedTasks = [];
      var deferredTasks = [];

      self.registerPreBootstrapTask({
        name: 'Hawtio Bootstrap',
        depends: '*',
        task: function() {
          if (deferredTasks.length > 0) {
            self.log.debug("Orphaned tasks: ");
            deferredTasks.forEach(function(task) {
              self.log.debug("  name: " + task.name + " depends: ", task.depends);
            });
          }
          self.log.debug("Executed tasks: ", executedTasks);
          callback(); 
        }
      });

      var executeTask = function() {
        var tObj = null;
        var tmp = [];
        // if we've executed all of the tasks, let's drain any deferred tasks
        // into the regular task queue
        if (self.tasks.length === 0) {
          tObj = deferredTasks.shift();
        }
        // first check and see what tasks have executed and see if we can pull a task
        // from the deferred queue
        while(!tObj && deferredTasks.length > 0) {
          var task = deferredTasks.shift();
          if (task.depends === '*') {
            if (self.tasks.length > 0) {
              tmp.push(task);
            } else {
              tObj = task;
            }
          } else {
            var intersect = intersection(executedTasks, task.depends);
            if (intersect.length === task.depends.length) {
              tObj = task;
            } else {
              tmp.push(task);
            }
          }
        }
        if (tmp.length > 0) {
          tmp.forEach(function(task) {
            deferredTasks.push(task);
          });
        }
        // no deferred tasks to execute, let's get a new task
        if (!tObj) {
          tObj = self.tasks.shift();
        }
        // check if task has dependencies
        if (tObj && tObj.depends && self.tasks.length > 0) {
          self.log.debug("Task '" + tObj.name + "' has dependencies: ", tObj.depends);
          if (tObj.depends === '*') {
            if (self.tasks.length > 0) {
              self.log.debug("Task '" + tObj.name + "' wants to run after all other tasks, deferring");
              deferredTasks.push(tObj);
              executeTask();
              return;
            }
          } else {
            var intersect = intersection(executedTasks, tObj.depends);
            if (intersect.length != tObj.depends.length) {
              self.log.debug("Deferring task: '" + tObj.name + "'");
              deferredTasks.push(tObj);
              executeTask();
              return;
            }
          }
        }
        if (tObj) {
          self.log.debug("Executing task: '" + tObj.name + "'");
          executedTasks.push(tObj.name);
          //self.log.debug("ExecutedTasks: ", executedTasks);
          tObj.task(executeTask);
        } else {
          self.log.debug("All tasks executed");
        }
      };
      executeTask();
    };

    var loadScripts = function() {

      // keep track of when scripts are loaded so we can execute the callback
      var loaded = 0;
      $.each(plugins, function(key, data) {
        loaded = loaded + data.Scripts.length;
      });

      var totalScripts = loaded;

      var scriptLoaded = function() {
        $.ajaxSetup({async:true});
        loaded = loaded - 1;
        if (lcb) {
          lcb.scriptLoaderCallback(lcb, totalScripts, loaded + 1);
        }
        if (loaded === 0) {
          bootstrap();
        }
      };

      if (loaded > 0) {
        $.each(plugins, function(key, data) {

          data.Scripts.forEach( function(script) {

            // log.debug("Loading script: ", data.Name + " script: " + script);

            var scriptName = data.Context + "/" + script;
            log.debug("Fetching script: ", scriptName);
            $.ajaxSetup({async:false});
            $.getScript(scriptName)
            .done(function(textStatus) {
              log.debug("Loaded script: ", scriptName);
            })
            .fail(function(jqxhr, settings, exception) {
              log.info("Failed loading script: \"", exception.message, "\" (<a href=\"", scriptName, ":", exception.lineNumber, "\">", scriptName, ":", exception.lineNumber, "</a>)");
            })
            .always(scriptLoaded);
          });
        });
      } else {
        // no scripts to load, so just do the callback
        $.ajaxSetup({async:true});
        bootstrap();
      }
    };

    if (urlsToLoad === 0) {
      loadScripts();
    } else {
      var urlLoaded = function () {
        urlsToLoad = urlsToLoad - 1;
        if (lcb) {
          lcb.urlLoaderCallback(lcb, totalUrls, urlsToLoad + 1);
        }
        if (urlsToLoad === 0) {
          loadScripts();
        }
      };

      var regex = new RegExp(/^jolokia:/);

      $.each(self.urls, function(index, url) {

        if (regex.test(url)) {
          var parts = url.split(':');
          parts = parts.reverse();
          parts.pop();

          url = parts.pop();
          var attribute = parts.reverse().join(':');
          var jolokia = new Jolokia(url);

          try {
            var data = jolokia.getAttribute(attribute, null);
            $.extend(plugins, data);
          } catch (Exception) {
            // console.error("Error fetching data: " + Exception);
          }
          urlLoaded();
        } else {

          log.debug("Trying url: ", url);

          $.get(url, function (data) {
            if (angular.isString(data)) {
              try {
                data = angular.fromJson(data);
              } catch (error) {
                // ignore this source of plugins
                return;
              }
            }
            // log.debug("got data: ", data);
            $.extend(plugins, data);
          }).always(function() {
            urlLoaded();
          });
        }
      });
    }
  };

  self.debug = function() {
    log.debug("urls and modules");
    log.debug(self.urls);
    log.debug(self.modules);
  };

  self.setLoaderCallback({
    scriptLoaderCallback: function (self, total, remaining) {
      log.debug("Total scripts: ", total, " Remaining: ", remaining);
    },
    urlLoaderCallback: function (self, total, remaining) {
      log.debug("Total URLs: ", total, " Remaining: ", remaining);
    }
  });

  return self;

})(hawtioPluginLoader || {}, window, undefined);

// Hawtio core plugin responsible for bootstrapping a hawtio app
var HawtioCore;
(function (HawtioCore) {
    /**
     * The app's injector, set once bootstrap is completed
     */
    HawtioCore.injector = null;
    /**
     * This plugin's name and angular module
     */
    HawtioCore.pluginName = "hawtio-core";
    /**
     * This plugins logger instance
     */
    var log = Logger.get(HawtioCore.pluginName);

    var _module = angular.module(HawtioCore.pluginName, []);
    _module.config(["$locationProvider", function ($locationProvider) {
      $locationProvider.html5Mode(true);
    }]);

    _module.run(['documentBase', function (documentBase) {
      log.debug("loaded");
    }]);

    var dummyLocalStorage = {
      length: 0,
      key: function(index) { return undefined; },
      getItem: function (key) { return dummyLocalStorage[key]; },
      setItem: function (key, data) { dummyLocalStorage[key] = data; },
      removeItem: function(key) {
        var removed = dummyLocalStorage[key];
        delete dummyLocalStorage[key];
        return removed;
      },
      clear: function() {

      }
    };
    HawtioCore.dummyLocalStorage = dummyLocalStorage;

    /**
     * services, mostly stubs
     */
    // localStorage service, returns a dummy impl
    // if for some reason it's not in the window
    // object
    _module.factory('localStorage', function() {
      return window.localStorage || dummyLocalStorage;
    });

    // Holds the document base so plugins can easily
    // figure out absolute URLs when needed
    _module.factory('documentBase', ['$document', function($document) {
      var base = $document.find('base');
      var answer = '/'
      if (base && base.length > 0) {
        answer = base.attr('href');
      } else {
        log.warn("Document is missing a 'base' tag, defaulting to '/'");
      }
      log.debug("Document base: ", answer);
      return answer;
    }]);


    // Holds a mapping of plugins to layouts, plugins use 
    // this to specify a full width view, tree view or their 
    // own custom view
    _module.factory('viewRegistry', function() {
      return {};
    });

    // Placeholder service for the help registry
    _module.factory('helpRegistry', function() {
      return {
        addUserDoc: function() {},
        addDevDoc: function() {},
        addSubTopic: function() {},
        getOrCreateTopic: function() { return undefined; },
        mapTopicName: function() { return undefined; },
        mapSubTopicName: function() { return undefined; },
        getTopics: function() { return undefined; },
        disableAutodiscover: function() {},
        discoverHelpFiles: function() {}
      };
    });

    // Placeholder service for the preferences registry
    _module.factory('preferencesRegistry', function() {
      return {
        addTab: function() {},
        getTab: function() { return undefined; },
        getTabs: function() { return undefined; }
      };
    });

    // Placeholder service for the page title service
    _module.factory('pageTitle', function() {
      return {
        addTitleElement: function() {},
        getTitle: function() { return undefined; },
        getTitleWithSeparator: function() { return undefined; },
        getTitleExcluding: function() { return undefined; },
        getTitleArrayExcluding: function() { return undefined; }
      };
    });

    // service for the javascript object that does notifications
    _module.factory('toastr', ["$window", function ($window) {
      var answer = $window.toastr;
      if (!answer) {
        // lets avoid any NPEs
        answer = {};
        $window.toastr = answer;
      }
      return answer;
    }]);

    _module.factory('HawtioDashboard', function() {
      return {
        hasDashboard: false,
        inDashboard: false,
        getAddLink: function() {
          return '';
        }
      }; 
    });

    // Placeholder service for branding
    _module.factory('branding', function() {
      return {};
    });

    // Placeholder user details service
    _module.factory('userDetails', function() {
      return {
        logout: function() {
          log.debug("Dummy userDetails.logout()");
        }
      };
    });

    hawtioPluginLoader.addModule("ng");
    hawtioPluginLoader.addModule("ngSanitize");
    hawtioPluginLoader.addModule(HawtioCore.pluginName);

    // bootstrap the app
    $(function () {

      jQuery.uaMatch = function( ua ) {
        ua = ua.toLowerCase();

        var match = /(chrome)[ \/]([\w.]+)/.exec( ua ) ||
          /(webkit)[ \/]([\w.]+)/.exec( ua ) ||
          /(opera)(?:.*version|)[ \/]([\w.]+)/.exec( ua ) ||
          /(msie) ([\w.]+)/.exec( ua ) ||
          ua.indexOf("compatible") < 0 && /(mozilla)(?:.*? rv:([\w.]+)|)/.exec( ua ) ||
          [];

        return {
          browser: match[ 1 ] || "",
          version: match[ 2 ] || "0"
        };
      };

      // Don't clobber any existing jQuery.browser in case it's different
      if ( !jQuery.browser ) {
        var matched = jQuery.uaMatch( navigator.userAgent );
        var browser = {};

        if ( matched.browser ) {
          browser[ matched.browser ] = true;
          browser.version = matched.version;
        }

        // Chrome is Webkit, but Webkit is also Safari.
        if ( browser.chrome ) {
          browser.webkit = true;
        } else if ( browser.webkit ) {
          browser.safari = true;
        }

        jQuery.browser = browser;
      }
      
      hawtioPluginLoader.loadPlugins(function() {
        if (!HawtioCore.injector) {
          var strictDi = localStorage['hawtioCoreStrictDi'] || false;
          if (strictDi) {
            log.debug("Using strict dependency injection");
          }
          HawtioCore.injector = angular.bootstrap(document, hawtioPluginLoader.getModules(), {
            strictDi: strictDi
          });
          log.debug("Bootstrapped application");
        } else {
          log.debug("Application already bootstrapped");
        }
      });
    });
})(HawtioCore || (HawtioCore = {}));

