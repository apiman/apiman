/// <reference path="includes.ts"/>
var StringHelpers;
(function (StringHelpers) {
    var dateRegex = /\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:/i;
    function isDate(str) {
        if (!angular.isString(str)) {
            // we only deal with strings
            return false;
        }
        return dateRegex.test(str);
    }
    StringHelpers.isDate = isDate;
    /**
     * Convert a string into a bunch of '*' of the same length
     * @param str
     * @returns {string}
     */
    function obfusicate(str) {
        if (!angular.isString(str)) {
            // return null so we don't show any old random non-string thing
            return null;
        }
        return str.chars().map(function (c) {
            return '*';
        }).join('');
    }
    StringHelpers.obfusicate = obfusicate;
    /**
     * Simple toString that obscures any field called 'password'
     * @param obj
     * @returns {string}
     */
    function toString(obj) {
        if (!obj) {
            return '{ null }';
        }
        var answer = [];
        angular.forEach(obj, function (value, key) {
            var val = value;
            if (('' + key).toLowerCase() === 'password') {
                val = StringHelpers.obfusicate(value);
            }
            else if (angular.isObject(val)) {
                val = toString(val);
            }
            answer.push(key + ': ' + val);
        });
        return '{ ' + answer.join(', ') + ' }';
    }
    StringHelpers.toString = toString;
})(StringHelpers || (StringHelpers = {}));

/// <reference path="includes.ts"/>
/// <reference path="stringHelpers.ts"/>
var Core;
(function (Core) {
    /**
     * Factory to create an instance of ConnectToServerOptions
     * @returns {ConnectToServerOptions}
     */
    function createConnectToServerOptions(options) {
        var defaults = {
            scheme: 'http',
            host: null,
            port: null,
            path: null,
            useProxy: true,
            jolokiaUrl: null,
            userName: null,
            password: null,
            view: null,
            name: null
        };
        var opts = options || {};
        return angular.extend(defaults, opts);
    }
    Core.createConnectToServerOptions = createConnectToServerOptions;
    function createConnectOptions(options) {
        return createConnectToServerOptions(options);
    }
    Core.createConnectOptions = createConnectOptions;
})(Core || (Core = {}));

/// <reference path="../libs/hawtio-core-dts/defs.d.ts"/>
/// <reference path="coreInterfaces.ts"/>

/// <reference path="includes.ts"/>
var ArrayHelpers;
(function (ArrayHelpers) {
    /**
     * Removes elements in the target array based on the new collection, returns true if
     * any changes were made
     */
    function removeElements(collection, newCollection, index) {
        if (index === void 0) { index = 'id'; }
        var oldLength = collection.length;
        collection.remove(function (item) {
            return !newCollection.any(function (c) {
                return c[index] === item[index];
            });
        });
        return collection.length !== oldLength;
    }
    ArrayHelpers.removeElements = removeElements;
    /**
     * Changes the existing collection to match the new collection to avoid re-assigning
     * the array pointer, returns true if the array size has changed
     */
    function sync(collection, newCollection, index) {
        if (index === void 0) { index = 'id'; }
        var answer = removeElements(collection, newCollection, index);
        if (newCollection) {
            newCollection.forEach(function (item) {
                var oldItem = collection.find(function (c) {
                    return c[index] === item[index];
                });
                if (!oldItem) {
                    answer = true;
                    collection.push(item);
                }
                else {
                    if (item !== oldItem) {
                        angular.copy(item, oldItem);
                        answer = true;
                    }
                }
            });
        }
        return answer;
    }
    ArrayHelpers.sync = sync;
})(ArrayHelpers || (ArrayHelpers = {}));

/// <reference path="includes.ts"/>
/// <reference path="baseHelpers.ts"/>
var UrlHelpers;
(function (UrlHelpers) {
    var log = Logger.get("UrlHelpers");
    /**
     * Returns the URL without the starting '#' if it's there
     * @param url
     * @returns {string}
     */
    function noHash(url) {
        if (url && url.startsWith('#')) {
            return url.last(url.length - 1);
        }
        else {
            return url;
        }
    }
    UrlHelpers.noHash = noHash;
    function extractPath(url) {
        if (url.has('?')) {
            return url.split('?')[0];
        }
        else {
            return url;
        }
    }
    UrlHelpers.extractPath = extractPath;
    /**
     * Returns whether or not the context is in the supplied URL.  If the search string starts/ends with '/' then the entire URL is checked.  If the search string doesn't start with '/' then the search string is compared against the end of the URL.  If the search string starts with '/' but doesn't end with '/' then the start of the URL is checked, excluding any '#'
     * @param url
     * @param thingICareAbout
     * @returns {boolean}
     */
    function contextActive(url, thingICareAbout) {
        var cleanUrl = extractPath(url);
        if (thingICareAbout.endsWith('/') && thingICareAbout.startsWith("/")) {
            return cleanUrl.has(thingICareAbout);
        }
        if (thingICareAbout.startsWith("/")) {
            return noHash(cleanUrl).startsWith(thingICareAbout);
        }
        return cleanUrl.endsWith(thingICareAbout);
    }
    UrlHelpers.contextActive = contextActive;
    /**
     * Joins the supplied strings together using '/', stripping any leading/ending '/'
     * from the supplied strings if needed, except the first and last string
     * @returns {string}
     */
    function join() {
        var paths = [];
        for (var _i = 0; _i < arguments.length; _i++) {
            paths[_i - 0] = arguments[_i];
        }
        var tmp = [];
        var length = paths.length - 1;
        paths.forEach(function (path, index) {
            if (Core.isBlank(path)) {
                return;
            }
            if (path === '/') {
                tmp.push('');
                return;
            }
            if (index !== 0 && path.first(1) === '/') {
                path = path.slice(1);
            }
            if (index !== length && path.last(1) === '/') {
                path = path.slice(0, path.length - 1);
            }
            if (!Core.isBlank(path)) {
                tmp.push(path);
            }
        });
        var rc = tmp.join('/');
        return rc;
    }
    UrlHelpers.join = join;
    function parseQueryString(text) {
        var uri = new URI(text);
        return URI.parseQuery(uri.query());
    }
    UrlHelpers.parseQueryString = parseQueryString;
    //export var parseQueryString = hawtioPluginLoader.parseQueryString;
    /**
     * Apply a proxy to the supplied URL if the jolokiaUrl is using the proxy, or if the URL is for a a different host/port
     * @param jolokiaUrl
     * @param url
     * @returns {*}
     */
    function maybeProxy(jolokiaUrl, url) {
        if (jolokiaUrl && jolokiaUrl.startsWith('proxy/')) {
            log.debug("Jolokia URL is proxied, applying proxy to: ", url);
            return join('proxy', url);
        }
        var origin = window.location['origin'];
        if (url && (url.startsWith('http') && !url.startsWith(origin))) {
            log.debug("Url doesn't match page origin: ", origin, " applying proxy to: ", url);
            return join('proxy', url);
        }
        log.debug("No need to proxy: ", url);
        return url;
    }
    UrlHelpers.maybeProxy = maybeProxy;
    /**
     * Escape any colons in the URL for ng-resource, mostly useful for handling proxified URLs
     * @param url
     * @returns {*}
     */
    function escapeColons(url) {
        var answer = url;
        if (url.startsWith('proxy')) {
            answer = url.replace(/:/g, '\\:');
        }
        else {
            answer = url.replace(/:([^\/])/, '\\:$1');
        }
        return answer;
    }
    UrlHelpers.escapeColons = escapeColons;
})(UrlHelpers || (UrlHelpers = {}));

/// <reference path="includes.ts"/>
/// <reference path="stringHelpers.ts"/>
/// <reference path="urlHelpers.ts"/>
/**
 * @module Core
 */
