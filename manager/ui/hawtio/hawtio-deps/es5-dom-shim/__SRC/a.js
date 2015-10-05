 /** @license MIT License (c) copyright Egor Halimonenko (termi1uc1@gmail.com | github.com/termi) */

// ==ClosureCompiler==
// @compilation_level ADVANCED_OPTIMIZATIONS
// @warning_level VERBOSE
// @jscomp_warning missingProperties
// @output_file_name a.js
// @check_types
// ==/ClosureCompiler==

/**
 * @version 7 pre-alpha
 * TODO::
 * 0. eng comments
 * 1. HTMLCanvasElement.toBlob (https://developer.mozilla.org/en/DOM/HTMLCanvasElement | http://stackoverflow.com/questions/4998908/convert-data-uri-to-file-then-append-to-formdata#answer-5100158)
 * 2. dateTime prop for IE < 8
 * 3. offset[Top/Left/Width/Height] for IE from https://raw.github.com/yui/yui3/master/src/dom/js/dom-style-ie.js
 * 4. MutationObserver http://hacks.mozilla.org/2012/05/dom-mutationobserver-reacting-to-dom-changes-without-killing-browser-performance/
 *                     http://updates.html5rocks.com/2012/02/Detect-DOM-changes-with-Mutation-Observers
 * 5. Web Animation API http://people.mozilla.org/~bbirtles/web-animations/web-animations.html#the-mediaitem-interface
 * 6. window.innerWidth for IE < 9 https://developer.mozilla.org/en/DOM/window.innerWidth
 * 7. http://dev.w3.org/csswg/selectors4/ querySelector[All] shim
 */








// [[[|||---=== GCC DEFINES START ===---|||]]]
/** @define {boolean} */
var __GCC__IS_DEBUG__ = false;
//IF __GCC__IS_DEBUG__ == true [
//0. Some errors in console
//1. Fix console From https://github.com/theshock/console-cap/blob/master/console.js
//]

/** @define {boolean} */
var __GCC__UNSTABLE_FUNCTIONS__ = false;
//IF __GCC__UNSTABLE_FUNCTIONS__ == true [
//]

/** @define {boolean} */
var __GCC__INCLUDE_EXTRAS__ = true;
//IF __GCC__INCLUDE_EXTRAS__ == true [
//Exporting these objects to global (window)
	/** 1. browser @define {boolean} */
	var __GCC__INCLUDE_EXTRAS__BROWSER__ = true;
	/** 2. Utils.Dom.DOMStringCollection @define {boolean} */
	var __GCC__INCLUDE_EXTRAS__DOMSTRINGCOLLECTION__ = true;
	/** 3. XHR from https://github.com/Raynos/xhr with customisations @define {boolean} */
 	var __GCC__INCLUDE_EXTRAS__XHR__ = true;
//Extending objects
	/** 1. Object.append(object, donor, [donor2, ...]) @define {boolean} */
	var __GCC__INCLUDE_EXTRAS__OBJECT_APPEND__ = true;
	/** 2. Object.extend(object, donor, [donor2, ...]) (Object.append with overwrite exists properties) @define {boolean} */
 	var __GCC__INCLUDE_EXTRAS__OBJECT_EXTEND__ = true;
	/** 3. Object.inherit(Child, Parent) @define {boolean} */
 	var __GCC__INCLUDE_EXTRAS__OBJECT_INHERIT__ = true;
	/** 4. Array.prototype.unique() @define {boolean} */
 	var __GCC__INCLUDE_EXTRAS__ARRAY_PROTOTYPE_UNIQUE__ = true;
	/** 5. String.random(length) @define {boolean} */
 	var __GCC__INCLUDE_EXTRAS__STRING_RANDOM__ = true;
//Extra polyfills
	/** 1. 'reversed' for <ol> with DOM API @define {boolean} */
	var __GCC__INCLUDE_EXTRAS__REVERSE_POLYFILL__ = true;
	/**
	 * 2. HTML*Element.labels
	 * 3. HTMLLabelElement.control
	 * @define {boolean}
	 */
 	var __GCC__INCLUDE_EXTRAS__LABELS_AND_CONTROL_POLYFILL__ = true;
//]

/** @define {boolean} */
var __GCC__ECMA_SCRIPT5__ = true;

/** @define {boolean} */
var __GCC__ECMA_SCRIPT6__ = true;

/** @define {boolean} */
var __GCC__SCRIPT_BUGFIXING__ = true;
//IF __GCC__SCRIPT_BUGFIXING__ == true [
	/** 1. Array.prototype.splice: IE < 9 bug: [1,2].splice(0).join("") == "" but should be "12" @define {boolean} */
	var __GCC__SCRIPT_BUGFIXING_ARRAY_PROTOTYPE_SPLICE__ = true;
	/** 2. String.prototype.trim @define {boolean} */
	var __GCC__SCRIPT_BUGFIXING_STRING_PROTOTYPE_TRIM__ = true;
	/** 3. String.prototype.split @define {boolean} */
	var __GCC__SCRIPT_BUGFIXING_STRING_PROTOTYPE_SPLIT__ = true;
	/** 4. Date: parse and toJSON fixes @define {boolean} */
	var __GCC__SCRIPT_BUGFIXING_DATE__ = true;
//]

/** @define {boolean} */
var __GCC__DOM_API_POLYFILL__ = true;
//IF __GCC__DOM_API_POLYFILL__ == true [
//TODO::
//]
// [[[|||---=== GCC DEFINES END ===---|||]]]









