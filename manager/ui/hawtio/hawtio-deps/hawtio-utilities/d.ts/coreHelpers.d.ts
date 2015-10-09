/// <reference path="baseHelpers.d.ts" />
/// <reference path="controllerHelpers.d.ts" />
/// <reference path="coreInterfaces.d.ts" />
/// <reference path="tasks.d.ts" />
declare module Core {
    var log: Logging.Logger;
    var lazyLoaders: {};
    var numberTypeNames: {
        'byte': boolean;
        'short': boolean;
        'int': boolean;
        'long': boolean;
        'float': boolean;
        'double': boolean;
        'java.lang.byte': boolean;
        'java.lang.short': boolean;
        'java.lang.integer': boolean;
        'java.lang.long': boolean;
        'java.lang.float': boolean;
        'java.lang.double': boolean;
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
    function lineCount(value: any): number;
    function safeNull(value: any): string;
    function safeNullAsString(value: any, type: string): string;
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
    function toSearchArgumentArray(value: any): string[];
    function folderMatchesPatterns(node: any, patterns: any): any;
    function scopeStoreJolokiaHandle($scope: any, jolokia: any, jolokiaHandle: any): void;
    function closeHandle($scope: any, jolokia: any): void;
    /**
     * Pass in null for the success function to switch to sync mode
     *
     * @method onSuccess
     * @static
     * @param {Function} Success callback function
     * @param {Object} Options object to pass on to Jolokia request
     * @return {Object} initialized options object
     */
    function onSuccess(fn: any, options?: {}): {};
    function supportsLocalStorage(): boolean;
    function isNumberTypeName(typeName: any): boolean;
    function encodeMBeanPath(mbean: any): any;
    function escapeMBeanPath(mbean: any): any;
    function encodeMBean(mbean: any): any;
    function escapeDots(text: string): string;
    /**
     * Escapes all dots and 'span' text in the css style names to avoid clashing with bootstrap stuff
     *
     * @method escapeTreeCssStyles
     * @static
     * @param {String} text
     * @return {String}
     */
    function escapeTreeCssStyles(text: string): string;
    function showLogPanel(): void;
    /**
     * Returns the CSS class for a log level based on if its info, warn, error etc.
     *
     * @method logLevelClass
     * @static
     * @param {String} level
     * @return {String}
     */
    function logLevelClass(level: string): string;
    function toPath(hashUrl: string): string;
    function parseMBean(mbean: any): any;
    function executePostLoginTasks(): void;
    function executePreLogoutTasks(onComplete: () => void): void;
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
    function logout(jolokiaUrl: any, userDetails: any, localStorage: Storage, $scope: any, successCB?: () => void, errorCB?: () => void): void;
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
    function createHref($location: any, href: any, removeParams?: any): any;
    /**
     * Turns the given search hash into a URI style query string
     * @method hashToString
     * @for Core
     * @static
     * @param {Object} hash
     * @return {String}
     */
    function hashToString(hash: any): string;
    /**
     * Parses the given string of x=y&bar=foo into a hash
     * @method stringToHash
     * @for Core
     * @static
     * @param {String} hashAsString
     * @return {Object}
     */
    function stringToHash(hashAsString: string): {};
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
    function registerForChanges(jolokia: any, $scope: any, arguments: any, callback: (response: any) => void, options?: any): () => void;
    interface IResponseHistory {
        [name: string]: any;
    }
    function getOrInitObjectFromLocalStorage(key: string): any;
    function getResponseHistory(): any;
    var MAX_RESPONSE_CACHE_SIZE: number;
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
    function register(jolokia: Jolokia.IJolokia, scope: any, arguments: any, callback: any): () => void;
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
    function unregister(jolokia: Jolokia.IJolokia, scope: any): void;
    /**
     * The default error handler which logs errors either using debug or log level logging based on the silent setting
     * @param response the response from a jolokia request
     */
    function defaultJolokiaErrorHandler(response: any, options?: {}): void;
    /**
     * Logs any failed operation and stack traces
     */
    function logJolokiaStackTrace(response: any): void;
    /**
     * Converts the given XML node to a string representation of the XML
     * @method xmlNodeToString
     * @for Core
     * @static
     * @param {Object} xmlNode
     * @return {Object}
     */
    function xmlNodeToString(xmlNode: any): any;
    /**
     * Returns true if the given DOM node is a text node
     * @method isTextNode
     * @for Core
     * @static
     * @param {Object} node
     * @return {Boolean}
     */
    function isTextNode(node: any): boolean;
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
    function fileExtension(name: string, defaultValue?: string): string;
    function getUUID(): string;
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
    function parseVersionNumbers(text: string): number[];
    /**
     * Converts a version string with numbers and dots of the form "123.456.790" into a string
     * which is sortable as a string, by left padding each string between the dots to at least 4 characters
     * so things just sort as a string.
     *
     * @param text
     * @return {string} the sortable version string
     */
    function versionToSortableString(version: string, maxDigitsBetweenDots?: number): string;
    function time(message: string, fn: any): any;
    /**
     * Compares the 2 version arrays and returns -1 if v1 is less than v2 or 0 if they are equal or 1 if v1 is greater than v2
     * @method compareVersionNumberArrays
     * @for Core
     * @static
     * @param {Array} v1 an array of version numbers with the most significant version first (major, minor, patch).
     * @param {Array} v2
     * @return {Number}
     */
    function compareVersionNumberArrays(v1: number[], v2: number[]): number;
    /**
     * Helper function which converts objects into tables of key/value properties and
     * lists into a <ul> for each value.
     * @method valueToHtml
     * @for Core
     * @static
     * @param {any} value
     * @return {String}
     */
    function valueToHtml(value: any): any;
    /**
     * If the string starts and ends with [] {} then try parse as JSON and return the parsed content or return null
     * if it does not appear to be JSON
     * @method tryParseJson
     * @for Core
     * @static
     * @param {String} text
     * @return {Object}
     */
    function tryParseJson(text: string): any;
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
    function maybePlural(count: Number, word: string): string;
    /**
     * given a JMX ObjectName of the form <code>domain:key=value,another=something</code> then return the object
     * <code>{key: "value", another: "something"}</code>
     * @method objectNameProperties
     * @for Core
     * @static
     * @param {String} name
     * @return {Object}
     */
    function objectNameProperties(objectName: string): {};
    /**
     * Removes dodgy characters from a value such as '/' or '.' so that it can be used as a DOM ID value
     * and used in jQuery / CSS selectors
     * @method toSafeDomID
     * @for Core
     * @static
     * @param {String} text
     * @return {String}
     */
    function toSafeDomID(text: string): string;
    /**
     * Invokes the given function on each leaf node in the array of folders
     * @method forEachLeafFolder
     * @for Core
     * @static
     * @param {Array[Folder]} folders
     * @param {Function} fn
     */
    function forEachLeafFolder(folders: any, fn: any): void;
    function extractHashURL(url: string): string;
    function authHeaderValue(userDetails: Core.UserDetails): string;
    function getBasicAuthHeader(username: string, password: string): string;
    /**
     * Breaks a URL up into a nice object
     * @method parseUrl
     * @for Core
     * @static
     * @param url
     * @returns object
     */
    function parseUrl(url: string): any;
    function getDocHeight(): number;
    /**
     * If a URL is external to the current web application, then
     * replace the URL with the proxy servlet URL
     * @method useProxyIfExternal
     * @for Core
     * @static
     * @param {String} connectUrl
     * @return {String}
     */
    function useProxyIfExternal(connectUrl: any): any;
    /**
     * Extracts the url of the target, eg usually http://localhost:port, but if we use fabric to proxy to another host,
     * then we return the url that we proxied too (eg the real target)
     *
     * @param {ng.ILocationService} $location
     * @param {String} scheme to force use a specific scheme, otherwise the scheme from location is used
     * @param {Number} port to force use a specific port number, otherwise the port from location is used
     */
    function extractTargetUrl($location: any, scheme: any, port: any): string;
    /**
     * Returns true if the $location is from the hawtio proxy
     */
    function isProxyUrl($location: ng.ILocationService): boolean;
    /**
     * handy do nothing converter for the below function
     **/
    function doNothing(value: any): any;
    var bindModelToSearchParam: typeof ControllerHelpers.bindModelToSearchParam;
    var reloadWhenParametersChange: typeof ControllerHelpers.reloadWhenParametersChange;
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
    function throttled(fn: any, millis: number): () => any;
    /**
     * Attempts to parse the given JSON text and returns the JSON object structure or null.
     *Bad JSON is logged at info level.
     *
     * @param text a JSON formatted string
     * @param message description of the thing being parsed logged if its invalid
     */
    function parseJsonText(text: string, message?: string): any;
    /**
     * Returns the humanized markup of the given value
     */
    function humanizeValueHtml(value: any): string;
    /**
     * Gets a query value from the given url
     *
     * @param url  url
     * @param parameterName the uri parameter value to get
     * @returns {*}
     */
    function getQueryParameterValue(url: any, parameterName: any): string;
    /**
     * Takes a value in ms and returns a human readable
     * duration
     * @param value
     */
    function humanizeMilliseconds(value: number): String;
    function getRegexs(): any;
    function removeRegex(name: any): void;
    function writeRegexs(regexs: any): void;
    function maskPassword(value: any): any;
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
    function matchFilterIgnoreCase(text: any, filter: any): any;
}