var Core;
(function (Core) {
    var _urlPrefix = null;
    Core.connectionSettingsKey = "jvmConnect";
    /**
     * Private method to support testing.
     *
     * @private
     */
    function _resetUrlPrefix() {
        _urlPrefix = null;
    }
    Core._resetUrlPrefix = _resetUrlPrefix;
    /**
     * Prefixes absolute URLs with current window.location.pathname
     *
     * @param path
     * @returns {string}
     */
    function url(path) {
        if (path) {
            if (path.startsWith && path.startsWith("/")) {
                if (!_urlPrefix) {
                    // lets discover the base url via the base html element
                    _urlPrefix = $('base').attr('href') || "";
                    if (_urlPrefix.endsWith && _urlPrefix.endsWith('/')) {
                        _urlPrefix = _urlPrefix.substring(0, _urlPrefix.length - 1);
                    }
                }
                if (_urlPrefix) {
                    return _urlPrefix + path;
                }
            }
        }
        return path;
    }
    Core.url = url;
    /**
     * Returns location of the global window
     *
     * @returns {string}
     */
    function windowLocation() {
        return window.location;
    }
    Core.windowLocation = windowLocation;
    // use a better implementation of unescapeHTML
    String.prototype.unescapeHTML = function () {
        var txt = document.createElement("textarea");
        txt.innerHTML = this;
        return txt.value;
    };
    // add object.keys if we don't have it, used
    // in a few places
    if (!Object.keys) {
        console.debug("Creating hawt.io version of Object.keys()");
        Object.keys = function (obj) {
            var keys = [], k;
            for (k in obj) {
                if (Object.prototype.hasOwnProperty.call(obj, k)) {
                    keys.push(k);
                }
            }
            return keys;
        };
    }
    /**
     * Private method to support testing.
     *
     * @private
     */
    function _resetJolokiaUrls() {
        // Add any other known possible jolokia URLs here
        jolokiaUrls = [
            Core.url("jolokia"),
            "/jolokia"
        ];
        return jolokiaUrls;
    }
    Core._resetJolokiaUrls = _resetJolokiaUrls;
    var jolokiaUrls = Core._resetJolokiaUrls();
    /**
     * Trims the leading prefix from a string if its present
     * @method trimLeading
     * @for Core
     * @static
     * @param {String} text
     * @param {String} prefix
     * @return {String}
     */
    function trimLeading(text, prefix) {
        if (text && prefix) {
            if (text.startsWith(prefix) || text.indexOf(prefix) === 0) {
                return text.substring(prefix.length);
            }
        }
        return text;
    }
    Core.trimLeading = trimLeading;
    /**
     * Trims the trailing postfix from a string if its present
     * @method trimTrailing
     * @for Core
     * @static
     * @param {String} trim
     * @param {String} postfix
     * @return {String}
     */
    function trimTrailing(text, postfix) {
        if (text && postfix) {
            if (text.endsWith(postfix)) {
                return text.substring(0, text.length - postfix.length);
            }
        }
        return text;
    }
    Core.trimTrailing = trimTrailing;
    /**
     * Ensure our main app container takes up at least the viewport
     * height
     */
    function adjustHeight() {
        var windowHeight = $(window).height();
        var headerHeight = $("#main-nav").height();
        var containerHeight = windowHeight - headerHeight;
        $("#main").css("min-height", "" + containerHeight + "px");
    }
    Core.adjustHeight = adjustHeight;
    function isChromeApp() {
        var answer = false;
        try {
            answer = (chrome && chrome.app && chrome.extension) ? true : false;
        }
        catch (e) {
            answer = false;
        }
        //log.info("isChromeApp is: " + answer);
        return answer;
    }
    Core.isChromeApp = isChromeApp;
    /**
     * Adds the specified CSS file to the document's head, handy
     * for external plugins that might bring along their own CSS
     *
     * @param path
     */
    function addCSS(path) {
        if ('createStyleSheet' in document) {
            // IE9
            document.createStyleSheet(path);
        }
        else {
            // Everyone else
            var link = $("<link>");
            $("head").append(link);
            link.attr({
                rel: 'stylesheet',
                type: 'text/css',
                href: path
            });
        }
    }
    Core.addCSS = addCSS;
    var dummyStorage = {};
    /**
     * Wrapper to get the window local storage object
     *
     * @returns {WindowLocalStorage}
     */
    function getLocalStorage() {
        // TODO Create correct implementation of windowLocalStorage
        var storage = window.localStorage || (function () {
            return dummyStorage;
        })();
        return storage;
    }
    Core.getLocalStorage = getLocalStorage;
    /**
     * If the value is not an array then wrap it in one
     *
     * @method asArray
     * @for Core
     * @static
     * @param {any} value
     * @return {Array}
     */
    function asArray(value) {
        return angular.isArray(value) ? value : [value];
    }
    Core.asArray = asArray;
    /**
     * Ensure whatever value is passed in is converted to a boolean
     *
     * In the branding module for now as it's needed before bootstrap
     *
     * @method parseBooleanValue
     * @for Core
     * @param {any} value
     * @param {Boolean} defaultValue default value to use if value is not defined
     * @return {Boolean}
     */
    function parseBooleanValue(value, defaultValue) {
        if (defaultValue === void 0) { defaultValue = false; }
        if (!angular.isDefined(value) || !value) {
            return defaultValue;
        }
        if (value.constructor === Boolean) {
            return value;
        }
        if (angular.isString(value)) {
            switch (value.toLowerCase()) {
                case "true":
                case "1":
                case "yes":
                    return true;
                default:
                    return false;
            }
        }
        if (angular.isNumber(value)) {
            return value !== 0;
        }
        throw new Error("Can't convert value " + value + " to boolean");
    }
    Core.parseBooleanValue = parseBooleanValue;
    function toString(value) {
        if (angular.isNumber(value)) {
            return numberToString(value);
        }
        else {
            return angular.toJson(value, true);
        }
    }
    Core.toString = toString;
    /**
     * Converts boolean value to string "true" or "false"
     *
     * @param value
     * @returns {string}
     */
    function booleanToString(value) {
        return "" + value;
    }
    Core.booleanToString = booleanToString;
    /**
     * object to integer converter
     *
     * @param value
     * @param description
     * @returns {*}
     */
    function parseIntValue(value, description) {
        if (description === void 0) { description = "integer"; }
        if (angular.isString(value)) {
            try {
                return parseInt(value);
            }
            catch (e) {
                console.log("Failed to parse " + description + " with text '" + value + "'");
            }
        }
        else if (angular.isNumber(value)) {
            return value;
        }
        return null;
    }
    Core.parseIntValue = parseIntValue;
    /**
     * Formats numbers as Strings.
     *
     * @param value
     * @returns {string}
     */
    function numberToString(value) {
        return "" + value;
    }
    Core.numberToString = numberToString;
    /**
     * object to integer converter
     *
     * @param value
     * @param description
     * @returns {*}
     */
    function parseFloatValue(value, description) {
        if (description === void 0) { description = "float"; }
        if (angular.isString(value)) {
            try {
                return parseFloat(value);
            }
            catch (e) {
                console.log("Failed to parse " + description + " with text '" + value + "'");
            }
        }
        else if (angular.isNumber(value)) {
            return value;
        }
        return null;
    }
    Core.parseFloatValue = parseFloatValue;
    /**
     * Navigates the given set of paths in turn on the source object
     * and returns the last most value of the path or null if it could not be found.
     *
     * @method pathGet
     * @for Core
     * @static
     * @param {Object} object the start object to start navigating from
     * @param {Array} paths an array of path names to navigate or a string of dot separated paths to navigate
     * @return {*} the last step on the path which is updated
     */
    function pathGet(object, paths) {
        var pathArray = (angular.isArray(paths)) ? paths : (paths || "").split(".");
        var value = object;
        angular.forEach(pathArray, function (name) {
            if (value) {
                try {
                    value = value[name];
                }
                catch (e) {
                    // ignore errors
                    return null;
                }
            }
            else {
                return null;
            }
        });
        return value;
    }
    Core.pathGet = pathGet;
    /**
     * Navigates the given set of paths in turn on the source object
     * and updates the last path value to the given newValue
     *
     * @method pathSet
     * @for Core
     * @static
     * @param {Object} object the start object to start navigating from
     * @param {Array} paths an array of path names to navigate or a string of dot separated paths to navigate
     * @param {Object} newValue the value to update
     * @return {*} the last step on the path which is updated
     */
    function pathSet(object, paths, newValue) {
        var pathArray = (angular.isArray(paths)) ? paths : (paths || "").split(".");
        var value = object;
        var lastIndex = pathArray.length - 1;
        angular.forEach(pathArray, function (name, idx) {
            var next = value[name];
            if (idx >= lastIndex || !angular.isObject(next)) {
                next = (idx < lastIndex) ? {} : newValue;
                value[name] = next;
            }
            value = next;
        });
        return value;
    }
    Core.pathSet = pathSet;
    function getPhase($scope) {
        if ($scope.$$phase) {
            return $scope.$$phase;
        }
        if (HawtioCore.injector) {
            var $rootScope = HawtioCore.injector.get('$rootScope');
            if ($rootScope) {
                return $rootScope.$$phase;
            }
        }
    }
    /**
     * Performs a $scope.$apply() if not in a digest right now otherwise it will fire a digest later
     *
     * @method $applyNowOrLater
     * @for Core
     * @static
     * @param {*} $scope
     */
    function $applyNowOrLater($scope) {
        if (getPhase($scope)) {
            setTimeout(function () {
                Core.$apply($scope);
            }, 50);
        }
        else {
            $scope.$apply();
        }
    }
    Core.$applyNowOrLater = $applyNowOrLater;
    /**
     * Performs a $scope.$apply() after the given timeout period
     *
     * @method $applyLater
     * @for Core
     * @static
     * @param {*} $scope
     * @param {Integer} timeout
     */
    function $applyLater($scope, timeout) {
        if (timeout === void 0) { timeout = 50; }
        setTimeout(function () {
            Core.$apply($scope);
        }, timeout);
    }
    Core.$applyLater = $applyLater;
    /**
     * Performs a $scope.$apply() if not in a digest or apply phase on the given scope
     *
     * @method $apply
     * @for Core
     * @static
     * @param {*} $scope
     */
    function $apply($scope) {
        var phase = getPhase($scope);
        if (!phase) {
            $scope.$apply();
        }
    }
    Core.$apply = $apply;
    /**
     * Performs a $scope.$digest() if not in a digest or apply phase on the given scope
     *
     * @method $apply
     * @for Core
     * @static
     * @param {*} $scope
     */
    function $digest($scope) {
        var phase = getPhase($scope);
        if (!phase) {
            $scope.$digest();
        }
    }
    Core.$digest = $digest;
    /**
     * Look up a list of child element names or lazily create them.
     *
     * Useful for example to get the <tbody> <tr> element from a <table> lazily creating one
     * if not present.
     *
     * Usage: var trElement = getOrCreateElements(tableElement, ["tbody", "tr"])
     * @method getOrCreateElements
     * @for Core
     * @static
     * @param {Object} domElement
     * @param {Array} arrayOfElementNames
     * @return {Object}
     */
    function getOrCreateElements(domElement, arrayOfElementNames) {
        var element = domElement;
        angular.forEach(arrayOfElementNames, function (name) {
            if (element) {
                var children = $(element).children(name);
                if (!children || !children.length) {
                    $("<" + name + "></" + name + ">").appendTo(element);
                    children = $(element).children(name);
                }
                element = children;
            }
        });
        return element;
    }
    Core.getOrCreateElements = getOrCreateElements;
    var _escapeHtmlChars = {
        "#": "&#35;",
        "'": "&#39;",
        "<": "&lt;",
        ">": "&gt;",
        "\"": "&quot;"
    };
    /**
     * static unescapeHtml
     *
     * @param str
     * @returns {any}
     */
    function unescapeHtml(str) {
        angular.forEach(_escapeHtmlChars, function (value, key) {
            var regex = new RegExp(value, "g");
            str = str.replace(regex, key);
        });
        str = str.replace(/&gt;/g, ">");
        return str;
    }
    Core.unescapeHtml = unescapeHtml;
    /**
     * static escapeHtml method
     *
     * @param str
     * @returns {*}
     */
    function escapeHtml(str) {
        if (angular.isString(str)) {
            var newStr = "";
            for (var i = 0; i < str.length; i++) {
                var ch = str.charAt(i);
                var ch = _escapeHtmlChars[ch] || ch;
                newStr += ch;
            }
            return newStr;
        }
        else {
            return str;
        }
    }
    Core.escapeHtml = escapeHtml;
    /**
     * Returns true if the string is either null or empty
     *
     * @method isBlank
     * @for Core
     * @static
     * @param {String} str
     * @return {Boolean}
     */
    function isBlank(str) {
        if (str === undefined || str === null) {
            return true;
        }
        if (angular.isString(str)) {
            return str.isBlank();
        }
        else {
            // TODO - not undefined but also not a string...
            return false;
        }
    }
    Core.isBlank = isBlank;
    /**
     * removes all quotes/apostrophes from beginning and end of string
     *
     * @param text
     * @returns {string}
     */
    function trimQuotes(text) {
        if (text) {
            while (text.endsWith('"') || text.endsWith("'")) {
                text = text.substring(0, text.length - 1);
            }
            while (text.startsWith('"') || text.startsWith("'")) {
                text = text.substring(1, text.length);
            }
        }
        return text;
    }
    Core.trimQuotes = trimQuotes;
    /**
     * Converts camel-case and dash-separated strings into Human readable forms
     *
     * @param value
     * @returns {*}
     */
    function humanizeValue(value) {
        if (value) {
            var text = value + '';
            try {
                text = text.underscore();
            }
            catch (e) {
            }
            try {
                text = text.humanize();
            }
            catch (e) {
            }
            return trimQuotes(text);
        }
        return value;
    }
    Core.humanizeValue = humanizeValue;
})(Core || (Core = {}));

/// <reference path="includes.ts"/>
var HawtioCompile;
(function (HawtioCompile) {
    var pluginName = 'hawtio-compile';
    var log = Logger.get(pluginName);
    HawtioCompile._module = angular.module(pluginName, []);
    HawtioCompile._module.run(function () {
        log.debug("loaded");
    });
    HawtioCompile._module.directive('compile', ['$compile', function ($compile) {
        return function (scope, element, attrs) {
            scope.$watch(function (scope) {
                // watch the 'compile' expression for changes
                return scope.$eval(attrs.compile);
            }, function (value) {
                // when the 'compile' expression changes
                // assign it into the current DOM
                element.html(value);
                // compile the new DOM and link it to the current
                // scope.
                // NOTE: we only compile .childNodes so that
                // we don't get into infinite loop compiling ourselves
                $compile(element.contents())(scope);
            });
        };
    }]);
    hawtioPluginLoader.addModule(pluginName);
})(HawtioCompile || (HawtioCompile = {}));