;(
/**
 * @type {Window}
 * @const */
function(global) {

"use strict";

/** @const @type {boolean} */
var DEBUG = __GCC__IS_DEBUG__;



var _browser_msie

	/** @const */
  , _Object_prototype = Object.prototype
  
	/** @const */
  , _Function_apply_ = Function.prototype.apply
	
	/** @const */
  , _Array_slice_ = Array.prototype.slice

	/** @const */
  , _String_split_ = String.prototype.split

	/** @const */
  , _Array_splice_ = Array.prototype.splice

  , _String_contains_

  , _Array_map_

  , _Array_from
  
	/** Use native "bind" or unsafe bind for service and performance needs
	 * @const
	 * @param {Object} object
	 * @param {...} var_args
	 * @return {Function} */
  , _unsafe_Function_bind_ = Function.prototype.bind || function(object, var_args) {
		var __method = this,
			args = _Array_slice_.call(arguments, 1);
		return function () {
			return _Function_apply_.call(__method, object, args.concat(_Array_slice_.call(arguments)));
		}
	}

	/** @const */
  , _hasOwnProperty = _unsafe_Function_bind_.call(Function.prototype.call, _Object_prototype.hasOwnProperty)

    /**
	 * Call _function
	 * @const
	 * @param {Function} _function function to call
	 * @param {*} context
	 * @param {...} var_args
	 * @return {*} mixed
	 * @version 2
	 */
  , _call_function = function(_function, context, var_args) {
		// If no callback function or if callback is not a callable function
		// it will throw TypeError
        return _Function_apply_.call(_function, context, _Array_slice_.call(arguments, 2))
	}

	/** @type{(Function|undefined)} */
  , _append = function(obj, ravArgs) {
		for(var i = 1; i < arguments.length; i++) {
			var extension = arguments[i];
			for(var key in extension)
				if(_hasOwnProperty(extension, key) &&
				   (!_hasOwnProperty(obj, key))
				  )obj[key] = extension[key];
		}

		return obj;
	}

	//Fixed `toObject` to work for strings in IE8 and Rhino. Added test spec for `forEach`.
	//https://github.com/kriskowal/es5-shim/pull/94
	/** @const */
  , NEED_PREPARE_STRING = (function(strObj) {
		// Check failure of by-index access of string characters (IE < 9)
		// and failure of `0 in strObj` (Rhino)
		return strObj[0] != "a" || !(0 in strObj);
	})(Object("a"))

	/**
	 * @const
	 * @param {Object} obj
	 * @param {boolean=} _allowNull
	 */
  , _toObject = function(obj, _allowNull) {
		if (obj == null && !_allowNull) // this matches both null and undefined
			throw new TypeError(); // TODO message

		// If the implementation doesn't support by-index access of
		// string characters (ex. IE < 9), split the string
		if (NEED_PREPARE_STRING && typeof obj == "string" && obj)
			return _String_split_.call(obj, "");

		return Object(obj);
	}

	/** @const */
  , _toString_ = _Object_prototype.toString

	/** @const */
  , _throwDOMException = function(errStr) {
		var ex = Object.create(DOMException.prototype);
		ex.code = DOMException[errStr];
		ex.message = errStr +': DOM Exception ' + ex.code;
		throw ex;
	}

	/** @type{(Function|undefined)} */
  , functionReturnFalse = function() { return false }

  , functionReturnFirstParam = function(param) { return param }

  , nodeList_methods_fromArray = ["every", "filter", "forEach", "indexOf", "join", "lastIndexOf", "map", "reduce", "reduceRight", "reverse", "slice", "some", "toString"]

	//Take Element.prototype or silently take a fake object
	// IE < 8 support in a.ielt8.js and a.ielt8.htc
  , _Element_prototype = global["Element"] && global["Element"].prototype || {}

	/** @const */
  , S_ELEMENT_CACHED_CLASSLIST_NAME = "_ccl_"

  , _document_createElement = _unsafe_Function_bind_.call(document["__orig__createElement__"] || document.createElement, document)

  , _testElement = _document_createElement('p')
  
  , dom4_mutationMacro

	/** @type {RegExp} @const */
  , RE_DOMSettableTokenList_lastSpaces = /\\s+$/g
	/** @type {RegExp} @const */
  , RE_DOMSettableTokenList_spaces = /\\s+/g

    /** @type {number} some unique identifire. must inc after use */
  , UUID = 1

  	/** @const @type{string} UUID property name */
  , UUID_PROP_NAME = "__UUID__"

  , _tmp_

  // ------------------------------ ==================  querySelector[All], match, find[All]  ================== ------------------------------
	/** @type {RegExp} @const */
  , RE__selector__easySelector = /^(\w+)?((?:\.(?:[\w\-]+))+)?$|^#([\w\-]+$)/
	/** @type {RegExp} @const */
  , RE__matchSelector__doubleSpaces = /\s*([,>+~ ])\s*/g//Note: Use with "$1"
	/** @type {RegExp} */
  , RE_document_find_scopedreplacer

  // ------------------------------ ==================  Events  ================== ------------------------------

  , _Event

  , _CustomEvent

  , _Event_prototype

  , _Custom_Event_prototype

  , implementation_stopImmediatePropagation

	// ------------------------------ ==================  Utils.Dom  ================== ------------------------------
  , DOMStringCollection

  , DOMStringCollection_init

  , DOMStringCollection_init_add

  , DOMStringCollection_getNodeClassName

  , DOMStringCollection_setNodeClassName

	// ------------------------------ ==================  es5-shim  ================== ------------------------------
  , _forEach

  , _shimed_Array_every

  , _String_trim_

  , _String_trim_whitespace = "\x09\x0A\x0B\x0C\x0D\x20\xA0\u1680\u180E\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200A\u202F\u205F\u3000\u2028\u2029\uFEFF"

  , _String_trim_beginRegexp

  , _String_trim_endRegexp

  , doesDefinePropertyWork

  , doesGetOwnPropertyDescriptorWork

  , definePropertyWorksOnObject

  , definePropertyWorksOnDom

  , definePropertyFallback

  , definePropertiesFallback
  
  , ERR_NON_OBJECT_DESCRIPTOR
  
  , ERR_NON_OBJECT_TARGET
  
  , ERR_ACCESSORS_NOT_SUPPORTED
  
  , getOwnPropertyDescriptorWorksOnObject
  
  , getOwnPropertyDescriptorWorksOnDom
  
  , getOwnPropertyDescriptorFallback

	// ------------------------------ ==================  Date  ================== ------------------------------
	/** @const */
  , _Native_Date = global["Date"]

  , _Shimed_Date

  , _Shimed_Date_isoDateExpression

  , _Shimed_Date_monthes

  , _Shimed_Date_leapYears

  , _Shimed_Date_test_negDate = -62198755200000

  , _Shimed_Date_test_yearStr = '-000001'

	// ------------------------------ ==================  __GCC__INCLUDE_EXTRAS__  ================== ------------------------------
  , browser

  , _nodesRecursivelyWalk

  	/** @type {string} Space separator list of labelable element names */
  , _labelable_elements

  , OL_reversed_Shim

  , OL_reversed_autoInitFunction

  , XHR
;


//Browser sniffing :) START
if(__GCC__INCLUDE_EXTRAS__ && __GCC__INCLUDE_EXTRAS__BROWSER__) {
	browser = {};
	/** @type {Array}
	 * @const */
	browser["names"] = (browser["agent"] = navigator.userAgent.toLowerCase()).match(/(mozilla|compatible|chrome|webkit|safari|opera|msie|iphone|ipod|ipad)/gi);
	
	_tmp_ = browser["names"] && browser["names"].length || 0;
	while(_tmp_-- > 0)browser[browser["names"][_tmp_]] = true;

	browser["mozilla"] = browser["mozilla"] && !browser["compatible"] && !browser["webkit"];
	browser["safari"] = browser["safari"] && !browser["chrome"];
	browser["msie"] = browser["msie"] && !browser["opera"];

	_browser_msie = browser["msie"] || void 0;
	
	global["browser"] = browser;//Export
}//if(__GCC__INCLUDE_EXTRAS__)
else {
	_browser_msie = (_browser_msie = /msie (\d+)/i.exec(navigator.userAgent)) && +_browser_msie[1] || void 0;
}
//Browser sniffing :) END




if(!global["HTMLDocument"])global["HTMLDocument"] = global["Document"];//For IE9
if(!global["Document"])global["Document"] = global["HTMLDocument"];//For IE8
//TODO:: for IE < 8 :: if(!global["Document"] && !global["HTMLDocument"])global["Document"] = global["HTMLDocument"] = ??;//for IE < 8





/**
 * Merge the contents of two or more objects together into the first object.
 * This function does not overwrite existing properties
 * @param {Object} obj Object to extend
 * @param {...} ravArgs extentions
 * @return {Object} the same object as `obj`
 */
if(__GCC__INCLUDE_EXTRAS__) {
/*  =======================================================================================  */
/*  ======================================  Classes  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<  */

if(__GCC__INCLUDE_EXTRAS__OBJECT_APPEND__) {
/**
 * Merge the contents of two or more objects together into the first object.
 * This function does not overwrite existing properties
 * @param {Object} obj Object to extend
 * @param {...} ravArgs extentions
 * @return {Object} the same object as `obj`
 */
Object["append"] = _append;
}

if(__GCC__INCLUDE_EXTRAS__OBJECT_EXTEND__) {
/**
 * Merge the contents of two or more objects together into the first object.
 * This function overwrite existing properties
 * @param {Object} obj Object to extend
 * @param {...} ravArgs extentions
 * @return {Object} the same object as `obj`
 */
if(!Object["extend"])Object["extend"] = function(obj, ravArgs) {
	for(var i = 1; i < arguments.length; i++) {
		var extension = arguments[i];
		for(var key in extension)
			if(_hasOwnProperty(extension, key))obj[key] = extension[key];
	}

	return obj;
};
}

if(__GCC__INCLUDE_EXTRAS__OBJECT_INHERIT__) {
/**
 * Inherit one Child 'class' (function) from Parent 'class' (function). Note: you need to apply Parent constructor in Child constructor manualy (<class>.superclass.constructor.apply(this, <arguments>))
 * @param {Function} Child
 * @param {Function} Parent
 *
 * Example:
 *  function A() { this.message = "World!"; this.subject = "Hello" };A.prototype.say = function() { alert(this.subject + " " + this.message) }
 *  function B() { B.superclass.constructor.call(this); this.message = "Classical inheritance!" }
 *  Object["inherit"](B, A);
 *  test = new B;
 *  test.say();
 */
Object["inherit"] = function(Child, Parent) {
	(Child.prototype = Object.create(Child["superclass"] = Parent.prototype)).constructor = Child;
};
}


/*  >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  Classes  ======================================  */
/*  =======================================================================================  */
}//if(__GCC__INCLUDE_EXTRAS__)









//  =======================================================================================
//  =======================================================================================
//  =======================================================================================
//  =======================================================================================
//  =======================================================================================
//  =======================================================================================
// --------------- ================ es5 shim ================ ---------------
//  =======================================================================================
//  =======================================================================================
//  =======================================================================================
//  =======================================================================================
//  =======================================================================================
//  =======================================================================================
// Based on https://github.com/kriskowal/es5-shim/blob/master/es5-shim.js



/*  ======================================================================================  */
/*  ==================================  Function prototype  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<  */

if(__GCC__ECMA_SCRIPT5__) {
/**
 * Wraps the function in another, locking its execution scope to an object specified by thisObj.
 * @param {Object} object
 * @param {...} var_args
 * @return {Function}
 * @version 3
 */
if(!Function.prototype.bind)Function.prototype.bind = function (object, var_args) {
	//If IsCallable(Target) is false, throw a TypeError exception.
	if (typeof this != "function") {
		if(!_browser_msie || _String_trim_.call(this + "").indexOf("function") !== 0) {
			throw new TypeError("Function.prototype.bind called on incompatible " + this);
		}
	}
	var __method = this, args = _Array_slice_.call(arguments, 1),
		_result = function () {
			return _Function_apply_.call(
				__method,
				this instanceof _result ?
					this ://The `object` value is ignored if the bound function is constructed using the new operator.
					object,
				args.concat(_Array_slice_.call(arguments))
			);
		};
	if(__method.prototype) {
		_result.prototype = Object.create(__method.prototype);
		//_result.constructor = __method;
	}
	return _result;
};
}//if __GCC__ECMA_SCRIPT5__
/*  >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  Function prototype  ==================================  */
/*  =======================================================================================  */


/*  =======================================================================================  */
/*  =================================  Object prototype  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<  */

if(__GCC__ECMA_SCRIPT5__) {
_append(Object, {
	/**
	 * ES5 15.2.3.14
	 * http://es5.github.com/#x15.2.3.14
	 * https://developer.mozilla.org/en/JavaScript/Reference/global_Objects/Object/keys
	 * Returns an array of all own enumerable properties found upon a given object, in the same order as that provided by a for-in loop (the difference being that a for-in loop enumerates properties in the prototype chain as well).
	 *
	 * Implementation from http://whattheheadsaid.com/2010/10/a-safer-object-keys-compatibility-implementation
	 *
	 * @param obj The object whose enumerable own properties are to be returned.
	 * @return {Array} object keys
	 */
	keys : (function() {
	    var DontEnums = [
	            'toString',
	            'toLocaleString',
	            'valueOf',
	            'hasOwnProperty',
	            'isPrototypeOf',
	            'propertyIsEnumerable',
	            'constructor'
	        ],
	        hasDontEnumBug = !{"toString":null}.propertyIsEnumerable(DontEnums[0]),
	        DontEnumsLength = DontEnums.length;

	    return function (obj) {
	        if (typeof obj != "object" && typeof obj != "function" || obj === null)
	            throw new TypeError("Object.keys called on a non-object");

	        var result = [];
	        for (var name in obj) {
	            if(_hasOwnProperty(obj, name))
	                result.push(name);
	        }

	        if (hasDontEnumBug) {
	            for (var i = 0; i < DontEnumsLength; i++) {
	                if(_hasOwnProperty(obj, DontEnums[i]))
	                    result.push(DontEnums[i]);
	            }
	        }

	        return result;
	    };
	})()

	/**
	 * ES5 15.2.3.4
	 * http://es5.github.com/#x15.2.3.4
	 * Returns an array of all properties (enumerable or not) found upon a given object.
	 * @param obj The object whose enumerable own properties are to be returned.
	 * @return {Array} object keys
	 */
  , getOwnPropertyNames : function(obj) {
		return Object.keys(obj);
	}

	/**
	 * ES5 15.2.3.8
	 * http://es5.github.com/#x15.2.3.8
	 * this is misleading and breaks feature-detection, but
	 * allows "securable" code to "gracefully" degrade to working
	 * but insecure code.
	 * @param {!Object} object
	 * @return {Object} the same object
	 */
  , seal : functionReturnFirstParam

	/**
	 * ES5 15.2.3.9
	 * http://es5.github.com/#x15.2.3.9
	 * this is misleading and breaks feature-detection, but
	 * allows "securable" code to "gracefully" degrade to working
	 * but insecure code.
	 * @param {!Object} object
	 * @return {Object} the same object
	 */
  , freeze : functionReturnFirstParam

	/** ES5 15.2.3.10
	 * http://es5.github.com/#x15.2.3.10
	 * this is misleading and breaks feature-detection, but
	 * allows "securable" code to "gracefully" degrade to working
	 * but insecure code.
	 * @param {!Object} object
	 * @return {Object} the same object
	 */
  , preventExtensions : functionReturnFirstParam

	/** ES5 15.2.3.11
	 * http://es5.github.com/#x15.2.3.11
	 * @param {!Object} object
	 * @param {boolean} is sealed?
	 */
  , isSealed : functionReturnFalse

	/** ES5 15.2.3.12
	 * http://es5.github.com/#x15.2.3.12
	 * @param {!Object} object
	 * @param {boolean} is frozen?
	 */
  , isFrozen : functionReturnFalse

	/** ES5 15.2.3.13
	 * http://es5.github.com/#x15.2.3.13
	 * @param {!Object} object
	 * @param {boolean} is extensible?
	 */
  , isExtensible : function(object) {
        // 1. If Type(O) is not Object throw a TypeError exception.
        if (Object(object) !== object) {
            throw new TypeError(); // TODO message
        }
        // 2. Return the Boolean value of the [[Extensible]] internal property of O.
        var name = '';
        while (_hasOwnProperty(object, name)) {
            name += '?';
        }
        object[name] = true;
        var returnValue = _hasOwnProperty(object, name);
        delete object[name];
        return returnValue;
    }

	/**
	 * ES5 15.2.3.2
	 * http://es5.github.com/#x15.2.3.2
	 * https://github.com/kriskowal/es5-shim/issues#issue/2
	 * http://ejohn.org/blog/objectgetprototypeof/
	 * recommended by fschaefer on github
	 * @param {!Object} object
	 * @return {Object} prototype of given object
	 */
  , getPrototypeOf : function getPrototypeOf(object) {
		return object.__proto__ || (
			object.constructor ?
			object.constructor.prototype :
			_Object_prototype
		);
	}

	/**
	 * https://developer.mozilla.org/en/JavaScript/Reference/global_Objects/Object/create
	 * JavaScript 1.8.5
	 * ES5 15.2.3.5
	 * http://es5.github.com/#x15.2.3.5
	 * Creates a new object with the specified prototype object and properties.
	 * @param {Object} _prototype The object which should be the prototype of the newly-created object.
	 * @param {Object=} properties If specified and not undefined, an object whose enumerable own properties (that is, those properties defined upon itself and not enumerable properties along its prototype chain) specify property descriptors to be added to the newly-created object, with the corresponding property names.
	 * @return {!Object}
	 */
  , create : function create(_prototype, properties) {
		var _object;
		if (_prototype === null) {
			_object = { "__proto__": null };
		} else {
			if (typeof _prototype != "object")
				throw new TypeError("typeof prototype["+(typeof _prototype)+"] != 'object'");

			var /** @constructor */Type = function () {};
			Type.prototype = _prototype;
			_object = new Type();
			// IE has no built-in implementation of `Object.getPrototypeOf`
			// neither `__proto__`, but this manually setting `__proto__` will
			// guarantee that `Object.getPrototypeOf` will work as expected with
			// objects created using `Object.create`
			_object.__proto__ = _prototype;
		}
		if(properties)
			Object.defineProperties(_object, properties);
		return _object;
	}

	//from https://github.com/paulmillr/es6-shim/blob/master/es6-shim.js
  , "is" : function(x, y) {
	  if (x === y) {
	    // 0 === -0, but they are not identical
	    if (x === 0) {
	      return 1 / x === 1 / y;
	    } else {
	      return true;
	    }
	  }

	  // NaN !== NaN, but they are identical.
	  // NaNs are the only non-reflexive value, i.e., if x !== x,
	  // then x is a NaN.
	  // isNaN is broken: it converts its argument to number, so
	  // isNaN("foo") => true
	  return x !== x && y !== y;
	}

  , "isnt" : function(x, y) {
	  return !Object["is"](x, y);
	}
});
}//if __GCC__ECMA_SCRIPT5__

if(__GCC__ECMA_SCRIPT5__) {
// ES5 15.2.3.6
// http://es5.github.com/#x15.2.3.6

// Patch for WebKit and IE8 standard mode
// Designed by hax <hax.github.com>
// related issue: https://github.com/kriskowal/es5-shim/issues#issue/5
// IE8 Reference:
//     http://msdn.microsoft.com/en-us/library/dd282900.aspx
//     http://msdn.microsoft.com/en-us/library/dd229916.aspx
// WebKit Bugs:
//     https://bugs.webkit.org/show_bug.cgi?id=36423
doesDefinePropertyWork = function(object) {
    try {
        Object.defineProperty(object, "sentinel", {});
        return "sentinel" in object;
    } catch (exception) {
		return void 0;
    }
}

// check whether defineProperty works if it's given. Otherwise,
// shim partially.
if (Object.defineProperty) {
    definePropertyWorksOnObject = doesDefinePropertyWork({});
	definePropertyWorksOnDom = doesDefinePropertyWork(_testElement);

    if (!definePropertyWorksOnObject || !definePropertyWorksOnDom) {
        definePropertyFallback = Object.defineProperty;
		definePropertiesFallback = Object.defineProperties;
    }
}

if (!Object.defineProperty || definePropertyFallback) {
	ERR_NON_OBJECT_DESCRIPTOR = "Property description must be an object: ";
    ERR_NON_OBJECT_TARGET = "Object.defineProperty called on non-object: ";
    ERR_ACCESSORS_NOT_SUPPORTED = "getters & setters not supported";

	/**
	 * Defines a new property directly on an object, or modifies an existing property on an object, and returns the object.
	 * @param {Object} object The object on which to define the property.
	 * @param {string} property The name of the property to be defined or modified.
	 * @param {Object} descriptor The descriptor for the property being defined or modified.
	 */
    Object.defineProperty = function defineProperty(object, property, descriptor) {
        if ((typeof object != "object" && typeof object != "function") || object === null)
            throw new TypeError(ERR_NON_OBJECT_TARGET + object);
        if ((typeof descriptor != "object" && typeof descriptor != "function") || descriptor === null)
            throw new TypeError(ERR_NON_OBJECT_DESCRIPTOR + descriptor);

        // make a valiant attempt to use the real defineProperty
        // for I8's DOM elements.
        if (definePropertyFallback) {
            try {
                return definePropertyFallback.call(Object, object, property, descriptor);
            } catch (exception) {
				if (exception["number"] === -0x7FF5EC54) {//[ielt9 ie8] IE 8 doesn't support enumerable:true
					descriptor.enumerable = false;
					try {
						return definePropertyFallback.call(Object, object, property, descriptor);
					} catch (exception2) {

					}
				}
                // try the shim if the real one doesn't work
            }
        }

        // If it's a data property.
        if(descriptor["value"] !== void 0) {
            // fail silently if "writable", "enumerable", or "configurable"
            // are requested but not supported
            /*
            // alternate approach:
            if ( // can't implement these features; allow false but not true
                !(_hasOwnProperty(descriptor, "writable") ? descriptor.writable : true) ||
                !(_hasOwnProperty(descriptor, "enumerable") ? descriptor.enumerable : true) ||
                !(_hasOwnProperty(descriptor, "configurable") ? descriptor.configurable : true)
            )
                throw new RangeError(
                    "This implementation of Object.defineProperty does not " +
                    "support configurable, enumerable, or writable."
                );
            */

            if (object.__defineGetter__ &&
				(object.__lookupGetter__(property) || object.__lookupSetter__(property))) {
                // As accessors are supported only on engines implementing
                // `__proto__` we can safely override `__proto__` while defining
                // a property to make sure that we don't hit an inherited
                // accessor.
                var _prototype = object.__proto__;
                object.__proto__ = _Object_prototype;
                // Deleting a property anyway since getter / setter may be
                // defined on object itself.
                delete object[property];
                object[property] = descriptor["value"];
                // Setting original `__proto__` back now.
                object.__proto__ = _prototype;
            } else {
                object[property] = descriptor["value"];
            }
        } else {
            if (!object.__defineGetter__) {
                if(Object.defineProperty["ielt8"]) {//[ielt9 ie8]
					if(descriptor["get"] !== void 0)
						object["get" + property] = descriptor["get"];
					if(descriptor["set"] !== void 0)
						object["set" + property] = descriptor["set"];
				}
				else throw new TypeError(ERR_ACCESSORS_NOT_SUPPORTED);
			}
			else {
				// If we got that far then getters and setters can be defined !!
				if(descriptor["get"] !== void 0)
					object.__defineGetter__(property, descriptor["get"]);
				if(descriptor["set"] !== void 0)
					object.__defineSetter__(property, descriptor["set"]);
			}
        }

        return object;
    };
}

//[ielt8] Set `Object.defineProperty["ielt8"] = true` for IE < 8
if(_Element_prototype["ie"] && _browser_msie < 8)_Element_prototype["ielt8"] = Object.defineProperty["ielt8"] = true;

// ES5 15.2.3.7
// http://es5.github.com/#x15.2.3.7
if (!Object.defineProperties || definePropertiesFallback) {
	/**
	 * Defines new or modifies existing properties directly on an object, returning the object.
	 * @param {Object} object The object on which to define or modify properties.
	 * @param {Object} properties An object whose own enumerable properties constitute descriptors for the properties to be defined or modified.
	 */
	Object.defineProperties = function defineProperties(object, properties) {
		// make a valiant attempt to use the real defineProperty
		// for I8's DOM elements.
		if (definePropertiesFallback) {
			try {
				return definePropertiesFallback.call(Object, object, properties);
			} catch (exception) {
				// try the shim if the real one doesn't work
			}
		}
        for (var property in properties) {
            if(_hasOwnProperty(properties, property))
                Object.defineProperty(object, property, properties[property]);
        }
        return object;
    };
}


// ES5 15.2.3.3
// http://es5.github.com/#x15.2.3.3
doesGetOwnPropertyDescriptorWork = function(object) {
    try {
        object["sentinel2"] = 0;
        return Object.getOwnPropertyDescriptor(
            object,
            "sentinel2"
        ).value === 0;
    } catch (exception) {
        return void 0;
    }
}

// check whether getOwnPropertyDescriptor works if it's given. Otherwise,
// shim partially.
if (Object.getOwnPropertyDescriptor) {
    getOwnPropertyDescriptorWorksOnObject = doesGetOwnPropertyDescriptorWork({});
    getOwnPropertyDescriptorWorksOnDom = doesGetOwnPropertyDescriptorWork(_testElement);
    if (!getOwnPropertyDescriptorWorksOnDom ||
        !getOwnPropertyDescriptorWorksOnObject
    ) {
        getOwnPropertyDescriptorFallback = Object.getOwnPropertyDescriptor;
    }
}

if (!Object.getOwnPropertyDescriptor || getOwnPropertyDescriptorFallback) {
    var ERR_NON_OBJECT = "Object.getOwnPropertyDescriptor called on a non-object: ";

	/**
	 * Returns a property descriptor for an own property (that is, one directly present on an object, not present by dint of being along an object's prototype chain) of a given object.
	 * @param {!Object} object The object in which to look for the property.
	 * @param {!string} property The name of the property whose description is to be retrieved
	 * @return {Object.<(ObjectPropertyDescriptor|null)>|undefined}
	 */
    Object.getOwnPropertyDescriptor = function _getOwnPropertyDescriptor(object, property) {
        if ((typeof object != "object" && typeof object != "function") || object === null) {
            throw new TypeError(ERR_NON_OBJECT + object);
        }

        // make a valiant attempt to use the real _getOwnPropertyDescriptor
        // for I8's DOM elements.
        if (getOwnPropertyDescriptorFallback) {
            try {
                return getOwnPropertyDescriptorFallback.call(Object, object, property);
            } catch (exception) {
                // try the shim if the real one doesn't work
            }
        }

        // If object does not owns property return undefined immediately.
        if (!_hasOwnProperty(object, property)) {
            return void 0;
        }

        // If object has a property then it's for sure both `enumerable` and
        // `configurable`.
        var descriptor =  { enumerable: true, configurable: true },
			getter,
			setter;

        // If JS engine supports accessor properties then property may be a
        // getter or setter.
        if (object.__defineGetter__) {
            // Unfortunately `__lookupGetter__` will return a getter even
            // if object has own non getter property along with a same named
            // inherited getter. To avoid misbehavior we temporary remove
            // `__proto__` so that `__lookupGetter__` will return getter only
            // if it's owned by an object.
            var _prototype = object.__proto__;
            object.__proto__ = _Object_prototype;

            getter = object.__lookupGetter__(property);
            setter = object.__lookupSetter__(property);

            // Once we have getter and setter we can put values back.
            object.__proto__ = _prototype;
        }
		else if(Object.defineProperty["ielt8"]) {//[ielt9 ie8]
			getter = object["get" + property];
			setter = object["set" + property];
		}

		if (getter || setter) {
			if (getter) {
				descriptor.get = getter;
			}
			if (setter) {
				descriptor.set = setter;
			}
			// If it was accessor property we're done and return here
			// in order to avoid adding `value` to the descriptor.
			return descriptor;
		}

        // If we got this far we know that object has an own property that is
        // not an accessor so we set it as a value and return descriptor.
        descriptor.value = object[property];
        return descriptor;
    };
}

}//if __GCC__ECMA_SCRIPT5__

//TODO::
// 1. getOwnPropertyDescriptors
// 2. getPropertyDescriptor
// 3. getPropertyNames

/*  >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  Object prototype  ==================================  */
/*  =======================================================================================  */

/*  ======================================================================================  */
/*  ==================================  Array.prototype  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<  */

/*  ================================ bug fixing  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<  */

if(__GCC__SCRIPT_BUGFIXING__ && __GCC__SCRIPT_BUGFIXING_ARRAY_PROTOTYPE_SPLICE__) {
// ES5 15.4.4.12
// http://es5.github.com/#x15.4.4.12
// Default value for second param
// [bugfix, ielt9, old browsers] 
// IE < 9 bug: [1,2].splice(0).join("") == "" but should be "12"
if([1,2].splice(0).length != 2) {
	Array.prototype.splice = function(start, deleteCount) {
        if(start === void 0 && deleteCount === void 0)return [];

		return _Array_splice_.apply(this, [
					start === void 0 ? 0 : start,
					deleteCount === void 0 ? (this.length - start) : deleteCount
				].concat(_Array_slice_.call(arguments, 2))
			);
	};
}
}//if __GCC__SCRIPT_BUGFIXING_ARRAY_PROTOTYPE_SPLICE__

/*  >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  bug fixing  ==================================  */

/*  ================================ ES6 ==================================  */
// Based on https://github.com/paulmillr/es6-shim/
_Array_from = function(iterable) {
	if(iterable instanceof Array || Array.isArray(iterable))return iterable;
	if(iterable.toArray)return iterable.toArray();

	var object = _toObject(iterable, true),
		length = object.length >>> 0,
		result;

	try {
		result = _Array_slice_.call(object);
	}
	catch(e) { }

	if(result && result.length === length)return result;

	result = [];

	for(var key = 0 ; key < length ; key++) {
		if(key in object)
			result[key] = object[key];
	}

	return result;
}

if (__GCC__ECMA_SCRIPT6__) {
_append(Array, {
	/**
	 * http://es5.github.com/#x15.4.3.2
	 * https://developer.mozilla.org/en/JavaScript/Reference/Global_Objects/Array/isArray
	 * https://gist.github.com/1034882
	 * Returns true if an object is an array, false if it is not.
	 * @param {*} obj The object to be checked
	 * @return {boolean}
	 */
	isArray : function(obj) {
		return _toString_.call(obj) == '[object Array]'// test with Object.prototype.toString
	}

	/** toArray function
	 * @param {Object|Array} iterable object
	 * @return {Array}
	 */
  , "from" : _Array_from

	/** return array of arguments of this function
	 * @param {...} args
	 * @return {Array}
	 */
  , "of" : function(args) {
		return _Array_slice_.call(arguments);
	}
});
}//if __GCC__SCRIPT_BUGFIXING_ARRAY_PROTOTYPE_SPLICE__

/*  ================================ ES5 ==================================  */
// Based on https://github.com/kriskowal/es5-shim

if (__GCC__ECMA_SCRIPT5__) {
_append(Array.prototype, {
	/**
	 * https://developer.mozilla.org/en/JavaScript/Reference/Global_Objects/Array/Reduce
	 *
	 * Apply a function against an accumulator and each value of the array (from left-to-right) as to reduce it to a single value.
	 * @param {Function} accumulator Function to execute on each value in the array, taking four arguments:
	 *	previousValue The value previously returned in the last invocation of the callback, or initialValue, if supplied. (See below.)
	 *	currentValue The current element being processed in the array.
	 *	index The index of the current element being processed in the array.
	 *	array The array reduce was called upon.
	 * @param {*=} initialValue Object to use as the first argument to the first call of the callback.
	 * @return {*} single value
	 */
	reduce : function(accumulator, initialValue) {
		// ES5 : "If IsCallable(callbackfn) is false, throw a TypeError exception." in "_call_function" function

		var thisArray = _toObject(this),
			l = thisArray.length >>> 0,
			i = 0;

		if(l === 0 && arguments.length <= 1)// == on purpose to test 0 and false.// no value to return if no initial value, empty array
			throw new TypeError("Array length is 0 and no second argument");

		if(initialValue === void 0)initialValue = (++i, thisArray[0]);

		for( ; i < l ; ++i) {
		  if(i in thisArray)
		    initialValue = _call_function(accumulator, void 0, initialValue, thisArray[i], i, thisArray);
		}

		return initialValue;
	}

	/**
	 * https://developer.mozilla.org/en/JavaScript/Reference/Global_Objects/Array/reduceRight
	 *
	Apply a function simultaneously against two values of the array (from right-to-left) as to reduce it to a single value.
	reduceRight executes the callback function once for each element present in the array, excluding holes in the array, receiving four arguments: the initial value (or value from the previous callback call), the value of the current element, the current index, and the array over which iteration is occurring.

	The call to the reduceRight callback would look something like this:
	array.reduceRight(function(previousValue, currentValue, index, array) {
	    // ...
	});

	The first time the function is called, the previousValue and currentValue can be one of two values. If an initialValue was provided in the call to reduceRight, then previousValue will be equal to initialValue and currentValue will be equal to the last value in the array. If no initialValue was provided, then previousValue will be equal to the last value in the array and currentValue will be equal to the second-to-last value.

	 * @param {Function} accumulator Function to execute on each value in the array.
	 * @param {*=} initialValue Object to use as the first argument to the first call of the callback.
	 */
  , reduceRight : function(accumulator, initialValue) {
		// ES5 : "If IsCallable(callbackfn) is false, throw a TypeError exception." in "_call_function" function

		var thisArray = _toObject(this),
			l = thisArray.length >>> 0;

		if(l === 0 && arguments.length <= 1)// == on purpose to test 0 and false.// no value to return if no initial value, empty array
			throw new TypeError("Array length is 0 and no second argument");

		--l;
		if(initialValue === void 0)initialValue = (--l, thisArray[l + 1]);

		for( ; l >= 0 ; --l) {
		  if(l in thisArray)
		    initialValue = _call_function(accumulator, void 0, initialValue, thisArray[l], l, thisArray);
		}

		return initialValue;
	}


	/** ES5 15.4.4.18
	 * http://es5.github.com/#x15.4.4.18
	 * https://developer.mozilla.org/en/JavaScript/Reference/Global_Objects/array/forEach
	 * Executes a provided function once per array element.
	 * @param {Function} iterator Function to execute for each element.
	 * @param {Object} context Object to use as this when executing callback.
	 */
  , forEach : function(iterator, context) {
		var thisArray = _toObject(this),
			length = thisArray.length >>> 0,
			i = -1;
		
		while (++i < length) {
			if (i in thisArray) {
				_call_function(iterator, context, thisArray[i], i, thisArray);
			}
		}			
	}

	/** ES5 15.4.4.14
	 * http://es5.github.com/#x15.4.4.14
	 * https://developer.mozilla.org/en/JavaScript/Reference/Global_Objects/Array/indexOf
	 * 
	 * https://gist.github.com/1034425
	 * 
	 * Returns the first index at which a given element can be found in the array, or -1 if it is not present.
	 * @param {*} searchElement Element to locate in the array.
	 * @param {number} fromIndex The index at which to begin the search. Defaults to 0, i.e. the whole array will be searched. If the index is greater than or equal to the length of the array, -1 is returned, i.e. the array will not be searched. If negative, it is taken as the offset from the end of the array. Note that even when the index is negative, the array is still searched from front to back. If the calculated index is less than 0, the whole array will be searched.
	 * @return {number}
	 */
  , indexOf : function(searchElement, fromIndex) {
		var thisArray = _toObject(this),
			length = thisArray.length >>> 0;

		if((fromIndex = ~~fromIndex) > length)return -1;

		for (
		  // initialize counter (allow for negative startIndex)
		  fromIndex = (length + fromIndex) % length ;
		  // loop if index is smaller than length,
		  // index is set in (possibly sparse) array
		  // and item at index is not identical to the searched one
		  fromIndex < length && (!(fromIndex in thisArray) || thisArray[fromIndex] !== searchElement) ;
		  // increment counter
		  fromIndex++
		){};
		// if counter equals length (not found), return -1, otherwise counter
		return fromIndex ^ length ? fromIndex : -1;
	}
	//From https://github.com/kriskowal/es5-shim
	/*if(!Array.prototype.indexOf)Array.prototype.indexOf = function(searchElement, fromIndex) {
		var thisArray = _toObject(this),
			length = thisArray.length >>> 0,
			i;
		
		if(!length)return -1;
		
		i = fromIndex || 0;
		
		// handle negative indices
	    i = i >= 0 ? i : Math.max(0, length + i);
		
	    //https://gist.github.com/1034425

		for( ; i < length ; i++) {
			if(i in thisArray && thisArray[i] === searchElement) {
				return i;
			}
		}
		
		return -1;
	};*/

	/** ES5 15.4.4.15
	 * http://es5.github.com/#x15.4.4.15
	 * https://developer.mozilla.org/en/JavaScript/Reference/Global_Objects/Array/lastIndexOf
	 * Returns the last index at which a given element can be found in the array, or -1 if it is not present. The array is searched backwards, starting at fromIndex.
	 * @param {*} searchElement Element to locate in the array.
	 * @param {number} fromIndex The index at which to start searching backwards. Defaults to the array's length, i.e. the whole array will be searched. If the index is greater than or equal to the length of the array, the whole array will be searched. If negative, it is taken as the offset from the end of the array. Note that even when the index is negative, the array is still searched from back to front. If the calculated index is less than 0, -1 is returned, i.e. the array will not be searched.
	 * @return {number}
	 */
  , lastIndexOf : function(searchElement, fromIndex) {
		var thisArray = _toObject(this),
			length = thisArray.length >>> 0,
			i;

		if(!length)return -1;

		i = length - 1;
		if(fromIndex !== void 0)i = Math.min(i, Number["toInteger"](fromIndex))
		
		// handle negative indices
		i = i >= 0 ? i : length - Math.abs(i);
		
		for (; i >= 0; i--) {
			if (i in thisArray && thisArray[i] === searchElement) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * ES5 15.4.4.16
	 * http://es5.github.com/#x15.4.4.16
	 * https://developer.mozilla.org/en/JavaScript/Reference/Global_Objects/Array/every
	 * Tests whether all elements in the array pass the test implemented by the provided function.
	 * @param {Function} callback Function to test for each element.
	 * @param {Object=} thisObject Object to use as this when executing callback.
	 * @param {boolean=} _option_isAll [DO NOT USE IT] system param
	 * @return {boolean}
	 */
  , every : function(callback, thisObject, _option_isAll) {
		if(_option_isAll === void 0)_option_isAll = true;//Default value = true
		var result = _option_isAll;
		_forEach(this, function(value, index) {
			if(result == _option_isAll)result = !!_call_function(callback, thisObject, value, index, this);
		}, this);
		return result;
	}

	/**
	 * ES5 15.4.4.17
	 * http://es5.github.com/#x15.4.4.17
	 * https://developer.mozilla.org/en/JavaScript/Reference/Global_Objects/Array/some
	 * Tests whether some element in the array passes the test implemented by the provided function.
	 * @param {Function} callback Function to test for each element.
	 * @param {Object=} thisObject Object to use as this when executing callback.
	 * @return {boolean}
	 */
  , some : function(callback, thisObject) {
		return _shimed_Array_every.call(this, callback, thisObject, false);
	}

	/**
	 * http://es5.github.com/#x15.4.4.17
	 * https://developer.mozilla.org/en/JavaScript/Reference/global_Objects/Array/filter
	 * Creates a new array with all elements that pass the test implemented by the provided function.
	 * @param {Function} callback Function to test each element of the array.
	 * @param {Object=} thisObject Object to use as this when executing callback.
	 * @return {boolean}
	 */
  , filter : function(callback, thisObject) {
		// ES5 : "If IsCallable(callback) is false, throw a TypeError exception." in "_call_function" function

		var thisArray = _toObject(this),
			len = thisArray.length >>> 0,
			result = [],
			val;

		for (var i = 0; i < len; i++)
			if (i in thisArray) {
				val = thisArray[i];// in case callback mutates this
				if(_call_function(callback, thisObject, val, i, thisArray))result.push(val);
			}

		return result;
	}

	/**
	 * http://es5.github.com/#x15.4.4.19
	 * https://developer.mozilla.org/en/JavaScript/Reference/Global_Objects/Array/map
	 * Creates a new array with the results of calling a provided function on every element in this array.
	 * @param {Function} callback Function that produces an element of the new Array from an element of the current one.
	 * @param {Object?} thisArg Object to use as this when executing callback.
	 * @return {Array}
	 */
  , map : function(callback, thisArg) {
		var thisArray = _toObject(this),
			len = thisArray.length >>> 0,
			result = [];

		for (var i = 0; i < len; i++)
			if (i in thisArray) {
				result[i] = _call_function(callback, thisArg, thisArray[i], i, this);
			}

	    return result;
	}

	/**
	 * Check if given object locate in current array
	 * @param {*} object object to locate in the current array.
	 * @return {boolean}
	 */
  , "contains" : function(object) {
		return !!~this.indexOf(object);
	}
});
}//if __GCC__ECMA_SCRIPT5__

if(__GCC__INCLUDE_EXTRAS__ && __GCC__INCLUDE_EXTRAS__ARRAY_PROTOTYPE_UNIQUE__) {

/**
 * __Non-standart method__ [(!!!)]
 * https://gist.github.com/1044540
 * Create a new Array with the all unique items
 * @return {Array}
 */
if(!Array.prototype["unique"])Array.prototype["unique"] = (function(a) {
  return function() {     // with a function that
	return this.filter(a);// filters by the cached function
  }
})(
  function(a,b,c) {       // which
	return c.indexOf(     // finds out whether the array contains
	  a,                  // the item
	  b + 1               // after the current index
	) <	0                 // and returns false if it does.
  }
);

}//if(__GCC__INCLUDE_EXTRAS__)

_forEach = _unsafe_Function_bind_.call(Function.prototype.call, Array.prototype.forEach);
_Array_map_ = Array.prototype.map;
_shimed_Array_every = Array.prototype.every;

/*  >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  Array.prototype  ==================================  */
/*  ======================================================================================  */

/*  ============================================================================  */
/*  ================================  String  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<  */
if(__GCC__INCLUDE_EXTRAS__ && __GCC__INCLUDE_EXTRAS__STRING_RANDOM__) {

/**
 * Random string
 * https://gist.github.com/973263
 * @param {!number} length Length of result string
 * @return {string}
 */
if(!String["random"])String["random"] = function String_random(length) {
	if(!length || length < 0)return "";
	
	return Array(++length).join(0).replace(/./g,function() {
		return(0 | Math.random() * 32).toString(32)
	});
};

}//if(__GCC__INCLUDE_EXTRAS__)


/*  >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  String  ==================================  */
/*  =============================================================================  */

/*  ======================================================================================  */
/*  ================================  String.prototype  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<  */

/*  ================================  bug fixing  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<  */
if(__GCC__SCRIPT_BUGFIXING__ && __GCC__SCRIPT_BUGFIXING_STRING_PROTOTYPE_TRIM__) {
/*
 [bugfix]
 * ES5 15.5.4.20
 * http://es5.github.com/#x15.5.4.20
 * Removes whitespace from both ends of the string.
 * The trim method returns the string stripped of whitespace from both ends. trim does not affect the value of the string itself.
 * https://developer.mozilla.org/en/JavaScript/Reference/Global_Objects/String/Trim
 */
if (!String.prototype.trim || _String_trim_whitespace.trim()) {
    // http://blog.stevenlevithan.com/archives/faster-trim-javascript
    // http://perfectionkills.com/whitespace-deviations/
    _String_trim_whitespace = "[" + _String_trim_whitespace + "]";
    _String_trim_beginRegexp = new RegExp("^" + _String_trim_whitespace + _String_trim_whitespace + "*");
    _String_trim_endRegexp = new RegExp(_String_trim_whitespace + _String_trim_whitespace + "*$");
	
    String.prototype.trim = function() {
        return String(this).replace(_String_trim_beginRegexp, "").replace(_String_trim_endRegexp, "");
    };
}
}//if __GCC__SCRIPT_BUGFIXING_STRING_PROTOTYPE_TRIM__
_String_trim_ = String.prototype.trim;

if(__GCC__SCRIPT_BUGFIXING__ && __GCC__SCRIPT_BUGFIXING_STRING_PROTOTYPE_SPLIT__) {
// ES5 15.5.4.14
// http://es5.github.com/#x15.5.4.14
// [bugfix, chrome]
// If separator is undefined, then the result array contains just one String, which is the this value (converted to a String). If limit is not undefined, then the output array is truncated so that it contains no more than limit elements.
// "0".split(undefined, 0) -> []
if("0".split(void 0, 0).length) {
	String.prototype.split = function(separator, limit) {
		if(separator === void 0 && limit === 0)return [];
		return _String_split_.call(this, separator, limit);
	}
}
}//if __GCC__SCRIPT_BUGFIXING_STRING_PROTOTYPE_SPLIT__
/*  >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  bug fixing  ==================================  */

_String_contains_ = function(substring, fromIndex) {
	return !!~this.indexOf(substring, fromIndex);
}

if (__GCC__ECMA_SCRIPT6__) {
_append(String.prototype, {
	//from https://github.com/paulmillr/es6-shim/blob/master/es6-shim.js
	/**
	 * String repeat
	 * @param {!number} count repeat times
	 * @return {string} result
	 */
	"repeat" : function(count) {
		return new Array(++count).join(this + "");
	}

	/**
	 * Check if given string locate in the begining of current string
	 * @param {string} substring substring to locate in the current string.
	 * @param {number=} fromIndex start the startsWith check at that position
	 * @return {boolean}
	 */
  , "startsWith" : function(substring, fromIndex) {
  		return this.lastIndexOf(substring, fromIndex) === (fromIndex || 0);
  		//return this.indexOf(value) == ((position -= position % 1) || 0);
	}

	/**
	 * Check if given string locate at the end of current string
	 * @param {string} substring substring to locate in the current string.
	 * @param {number=} fromIndex end the endsWith check at that position
	 * @return {boolean}
	 */
  , "endsWith" : function(substring, fromIndex) {
		substring = substring + "";
		return this.substr(-substring.length - (fromIndex || 0), fromIndex) == substring;
		//var length = this.length - value.length;
  		//position = typeof position == "undefined" ? length : ((position -= position % 1) || 0);
  		//return length > -1 && this.indexOf(value, position) == position;
	}

	/**
	 * Check if given string locate in current string
	 * @param {string} substring substring to locate in the current string.
	 * @param {number=} fromIndex start the contains check at that position
	 * @return {boolean}
	 */
  , "contains" : _String_contains_

	/**
	 * String to Array
	 * @return {Array}
	 */
  , "toArray" : function() {
		return _String_split_.call(this, "");
	}

	/**
	 * Reverse string
	 * @return {Array}
	 */
  , "reverse" : function() {
		return Array.prototype.reverse.call(_String_split_.call(this + "", "")).join("");
	}
})
}//if __GCC__ECMA_SCRIPT6__

// TODO::
//  1. Maybe https://developer.mozilla.org/en/JavaScript/Reference/Global_Objects/String/TrimRight ?
//  2. Maybe https://developer.mozilla.org/en/JavaScript/Reference/Global_Objects/String/TrimLeft ?
//  https://gist.github.com/1036520



/*  ======================================================================================  */
/*  ================================  Number  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<  */
//from https://github.com/paulmillr/es6-shim/blob/master/es6-shim.js

if (__GCC__ECMA_SCRIPT6__) {
_append(Number, {
	"isFinite" : function(value) {
		return typeof value === 'number' && global.isFinite(value);	
	}

  , "isInteger" : function(value) {
		return Number["isFinite"](value) &&
			value >= -9007199254740992 && value <= 9007199254740992 &&
			Math.floor(value) === value;
	}

  , "isNaN" : function(value) {
		return Object["is"](value, NaN);
	}

  , "toInteger" : function(value) {
		var number = +value;
		if (Number["isNaN"](number)) return +0;
		if (number === 0 || !global.isFinite(number)) return number;
		return ((number < 0) ? -1 : 1) * Math.floor(Math.abs(number));
	}
})
}//if __GCC__ECMA_SCRIPT6__
/*  >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  Number  ==================================  */
/*  ======================================================================================  */


/*  ======================================================================================  */
/*  ======================================  Events  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<  */

// new Event(...) and new CustomEvent(...) from github.com/Raynos/DOM-shim/ with my fixing
// Chrome throws error if using Error
// IE9 says Event is an object and not a function -.- 
// IE8 doesn't like it and gives a different error messsage!
// Firefox also says no
// Safari says me too, me too!
// Opera throws a DOM exception instead ¬_¬
/**
 * @constructor
 * @param {string} type
 * @param {Object=} dict
 */
_Event = function (type, dict) {// Event constructor
	var e = document.createEvent("Events");

	dict = dict || {};
	e.initEvent(type, dict.bubbles || false, dict.cancelable || false);
	if(!("isTrusted" in e))e.isTrusted = false;

	return e;
};

try {
	_Event_prototype = Event.prototype;
	new Event("click");
} catch (e) {
	global["Event"] = _Event;

	if(_Event_prototype)_Event.prototype = _Event_prototype;//В IE < 8 не удастся получить Event.prototype
}

// Chrome calling .initEvent on a CustomEvent object is a no-no
// IE9 doesn't like it either
// IE8 says no in its own special way.
// Firefox agrees this cannot be done
// Safari says lul wut?
// Opera says have another DOM exception!
/**
 * @constructor
 * @param {string} type
 * @param {Object=} dict
 */
_CustomEvent = function (type, dict) {// CustomEvent constructor
	var e
	  , _detail
	;
	try {
		e = document.createEvent("CustomEvent");
	}
	catch(err) {//FF 3.6 cant create "CustomEvent"
		e = document.createEvent("Event");
	}

	dict = dict || {};
	_detail = dict.detail !== void 0 ? dict.detail : null;
	(e.initCustomEvent || (e.detail = _detail, e.initEvent)).call
		(e, type, dict.bubbles || false, dict.cancelable || false, _detail);
	if(!("isTrusted" in e))e.isTrusted = false;

	return e;
};

try {
	_Custom_Event_prototype = (global["CustomEvent"] || Event).prototype;//use global to prevent Exception if the is not CustomEvent || CustomEvent.prototype
	var c = new CustomEvent("magic");
} catch (e) {
	global["CustomEvent"] = _CustomEvent;

	if(_Custom_Event_prototype || _Event_prototype)_CustomEvent.prototype = _Custom_Event_prototype || _Event_prototype;//The is no CustomEvent.prototype in IE < 8
}

//Browser not implement Event.prototype.stopImmediatePropagation
if(!_Event_prototype["stopImmediatePropagation"]) {
	implementation_stopImmediatePropagation = function(e) {
		var listener = this._listener,
			thisObj = this._this;

		if(typeof listener !== "function") {
			if("handleEvent" in listener) {
				thisObj = listener;
				listener = listener.handleEvent;
			}
			else return;
		}

		if(e["__stopNow"]) {
			e.stopPropagation();
		}
		else return listener.apply(thisObj, arguments);
	};

	_Event_prototype["stopImmediatePropagation"] = function() {
		this["__stopNow"] = true;
	}
}

//fix [add|remove]EventListener for all browsers that support it natively
if("addEventListener" in _testElement && 
	!_testElement.addEventListener["__shim__"]//Indicator that this is not native implementation
  ) {
  	// FF fails when you "forgot" the optional parameter for addEventListener and removeEventListener
	// Arg!!! FF 3.6 Unable to override addEventListener https://bugzilla.mozilla.org/show_bug.cgi?id=428229
	// Opera didn't do anything without optional parameter
	// Opera do not discarded multiple identical EventListeners are registered on the same EventTarget with the same parameters
	_tmp_ = 0;

	try {
		//we can use "dom4_mutationMacro" var here :]
		dom4_mutationMacro = function () {
			_tmp_++
		};
		_testElement.addEventListener("click", dom4_mutationMacro);
		_testElement.addEventListener("click", dom4_mutationMacro);
		if(_testElement.click)// NO: Opera 10.10
			_testElement.click();//testing
		else
			_testElement.dispatchEvent(new _Event("click"));
	} catch (e) {

	} finally {
		if(_tmp_ == 0 || _tmp_ == 2 || implementation_stopImmediatePropagation) {//fixEventListenerAll
			(function() {//closure
				var _addEventListener_dublicate_bug = _tmp_ == 2
				  	/** @const @type {string} */
				  , _event_eventsUUID = "_e_8vj"
				;

				_forEach(
					[global["HTMLDocument"] && global["HTMLDocument"].prototype || global["document"],
					 global["Window"] && global["Window"].prototype || global,
					 _Element_prototype],
					function (elementToFix) {
						if(elementToFix) {
							var old_addEventListener = elementToFix.addEventListener,
								old_removeEventListener = elementToFix.removeEventListener;

							if(old_addEventListener) {
								elementToFix.addEventListener = function(type, listener, useCapture) {
									var _,
										_eventsUUID;

									useCapture = useCapture || false;

									if(_addEventListener_dublicate_bug || implementation_stopImmediatePropagation) {
										_eventsUUID = _event_eventsUUID + (useCapture ? "-" : "") + (listener[UUID_PROP_NAME] || (listener[UUID_PROP_NAME] = ++UUID)) + type
									
										if(!(_ = this["_"]))_ = this["_"] = {};
										//If multiple identical EventListeners are registered on the same EventTarget with the same parameters, the duplicate instances are discarded. They do not cause the EventListener to be called twice, and since the duplicates are discarded, they do not need to be removed manually with the removeEventListener method.
										if(_eventsUUID in _)return;

										listener = implementation_stopImmediatePropagation ? (
											_[_eventsUUID] = _unsafe_Function_bind_.call(implementation_stopImmediatePropagation, {_listener : listener, _this : this})
										) : (_[_eventsUUID] = void 0), listener;
									}

									return old_addEventListener.call(this, type, listener, useCapture);
								};

								//elementToFix.addEventListener.__shim__ = true;
								if(old_removeEventListener)elementToFix.removeEventListener = function(type, listener, useCapture) {
									var _,
										_eventsUUID;

									useCapture = useCapture || false;

									if(_addEventListener_dublicate_bug || implementation_stopImmediatePropagation) {
										_ = this["_"];
										if(_ && _[_eventsUUID = _event_eventsUUID + (useCapture ? "-" : "") + listener[UUID_PROP_NAME] + type]) {
											listener = _[_eventsUUID];
											delete _[_eventsUUID];
										}
									}

									return old_removeEventListener.call(this, type, listener, useCapture);
								};
								//elementToFix.removeEventListener.__shim__ = true;
							}
						}
					}
				);
 			})();
		}
	}
}
else if(DEBUG && !document.addEventListener) {
	console.error("[add|remove]EventListener not supported")
}

/*  >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  Events  ======================================  */
/*  ======================================================================================  */



/*  =======================================================================================  */
/*  =================================  Utils.Dom  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<  */

/**
 * __Non-standart__
 * Utils.Dom.DOMStringCollection
 * DOMSettableTokenList like object
 * http://www.w3.org/TR/html5/common-dom-interfaces.html#domsettabletokenlist-0
 * @param {Function} getter callback for onchange event
 * @param {Function} setter callback for onchange event
 * @param {Object} object_this context of onchange function
 * @constructor
 */
DOMStringCollection = function(getter, setter, object_this) {
	/**
	 * Event fired when any change apply to the object
	 */
	this._getter = getter;
	this._setter = setter;
	this._object_this = object_this;
	this.length = 0;
	this.value = "";

	this.DOMStringCollection_check_currentValue_and_Token("1");//"1" - fakse token, need only thisObj.value check
};
/**
 * @param {DOMStringCollection} _DOMStringCollection
 * @param {string} _string
 */
DOMStringCollection_init = function(_DOMStringCollection, _string) {
	var thisObj = _DOMStringCollection
	  , string = _string || ""//default value
	  , isChange = !!thisObj.length
	;

	if(isChange) {
		while(thisObj.length > 0)
			delete thisObj[--thisObj.length];

		thisObj.value = "";
	}

	if(string) {
		if(string = _String_trim_.call(string)) {
			_String_split_.call(string, RE_DOMSettableTokenList_spaces).forEach(DOMStringCollection_init_add, thisObj);
		}
		thisObj.value = _string;//empty value should stringify to contain the attribute's whitespace
	}			

	if(isChange && thisObj._setter)thisObj._setter.call(thisObj._object_this, thisObj.value);
};
/**
 * @param {string} token
 * @this {DOMStringCollection}
 */
DOMStringCollection_init_add = function(token) {
	this[this.length++] = token;
};

_append(DOMStringCollection.prototype, {
	DOMStringCollection_check_currentValue_and_Token : function(token) {
		var string = this._getter.call(this._object_this);
		if(string != this.value)DOMStringCollection_init(this, string);

		if(token === "")_throwDOMException("SYNTAX_ERR");
		if(_String_contains_.call(token + "", " "))_throwDOMException("INVALID_CHARACTER_ERR");
	},
	"add": function(token) {
		var thisObj = this, v = thisObj.value;

		if(thisObj.contains(token)//DOMStringCollection_check_currentValue_and_Token(token) here
			)return;

		thisObj.value += ((v && !v.match(RE_DOMSettableTokenList_lastSpaces) ? " " : "") + token);

		this[this.length++] = token;

		if(thisObj._setter)thisObj._setter.call(thisObj._object_this, thisObj.value);
	},
	"remove": function(token) {
		this.DOMStringCollection_check_currentValue_and_Token(token);

		var i, itemsArray, thisObj = this;

		thisObj.value = thisObj.value.replace(new RegExp("((?:\ +|^)" + token + "(?:\ +|$))", "g"), function(find, p1, offset, string) {
			return offset && find.length + offset < string.length ? " " : "";
		});

		itemsArray = _String_split_.call(thisObj.value, " ");

		for(i = thisObj.length - 1 ; i > 0  ; --i) {
			if(!(thisObj[i] = itemsArray[i])) {
				thisObj.length--;
				delete thisObj[i];
			}
		}

		if(thisObj._setter)thisObj._setter.call(thisObj._object_this, thisObj.value)
	},
	"contains": function(token) {
		this.DOMStringCollection_check_currentValue_and_Token(token);

		return _String_contains_.call(" " + this.value + " ", " " + token + " ");
	},
	"item": function(index) {
		this.DOMStringCollection_check_currentValue_and_Token("1");//"1" - fakse token, need only thisObj.value check

		return this[index] || null;
	},
	"toggle": function(token) {
		var result = thisObj.contains(token); //DOMStringCollection_checkToken(token) here;

		this[result ? 'add' : 'remove'](token);

		return result;
	}
});

DOMStringCollection.prototype.toString = function() {//_append function do not overwrite Object.prototype.toString
	return this.value || ""
};

if(__GCC__INCLUDE_EXTRAS__ && __GCC__INCLUDE_EXTRAS__DOMSTRINGCOLLECTION__) {//Export DOMStringCollection
	if(!global["Utils"])global["Utils"] = {};
	if(!global["Utils"]["Dom"])global["Utils"]["Dom"] = {};
	global["Utils"]["Dom"]["DOMStringCollection"] = DOMStringCollection;
}


/*  >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  Utils.Dom  ==================================  */
/*  =======================================================================================  */


/*  ======================================================================================  */
/*  ========================================  DOM  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<  */

//[Opera lt 12]
if(!_Event_prototype["AT_TARGET"]) {
	_Event_prototype["AT_TARGET"] = 2;
	_Event_prototype["BUBBLING_PHASE"] = 3;
	_Event_prototype["CAPTURING_PHASE"] = 1;/*,
		"BLUR": 8192,
		"CHANGE": 32768,
		"CLICK": 64,
		"DBLCLICK": 128,
		"DRAGDROP": 2048,
		"FOCUS": 4096,
		"KEYDOWN": 256,
		"KEYPRESS": 1024,
		"KEYUP": 512,
		"MOUSEDOWN": 1,
		"MOUSEDRAG": 32,
		"MOUSEMOVE": 16,
		"MOUSEOUT": 8,
		"MOUSEOVER": 4,
		"MOUSEUP": 2,
		"SELECT": 16384*/
}
if(!Event["AT_TARGET"]) {
	Event["AT_TARGET"] = 2;
	Event["BUBBLING_PHASE"] = 3;
	Event["CAPTURING_PHASE"] = 1;
}


	// window.getComputedStyle fix
	//FF say that pseudoElt param is required
	try {
		global.getComputedStyle(_testElement)
	}
	catch(e) {
		global.getComputedStyle = _unsafe_Function_bind_(function(obj, pseudoElt) {
			return this.call(global, obj, pseudoElt || null)
		}, global.getComputedStyle);
	}

/*  ======================================================================================  */
/*  >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  DOM  =======================================  */

/*  ======================================================================================  */
/*  ================================  Element.prototype  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<  */


/*  Some of from https://github.com/Raynos/DOM-shim/  */

//https://developer.mozilla.org/en/DOM/Element.classList
//Add JS 1.8 Element property classList	   
// IE < 8 support in a.ielt8.js and a.ielt8.htc
if(!("classList" in _testElement)) {
	DOMStringCollection_setNodeClassName = function(newValue) {
		this.className = newValue;
	};
	DOMStringCollection_getNodeClassName = function(newValue) {
		return this.className;
	};

	Object.defineProperty(_Element_prototype, "classList", {
		"get" : function() {
			if(!this.tagName)return void 0;

			var thisObj = this,
				cont = thisObj["_"] || (thisObj["_"] = {});//Положим S_ELEMENT_CACHED_CLASSLIST_NAME в контейнер "_";

			if(!cont[S_ELEMENT_CACHED_CLASSLIST_NAME]) {
				cont[S_ELEMENT_CACHED_CLASSLIST_NAME] = 
					new DOMStringCollection(DOMStringCollection_getNodeClassName, DOMStringCollection_setNodeClassName, thisObj);
			}

			return cont[S_ELEMENT_CACHED_CLASSLIST_NAME];
		}
	});
}

//https://developer.mozilla.org/en/DOM/Node.parentElement
//[FF lt 9]
if(!("parentElement" in _testElement)) {
	Object.defineProperty(_Element_prototype, "parentElement", {"get" : function() {
		var parent = this.parentNode;

	    if(parent && parent.nodeType === 1)return parent;

	    return null;
	}});
}

//https://developer.mozilla.org/En/DOM/Node.contains
//[FF lt 9]
if(!("contains" in _testElement)) {
	global["Node"].prototype.contains = function (arg) {
		return !!(this.compareDocumentPosition(arg) & 16)
	};
}

//http://html5.org/specs/dom-parsing.html#insertadjacenthtml()
//https://developer.mozilla.org/En/DOM/Element.insertAdjacentHTML
/*https://gist.github.com/1276030 by https://gist.github.com/eligrey*/
if(!("insertAdjacentHTML" in _testElement)) {
/**
 * insertAdjacentHTML() parses the specified text as HTML or XML and inserts the resulting nodes into the DOM tree at a specified position. It does not reparse the element it is being used on and thus it does not corrupt the existing elements inside the element. This, and avoiding the extra step of serialization make it much faster than direct innerHTML manipulation.
 * @param {string} position is the position relative to the element, and must be one of the following strings:
 *  'beforebegin' - Before the element itself.
 *  'afterbegin' - Just inside the element, before its first child.
 *  'beforeend' - Just inside the element, after its last child.
 *  'afterend' - After the element itself.
 *  Note: The beforebegin and afterend positions work only if the node is in a tree and has an element parent.
 * @param {string} html is the string to be parsed as HTML or XML and inserted into the tree.
 */
	global["HTMLElement"].prototype.insertAdjacentHTML = function(position, html) {
		var	ref = this,
			container = ref.ownerDocument.createElement("_"),
			nodes,
			translate = {
				"beforebegin" : "before",
				"afterbegin" : "prepend",
				"beforeend" : "append",
				"afterend" : "after"
			},
			func;

		container.innerHTML = html;
		nodes = container.childNodes;

		if(nodes && nodes.length && 
			(func = ref[translate[position]])) {
			func.apply(this, nodes)
		}

		nodes = container = void 0;
	};
}


// Emuleted HTMLTimeElement
// TODO:: need more work
/*
if(!(global["HTMLTimeElement"] && global["HTMLTimeElement"].prototype))
Object.defineProperty((global["HTMLUnknownElement"] && global["HTMLUnknownElement"].prototype) || _Element_prototype,
	"dateTime", {
	"get" : function() {
		var thisObj = this,
			elTag = thisObj.tagName;

		return thisObj.tagName.toUpperCase() == "TIME" ? (thisObj.getAttribute("datetime") || "") : void 0;
	},
	"set" : function(val) {
		var thisObj = this,
			elTag = thisObj.tagName;

		if(thisObj.tagName.toUpperCase() == "TIME") {
			thisObj.setAttribute("datetime", val);
			return val;
		}

		return null;
	}
});
*/
// IE9 thinks the argument is not optional
// FF thinks the argument is not optional
// Opera agress that its not optional
// IE < 9 has javascript implimentation marked as `shim`
// FROM https://github.com/Raynos/DOM-shim/blob/master/src/all/bugs.js
if(document.importNode && !document.importNode["shim"])
try {
	document.importNode(_testElement);
} catch (e) {
	var importNode = document.importNode;
	delete document.importNode;
	document.importNode = function (node, bool) {
		if (bool === void 0) {
			bool = true;
		}
		return importNode.call(this, node, bool);
	}
}

// Firefox fails on .cloneNode thinking argument is not optional
// Opera agress that its not optional.
// FROM https://github.com/Raynos/DOM-shim/blob/master/src/all/bugs.js
try {
	_testElement.cloneNode();
} catch (e) {
	[
		Node.prototype,
		//Comment.prototype,
		_Element_prototype,
		//ProcessingInstruction.prototype,
		Document.prototype,
		//DocumentType.prototype,
		DocumentFragment.prototype
	].forEach(fixNodeOnProto);
}
function fixNodeOnProto(proto) {
	var cloneNode = proto.cloneNode;
	delete proto.cloneNode;
	proto.cloneNode = function _cloneNode(bool) {
		if (bool === void 0) {
			bool = true;
		}
		return cloneNode.call(this, bool);
	};
}

//New DOM4 API
if(!_testElement["prepend"]) {
	dom4_mutationMacro = function(nodes) {
		var resultNode = null
		  , i = 0
		  , maxLength = nodes.length
		  , curLength
		;
		
		nodes = _Array_map_.call(nodes, function (node) {
			return typeof node === "string" ?
				document.createTextNode(node) :
				node;
		});
		
		if (maxLength === 1) {
			resultNode = nodes[0];
		} else {
			resultNode = document.createDocumentFragment();

			//nodes can be a live NodeList so we can't use forEach and need to check nodes.length after each appendChild
			for(var i = 0, maxLength = nodes.length, curLength ; i < (curLength = nodes.length) ; ++i) {
				i -= (maxLength - curLength);
				resultNode.appendChild(nodes[i]);
			}			
		}
		
		return resultNode;
	}
	
	_Element_prototype["after"] = function () {
		this.parentNode && this.parentNode.insertBefore(dom4_mutationMacro(arguments), this.nextSibling);
	};

	_Element_prototype["before"] = function () {
		this.parentNode && this.parentNode.insertBefore(dom4_mutationMacro(arguments), this);
	};

	_Element_prototype["append"] = function () {
		this.appendChild(dom4_mutationMacro(arguments));
	};
	
	_Element_prototype["prepend"] = function () {
		this.insertBefore(dom4_mutationMacro(arguments), this.firstChild);
	};

	_Element_prototype["replace"] = function () {
		this.parentNode && this.parentNode.replaceChild(dom4_mutationMacro(arguments), this);
	};

	_Element_prototype["remove"] = function () {
		this.parentNode && this.parentNode.removeChild(this);
	};

	if(!("prepend" in document)) {
		document["prepend"] = function() {
			_Element_prototype["prepend"].apply(this.documentElement, arguments)
		}
		document["append"] = function() {
			_Element_prototype["append"].apply(this.documentElement, arguments)
		}
	}
}

if(!_Element_prototype.matchesSelector) {
	_Element_prototype.matchesSelector =
		_Element_prototype["webkitMatchesSelector"] ||
		_Element_prototype["mozMatchesSelector"] ||
		_Element_prototype["msMatchesSelector"] ||
		_Element_prototype["oMatchesSelector"] || function(selector, refNodes) {
			if(!selector)return false;
			if(selector === "*")return true;

			var thisObj,
				parent,
				i,
				k = 0,
				str,
				rules,
				tmp,
				match;

			if(refNodes) {
				if("length" in refNodes) {//fast and unsafe isArray
					thisObj = refNodes[0];
				}
				else {
					thisObj = refNodes;
					refNodes = void 0;
				}
			} 
			else thisObj = this;

			do {
				match = false;
				if(thisObj === document.documentElement)match = selector === ":root";
	  			else if(thisObj === document.body)match = selector.toUpperCase() === "BODY";

				if(!match) {
					selector = _String_trim_.call(selector.replace(RE__matchSelector__doubleSpaces, "$1"));

					if(rules = selector.match(RE__selector__easySelector)) {
						switch (selector.charAt(0)) {
							case '#':
								match = thisObj.id === selector.slice(1);
							break;
							default:
								match = !rules[1] || (!("tagName" in thisObj) || thisObj.tagName.toUpperCase() === rules[1].toUpperCase());
								if(match && rules[2]) {
									i = -1;
									tmp = rules[2].slice(1).split(".");
									str = " " + thisObj.className + " ";
									while(tmp[++i] && match) {
										match = _String_contains_.call(str, " " + tmp[i] + " ");
									}
								}
						}
					}
					
					if(!/([,>+~ ])/.test(selector) && (parent = thisObj.parentNode) && parent.querySelector) {
						match = parent.querySelector(selector) === thisObj;
					}

					if(!match && (parent = thisObj.ownerDocument)) {
						tmp = parent.querySelectorAll(selector);
						i = -1;
						while(!match && tmp[++i]) {
					        match = tmp[i] === thisObj;
					    }
					}
				}
			}
			while(match && refNodes && (thisObj = refNodes[++k]));

		    return refNodes && "length" in refNodes ? match && refNodes.length == k : match;
		}
}
if(!document.documentElement.matchesSelector)document.documentElement.matchesSelector = _Element_prototype.matchesSelector;
if(!("matches" in _Element_prototype))_Element_prototype["matches"] = document.documentElement["matches"] = _Element_prototype.matchesSelector;

if(!("find" in document)) {
	RE_document_find_scopedreplacer = /(\:scope)(?=[ >~+])/;

	document["find"] = DocumentFragment.prototype["find"] = function(selector, refNodes) {
		refNodes && ("length" in refNodes || (refNodes = [refNodes])) || (refNodes = [this]);

		var result
		  , i = 0
		  , l = refNodes.length
		  , node
		;

		do {
			node = refNodes[i];
			selector = selector.replace(RE_document_find_scopedreplacer, node.nodeType == 9/*Node.DOCUMENT_NODE*/ ?
				":root"
				:
				function() {
					return "#" + (node.id || (node.id = "find" + ++UUID));
				}
			);

			result = node.querySelector(selector);
		}
		while(!result && ++i < l);

		return result || null;
	};

	document["findAll"] = DocumentFragment.prototype["findAll"] = function(selector, refNodes) {
		refNodes && ("length" in refNodes || (refNodes = [refNodes])) || (refNodes = [this]);

		var result = []
		  , i = 0
		  , l = refNodes.length
		  , node
		  , tmpResult
		  , node2
		  , k
		  , n
		  , uuid
		  , uniqueMap = {}
		  , forse
		;

		do {
			node = refNodes[i];
			selector = selector.replace(RE_document_find_scopedreplacer, node.nodeType == 9/*Node.DOCUMENT_NODE*/ ?
				":root"
				:
				function() {
					return "#" + (node.id || (node.id = "find" + ++UUID));
				}
			);

			tmpResult = node.querySelectorAll(selector);
			for(k = 0, n = tmpResult.length ; k < n ; ++k) {
				node2 = tmpResult[k];
				uuid = (forse = false, node2[UUID_PROP_NAME]) || (forse = true, (node2[UUID_PROP_NAME] = ++UUID));

				if(forse || !(uuid in uniqueMap)) {
					uniqueMap[uuid] = void 0;

					result.push(tmpResult[k]);
				}
			}
		}
		while(++i < l);

		return result;
	};
}
if(!("find" in _Element_prototype)) {
	//TODO:: add ':scope' support
	// http://lists.w3.org/Archives/Public/public-webapps/2011OctDec/0316.html (Re: QSA, the problem with ":scope", and naming)
	_Element_prototype["find"] = _Element_prototype.querySelector;
	_Element_prototype["findAll"] = _Element_prototype.querySelectorAll;
}

/*  >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  Element.prototype  ==================================  */
/*  ======================================================================================  */

/*  ======================================================================================  */
/*  ================================  HTMLInputElement.prototype  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<  */
/*  ================================  HTMLButtonElement.prototype  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<  */
/*  ================================  HTMLKeygenElement.prototype  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<  */
/*  ================================  HTMLMeterElement.prototype  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<  */
/*  ================================  HTMLOutputElement.prototype  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<  */
/*  ================================  HTMLProgressElement.prototype  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<  */
/*  ================================  HTMLTextAreaElement.prototype  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<  */
/*  ================================  HTMLSelectElement.prototype  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<  */

if(__GCC__INCLUDE_EXTRAS__ && __GCC__INCLUDE_EXTRAS__LABELS_AND_CONTROL_POLYFILL__) {
	_labelable_elements = " INPUT BUTTON KEYGEN METER OUTPUT PROGRESS TEXTAREA SELECT ";
	/*
	Implement HTML*Element.labels
	https://developer.mozilla.org/en/DOM/HTMLInputElement
	http://www.w3.org/TR/html5/forms.html#dom-lfe-labels
	*/
	if(!("labels" in _document_createElement("input")))
		Object.defineProperty(_Element_prototype, "labels", {
			enumerable: true,
			"get" : function() {
				if(!(_String_contains_.call(_labelable_elements, " " + this.nodeName.toUpperCase() + " ")))
					return void 0;

				var node = this,
					/**
					 * represents the list of label elements, in [!]tree order[!]
					 * @type {Array}
					 */
					result = this.id ?
						_Array_from(document.querySelectorAll("label[for='" + this.id + "']")) :
						[],
					_lastInTreeOrder_index = result.length - 1;

				while((node = node.parentNode) && (!node.control || node.control === this))
					if(node.nodeName.toUpperCase() === "LABEL") {

						while(result[_lastInTreeOrder_index] &&
							result[_lastInTreeOrder_index].compareDocumentPosition(node) & 2)//DOCUMENT_POSITION_PRECEDING
							_lastInTreeOrder_index--;
						_Array_splice_.call(result, _lastInTreeOrder_index + 1, 0, node)
					}

				return result;
			}
		});
}//__GCC__INCLUDE_EXTRAS__

/*  ======================================================================================  */

/*  ======================================================================================  */
/*  ================================  HTMLLabelElement.prototype  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<  */

if(__GCC__INCLUDE_EXTRAS__ && __GCC__INCLUDE_EXTRAS__LABELS_AND_CONTROL_POLYFILL__) {
	/*
	Implement HTMLLabelElement.control
	https://developer.mozilla.org/en/DOM/HTMLLabelElement
	http://www.w3.org/TR/html5/forms.html#dom-label-control
	*/
	if(!("control" in _document_createElement("label"))) {
		_nodesRecursivelyWalk = function (nodes, cb) {
			for (var i = 0, len = nodes.length; i < len; i++) {
				var node = nodes[i],
					ret = cb(node);
				if (ret) {
					return ret;
				}
				if (node.childNodes && node.childNodes.length > 0) {
					ret = _nodesRecursivelyWalk(node.childNodes, cb);
					if (ret) {
						return ret;
					}
				}
			}
		};

		Object.defineProperty(global["HTMLLabelElement"] && global["HTMLLabelElement"].prototype || _Element_prototype, "control", {
			enumerable: true,
			"get" : function() {
				if(this.nodeName.toUpperCase() !== "LABEL")
					return void 0;

				if(this.hasAttribute("for"))
					return document.getElementById(this.htmlFor);

				return _nodesRecursivelyWalk(this.childNodes,
						function(el) {
							if(_String_contains_.call(_labelable_elements, " " + el.nodeName.toUpperCase() + " "))
								return el
						}
					) || null;
			}
		});
	}
}//__GCC__INCLUDE_EXTRAS__

/*  >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  HTMLLabelElement.prototype  ==================================  */
/*  ======================================================================================  */

/*  ======================================================================================  */
/*  ================================  HTMLSelectElement.prototype  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<  */

/*
 * Reverse Ordered Lists in HTML5
 * a polyfill for the ordered-list reversed attribute
 * Based on https://gist.github.com/1671548
 *
 * http://www.whatwg.org/specs/web-apps/current-work/multipage/grouping-content.html#the-ol-element
 * http://www.whatwg.org/specs/web-apps/current-work/multipage/grouping-content.html#dom-li-value
 * http://www.impressivewebs.com/reverse-ordered-lists-html5/
 * http://html5doctor.com/ol-element-attributes/
 * TODO::
 *  1. Equivalent list-style-type:
		type="1"	decimal (default style)
		type="a"	lower-alpha
		type="A"	upper-alpha
		type="i"	lower-roman
		type="I"	upper-roman
 */
//In strict mode code, functions can only be declared at top level or immediately within another function
if(__GCC__INCLUDE_EXTRAS__ && __GCC__INCLUDE_EXTRAS__REVERSE_POLYFILL__ && !('reversed' in _document_createElement("ol"))) {
	OL_reversed_Shim = function(list) {
		var reversed = list.getAttribute('reversed')
		  , _ = list["_"]
		  , children
		  , count
		;

		if(!_)_ = list["_"] = {};//Values container

		if("olreversed" in _ && _["olreversed"] == (reversed !== null))return;

		children = list.children;
		count = list.getAttribute('start');

		//check to see if a start attribute is provided
		if(count !== null) {
			count = Number(count);

			if(isNaN(count))count = null;
		}

		if(reversed !== null) {
			_["olreversed"] = true;

			//no, this isn't duplication - start will be set to null
			// in the previous if statement if an invalid start attribute
			// is provided
			if(count === null)
				count = children.length;

			_forEach(children, function(child) {
				child["value"] = count--;
			});
		}
		else {
			_["olreversed"] = false;

			if(children[0]) {
				children[0]["value"] = count || 0;
			}

			_forEach(children, function(child) {
				child.removeAttribute("value");
			});
		}
	};

	Object.defineProperty(global["HTMLOListElement"] && global["HTMLOListElement"].prototype || _Element_prototype, "reversed", {
		get : function () {
			var thisObj = this;

			if((thisObj.nodeName || "").toUpperCase() !== "OL")return void 0;

			return thisObj.getAttribute('reversed') !== null;
		},
		/** @param {boolean} value */
		set : function (value) {
			var thisObj = this;

			if((thisObj.nodeName || "").toUpperCase() !== "OL")return void 0;

			thisObj[(value ? "set" : "remove") + "Attribute"]('reversed', "");

            OL_reversed_Shim(thisObj); //Run shim

			return value;
		}
	});

	//Auto init
	OL_reversed_autoInitFunction = function() {
		document.removeEventListener('DOMContentLoaded', OL_reversed_autoInitFunction, false);
		OL_reversed_autoInitFunction = void 0;
		_forEach(document.getElementsByTagName("ol"), OL_reversed_Shim);
	};
	if(document.readyState == 'complete')
		OL_reversed_autoInitFunction();
	else 
		document.addEventListener('DOMContentLoaded', OL_reversed_autoInitFunction, false);
};//__GCC__INCLUDE_EXTRAS__
/*  >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  HTMLLabelElement.prototype  ==================================  */
/*  ======================================================================================  */

/*  =======================================================================================  */
/*  ================================  NodeList.prototype  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<  */

//Inherit NodeList from Array
function extendNodeListPrototype(nodeList_proto) {
	//in old FF nodeList_proto.__proto__ != nodeList_proto.constructor.prototype
	nodeList_proto = nodeList_proto.__proto__ || nodeList_proto.constructor.prototype;

	if(nodeList_proto && nodeList_proto !== Array.prototype) {
		nodeList_methods_fromArray.forEach(function(key) {
			if(!nodeList_proto[key])nodeList_proto[key] = Array.prototype[key];
		})
	}
}
if(document.getElementsByClassName)extendNodeListPrototype(document.getElementsByClassName(""));
if(document.querySelectorAll)extendNodeListPrototype(document.querySelectorAll("#z"));

/*  >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  NodeList.prototype  ==================================  */
/*  ======================================================================================  */

/*  =======================================================================================  */
/*  ======================================  Network  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<  */

if(__GCC__INCLUDE_EXTRAS__ && __GCC__INCLUDE_EXTRAS__XHR__) {

//https://github.com/Raynos/xhr/blob/master/index.js
//Thx Raynos !!!
/*
example:
XHR({
    method: "GET",
    data: {
		param1 : "value",
		param2 : ["value1", "value2", "value3"]
    },
    uri: "/foo"
}, function (err, result) {
    // this === xhr
})

xhr({
    method: "GET",
    data: '{"param1":"value","param2":["value1", "value2", "value3"]}',
    uri: "/foo",
    headers: {
        "Content-Type": "application/json"
    }
}, function (err, result) {
    // this === xhr
})
*/
XHR = global["XHR"] = function(options, callback) {
	options = _append(options || {}, XHR.defaults);
	Object.keys(XHR.defaults).forEach(function (defaultPropName) {
		if(typeof this[defaultPropName] == "object") {
	        _append(options[defaultPropName], this[defaultPropName]);
	    }
    }, XHR.defaults);

    var xhr = new XMLHttpRequest()
      , data = options["data"] || ""
      , isPOST = options["method"].toLowerCase() == "post"
      , url = options["uri"]
    ;

    if(typeof data == "object" && options == XHR.defaults["headers"]["Content-Type"]) {
    	data = Object.keys(data).map(function(a) {
    		var value = this[a];
    		if(typeof value == "object") {
    			return Object.keys(value).map(function(b) {
    				return a + "=" + this[b];
    			}, value).join("&")
    		}

	    	return a + "=" + value;
	    }, data).join("&");
    }
    
    xhr.onreadystatechange = function () {
        if (this.readyState === 4) {
        	callback.call(this, null/*error*/, this.response || this.responseText || this.responseXML);            
        }
    }

    xhr.onerror = function (evt) {
        callback.call(this, evt)
    }

    try {
	    xhr.open(options["method"], url + (isPOST ? "" : (url.indexOf("?") ? "&" : "?" + data)));
	    if (options["headers"]) {
	        Object.keys(options["headers"]).forEach(function (key) {
	            xhr.setRequestHeader(key, options["headers"][key])
	        })
	    }

	    xhr.send(isPOST && data || null);
	}
	catch (e) {
		callback.call(this, e);
	}

    return xhr;
}
XHR.defaults = {
	"headers" : {
		"X-Requested-With" : "HTTPRequest",
		"Content-Type" : "application/x-www-form-urlencoded;charset=utf-8"
	}
};

}//if(__GCC__INCLUDE_EXTRAS__)


/*  >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  Network  ======================================  */
/*  =======================================================================================  */

/*  ======================================================================================  */


/*  =======================================================================================  */
/*  ========================================  Date  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<  */
//
// Date
// ====
//

if(__GCC__SCRIPT_BUGFIXING__ && __GCC__SCRIPT_BUGFIXING_DATE__) {
// ES5 15.9.5.43
// http://es5.github.com/#x15.9.5.43
// This function returns a String value represent the instance in time
// represented by this Date object. The format of the String is the Date Time
// string format defined in 15.9.1.15. All fields are present in the String.
// The time zone is always UTC, denoted by the suffix Z. If the time value of
// this object is not a finite Number a RangeError exception is thrown.
if(!_Native_Date.prototype.toISOString || (_String_contains_.call(new _Native_Date(_Shimed_Date_test_negDate).toISOString(), _Shimed_Date_test_yearStr)) || (new _Native_Date(-1).toISOString() !== '1969-12-31T23:59:59.999Z'))
    _Native_Date.prototype.toISOString = function() {
        var result,
        	length,
        	value,
        	year,
        	month;

        if (!isFinite(this)) {
            throw new RangeError("Date.prototype.toISOString called on non-finite value.");
        }

        year = this.getUTCFullYear();

        month = this.getUTCMonth();
        // see https://github.com/kriskowal/es5-shim/issues/111
        year += ~~(month / 12);
        month = (month % 12 + 12) % 12;

        // the date time string format is specified in 15.9.1.15.
        result = [month + 1, this.getUTCDate(),
            this.getUTCHours(), this.getUTCMinutes(), this.getUTCSeconds()];
        year = (year < 0 ? '-' : (year > 9999 ? '+' : '')) + ('00000' + Math.abs(year)).slice(0 <= year && year <= 9999 ? -4 : -6);

        length = result.length;
        while (length--) {
            value = result[length];
            // pad months, days, hours, minutes, and seconds to have two digits.
            if (value < 10) {
                result[length] = "0" + value;
            }
        }
        // pad milliseconds to have three digits.
        return year + "-" + result.slice(0, 2).join("-") + "T" + result.slice(2).join(":") + "." +
            ("000" + this.getUTCMilliseconds()).slice(-3) + "Z";
    }

// ES5 15.9.4.4
// http://es5.github.com/#x15.9.4.4
if(!_Native_Date.now)_Native_Date.now = function() {
	return new _Native_Date().getTime();
};

// ES5 15.9.5.44
// http://es5.github.com/#x15.9.5.44
// This function provides a String representation of a Date object for use by
// JSON.stringify (15.12.3).
if(!_Native_Date.prototype.toJSON || _String_contains_.call((new _Native_Date(_Shimed_Date_test_negDate)).toJSON(), _Shimed_Date_test_yearStr) ||
    ~(function() {
        // is Date.prototype.toJSON non-generic?
        try {
            return _Native_Date.prototype.toJSON.call({toISOString:function(){return -1;}});
        } catch (err) {}
    }())) {
_Native_Date.prototype.toJSON = function(key) {
	// When the toJSON method is called with argument key, the following
	// steps are taken:

	// 1.  Let O be the result of calling ToObject, giving it the this
	// value as its argument.
	// 2. Let tv be ToPrimitive(O, hint Number).
	// 3. If tv is a Number and is not finite, return null.
	// XXX
	// 4. Let toISO be the result of calling the [[Get]] internal method of
	// O with argument "toISOString".
	// 5. If IsCallable(toISO) is false, throw a TypeError exception.
	// In "_call_function"
	// 6. Return the result of calling the [[Call]] internal method of
	//  toISO with O as the this value and an empty argument list.
	return _call_function(this.toISOString, this);

	// NOTE 1 The argument is ignored.

	// NOTE 2 The toJSON function is intentionally generic; it does not
	// require that its this value be a Date object. Therefore, it can be
	// transferred to other kinds of objects for use as a method. However,
	// it does require that any such object have a toISOString method. An
	// object is free to use the argument key to filter its
	// stringification.
};
}

// ES5 15.9.4.2
// http://es5.github.com/#x15.9.4.2
// based on work shared by Daniel Friesen (dantman)
// http://gist.github.com/303249
/*
OR
https://github.com/csnover/js-iso8601
https://raw.github.com/csnover/js-iso8601/master/iso8601.min.js
?
*/
if (!_Native_Date.parse/* || "Date.parse is buggy"*/) {
    // Date.length === 7
    _Shimed_Date = function(Y, M, D, h, m, s, ms) {
        var length = arguments.length;
        if (this instanceof _Native_Date) {
            var date = length == 1 && String(Y) === Y ? // isString(Y)
                // We explicitly pass it through parse:
                new _Native_Date(_Shimed_Date.parse(Y)) :
                // We have to manually make calls depending on argument
                // length here
                length >= 7 ? new _Native_Date(Y, M, D, h, m, s, ms) :
                length >= 6 ? new _Native_Date(Y, M, D, h, m, s) :
                length >= 5 ? new _Native_Date(Y, M, D, h, m) :
                length >= 4 ? new _Native_Date(Y, M, D, h) :
                length >= 3 ? new _Native_Date(Y, M, D) :
                length >= 2 ? new _Native_Date(Y, M) :
                length >= 1 ? new _Native_Date(Y) :
                              new _Native_Date();
            // Prevent mixups with unfixed Date object
            date.constructor = _Shimed_Date;
            return date;
        }
        return _Native_Date.apply(this, arguments);
    };

    // 15.9.1.15 Date Time String Format.
    _Shimed_Date_isoDateExpression = new RegExp("^" +
        "(\\d{4}|[\+\-]\\d{6})" + // four-digit year capture or sign + 6-digit extended year
        "(?:-(\\d{2})" + // optional month capture
        "(?:-(\\d{2})" + // optional day capture
        "(?:" + // capture hours:minutes:seconds.milliseconds
            "T(\\d{2})" + // hours capture
            ":(\\d{2})" + // minutes capture
            "(?:" + // optional :seconds.milliseconds
                ":(\\d{2})" + // seconds capture
                "(?:\\.(\\d{3}))?" + // milliseconds capture
            ")?" +
        "(" + // capture UTC offset component
            "Z|" + // UTC capture
            "(?:" + // offset specifier +/-hours:minutes
                "([-+])" + // sign capture
                "(\\d{2})" + // hours offset capture
                ":(\\d{2})" + // minutes offset capture
            ")" +
        ")?)?)?)?" +
    "$");

	_Shimed_Date_monthes = [0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334, 365];

	// Returns count of leap years before "year" since 0 CE
	_Shimed_Date_leapYears = function(year) {
	    return Math.ceil(year / 4) - Math.ceil(year / 100) + Math.ceil(year / 400);
	}

	// Copy any custom methods a 3rd party library may have added
	for (_tmp_ in _Native_Date) {
	    _Shimed_Date[_tmp_] = _Native_Date[_tmp_];
	}

	// Copy "native" methods explicitly; they may be non-enumerable
	_Shimed_Date.now = _Native_Date.now;
	_Shimed_Date.UTC = _Native_Date.UTC;
	_Shimed_Date.prototype = _Native_Date.prototype;
	_Shimed_Date.prototype.constructor = _Shimed_Date;

	// Upgrade Date.parse to handle simplified ISO 8601 strings
	_Shimed_Date.parse = function parse(string) {
	    var match = _Shimed_Date_isoDateExpression.exec(string);
	    if (match) {
	        // parse months, days, hours, minutes, seconds, and milliseconds
	        // provide default values if necessary
	        // parse the UTC offset component
	        var year = Number(match[1]),
	            month = Number(match[2] || 1),
	            day = Number(match[3] || 1),
	            hour = Number(match[4] || 0),
	            minute = Number(match[5] || 0),
	            second = Number(match[6] || 0),
	            millisecond = Number(match[7] || 0),
	            // When time zone is missed, local offset should be used (ES 5.1 bug)
	            // see https://bugs.ecmascript.org/show_bug.cgi?id=112
	            offset = match[8] ? 0 : Number(new Date(1970, 0)),
	            signOffset = match[9] === "-" ? 1 : -1,
	            hourOffset = Number(match[10] || 0),
	            minuteOffset = Number(match[11] || 0),    
	            leapYears0 = _Shimed_Date_leapYears(year),
	            leapYears1 = _Shimed_Date_leapYears(year + 1),
	            result;

	        if (hour < (minute > 0 || second > 0 || millisecond > 0 ? 24 : 25) &&
	            minute < 60 &&
	            second < 60 &&
	            millisecond < 1000 &&
	            hourOffset < 24 && // detect invalid offsets
	            minuteOffset < 60 &&
	            month > 0 &&
	            month < 13 &&
	            day > 0 &&
	            day < (1 + _Shimed_Date_monthes[month] - _Shimed_Date_monthes[month - 1] + (month === 2 ? leapYears1 - leapYears0 : 0))) {

		            result = 365 * (year - 1970) + (month > 2 ? leapYears1 : leapYears0) - _Shimed_Date_leapYears(1970) + _Shimed_Date_monthes[month - 1] + day - 1;
		            result = (((result * 24 + hour + hourOffset * signOffset) * 60 + minute + minuteOffset * signOffset) * 60 + second) * 1000 + millisecond + offset;
		            if (-8.64e15 <= result && result <= 8.64e15) {
		                return result;
		            }
		            
	        }
	        return NaN;
	    }
	    return _Native_Date.parse.apply(this, arguments);
	};

    global["Date"] = _Shimed_Date;
}
}//if __GCC__SCRIPT_BUGFIXING__
/*  ======================================================================================  */
/*  >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  Date  =====================================  */


/*  =======================================================================================  */
/*  ========================================  DEBUG  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<  */

if(DEBUG) {
// friendly console
// http://habrahabr.ru/blogs/javascript/116852/
// https://github.com/theshock/console-cap/blob/master/console.js
// 21.02.2012 Update with CHANGES!!!
(function (console) {

var i,
	methods = ['assert','count','debug','dir','dirxml','error','group','groupCollapsed','groupEnd','info','log','markTimeline','profile','profileEnd','table','time','timeEnd','trace','warn'],
	empty   = {},
	timeCounters;

for (i = methods.length; i--;) empty[methods[i]] = functionReturnFirstParam;

if (console) {

	if (!console.time) {
		timeCounters = {};

		console.time = function(name, reset){
			if (name) {
				var time = +new Date, key = "KEY" + name.toString();
				if (reset || !timeCounters[key]) timeCounters[key] = time;
			}
		};

		console.timeEnd = function(name){
			var diff,
				time = +new Date,
				key = "KEY" + name.toString(),
				timeCounter = timeCounters[key];

			if (timeCounter) {
				diff  = time - timeCounter;
				console.info( name + ": " + diff + "ms" );
				delete timeCounters[key];
			}
			return diff;
		};
	}

	for (i = methods.length; i--;) {
		console[methods[i]] = methods[i] in console ?
			_unsafe_Function_bind_.call(console[methods[i]], console) : functionReturnFirstParam;
	}
	console.disable = function () { global.console = empty;   };
	  empty.enable  = function () { global.console = console; };

	empty.disable = console.enable = functionReturnFirstParam;

} else {
	console = global.console = empty;
	console.disable = console.enable = functionReturnFirstParam;
}

methods = void 0;

})( typeof console === 'undefined' ? null : console );

}//if(DEBUG)

/*  ======================================================================================  */
/*  >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  DEBUG  =====================================  */


//apply IE lt 9 shims
if((_tmp_ = global["_"]) && _tmp_["ielt9shims"]) {
	_tmp_["ielt9shims"].forEach(_call_function);
	//Restore original "_" or set "_" to undefined
	global["_"] = _tmp_["orig_"];
}

// no need any more
_append = _tmp_ = _testElement = nodeList_methods_fromArray = _document_createElement = _Event =
	_CustomEvent = _Event_prototype = _Custom_Event_prototype = 
	_Element_prototype = _Shimed_Date = functionReturnFalse = void 0;

if(!__GCC__INCLUDE_EXTRAS__) {
	if(!definePropertyWorksOnObject) {
		Object.defineProperty = null;
		delete Object.defineProperty;
	}
}



})(window);
