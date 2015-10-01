
# ES5 and DOM4 shim for all browsers with IE6 and IE7 support
based on:

- https://github.com/kriskowal/es5-shim
- https://github.com/paulmillr/es6-shim
- https://github.com/Raynos/DOM-shim

__Status__: Stable Beta
[__Demo__](http://h123.ru/ES5-DOM-SHIM/simple/index.html) Try it in IE7 (or in IE6 ¬_¬)! And take a look at the source

## Goal

 - Normalizing the JS and DOM across all browsers
 - Less code (eg less closures, reusable functions) & file size
 - IE6+ support
 - Include all we need from ES5, ES6 and DOM shim in one file
 - some extra stuff (if you don't need it, use `a.noextras.js` instead of `a.js`)

## Caution !!!

 - This lib is not about performance in IE < 9
 - The a.ielt8.htc requests should respond with the mime type "text/x-component"
 - That's all cautions
 
## Install
###`(Note: if you don't need EXSTRAs use a.noexstras.js instead of a.js)`
 - For modern browsers:
  Add main script in `head` section
  
            <script src="a.js"></script>
			
 - For IE8 support:
			
  1. First[!] add `a.ie8.js` in `head` section
  
            <!--[if IE 8]>
			<script src="a.ie8.js"></script>
			<![endif]-->
			
  2. Add `a.js` in `head` section
  
            <script src="a.js"></script>

 - For IE6 and IE7, and IE8 support:			
  1. Add `a.ie8.js` and `a.ielt8.js`, `a.js` in `head` section
  
			<!--[if lt IE 8]>
			<script src="a.ielt8.js"></script>
			<![endif]-->
            <!--[if IE 8]>
			<script src="a.ie8.js"></script>
			<![endif]-->
			<script src="a.js"></script>
			
  2. Put `a.ielt8.htc` to the root of your site

## EXSTRAs

IF INCLUDE_EXTRAS == false ->
 broken Object.defineProperty will be deleted

INCLUDE_EXTRAS:

 - Exporting these objects to global (window)
	1. browser
	2. Utils.Dom.DOMStringCollection
	3. XHR from https://github.com/Raynos/xhr with customisations
	4. (If where is no any other) $(selector, root) alias for root.querySelector(selector) (with ">[any selector]" support)
	5. $$(selector, root) alias for root.querySelectorAll(selector) (with ">[any selector]" support)
	6. $$0 alias for $
 - Extending objects
	1. Object.append(object, donor, [donor2, ...])
	2. Object.extend(object, donor, [donor2, ...]) (Object.append with overwrite exists properties)
	3. Object.inherit(Child, Parent)
	4. Array.prototype.unique()
	5. String.random(length)
 - Extra polyfills
	1. 'reversed' for <ol> with DOM API
	2. HTML*Element.labels
	3. HTMLLabelElement.control

## DEBUG

 - Fix console From https://github.com/theshock/console-cap/blob/master/console.js
 
## Same-domain limitation

IE requires that the .htc behavior file must be in the same domain as the HTML page which uses it. If you try to load the behavior from a different domain, you will get an "Access Denied" error.
Note that the domain must be exactly the same; that means that http://www.foo.com is a different domain than http://foo.com.
http://css3pie.com/documentation/known-issues/#x-domain

### Solve Same-domain limitation
Russian instruction in extra/SameDomainLimitation.SOLVE_RUS.odt

### Temporary testing
http://jsperf.com/es5-dom-shim-test

## Known issues:
0. Lack of test cases
1. Same-domain limitation (can be solve only on server)
2. Incompatibility with http://code.google.com/p/ie7-js/ [working on it]

## TODO
1. Tests
2. http://dvcs.w3.org/hg/url/raw-file/tip/Overview.html (https://gist.github.com/1384398 & https://gist.github.com/1235332)

## License

    MIT
	