var ControllerHelpers;
(function (ControllerHelpers) {
    var log = Logger.get("ControllerHelpers");
    function createClassSelector(config) {
        return function (selector, model) {
            if (selector === model && selector in config) {
                return config[selector];
            }
            return '';
        };
    }
    ControllerHelpers.createClassSelector = createClassSelector;
    function createValueClassSelector(config) {
        return function (model) {
            if (model in config) {
                return config[model];
            }
            else {
                return '';
            }
        };
    }
    ControllerHelpers.createValueClassSelector = createValueClassSelector;
    /**
     * Binds a $location.search() property to a model on a scope; so that its initialised correctly on startup
     * and its then watched so as the model changes, the $location.search() is updated to reflect its new value
     * @method bindModelToSearchParam
     * @for Core
     * @static
     * @param {*} $scope
     * @param {ng.ILocationService} $location
     * @param {String} modelName
     * @param {String} paramName
     * @param {Object} initialValue
     */
    function bindModelToSearchParam($scope, $location, modelName, paramName, initialValue, to, from) {
        if (!(modelName in $scope)) {
            $scope[modelName] = initialValue;
        }
        var toConverter = to || Core.doNothing;
        var fromConverter = from || Core.doNothing;
        function currentValue() {
            return fromConverter($location.search()[paramName] || initialValue);
        }
        var value = currentValue();
        Core.pathSet($scope, modelName, value);
        $scope.$watch(modelName, function (newValue, oldValue) {
            if (newValue !== oldValue) {
                if (newValue !== undefined && newValue !== null) {
                    $location.search(paramName, toConverter(newValue));
                }
                else {
                    $location.search(paramName, '');
                }
            }
        });
    }
    ControllerHelpers.bindModelToSearchParam = bindModelToSearchParam;
    /**
     * For controllers where reloading is disabled via "reloadOnSearch: false" on the registration; lets pick which
     * query parameters need to change to force the reload. We default to the JMX selection parameter 'nid'
     * @method reloadWhenParametersChange
     * @for Core
     * @static
     * @param {Object} $route
     * @param {*} $scope
     * @param {ng.ILocationService} $location
     * @param {Array[String]} parameters
     */
    function reloadWhenParametersChange($route, $scope, $location, parameters) {
        if (parameters === void 0) { parameters = ["nid"]; }
        var initial = angular.copy($location.search());
        $scope.$on('$routeUpdate', function () {
            // lets check if any of the parameters changed
            var current = $location.search();
            var changed = [];
            angular.forEach(parameters, function (param) {
                if (current[param] !== initial[param]) {
                    changed.push(param);
                }
            });
            if (changed.length) {
                //log.info("Reloading page due to change to parameters: " + changed);
                $route.reload();
            }
        });
    }
    ControllerHelpers.reloadWhenParametersChange = reloadWhenParametersChange;
})(ControllerHelpers || (ControllerHelpers = {}));

var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};
/// <reference path="includes.ts"/>
var Core;
(function (Core) {
    var log = Logger.get("hawtio-tasks");
    var TasksImpl = (function () {
        function TasksImpl() {
            this.tasks = {};
            this.tasksExecuted = false;
            this._onComplete = null;
        }
        TasksImpl.prototype.addTask = function (name, task) {
            this.tasks[name] = task;
            if (this.tasksExecuted) {
                this.executeTask(name, task);
            }
        };
        TasksImpl.prototype.executeTask = function (name, task) {
            if (angular.isFunction(task)) {
                log.debug("Executing task : ", name);
                try {
                    task();
                }
                catch (error) {
                    log.debug("Failed to execute task: ", name, " error: ", error);
                }
            }
        };
        TasksImpl.prototype.onComplete = function (cb) {
            this._onComplete = cb;
        };
        TasksImpl.prototype.execute = function () {
            var _this = this;
            if (this.tasksExecuted) {
                return;
            }
            angular.forEach(this.tasks, function (task, name) {
                _this.executeTask(name, task);
            });
            this.tasksExecuted = true;
            if (angular.isFunction(this._onComplete)) {
                this._onComplete();
            }
        };
        TasksImpl.prototype.reset = function () {
            this.tasksExecuted = false;
        };
        return TasksImpl;
    })();
    Core.TasksImpl = TasksImpl;
    var ParameterizedTasksImpl = (function (_super) {
        __extends(ParameterizedTasksImpl, _super);
        function ParameterizedTasksImpl() {
            var _this = this;
            _super.call(this);
            this.tasks = {};
            this.onComplete(function () {
                _this.reset();
            });
        }
        ParameterizedTasksImpl.prototype.addTask = function (name, task) {
            this.tasks[name] = task;
        };
        ParameterizedTasksImpl.prototype.execute = function () {
            var _this = this;
            var params = [];
            for (var _i = 0; _i < arguments.length; _i++) {
                params[_i - 0] = arguments[_i];
            }
            if (this.tasksExecuted) {
                return;
            }
            var theArgs = params;
            var keys = _.keys(this.tasks);
            keys.forEach(function (name) {
                var task = _this.tasks[name];
                if (angular.isFunction(task)) {
                    log.debug("Executing task: ", name, " with parameters: ", theArgs);
                    try {
                        task.apply(task, theArgs);
                    }
                    catch (e) {
                        log.debug("Failed to execute task: ", name, " error: ", e);
                    }
                }
            });
            this.tasksExecuted = true;
            if (angular.isFunction(this._onComplete)) {
                this._onComplete();
            }
        };
        return ParameterizedTasksImpl;
    })(TasksImpl);
    Core.ParameterizedTasksImpl = ParameterizedTasksImpl;
    Core.postLoginTasks = new Core.TasksImpl();
    Core.preLogoutTasks = new Core.TasksImpl();
    Core.postLogoutTasks = new Core.TasksImpl();
})(Core || (Core = {}));

/// <reference path="baseHelpers.ts"/>
/// <reference path="controllerHelpers.ts"/>
/// <reference path="coreInterfaces.ts"/>
/// <reference path="tasks.ts"/>
var Core;
(function (Core) {
    Core.log = Logger.get("Core");
    Core.lazyLoaders = {};
    Core.numberTypeNames = {
        'byte': true,
        'short': true,
        'int': true,
        'long': true,
        'float': true,
        'double': true,
        'java.lang.byte': true,
        'java.lang.short': true,
        'java.lang.integer': true,
        'java.lang.long': true,
        'java.lang.float': true,
        'java.lang.double': true
    };
    /**
     * Returns the number of lines in the given text
     *
     * @method lineCount
     * @static
     * @param {String} value
     * @return {Number}
     *
     */
    function lineCount(value) {
        var rows = 0;
        if (value) {
            rows = 1;
            value.toString().each(/\n/, function () { return rows++; });
        }
        return rows;
    }
    Core.lineCount = lineCount;
    function safeNull(value) {
        if (typeof value === 'boolean') {
            return value;
        }
        else if (typeof value === 'number') {
            // return numbers as-is
            return value;
        }
        if (value) {
            return value;
        }
        else {
            return "";
        }
    }
    Core.safeNull = safeNull;
    function safeNullAsString(value, type) {
        if (typeof value === 'boolean') {
            return "" + value;
        }
        else if (typeof value === 'number') {
            // return numbers as-is
            return "" + value;
        }
        else if (typeof value === 'string') {
            // its a string
            return "" + value;
        }
        else if (type === 'javax.management.openmbean.CompositeData' || type === '[Ljavax.management.openmbean.CompositeData;' || type === 'java.util.Map') {
            // composite data or composite data array, we just display as json
            // use json representation
            var data = angular.toJson(value, true);
            return data;
        }
        else if (type === 'javax.management.ObjectName') {
            return "" + (value == null ? "" : value.canonicalName);
        }
        else if (type === 'javax.management.openmbean.TabularData') {
            // tabular data is a key/value structure so loop each field and convert to array we can
            // turn into a String
            var arr = [];
            for (var key in value) {
                var val = value[key];
                var line = "" + key + "=" + val;
                arr.push(line);
            }
            // sort array so the values is listed nicely
            arr = arr.sortBy(function (row) { return row.toString(); });
            return arr.join("\n");
        }
        else if (angular.isArray(value)) {
            // join array with new line, and do not sort as the order in the array may matter
            return value.join("\n");
        }
        else if (value) {
            // force as string
            return "" + value;
        }
        else {
            return "";
        }
    }
    Core.safeNullAsString = safeNullAsString;
    /**
     * Converts the given value to an array of query arguments.
     *
     * If the value is null an empty array is returned.
     * If the value is a non empty string then the string is split by commas
     *
     * @method toSearchArgumentArray
     * @static
     * @param {*} value
     * @return {String[]}
     *
     */
    function toSearchArgumentArray(value) {
        if (value) {
            if (angular.isArray(value))
                return value;
            if (angular.isString(value))
                return value.split(',');
        }
        return [];
    }
    Core.toSearchArgumentArray = toSearchArgumentArray;
    function folderMatchesPatterns(node, patterns) {
        if (node) {
            var folderNames = node.folderNames;
            if (folderNames) {
                return patterns.any(function (ignorePaths) {
                    for (var i = 0; i < ignorePaths.length; i++) {
                        var folderName = folderNames[i];
                        var ignorePath = ignorePaths[i];
                        if (!folderName)
                            return false;
                        var idx = ignorePath.indexOf(folderName);
                        if (idx < 0) {
                            return false;
                        }
                    }
                    return true;
                });
            }
        }
        return false;
    }
    Core.folderMatchesPatterns = folderMatchesPatterns;
    function scopeStoreJolokiaHandle($scope, jolokia, jolokiaHandle) {
        // TODO do we even need to store the jolokiaHandle in the scope?
        if (jolokiaHandle) {
            $scope.$on('$destroy', function () {
                closeHandle($scope, jolokia);
            });
            $scope.jolokiaHandle = jolokiaHandle;
        }
    }
    Core.scopeStoreJolokiaHandle = scopeStoreJolokiaHandle;
    function closeHandle($scope, jolokia) {
        var jolokiaHandle = $scope.jolokiaHandle;
        if (jolokiaHandle) {
            //console.log('Closing the handle ' + jolokiaHandle);
            jolokia.unregister(jolokiaHandle);
            $scope.jolokiaHandle = null;
        }
    }
    Core.closeHandle = closeHandle;
    /**
     * Pass in null for the success function to switch to sync mode
     *
     * @method onSuccess
     * @static
     * @param {Function} Success callback function
     * @param {Object} Options object to pass on to Jolokia request
     * @return {Object} initialized options object
     */
    function onSuccess(fn, options) {
        if (options === void 0) { options = {}; }
        options['mimeType'] = 'application/json';
        if (angular.isDefined(fn)) {
            options['success'] = fn;
        }
        if (!options['method']) {
            options['method'] = "POST";
        }
        options['canonicalNaming'] = false;
        options['canonicalProperties'] = false;
        if (!options['error']) {
            options['error'] = function (response) {
                Core.defaultJolokiaErrorHandler(response, options);
            };
        }
        return options;
    }
    Core.onSuccess = onSuccess;
    function supportsLocalStorage() {
        try {
            return 'localStorage' in window && window['localStorage'] !== null;
        }
        catch (e) {
            return false;
        }
    }
    Core.supportsLocalStorage = supportsLocalStorage;
    function isNumberTypeName(typeName) {
        if (typeName) {
            var text = typeName.toString().toLowerCase();
            var flag = Core.numberTypeNames[text];
            return flag;
        }
        return false;
    }
    Core.isNumberTypeName = isNumberTypeName;
    function encodeMBeanPath(mbean) {
        return mbean.replace(/\//g, '!/').replace(':', '/').escapeURL();
    }
    Core.encodeMBeanPath = encodeMBeanPath;
    function escapeMBeanPath(mbean) {
        return mbean.replace(/\//g, '!/').replace(':', '/');
    }
    Core.escapeMBeanPath = escapeMBeanPath;
    function encodeMBean(mbean) {
        return mbean.replace(/\//g, '!/').escapeURL();
    }
    Core.encodeMBean = encodeMBean;
    function escapeDots(text) {
        return text.replace(/\./g, '-');
    }
    Core.escapeDots = escapeDots;
    /**
     * Escapes all dots and 'span' text in the css style names to avoid clashing with bootstrap stuff
     *
     * @method escapeTreeCssStyles
     * @static
     * @param {String} text
     * @return {String}
     */
    function escapeTreeCssStyles(text) {
        return escapeDots(text).replace(/span/g, 'sp-an');
    }
    Core.escapeTreeCssStyles = escapeTreeCssStyles;
    function showLogPanel() {
        var log = $("#log-panel");
        var body = $('body');
        localStorage['showLog'] = 'true';
        log.css({ 'bottom': '50%' });
        body.css({
            'overflow-y': 'hidden'
        });
    }
    Core.showLogPanel = showLogPanel;
    /**
     * Returns the CSS class for a log level based on if its info, warn, error etc.
     *
     * @method logLevelClass
     * @static
     * @param {String} level
     * @return {String}
     */
    function logLevelClass(level) {
        if (level) {
            var first = level[0];
            if (first === 'w' || first === "W") {
                return "warning";
            }
            else if (first === 'e' || first === "E") {
                return "error";
            }
            else if (first === 'i' || first === "I") {
                return "info";
            }
            else if (first === 'd' || first === "D") {
                // we have no debug css style
                return "";
            }
        }
        return "";
    }
    Core.logLevelClass = logLevelClass;
    function toPath(hashUrl) {
        if (Core.isBlank(hashUrl)) {
            return hashUrl;
        }
        if (hashUrl.startsWith("#")) {
            return hashUrl.substring(1);
        }
        else {
            return hashUrl;
        }
    }
    Core.toPath = toPath;
    function parseMBean(mbean) {
        var answer = {};
        var parts = mbean.split(":");
        if (parts.length > 1) {
            answer['domain'] = parts.first();
            parts = parts.exclude(parts.first());
            parts = parts.join(":");
            answer['attributes'] = {};
            var nameValues = parts.split(",");
            nameValues.forEach(function (str) {
                var nameValue = str.split('=');
                var name = nameValue.first().trim();
                nameValue = nameValue.exclude(nameValue.first());
                answer['attributes'][name] = nameValue.join('=').trim();
            });
        }
        return answer;
    }
    Core.parseMBean = parseMBean;
    function executePostLoginTasks() {
        Core.log.debug("Executing post login tasks");
        Core.postLoginTasks.execute();
    }
    Core.executePostLoginTasks = executePostLoginTasks;
    function executePreLogoutTasks(onComplete) {
        Core.log.debug("Executing pre logout tasks");
        Core.preLogoutTasks.onComplete(onComplete);
        Core.preLogoutTasks.execute();
    }
    Core.executePreLogoutTasks = executePreLogoutTasks;
    /**
     * log out the current user
     * @for Core
     * @static
     * @method logout
     * @param {String} jolokiaUrl
     * @param {*} userDetails
     * @param {Object} localStorage
     * @param {Object} $scope
     * @param {Function} successCB
     * @param {Function} errorCB
     *
     */
    function logout(jolokiaUrl, userDetails, localStorage, $scope, successCB, errorCB) {
        if (successCB === void 0) { successCB = null; }
        if (errorCB === void 0) { errorCB = null; }
        if (jolokiaUrl) {
            var url = "auth/logout/";
            Core.executePreLogoutTasks(function () {
                $.ajax(url, {
                    type: "POST",
                    success: function () {
                        userDetails.username = null;
                        userDetails.password = null;
                        userDetails.loginDetails = null;
                        userDetails.rememberMe = false;
                        delete localStorage['userDetails'];
                        var jvmConnect = angular.fromJson(localStorage['jvmConnect']);
                        _.each(jvmConnect, function (value) {
                            delete value['userName'];
                            delete value['password'];
                        });
                        localStorage.setItem('jvmConnect', angular.toJson(jvmConnect));
                        localStorage.removeItem('activemqUserName');
                        localStorage.removeItem('activemqPassword');
                        if (successCB && angular.isFunction(successCB)) {
                            successCB();
                        }
                        Core.$apply($scope);
                    },
                    error: function (xhr, textStatus, error) {
                        userDetails.username = null;
                        userDetails.password = null;
                        userDetails.loginDetails = null;
                        userDetails.rememberMe = false;
                        delete localStorage['userDetails'];
                        var jvmConnect = angular.fromJson(localStorage['jvmConnect']);
                        _.each(jvmConnect, function (value) {
                            delete value['userName'];
                            delete value['password'];
                        });
                        localStorage.setItem('jvmConnect', angular.toJson(jvmConnect));
                        localStorage.removeItem('activemqUserName');
                        localStorage.removeItem('activemqPassword');
                        switch (xhr.status) {
                            case 401:
                                Core.log.debug('Failed to log out, ', error);
                                break;
                            case 403:
                                Core.log.debug('Failed to log out, ', error);
                                break;
                            case 0:
                                break;
                            default:
                                Core.log.debug('Failed to log out, ', error);
                                break;
                        }
                        if (errorCB && angular.isFunction(errorCB)) {
                            errorCB();
                        }
                        Core.$apply($scope);
                    }
                });
            });
        }
    }
    Core.logout = logout;
    /**
     * Creates a link by appending the current $location.search() hash to the given href link,
     * removing any required parameters from the link
     * @method createHref
     * @for Core
     * @static
     * @param {ng.ILocationService} $location
     * @param {String} href the link to have any $location.search() hash parameters appended
     * @param {Array} removeParams any parameters to be removed from the $location.search()
     * @return {Object} the link with any $location.search() parameters added
     */
    function createHref($location, href, removeParams) {
        if (removeParams === void 0) { removeParams = null; }
        var hashMap = angular.copy($location.search());
        // lets remove any top level nav bar related hash searches
        if (removeParams) {
            angular.forEach(removeParams, function (param) { return delete hashMap[param]; });
        }
        var hash = Core.hashToString(hashMap);
        if (hash) {
            var prefix = (href.indexOf("?") >= 0) ? "&" : "?";
            href += prefix + hash;
        }
        return href;
    }
    Core.createHref = createHref;
    /**
     * Turns the given search hash into a URI style query string
     * @method hashToString
     * @for Core
     * @static
     * @param {Object} hash
     * @return {String}
     */
    function hashToString(hash) {
        var keyValuePairs = [];
        angular.forEach(hash, function (value, key) {
            keyValuePairs.push(key + "=" + value);
        });
        var params = keyValuePairs.join("&");
        return encodeURI(params);
    }
    Core.hashToString = hashToString;
    /**
     * Parses the given string of x=y&bar=foo into a hash
     * @method stringToHash
     * @for Core
     * @static
     * @param {String} hashAsString
     * @return {Object}
     */
    function stringToHash(hashAsString) {
        var entries = {};
        if (hashAsString) {
            var text = decodeURI(hashAsString);
            var items = text.split('&');
            angular.forEach(items, function (item) {
                var kv = item.split('=');
                var key = kv[0];
                var value = kv[1] || key;
                entries[key] = value;
            });
        }
        return entries;
    }
    Core.stringToHash = stringToHash;
    /**
     * Register a JMX operation to poll for changes, only
     * calls back when a change occurs
     *
     * @param jolokia
     * @param scope
     * @param arguments
     * @param callback
     * @param options
     * @returns Object
     */
    function registerForChanges(jolokia, $scope, arguments, callback, options) {
        var decorated = {
            responseJson: '',
            success: function (response) {
                var json = angular.toJson(response.value);
                if (decorated.responseJson !== json) {
                    decorated.responseJson = json;
                    callback(response);
                }
            }
        };
        angular.extend(decorated, options);
        return Core.register(jolokia, $scope, arguments, onSuccess(undefined, decorated));
    }
    Core.registerForChanges = registerForChanges;
    var responseHistory = null;
    function getOrInitObjectFromLocalStorage(key) {
        var answer = undefined;
        if (!(key in localStorage)) {
            localStorage[key] = angular.toJson({});
        }
        return angular.fromJson(localStorage[key]);
    }
    Core.getOrInitObjectFromLocalStorage = getOrInitObjectFromLocalStorage;
    function argumentsToString(arguments) {
        return StringHelpers.toString(arguments);
    }
    function keyForArgument(argument) {
        if (!('type' in argument)) {
            return null;
        }
        var answer = argument['type'];
        switch (answer.toLowerCase()) {
            case 'exec':
                answer += ':' + argument['mbean'] + ':' + argument['operation'];
                var argString = argumentsToString(argument['arguments']);
                if (!Core.isBlank(argString)) {
                    answer += ':' + argString;
                }
                break;
            case 'read':
                answer += ':' + argument['mbean'] + ':' + argument['attribute'];
                break;
            default:
                return null;
        }
        return answer;
    }
    function createResponseKey(arguments) {
        var answer = '';
        if (angular.isArray(arguments)) {
            answer = arguments.map(function (arg) {
                return keyForArgument(arg);
            }).join(':');
        }
        else {
            answer = keyForArgument(arguments);
        }
        return answer;
    }
    function getResponseHistory() {
        if (responseHistory === null) {
            //responseHistory = getOrInitObjectFromLocalStorage('responseHistory');
            responseHistory = {};
            Core.log.debug("Created response history", responseHistory);
        }
        return responseHistory;
    }
    Core.getResponseHistory = getResponseHistory;
    Core.MAX_RESPONSE_CACHE_SIZE = 20;
    function getOldestKey(responseHistory) {
        var oldest = null;
        var oldestKey = null;
        angular.forEach(responseHistory, function (value, key) {
            //log.debug("Checking entry: ", key);
            //log.debug("Oldest timestamp: ", oldest, " key: ", key, " value: ", value);
            if (!value || !value.timestamp) {
                // null value is an excellent candidate for deletion
                oldest = 0;
                oldestKey = key;
            }
            else if (oldest === null || value.timestamp < oldest) {
                oldest = value.timestamp;
                oldestKey = key;
            }
        });
        return oldestKey;
    }
    function addResponse(arguments, value) {
        var responseHistory = getResponseHistory();
        var key = createResponseKey(arguments);
        if (key === null) {
            Core.log.debug("key for arguments is null, not caching: ", StringHelpers.toString(arguments));
            return;
        }
        //log.debug("Adding response to history, key: ", key, " value: ", value);
        // trim the cache if needed
        var keys = _.keys(responseHistory);
        //log.debug("Number of stored responses: ", keys.length);
        if (keys.length >= Core.MAX_RESPONSE_CACHE_SIZE) {
            Core.log.debug("Cache limit (", Core.MAX_RESPONSE_CACHE_SIZE, ") met or  exceeded (", keys.length, "), trimming oldest response");
            var oldestKey = getOldestKey(responseHistory);
            if (oldestKey !== null) {
                // delete the oldest entry
                Core.log.debug("Deleting key: ", oldestKey);
                delete responseHistory[oldestKey];
            }
            else {
                Core.log.debug("Got null key, could be a cache problem, wiping cache");
                keys.forEach(function (key) {
                    Core.log.debug("Deleting key: ", key);
                    delete responseHistory[key];
                });
            }
        }
        responseHistory[key] = value;
        //localStorage['responseHistory'] = angular.toJson(responseHistory);
    }
    function getResponse(jolokia, arguments, callback) {
        var responseHistory = getResponseHistory();
        var key = createResponseKey(arguments);
        if (key === null) {
            jolokia.request(arguments, callback);
            return;
        }
        if (key in responseHistory && 'success' in callback) {
            var value = responseHistory[key];
            // do this async, the controller might not handle us immediately calling back
            setTimeout(function () {
                callback['success'](value);
            }, 10);
        }
        else {
            Core.log.debug("Unable to find existing response for key: ", key);
            jolokia.request(arguments, callback);
        }
    }
    // end jolokia caching stuff
    /**
     * Register a JMX operation to poll for changes
     * @method register
     * @for Core
     * @static
     * @return {Function} a zero argument function for unregistering  this registration
     * @param {*} jolokia
     * @param {*} scope
     * @param {Object} arguments
     * @param {Function} callback
     */
    function register(jolokia, scope, arguments, callback) {
        /*
        if (scope && !Core.isBlank(scope.name)) {
          Core.log.debug("Calling register from scope: ", scope.name);
        } else {
          Core.log.debug("Calling register from anonymous scope");
        }
        */
        if (!angular.isDefined(scope.$jhandle) || !angular.isArray(scope.$jhandle)) {
            //log.debug("No existing handle set, creating one");
            scope.$jhandle = [];
        }
        else {
        }
        if (angular.isDefined(scope.$on)) {
            scope.$on('$destroy', function (event) {
                unregister(jolokia, scope);
            });
        }
        var handle = null;
        if ('success' in callback) {
            var cb = callback.success;
            var args = arguments;
            callback.success = function (response) {
                addResponse(args, response);
                cb(response);
            };
        }
        if (angular.isArray(arguments)) {
            if (arguments.length >= 1) {
                // TODO can't get this to compile in typescript :)
                //var args = [callback].concat(arguments);
                var args = [callback];
                angular.forEach(arguments, function (value) { return args.push(value); });
                //var args = [callback];
                //args.push(arguments);
                var registerFn = jolokia.register;
                handle = registerFn.apply(jolokia, args);
                scope.$jhandle.push(handle);
                getResponse(jolokia, arguments, callback);
            }
        }
        else {
            handle = jolokia.register(callback, arguments);
            scope.$jhandle.push(handle);
            getResponse(jolokia, arguments, callback);
        }
        return function () {
            if (handle !== null) {
                scope.$jhandle.remove(handle);
                jolokia.unregister(handle);
            }
        };
    }
    Core.register = register;
    /**
     * Register a JMX operation to poll for changes using a jolokia search using the given mbean pattern
     * @method registerSearch
     * @for Core
     * @static
     * @paran {*} jolokia
     * @param {*} scope
     * @param {String} mbeanPattern
     * @param {Function} callback
     */
    /*
    TODO - won't compile, and where is 'arguments' coming from?
    export function registerSearch(jolokia:Jolokia.IJolokia, scope, mbeanPattern:string, callback) {
        if (!angular.isDefined(scope.$jhandle) || !angular.isArray(scope.$jhandle)) {
            scope.$jhandle = [];
        }
        if (angular.isDefined(scope.$on)) {
            scope.$on('$destroy', function (event) {
                unregister(jolokia, scope);
            });
        }
        if (angular.isArray(arguments)) {
            if (arguments.length >= 1) {
                // TODO can't get this to compile in typescript :)
                //var args = [callback].concat(arguments);
                var args = [callback];
                angular.forEach(arguments, (value) => args.push(value));
                //var args = [callback];
                //args.push(arguments);
                var registerFn = jolokia.register;
                var handle = registerFn.apply(jolokia, args);
                scope.$jhandle.push(handle);
                jolokia.search(mbeanPattern, callback);
            }
        } else {
            var handle = jolokia.register(callback, arguments);
            scope.$jhandle.push(handle);
            jolokia.search(mbeanPattern, callback);
        }
    }
    */
    function unregister(jolokia, scope) {
        if (angular.isDefined(scope.$jhandle)) {
            scope.$jhandle.forEach(function (handle) {
                jolokia.unregister(handle);
            });
            delete scope.$jhandle;
        }
    }
    Core.unregister = unregister;
    /**
     * The default error handler which logs errors either using debug or log level logging based on the silent setting
     * @param response the response from a jolokia request
     */
    function defaultJolokiaErrorHandler(response, options) {
        if (options === void 0) { options = {}; }
        //alert("Jolokia request failed: " + response.error);
        var stacktrace = response.stacktrace;
        if (stacktrace) {
            var silent = options['silent'];
            if (!silent) {
                var operation = Core.pathGet(response, ['request', 'operation']) || "unknown";
                if (stacktrace.indexOf("javax.management.InstanceNotFoundException") >= 0 || stacktrace.indexOf("javax.management.AttributeNotFoundException") >= 0 || stacktrace.indexOf("java.lang.IllegalArgumentException: No operation") >= 0) {
                    // ignore these errors as they can happen on timing issues
                    // such as its been removed
                    // or if we run against older containers
                    Core.log.debug("Operation ", operation, " failed due to: ", response['error']);
                }
                else {
                    Core.log.warn("Operation ", operation, " failed due to: ", response['error']);
                }
            }
            else {
                Core.log.debug("Operation ", operation, " failed due to: ", response['error']);
            }
        }
    }
    Core.defaultJolokiaErrorHandler = defaultJolokiaErrorHandler;
    /**
     * Logs any failed operation and stack traces
     */
    function logJolokiaStackTrace(response) {
        var stacktrace = response.stacktrace;
        if (stacktrace) {
            var operation = Core.pathGet(response, ['request', 'operation']) || "unknown";
            Core.log.info("Operation ", operation, " failed due to: ", response['error']);
        }
    }
    Core.logJolokiaStackTrace = logJolokiaStackTrace;
    /**
     * Converts the given XML node to a string representation of the XML
     * @method xmlNodeToString
     * @for Core
     * @static
     * @param {Object} xmlNode
     * @return {Object}
     */
    function xmlNodeToString(xmlNode) {
        try {
            // Gecko- and Webkit-based browsers (Firefox, Chrome), Opera.
            return (new XMLSerializer()).serializeToString(xmlNode);
        }
        catch (e) {
            try {
                // Internet Explorer.
                return xmlNode.xml;
            }
            catch (e) {
                //Other browsers without XML Serializer
                console.log('WARNING: XMLSerializer not supported');
            }
        }
        return false;
    }
    Core.xmlNodeToString = xmlNodeToString;
    /**
     * Returns true if the given DOM node is a text node
     * @method isTextNode
     * @for Core
     * @static
     * @param {Object} node
     * @return {Boolean}
     */
    function isTextNode(node) {
        return node && node.nodeType === 3;
    }
    Core.isTextNode = isTextNode;
    /**
     * Returns the lowercase file extension of the given file name or returns the empty
     * string if the file does not have an extension
     * @method fileExtension
     * @for Core
     * @static
     * @param {String} name
     * @param {String} defaultValue
     * @return {String}
     */
    function fileExtension(name, defaultValue) {
        if (defaultValue === void 0) { defaultValue = ""; }
        var extension = defaultValue;
        if (name) {
            var idx = name.lastIndexOf(".");
            if (idx > 0) {
                extension = name.substring(idx + 1, name.length).toLowerCase();
            }
        }
        return extension;
    }
    Core.fileExtension = fileExtension;
    function getUUID() {
        var d = new Date();
        var ms = (d.getTime() * 1000) + d.getUTCMilliseconds();
        var random = Math.floor((1 + Math.random()) * 0x10000);
        return ms.toString(16) + random.toString(16);
    }
    Core.getUUID = getUUID;
    var _versionRegex = /[^\d]*(\d+)\.(\d+)(\.(\d+))?.*/;
    /**
     * Parses some text of the form "xxxx2.3.4xxxx"
     * to extract the version numbers as an array of numbers then returns an array of 2 or 3 numbers.
     *
     * Characters before the first digit are ignored as are characters after the last digit.
     * @method parseVersionNumbers
     * @for Core
     * @static
     * @param {String} text a maven like string containing a dash then numbers separated by dots
     * @return {Array}
     */
    function parseVersionNumbers(text) {
        if (text) {
            var m = text.match(_versionRegex);
            if (m && m.length > 4) {
                var m1 = m[1];
                var m2 = m[2];
                var m4 = m[4];
                if (angular.isDefined(m4)) {
                    return [parseInt(m1), parseInt(m2), parseInt(m4)];
                }
                else if (angular.isDefined(m2)) {
                    return [parseInt(m1), parseInt(m2)];
                }
                else if (angular.isDefined(m1)) {
                    return [parseInt(m1)];
                }
            }
        }
        return null;
    }
    Core.parseVersionNumbers = parseVersionNumbers;
    /**
     * Converts a version string with numbers and dots of the form "123.456.790" into a string
     * which is sortable as a string, by left padding each string between the dots to at least 4 characters
     * so things just sort as a string.
     *
     * @param text
     * @return {string} the sortable version string
     */
    function versionToSortableString(version, maxDigitsBetweenDots) {
        if (maxDigitsBetweenDots === void 0) { maxDigitsBetweenDots = 4; }
        return (version || "").split(".").map(function (x) {
            var length = x.length;
            return (length >= maxDigitsBetweenDots) ? x : x.padLeft(' ', maxDigitsBetweenDots - length);
        }).join(".");
    }
    Core.versionToSortableString = versionToSortableString;
    function time(message, fn) {
        var start = new Date().getTime();
        var answer = fn();
        var elapsed = new Date().getTime() - start;
        console.log(message + " " + elapsed);
        return answer;
    }
    Core.time = time;
    /**
     * Compares the 2 version arrays and returns -1 if v1 is less than v2 or 0 if they are equal or 1 if v1 is greater than v2
     * @method compareVersionNumberArrays
     * @for Core
     * @static
     * @param {Array} v1 an array of version numbers with the most significant version first (major, minor, patch).
     * @param {Array} v2
     * @return {Number}
     */
    function compareVersionNumberArrays(v1, v2) {
        if (v1 && !v2) {
            return 1;
        }
        if (!v1 && v2) {
            return -1;
        }
        if (v1 === v2) {
            return 0;
        }
        for (var i = 0; i < v1.length; i++) {
            var n1 = v1[i];
            if (i >= v2.length) {
                return 1;
            }
            var n2 = v2[i];
            if (!angular.isDefined(n1)) {
                return -1;
            }
            if (!angular.isDefined(n2)) {
                return 1;
            }
            if (n1 > n2) {
                return 1;
            }
            else if (n1 < n2) {
                return -1;
            }
        }
        return 0;
    }
    Core.compareVersionNumberArrays = compareVersionNumberArrays;
    /**
     * Helper function which converts objects into tables of key/value properties and
     * lists into a <ul> for each value.
     * @method valueToHtml
     * @for Core
     * @static
     * @param {any} value
     * @return {String}
     */
    function valueToHtml(value) {
        if (angular.isArray(value)) {
            var size = value.length;
            if (!size) {
                return "";
            }
            else if (size === 1) {
                return valueToHtml(value[0]);
            }
            else {
                var buffer = "<ul>";
                angular.forEach(value, function (childValue) {
                    buffer += "<li>" + valueToHtml(childValue) + "</li>";
                });
                return buffer + "</ul>";
            }
        }
        else if (angular.isObject(value)) {
            var buffer = "<table>";
            angular.forEach(value, function (childValue, key) {
                buffer += "<tr><td>" + key + "</td><td>" + valueToHtml(childValue) + "</td></tr>";
            });
            return buffer + "</table>";
        }
        else if (angular.isString(value)) {
            var uriPrefixes = ["http://", "https://", "file://", "mailto:"];
            var answer = value;
            angular.forEach(uriPrefixes, function (prefix) {
                if (answer.startsWith(prefix)) {
                    answer = "<a href='" + value + "'>" + value + "</a>";
                }
            });
            return answer;
        }
        return value;
    }
    Core.valueToHtml = valueToHtml;
    /**
     * If the string starts and ends with [] {} then try parse as JSON and return the parsed content or return null
     * if it does not appear to be JSON
     * @method tryParseJson
     * @for Core
     * @static
     * @param {String} text
     * @return {Object}
     */
    function tryParseJson(text) {
        text = text.trim();
        if ((text.startsWith("[") && text.endsWith("]")) || (text.startsWith("{") && text.endsWith("}"))) {
            try {
                return JSON.parse(text);
            }
            catch (e) {
            }
        }
        return null;
    }
    Core.tryParseJson = tryParseJson;
    /**
     * Given values (n, "person") will return either "1 person" or "2 people" depending on if a plural
     * is required using the String.pluralize() function from sugarjs
     * @method maybePlural
     * @for Core
     * @static
     * @param {Number} count
     * @param {String} word
     * @return {String}
     */
    function maybePlural(count, word) {
        var pluralWord = (count === 1) ? word : word.pluralize();
        return "" + count + " " + pluralWord;
    }
    Core.maybePlural = maybePlural;
    /**
     * given a JMX ObjectName of the form <code>domain:key=value,another=something</code> then return the object
     * <code>{key: "value", another: "something"}</code>
     * @method objectNameProperties
     * @for Core
     * @static
     * @param {String} name
     * @return {Object}
     */
    function objectNameProperties(objectName) {
        var entries = {};
        if (objectName) {
            var idx = objectName.indexOf(":");
            if (idx > 0) {
                var path = objectName.substring(idx + 1);
                var items = path.split(',');
                angular.forEach(items, function (item) {
                    var kv = item.split('=');
                    var key = kv[0];
                    var value = kv[1] || key;
                    entries[key] = value;
                });
            }
        }
        return entries;
    }
    Core.objectNameProperties = objectNameProperties;
    /*
    export function setPageTitle($document, title:Core.PageTitle) {
      $document.attr('title', title.getTitleWithSeparator(' '));
    }
  
    export function setPageTitleWithTab($document, title:Core.PageTitle, tab:string) {
      $document.attr('title', title.getTitleWithSeparator(' ') + " " + tab);
    }
    */
    /**
     * Removes dodgy characters from a value such as '/' or '.' so that it can be used as a DOM ID value
     * and used in jQuery / CSS selectors
     * @method toSafeDomID
     * @for Core
     * @static
     * @param {String} text
     * @return {String}
     */
    function toSafeDomID(text) {
        return text ? text.replace(/(\/|\.)/g, "_") : text;
    }
    Core.toSafeDomID = toSafeDomID;
    /**
     * Invokes the given function on each leaf node in the array of folders
     * @method forEachLeafFolder
     * @for Core
     * @static
     * @param {Array[Folder]} folders
     * @param {Function} fn
     */
    function forEachLeafFolder(folders, fn) {
        angular.forEach(folders, function (folder) {
            var children = folder["children"];
            if (angular.isArray(children) && children.length > 0) {
                forEachLeafFolder(children, fn);
            }
            else {
                fn(folder);
            }
        });
    }
    Core.forEachLeafFolder = forEachLeafFolder;
    function extractHashURL(url) {
        var parts = url.split('#');
        if (parts.length === 0) {
            return url;
        }
        var answer = parts[1];
        if (parts.length > 1) {
            var remaining = parts.last(parts.length - 2);
            remaining.forEach(function (part) {
                answer = answer + "#" + part;
            });
        }
        return answer;
    }
    Core.extractHashURL = extractHashURL;
    function authHeaderValue(userDetails) {
        return getBasicAuthHeader(userDetails.username, userDetails.password);
    }
    Core.authHeaderValue = authHeaderValue;
    function getBasicAuthHeader(username, password) {
        var authInfo = username + ":" + password;
        authInfo = authInfo.encodeBase64();
        return "Basic " + authInfo;
    }
    Core.getBasicAuthHeader = getBasicAuthHeader;
    var httpRegex = new RegExp('^(https?):\/\/(([^:/?#]*)(?::([0-9]+))?)');
    /**
     * Breaks a URL up into a nice object
     * @method parseUrl
     * @for Core
     * @static
     * @param url
     * @returns object
     */
    function parseUrl(url) {
        if (Core.isBlank(url)) {
            return null;
        }
        var matches = url.match(httpRegex);
        if (matches === null) {
            return null;
        }
        //log.debug("matches: ", matches);
        var scheme = matches[1];
        var host = matches[3];
        var port = matches[4];
        var parts = null;
        if (!Core.isBlank(port)) {
            parts = url.split(port);
        }
        else {
            parts = url.split(host);
        }
        var path = parts[1];
        if (path && path.startsWith('/')) {
            path = path.slice(1, path.length);
        }
        //log.debug("parts: ", parts);
        return {
            scheme: scheme,
            host: host,
            port: port,
            path: path
        };
    }
    Core.parseUrl = parseUrl;
    function getDocHeight() {
        var D = document;
        return Math.max(Math.max(D.body.scrollHeight, D.documentElement.scrollHeight), Math.max(D.body.offsetHeight, D.documentElement.offsetHeight), Math.max(D.body.clientHeight, D.documentElement.clientHeight));
    }
    Core.getDocHeight = getDocHeight;
    /**
     * If a URL is external to the current web application, then
     * replace the URL with the proxy servlet URL
     * @method useProxyIfExternal
     * @for Core
     * @static
     * @param {String} connectUrl
     * @return {String}
     */
    function useProxyIfExternal(connectUrl) {
        if (Core.isChromeApp()) {
            return connectUrl;
        }
        var host = window.location.host;
        if (!connectUrl.startsWith("http://" + host + "/") && !connectUrl.startsWith("https://" + host + "/")) {
            // lets remove the http stuff
            var idx = connectUrl.indexOf("://");
            if (idx > 0) {
                connectUrl = connectUrl.substring(idx + 3);
            }
            // lets replace the : with a /
            connectUrl = connectUrl.replace(":", "/");
            connectUrl = Core.trimLeading(connectUrl, "/");
            connectUrl = Core.trimTrailing(connectUrl, "/");
            connectUrl = Core.url("/proxy/" + connectUrl);
        }
        return connectUrl;
    }
    Core.useProxyIfExternal = useProxyIfExternal;
    /*
    export function checkInjectorLoaded() {
      // TODO sometimes the injector is not yet initialised; so lets try initialise it here just in case
      if (!Core.injector) {
        Core.injector = angular.element(document.documentElement).injector();
      }
    }
    */
    /**
     * Extracts the url of the target, eg usually http://localhost:port, but if we use fabric to proxy to another host,
     * then we return the url that we proxied too (eg the real target)
     *
     * @param {ng.ILocationService} $location
     * @param {String} scheme to force use a specific scheme, otherwise the scheme from location is used
     * @param {Number} port to force use a specific port number, otherwise the port from location is used
     */
    function extractTargetUrl($location, scheme, port) {
        if (angular.isUndefined(scheme)) {
            scheme = $location.scheme();
        }
        var host = $location.host();
        //  $location.search()['url']; does not work for some strange reason
        // var qUrl = $location.search()['url'];
        // if its a proxy request using hawtio-proxy servlet, then the url parameter
        // has the actual host/port
        var qUrl = $location.absUrl();
        var idx = qUrl.indexOf("url=");
        if (idx > 0) {
            qUrl = qUrl.substr(idx + 4);
            var value = decodeURIComponent(qUrl);
            if (value) {
                idx = value.indexOf("/proxy/");
                // after proxy we have host and optional port (if port is not 80)
                if (idx > 0) {
                    value = value.substr(idx + 7);
                    // if the path has http:// or some other scheme in it lets trim that off
                    idx = value.indexOf("://");
                    if (idx > 0) {
                        value = value.substr(idx + 3);
                    }
                    var data = value.split("/");
                    if (data.length >= 1) {
                        host = data[0];
                    }
                    if (angular.isUndefined(port) && data.length >= 2) {
                        var qPort = Core.parseIntValue(data[1], "port number");
                        if (qPort) {
                            port = qPort;
                        }
                    }
                }
            }
        }
        if (angular.isUndefined(port)) {
            port = $location.port();
        }
        var url = scheme + "://" + host;
        if (port != 80) {
            url += ":" + port;
        }
        return url;
    }
    Core.extractTargetUrl = extractTargetUrl;
    /**
     * Returns true if the $location is from the hawtio proxy
     */
    function isProxyUrl($location) {
        var url = $location.url();
        return url.indexOf('/hawtio/proxy/') > 0;
    }
    Core.isProxyUrl = isProxyUrl;
    /**
     * handy do nothing converter for the below function
     **/
    function doNothing(value) {
        return value;
    }
    Core.doNothing = doNothing;
    // moved these into their own helper file
    Core.bindModelToSearchParam = ControllerHelpers.bindModelToSearchParam;
    Core.reloadWhenParametersChange = ControllerHelpers.reloadWhenParametersChange;
    /**
     * Returns a new function which ensures that the delegate function is only invoked at most once
     * within the given number of millseconds
     * @method throttled
     * @for Core
     * @static
     * @param {Function} fn the function to be invoked at most once within the given number of millis
     * @param {Number} millis the time window during which this function should only be called at most once
     * @return {Object}
     */
    function throttled(fn, millis) {
        var nextInvokeTime = 0;
        var lastAnswer = null;
        return function () {
            var now = Date.now();
            if (nextInvokeTime < now) {
                nextInvokeTime = now + millis;
                lastAnswer = fn();
            }
            else {
            }
            return lastAnswer;
        };
    }
    Core.throttled = throttled;
    /**
     * Attempts to parse the given JSON text and returns the JSON object structure or null.
     *Bad JSON is logged at info level.
     *
     * @param text a JSON formatted string
     * @param message description of the thing being parsed logged if its invalid
     */
    function parseJsonText(text, message) {
        if (message === void 0) { message = "JSON"; }
        var answer = null;
        try {
            answer = angular.fromJson(text);
        }
        catch (e) {
            Core.log.info("Failed to parse " + message + " from: " + text + ". " + e);
        }
        return answer;
    }
    Core.parseJsonText = parseJsonText;
    /**
     * Returns the humanized markup of the given value
     */
    function humanizeValueHtml(value) {
        var formattedValue = "";
        if (value === true) {
            formattedValue = '<i class="icon-check"></i>';
        }
        else if (value === false) {
            formattedValue = '<i class="icon-check-empty"></i>';
        }
        else {
            formattedValue = Core.humanizeValue(value);
        }
        return formattedValue;
    }
    Core.humanizeValueHtml = humanizeValueHtml;
    /**
     * Gets a query value from the given url
     *
     * @param url  url
     * @param parameterName the uri parameter value to get
     * @returns {*}
     */
    function getQueryParameterValue(url, parameterName) {
        var parts;
        var query = (url || '').split('?');
        if (query && query.length > 0) {
            parts = query[1];
        }
        else {
            parts = '';
        }
        var vars = parts.split('&');
        for (var i = 0; i < vars.length; i++) {
            var pair = vars[i].split('=');
            if (decodeURIComponent(pair[0]) == parameterName) {
                return decodeURIComponent(pair[1]);
            }
        }
        // not found
        return null;
    }
    Core.getQueryParameterValue = getQueryParameterValue;
    /**
     * Takes a value in ms and returns a human readable
     * duration
     * @param value
     */
    function humanizeMilliseconds(value) {
        if (!angular.isNumber(value)) {
            return "XXX";
        }
        var seconds = value / 1000;
        var years = Math.floor(seconds / 31536000);
        if (years) {
            return maybePlural(years, "year");
        }
        var days = Math.floor((seconds %= 31536000) / 86400);
        if (days) {
            return maybePlural(days, "day");
        }
        var hours = Math.floor((seconds %= 86400) / 3600);
        if (hours) {
            return maybePlural(hours, 'hour');
        }
        var minutes = Math.floor((seconds %= 3600) / 60);
        if (minutes) {
            return maybePlural(minutes, 'minute');
        }
        seconds = Math.floor(seconds % 60);
        if (seconds) {
            return maybePlural(seconds, 'second');
        }
        return value + " ms";
    }
    Core.humanizeMilliseconds = humanizeMilliseconds;
    /*
      export function storeConnectionRegex(regexs, name, json) {
        if (!regexs.any((r) => { r['name'] === name })) {
          var regex:string = '';
    
          if (json['useProxy']) {
            regex = '/hawtio/proxy/';
          } else {
            regex = '//';
          }
          regex += json['host'] + ':' + json['port'] + '/' + json['path'];
          regexs.push({
            name: name,
            regex: regex.escapeURL(true),
            color: UI.colors.sample()
          });
          writeRegexs(regexs);
        }
      }
    */
    function getRegexs() {
        var regexs = [];
        try {
            regexs = angular.fromJson(localStorage['regexs']);
        }
        catch (e) {
            // corrupted config
            delete localStorage['regexs'];
        }
        return regexs;
    }
    Core.getRegexs = getRegexs;
    function removeRegex(name) {
        var regexs = Core.getRegexs();
        var hasFunc = function (r) {
            return r['name'] === name;
        };
        if (regexs.any(hasFunc)) {
            regexs = regexs.exclude(hasFunc);
            Core.writeRegexs(regexs);
        }
    }
    Core.removeRegex = removeRegex;
    function writeRegexs(regexs) {
        localStorage['regexs'] = angular.toJson(regexs);
    }
    Core.writeRegexs = writeRegexs;
    function maskPassword(value) {
        if (value) {
            var text = '' + value;
            // we use the same patterns as in Apache Camel in its
            // org.apache.camel.util.URISupport.sanitizeUri
            var userInfoPattern = "(.*://.*:)(.*)(@)";
            value = value.replace(new RegExp(userInfoPattern, 'i'), "$1xxxxxx$3");
        }
        return value;
    }
    Core.maskPassword = maskPassword;
    /**
     * Match the given filter against the text, ignoring any case.
     * <p/>
     * This operation will regard as a match if either filter or text is null/undefined.
     * As its used for filtering out, unmatched.
     * <p/>
     *
     * @param text   the text
     * @param filter the filter
     * @return true if matched, false if not.
     */
    function matchFilterIgnoreCase(text, filter) {
        if (angular.isUndefined(text) || angular.isUndefined(filter)) {
            return true;
        }
        if (text == null || filter == null) {
            return true;
        }
        text = text.toString().trim().toLowerCase();
        filter = filter.toString().trim().toLowerCase();
        if (text.length === 0 || filter.length === 0) {
            return true;
        }
        // there can be more tokens separated by comma
        var tokens = filter.split(",");
        // filter out empty tokens, and make sure its trimmed
        tokens = tokens.filter(function (t) {
            return t.length > 0;
        }).map(function (t) {
            return t.trim();
        });
        // match if any of the tokens matches the text
        var answer = tokens.some(function (t) {
            var bool = text.indexOf(t) > -1;
            return bool;
        });
        return answer;
    }
    Core.matchFilterIgnoreCase = matchFilterIgnoreCase;
})(Core || (Core = {}));

/// <reference path="coreHelpers.ts" />
var CoreFilters;
(function (CoreFilters) {
    var pluginName = 'hawtio-core-filters';
    var _module = angular.module(pluginName, []);
    _module.filter("valueToHtml", function () { return Core.valueToHtml; });
    _module.filter('humanize', function () { return Core.humanizeValue; });
    _module.filter('humanizeMs', function () { return Core.humanizeMilliseconds; });
    _module.filter('maskPassword', function () { return Core.maskPassword; });
    hawtioPluginLoader.addModule(pluginName);
})(CoreFilters || (CoreFilters = {}));

/// <reference path="includes.ts"/>
/// <reference path="tasks.ts"/>
var EventServices;
(function (EventServices) {
    var pluginName = 'hawtio-event-tasks';
    var log = Logger.get(pluginName);
    var _module = angular.module(pluginName, []);
    // service to register tasks that should happen when the URL changes
    _module.factory('locationChangeStartTasks', function () {
        return new Core.ParameterizedTasksImpl();
    });
    // service to register stuff that should happen when the user logs in
    _module.factory('postLoginTasks', function () {
        return Core.postLoginTasks;
    });
    // service to register stuff that should happen when the user logs out
    _module.factory('preLogoutTasks', function () {
        return Core.preLogoutTasks;
    });
    // service to register stuff that should happen after the user logs out
    _module.factory('postLogoutTasks', function () {
        return Core.postLogoutTasks;
    });
    _module.run(['$rootScope', 'locationChangeStartTasks', 'postLoginTasks', 'preLogoutTasks', 'postLogoutTasks', function ($rootScope, locationChangeStartTasks, postLoginTasks, preLogoutTasks, postLogoutTasks) {
        preLogoutTasks.addTask("ResetPreLogoutTasks", function () {
            preLogoutTasks.reset();
        });
        preLogoutTasks.addTask("ResetPostLoginTasks", function () {
            preLogoutTasks.reset();
        });
        postLoginTasks.addTask("ResetPostLogoutTasks", function () {
            postLogoutTasks.reset();
        });
        $rootScope.$on('$locationChangeStart', function ($event, newUrl, oldUrl) {
            locationChangeStartTasks.execute($event, newUrl, oldUrl);
        });
        log.debug("loaded");
    }]);
    hawtioPluginLoader.addModule(pluginName);
})(EventServices || (EventServices = {}));

/// <reference path="includes.ts"/>
/// <reference path="baseHelpers.ts"/>
/// <reference path="coreHelpers.ts"/>
var FileUpload;
(function (FileUpload) {
    // add angular-file-upload to the plugin loader too
    hawtioPluginLoader.addModule('angularFileUpload');
    function useJolokiaTransport($scope, uploader, jolokia, onLoad) {
        // cast the uploader to one that lets us fiddle with it's goodies
        var uploaderInternal = uploader;
        // replace the uploader's transport with one that can post a
        // jolokia request
        uploaderInternal._xhrTransport = function (item) {
            var reader = new FileReader();
            reader.onload = function () {
                // should be FileReader.DONE, but tsc didn't like that
                if (reader.readyState === 2) {
                    var parameters = onLoad(reader.result);
                    jolokia.request(parameters, Core.onSuccess(function (response) {
                        item.json = reader.result;
                        uploaderInternal._onSuccessItem(item, response, response.status, {});
                        uploaderInternal._onCompleteItem(item, response, response.status, {});
                        Core.$apply($scope);
                    }, {
                        error: function (response) {
                            uploaderInternal._onErrorItem(item, response, response.status, {});
                            uploaderInternal._onCompleteItem(item, response, response.status, {});
                            Core.$apply($scope);
                        }
                    }));
                }
            };
            reader.readAsText(item._file);
        };
    }
    FileUpload.useJolokiaTransport = useJolokiaTransport;
})(FileUpload || (FileUpload = {}));

/// <reference path="baseHelpers.ts"/>
var FilterHelpers;
(function (FilterHelpers) {
    FilterHelpers.log = Logger.get("FilterHelpers");
    function search(object, filter, maxDepth, and) {
        if (maxDepth === void 0) { maxDepth = -1; }
        if (and === void 0) { and = true; }
        var f = filter.split(" ");
        var matches = f.filter(function (f) {
            return searchObject(object, f, maxDepth);
        });
        if (and) {
            return matches.length === f.length;
        }
        else {
            return matches.length > 0;
        }
    }
    FilterHelpers.search = search;
    /**
     * Tests if an object contains the text in "filter".  The function
     * only checks the values in an object and ignores keys altogether,
     * can also work with strings/numbers/arrays
     * @param object
     * @param filter
     * @returns {boolean}
     */
    function searchObject(object, filter, maxDepth, depth) {
        if (maxDepth === void 0) { maxDepth = -1; }
        if (depth === void 0) { depth = 0; }
        // avoid inifinite recursion...
        if ((maxDepth > 0 && depth >= maxDepth) || depth > 50) {
            return false;
        }
        var f = filter.toLowerCase();
        var answer = false;
        if (angular.isString(object)) {
            answer = object.toLowerCase().has(f);
        }
        else if (angular.isNumber(object)) {
            answer = ("" + object).toLowerCase().has(f);
        }
        else if (angular.isArray(object)) {
            answer = object.some(function (item) {
                return searchObject(item, f, maxDepth, depth + 1);
            });
        }
        else if (angular.isObject(object)) {
            answer = searchObject(_.values(object), f, maxDepth, depth);
        }
        return answer;
    }
    FilterHelpers.searchObject = searchObject;
})(FilterHelpers || (FilterHelpers = {}));

/// <reference path="includes.ts"/>
var Core;
(function (Core) {
    // helper functions
    function operationToString(name, args) {
        if (!args || args.length === 0) {
            return name + '()';
        }
        else {
            return name + '(' + args.map(function (arg) {
                if (angular.isString(arg)) {
                    arg = angular.fromJson(arg);
                }
                return arg.type;
            }).join(',') + ')';
        }
    }
    Core.operationToString = operationToString;
})(Core || (Core = {}));

/// <reference path="includes.ts"/>
var Log;
(function (Log) {
    var _stackRegex = /\s*at\s+([\w\.$_]+(\.([\w$_]+))*)\((.*)?:(\d+)\).*\[(.*)\]/;
    function formatStackTrace(exception) {
        if (!exception) {
            return '';
        }
        // turn exception into an array
        if (!angular.isArray(exception) && angular.isString(exception)) {
            exception = exception.split('\n');
        }
        if (!angular.isArray(exception)) {
            return "";
        }
        var answer = '<ul class="unstyled">\n';
        exception.each(function (line) {
            answer += "<li>" + Log.formatStackLine(line) + "</li>\n";
        });
        answer += "</ul>\n";
        return answer;
    }
    Log.formatStackTrace = formatStackTrace;
    function formatStackLine(line) {
        var match = _stackRegex.exec(line);
        if (match && match.length > 6) {
            var classAndMethod = match[1];
            var fileName = match[4];
            var line = match[5];
            var mvnCoords = match[6];
            // we can ignore line if its not present...
            if (classAndMethod && fileName && mvnCoords) {
                var className = classAndMethod;
                var idx = classAndMethod.lastIndexOf('.');
                if (idx > 0) {
                    className = classAndMethod.substring(0, idx);
                }
                var link = "#/source/view/" + mvnCoords + "/class/" + className + "/" + fileName;
                if (angular.isDefined(line)) {
                    link += "?line=" + line;
                }
                /*
                        console.log("classAndMethod: " + classAndMethod);
                        console.log("fileName: " + fileName);
                        console.log("line: " + line);
                        console.log("mvnCoords: " + mvnCoords);
                        console.log("Matched " + JSON.stringify(match));
                */
                return "<div class='stack-line'>  at <a href='" + link + "'>" + classAndMethod + "</a>(<span class='fileName'>" + fileName + "</span>:<span class='lineNumber'>" + line + "</span>)[<span class='mavenCoords'>" + mvnCoords + "</span>]</div>";
            }
        }
        var bold = true;
        if (line) {
            line = line.trim();
            if (line.startsWith('at')) {
                line = '  ' + line;
                bold = false;
            }
        }
        if (bold) {
            return '<pre class="stack-line bold">' + line + '</pre>';
        }
        else {
            return '<pre class="stack-line">' + line + '</pre>';
        }
    }
    Log.formatStackLine = formatStackLine;
})(Log || (Log = {}));

/// <reference path="includes.ts"/>
/**
 * Module that provides functions related to working with javascript objects
 */
var ObjectHelpers;
(function (ObjectHelpers) {
    /**
     * Convert an array of 'things' to an object, using 'index' as the attribute name for that value
     * @param arr
     * @param index
     * @param decorator
     */
    function toMap(arr, index, decorator) {
        if (!arr || arr.length === 0) {
            return {};
        }
        var answer = {};
        arr.forEach(function (item) {
            if (angular.isObject(item)) {
                answer[item[index]] = item;
                if (angular.isFunction(decorator)) {
                    decorator(item);
                }
            }
        });
        return answer;
    }
    ObjectHelpers.toMap = toMap;
})(ObjectHelpers || (ObjectHelpers = {}));

/// <reference path="includes.ts"/>
/// <reference path="urlHelpers.ts"/>
var PluginHelpers;
(function (PluginHelpers) {
    // creates a nice little shortcut function that plugins can use to easily
    // prefix controllers with the plugin name, helps avoid redundancy and typos
    function createControllerFunction(_module, pluginName) {
        return function (name, inlineAnnotatedConstructor) {
            return _module.controller(pluginName + '.' + name, inlineAnnotatedConstructor);
        };
    }
    PluginHelpers.createControllerFunction = createControllerFunction;
    // shorthand function to create a configuration for a route, saves a bit
    // of typing
    function createRoutingFunction(templateUrl) {
        return function (templateName, reloadOnSearch) {
            if (reloadOnSearch === void 0) { reloadOnSearch = true; }
            return {
                templateUrl: UrlHelpers.join(templateUrl, templateName),
                reloadOnSearch: reloadOnSearch
            };
        };
    }
    PluginHelpers.createRoutingFunction = createRoutingFunction;
})(PluginHelpers || (PluginHelpers = {}));

/// <reference path="baseHelpers.ts"/>
var PollHelpers;
(function (PollHelpers) {
    var log = Logger.get("PollHelpers");
    function setupPolling($scope, updateFunction, period, $timeout, jolokia) {
        if (period === void 0) { period = 2000; }
        if ($scope.$hasPoller) {
            log.debug("scope already has polling set up, ignoring subsequent polling request");
            return;
        }
        $scope.$hasPoller = true;
        if (!$timeout) {
            $timeout = HawtioCore.injector.get('$timeout');
        }
        if (!jolokia) {
            jolokia = HawtioCore.injector.get('jolokia');
        }
        var promise = undefined;
        var name = $scope.name || 'anonymous scope';
        var refreshFunction = function () {
            // log.debug("polling for scope: ", name);
            updateFunction(function () {
                var keenPollingFn = $scope.$keepPolling;
                if (!angular.isFunction(keenPollingFn)) {
                    keenPollingFn = function () {
                        return !jolokia || jolokia.isRunning();
                    };
                }
                if (keenPollingFn() && $scope.$hasPoller) {
                    promise = $timeout(refreshFunction, period);
                }
            });
        };
        if ($scope.$on) {
            $scope.$on('$destroy', function () {
                log.debug("scope", name, " being destroyed, cancelling polling");
                delete $scope.$hasPoller;
                $timeout.cancel(promise);
            });
            $scope.$on('$routeChangeStart', function () {
                log.debug("route changing, cancelling polling for scope: ", name);
                delete $scope.$hasPoller;
                $timeout.cancel(promise);
            });
        }
        return refreshFunction;
    }
    PollHelpers.setupPolling = setupPolling;
})(PollHelpers || (PollHelpers = {}));

/// <reference path="includes.ts"/>
var Core;
(function (Core) {
    /**
    * Parsers the given value as JSON if it is define
    */
    function parsePreferencesJson(value, key) {
        var answer = null;
        if (angular.isDefined(value)) {
            answer = Core.parseJsonText(value, "localStorage for " + key);
        }
        return answer;
    }
    Core.parsePreferencesJson = parsePreferencesJson;
    function initPreferenceScope($scope, localStorage, defaults) {
        angular.forEach(defaults, function (_default, key) {
            $scope[key] = _default['value'];
            var converter = _default['converter'];
            var formatter = _default['formatter'];
            if (!formatter) {
                formatter = function (value) {
                    return value;
                };
            }
            if (!converter) {
                converter = function (value) {
                    return value;
                };
            }
            if (key in localStorage) {
                var value = converter(localStorage[key]);
                Core.log.debug("from local storage, setting ", key, " to ", value);
                $scope[key] = value;
            }
            else {
                var value = _default['value'];
                Core.log.debug("from default, setting ", key, " to ", value);
                localStorage[key] = value;
            }
            var watchFunc = _default['override'];
            if (!watchFunc) {
                watchFunc = function (newValue, oldValue) {
                    if (newValue !== oldValue) {
                        if (angular.isFunction(_default['pre'])) {
                            _default.pre(newValue);
                        }
                        var value = formatter(newValue);
                        Core.log.debug("to local storage, setting ", key, " to ", value);
                        localStorage[key] = value;
                        if (angular.isFunction(_default['post'])) {
                            _default.post(newValue);
                        }
                    }
                };
            }
            if (_default['compareAsObject']) {
                $scope.$watch(key, watchFunc, true);
            }
            else {
                $scope.$watch(key, watchFunc);
            }
        });
    }
    Core.initPreferenceScope = initPreferenceScope;
    /**
     * Returns true if there is no validFn defined or if its defined
     * then the function returns true.
     *
     * @method isValidFunction
     * @for Perspective
     * @param {Core.Workspace} workspace
     * @param {Function} validFn
     * @param {string} perspectiveId
     * @return {Boolean}
     */
    function isValidFunction(workspace, validFn, perspectiveId) {
        return !validFn || validFn(workspace, perspectiveId);
    }
    Core.isValidFunction = isValidFunction;
})(Core || (Core = {}));

/// <reference path="baseHelpers.ts"/>
var SelectionHelpers;
(function (SelectionHelpers) {
    var log = Logger.get("SelectionHelpers");
    // these functions deal with adding/using a 'selected' item on a group of objects
    function selectNone(group) {
        group.forEach(function (item) {
            item['selected'] = false;
        });
    }
    SelectionHelpers.selectNone = selectNone;
    function selectAll(group, filter) {
        group.forEach(function (item) {
            if (!filter) {
                item['selected'] = true;
            }
            else {
                if (filter(item)) {
                    item['selected'] = true;
                }
            }
        });
    }
    SelectionHelpers.selectAll = selectAll;
    function toggleSelection(item) {
        item['selected'] = !item['selected'];
    }
    SelectionHelpers.toggleSelection = toggleSelection;
    function selectOne(group, item) {
        selectNone(group);
        toggleSelection(item);
    }
    SelectionHelpers.selectOne = selectOne;
    function sync(selections, group, index) {
        group.forEach(function (item) {
            item['selected'] = selections.any(function (selection) {
                return selection[index] === item[index];
            });
        });
        return group.filter(function (item) {
            return item['selected'];
        });
    }
    SelectionHelpers.sync = sync;
    function select(group, item, $event) {
        var ctrlKey = $event.ctrlKey;
        if (!ctrlKey) {
            if (item['selected']) {
                toggleSelection(item);
            }
            else {
                selectOne(group, item);
            }
        }
        else {
            toggleSelection(item);
        }
    }
    SelectionHelpers.select = select;
    function isSelected(item, yes, no) {
        return maybe(item['selected'], yes, no);
    }
    SelectionHelpers.isSelected = isSelected;
    // these functions deal with using a separate selection array
    function clearGroup(group) {
        group.length = 0;
    }
    SelectionHelpers.clearGroup = clearGroup;
    function toggleSelectionFromGroup(group, item, search) {
        var searchMethod = search || item;
        if (group.any(searchMethod)) {
            group.remove(searchMethod);
        }
        else {
            group.add(item);
        }
    }
    SelectionHelpers.toggleSelectionFromGroup = toggleSelectionFromGroup;
    function stringOrBoolean(str, answer) {
        if (angular.isDefined(str)) {
            return str;
        }
        else {
            return answer;
        }
    }
    function nope(str) {
        return stringOrBoolean(str, false);
    }
    function yup(str) {
        return stringOrBoolean(str, true);
    }
    function maybe(answer, yes, no) {
        if (answer) {
            return yup(yes);
        }
        else {
            return nope(no);
        }
    }
    function isInGroup(group, item, yes, no, search) {
        if (!group) {
            return nope(no);
        }
        var searchMethod = search || item;
        return maybe(group.any(searchMethod), yes, no);
    }
    SelectionHelpers.isInGroup = isInGroup;
    function filterByGroup(group, item, yes, no, search) {
        if (group.length === 0) {
            return yup(yes);
        }
        var searchMethod = search || item;
        if (angular.isArray(item)) {
            return maybe(group.intersect(item).length === group.length, yes, no);
        }
        else {
            return maybe(group.any(searchMethod), yes, no);
        }
    }
    SelectionHelpers.filterByGroup = filterByGroup;
    function syncGroupSelection(group, collection, attribute) {
        var newGroup = [];
        if (attribute) {
            group.forEach(function (groupItem) {
                var first = collection.find(function (collectionItem) {
                    return groupItem[attribute] === collectionItem[attribute];
                });
                if (first) {
                    newGroup.push(first);
                }
            });
        }
        else {
            group.forEach(function (groupItem) {
                var first = collection.find(function (collectionItem) {
                    return _.isEqual(groupItem, collectionItem);
                });
                if (first) {
                    newGroup.push(first);
                }
            });
        }
        clearGroup(group);
        group.add(newGroup);
    }
    SelectionHelpers.syncGroupSelection = syncGroupSelection;
    function decorate($scope) {
        $scope.selectNone = selectNone;
        $scope.selectAll = selectAll;
        $scope.toggleSelection = toggleSelection;
        $scope.selectOne = selectOne;
        $scope.select = select;
        $scope.clearGroup = clearGroup;
        $scope.toggleSelectionFromGroup = toggleSelectionFromGroup;
        $scope.isInGroup = isInGroup;
        $scope.viewOnly = false; // true=disable checkmarks
        $scope.filterByGroup = filterByGroup;
    }
    SelectionHelpers.decorate = decorate;
})(SelectionHelpers || (SelectionHelpers = {}));

/// <reference path="coreHelpers.ts"/>
/// <reference path="controllerHelpers.ts"/>
var StorageHelpers;
(function (StorageHelpers) {
    function bindModelToLocalStorage(options) {
        var prefix = options.$scope.name + ':' || '::';
        var storageKey = prefix + options.modelName;
        var toParam = options.to || Core.doNothing;
        var fromParam = options.from || Core.doNothing;
        var toWrapper = function (value) {
            if (angular.isFunction(options.onChange)) {
                options.onChange(value);
            }
            var answer = toParam(value);
            options.localStorage[storageKey] = answer;
            return answer;
        };
        var fromWrapper = function (value) {
            if (value === undefined || value === null) {
                value = options.localStorage[storageKey];
            }
            return fromParam(value);
        };
        var storedValue = fromWrapper(undefined);
        ControllerHelpers.bindModelToSearchParam(options.$scope, options.$location, options.modelName, options.paramName, storedValue || options.initialValue, toWrapper, fromWrapper);
    }
    StorageHelpers.bindModelToLocalStorage = bindModelToLocalStorage;
})(StorageHelpers || (StorageHelpers = {}));

/// <reference path="includes.ts"/>
/**
 * @module UI
 */
var UI;
(function (UI) {
    UI.scrollBarWidth = null;
    function findParentWith($scope, attribute) {
        if (attribute in $scope) {
            return $scope;
        }
        if (!$scope.$parent) {
            return null;
        }
        // let's go up the scope tree
        return findParentWith($scope.$parent, attribute);
    }
    UI.findParentWith = findParentWith;
    function getIfSet(attribute, $attr, def) {
        if (attribute in $attr) {
            var wantedAnswer = $attr[attribute];
            if (wantedAnswer && !wantedAnswer.isBlank()) {
                return wantedAnswer;
            }
        }
        return def;
    }
    UI.getIfSet = getIfSet;
    /*
     * Helper function to ensure a directive attribute has some default value
     */
    function observe($scope, $attrs, key, defValue, callbackFunc) {
        if (callbackFunc === void 0) { callbackFunc = null; }
        $attrs.$observe(key, function (value) {
            if (!angular.isDefined(value)) {
                $scope[key] = defValue;
            }
            else {
                $scope[key] = value;
            }
            if (angular.isDefined(callbackFunc) && callbackFunc) {
                callbackFunc($scope[key]);
            }
        });
    }
    UI.observe = observe;
    function getScrollbarWidth() {
        if (!angular.isDefined(UI.scrollBarWidth)) {
            var div = document.createElement('div');
            div.innerHTML = '<div style="width:50px;height:50px;position:absolute;left:-50px;top:-50px;overflow:auto;"><div style="width:1px;height:100px;"></div></div>';
            div = div.firstChild;
            document.body.appendChild(div);
            UI.scrollBarWidth = div.offsetWidth - div.clientWidth;
            document.body.removeChild(div);
        }
        return UI.scrollBarWidth;
    }
    UI.getScrollbarWidth = getScrollbarWidth;
})(UI || (UI = {}));
