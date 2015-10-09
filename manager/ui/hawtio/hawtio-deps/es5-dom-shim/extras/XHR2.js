/*
Over fings:
1. sendAsBinary shim for browsers with native Uint8Array http://javascript0.org/wiki/Portable_sendAsBinary
2. http://habrahabr.ru/post/120917/ (Новые возможности XMLHttpRequest2)

TODO::
1. https://raw.github.com/vjeux/jDataView/master/src/jdataview.js  (http://habrahabr.ru/post/144008/)
2. http://readd.ru/read.php?aHR0cDovL2hhYnJhaGFici5ydS9ibG9ncy9odG1sNS8xMzMzNTEvXn5e0KXQsNCx0YDQsNGF0LDQsdGAXn5eMjUuMTEuMTEgMDE6NTg= (Воркараунд xhr.send(Blob) для Opera)
*/

if(typeof FormData == "undefined") {

!function(global) {

/** @const */
var UUID_PREFIX = "m392bhj0d" + (Math.random() * 9e9);


var _XMLHttpRequest_ = global.XMLHttpRequest

	/** type {Object} oroginal XHR */
  , XHR = _XMLHttpRequest_ || function() {
		var xhr = new ActiveXObject("Microsoft.XMLHTTP"),
			_xhr_send = xhr.send;
		
		xhr.send = function() {
			//Fixes IE Caching problem
	        this.setRequestHeader("If-Modified-Since", "Sat, 1 Jan 2000 00:00:00 GMT");//TODO:: tests
	        _xhr_send.apply(this, arguments);
		}
	}

  , UUID = 0

  , _Document = global["Document"] || global["HTMLDocument"]

  , _ArrayBuffer = global["ArrayBuffer"]

  , IEBinaryToArray_ByteStr__NAME = UUID_PREFIX + "IEBinaryToArray_ByteStr";
;
function convertResponseBodyToText(byteArray) {
	// http://jsperf.com/vbscript-binary-download/6
	var scrambledStr;
	try {
		scrambledStr = global[IEBinaryToArray_ByteStr__NAME](byteArray);
	} catch (e) {
		// http://stackoverflow.com/questions/1919972/how-do-i-access-xhr-responsebody-for-binary-data-from-javascript-in-ie
		// http://miskun.com/javascript/internet-explorer-and-binary-files-data-access/
		var IEBinaryToArray_ByteStr_Script =
			"Function " + IEBinaryToArray_ByteStr__NAME + "\r\n"+
			"	" + IEBinaryToArray_ByteStr__NAME + " = CStr(Binary)\r\n"+
			"End Function\r\n"+
			"Function " + IEBinaryToArray_ByteStr__NAME + "_Last(Binary)\r\n"+
			"	Dim lastIndex\r\n"+
			"	lastIndex = LenB(Binary)\r\n"+
			"	if lastIndex mod 2 Then\r\n"+
			"		" + IEBinaryToArray_ByteStr__NAME + "_Last = AscB( MidB( Binary, lastIndex, 1 ) )\r\n"+
			"	Else\r\n"+
			"		" + IEBinaryToArray_ByteStr__NAME + "_Last = -1\r\n"+
			"	End If\r\n"+
			"End Function\r\n";

		// http://msdn.microsoft.com/en-us/library/ms536420(v=vs.85).aspx
		// proprietary IE function
		window.execScript(IEBinaryToArray_ByteStr_Script, 'vbscript');

		scrambledStr = global[IEBinaryToArray_ByteStr__NAME](byteArray);
	}

	var lastChr = global[IEBinaryToArray_ByteStr__NAME + "_Last"](byteArray),
	result = "",
	i = 0,
	l = scrambledStr.length % 8,
	thischar;
	while (i < l) {
		thischar = scrambledStr.charCodeAt(i++);
		result += String.fromCharCode(thischar & 0xff, thischar >> 8);
	}
	l = scrambledStr.length
	while (i < l) {
		result += String.fromCharCode(
			(thischar = scrambledStr.charCodeAt(i++), thischar & 0xff), thischar >> 8,
			(thischar = scrambledStr.charCodeAt(i++), thischar & 0xff), thischar >> 8,
			(thischar = scrambledStr.charCodeAt(i++), thischar & 0xff), thischar >> 8,
			(thischar = scrambledStr.charCodeAt(i++), thischar & 0xff), thischar >> 8,
			(thischar = scrambledStr.charCodeAt(i++), thischar & 0xff), thischar >> 8,
			(thischar = scrambledStr.charCodeAt(i++), thischar & 0xff), thischar >> 8,
			(thischar = scrambledStr.charCodeAt(i++), thischar & 0xff), thischar >> 8,
			(thischar = scrambledStr.charCodeAt(i++), thischar & 0xff), thischar >> 8);
	}
	if (lastChr > -1) {
		result += String.fromCharCode(lastChr);
	}
	return result;
};

if("DOMParser" in blobal) {
	//DOMParser HTML extension - Now a polyfill since HTML parsing was added to the DOMParser specification
	//https://gist.github.com/1129031
	(function(real_parseFromString) {
		// Firefox/Opera/IE throw errors on unsupported types
		try {
			// WebKit returns null on unsupported types
			if ((new DOMParser).parseFromString("", "text/html")) {
				// text/html parsing is natively supported
				return;
			}
		} catch (ex) {}

		DOMParser.prototype.parseFromString = function(markup, type) {
			if (/^\s*text\/html\s*(?:;|$)/i.test(type)) {
				var
				  doc = document.implementation.createHTMLDocument("")
				, doc_elt = doc.documentElement
				, first_elt
				;

				doc_elt.innerHTML = markup;
				first_elt = doc_elt.firstElementChild;

				if ( // are we dealing with an entire document or a fragment?
					   doc_elt.childElementCount === 1
					&& first_elt.localName.toLowerCase() === "html"
				) {
					doc.replaceChild(first_elt, doc_elt);
				}

				return doc;
			} else {
				return real_parseFromString.apply(this, arguments);
			}
		};
	})(DOMParser.prototype.parseFromString);
}
else if(_XMLHttpRequest_) {//Old Safary
	//Do we REALY need this?
	//https://sites.google.com/a/van-steenbeek.net/archive/explorer_domparser_parsefromstring : part from 'if(typeof(XMLHttpRequest) != 'undefined') '
}
else {//IE < 9
	global["DOMParser"] = function _DOMParser(){};
	_DOMParser.prototype.parseFromString = function(markup, type) {
		if(/xml$/.test(type)) {
			//Only for XML http://erik.eae.net/archives/2005/07/03/20.19.18/
			var xml = new ActiveXObject("Microsoft.XMLDOM");
			xml.async = "false";
			xml.loadXML(str);
			return xml
		}
		else {
			//TODO::
			// 1. iframe | side effect: I. <script> will run (but we can do some string.replace work). II. Images will load (realy?)
			throw new Error("Unsupported for now")
		}
	}
}


/**
 * https://raw.github.com/francois2metz/html5-formdata/master/formdata.js
 * Emulate FormData for some browsers
 * MIT License
 * (c) 2010 FranÃ§ois de Metz
 */
/*
/*
Using:
if (data.fake) {
   xhr.setRequestHeader("Content-Type", "multipart/form-data; boundary="+ data.boundary);
   xhr.sendAsBinary(data.toString());
}

Only for FF3.6

if(!("FormData" in global)) {
	(function(_orig_XMLHttpRequest_send) {
		var _FormData = global["FormData"] = function() {
	        this["fake"] = true;
	        this["boundary"] = "--------FormData" + Math.random();
	        this["__fields__"] = [];
	    };
	    _FormData.prototype["append"] = function(key, value) {
	        this["__fields__"].push([key, value]);
	    };
	    _FormData.prototype.toString = function() {
	        var boundary = this["boundary"],
	        	body = "";

	        this["__fields__"].forEach(function(field) {
	            body += "--" + boundary + "\r\n";
	            // file upload
	            if ("name" in field[1]) {
	                var file = field[1];
	                body += "Content-Disposition: form-data; name=\""+ field[0] +"\"; filename=\""+ file["name"] +"\"\r\n";
	                body += "Content-Type: "+ file["type"] +"\r\n\r\n";
	                body += file["getAsBinary"]() + "\r\n";
	            } else {
	                body += "Content-Disposition: form-data; name=\""+ field[0] +"\";\r\n\r\n";
	                body += field[1] + "\r\n";
	            }
	        });
	        body += "--" + boundary +"--";
	        return body;
	    };

    	XMLHttpRequest.prototype.send = function(data) {
    		if(data instanceof _FormData)arguments[0] = data + "";

    		return  _orig_XMLHttpRequest_send.apply(this, arguments);
    	}
    })(XMLHttpRequest.prototype.send)
    
}

//http://otvety.google.ru/otvety/thread?tid=254a35ce8ae1ca92&pli=1
//FromData для оперы. XMLHttpRequest
encodeFormData: function(data) {
  if(!data) return false;
    var pairs = [],
          regexp = /%20/g;

    for(var name in data) {
        var value = data[name].toString(),
        pair = encodeURIComponent(name).replace(regexp, '+') + '=' + encodeURIComponent(value).replace(regexp, '+');

        pairs.push(pair);
    }

    return pairs.join('&');
}
*/
/**
 * https://developer.mozilla.org/en/XMLHttpRequest/FormData
 * https://developer.mozilla.org/en/DOM/XMLHttpRequest/FormData/Using_FormData_Objects
 * @constructor
 */
function _FormData(_form) {
	var options = this["_options"] = {};

	this._filesCount = 0;
	this._pairs = {};
	
	if(_form) {
		for(var i = 0, l = _form.length ; i < l ; i++) {
			this["append"](_form[i].name, _form[i].value, _form[i].type, _form[i]);
		};

		["method", "action", "enctype", "accept-charset"].forEach(function(attr, attrValue) {
			if((attrValue = _form.getAttribute(attr)) != null) {
				options[attr] = attrValue;
			}
		}, this);
	}
}
_FormData.prototype = {
	constructor : _FormData
	, "append" : function(name, value) {
		//TODO:: if(value instanceof Blob) {}
		//TODO:: if(value instanceof File) {}

		if(arguments[2] === "file") {//input.type
			this["_file" + this._filesCount++] = arguments[3];
		}
		else {
			this._pairs[name] = value;
		}
	}
	, "_send" : function(_shim_XHR2_object, cors) {
		var _method = _shim_XHR2_object.__method__,
			_url = _shim_XHR2_object.__uri__,
			xhr = _shim_XHR2_object.XHR1,
			frame,
			frameId,
			prevs,
			i = 0,
			l = this._filesCount,
			fileInputs = l !== 0,
			fileInput,
			tmp,
			options = this["_options"],
			form,
			result = null;
		

		if(fileInputs) {
			if(cors) {
				throw new Error("CORS is unsupporting with FormData with file inputs");
			}

			frame = this.frame = document.createElement("iframe");
			frameId = frame.name = frame.id = UUID_PREFIX + ++UUID;
			form = document.createElement("form");
			prevs = [];

			Object.keys(options).forEach(function(attr) {
				form.setAttribute(attr, options[attr]);			
			})

			Object.keys(this._pairs).forEach(function(name) {
				var input = document.createElement("input"),
					value = this._pairs[name];

				input.type = "hidden";
				input.name = name;
				input.value = value;
				form.appendChild(input);
			}, this);

			frame.src = "about:blank";
			frame.style.display = "none";
			document.body.appendChild(frame);

			while(i < l) {
				fileInput = this["_file" + i];
				prevs[i * 2] = fileInput.parentNode;
				prevs[i * 2 + 1] = fileInput.nextSibling;
				form.appendChild(fileInput);
				i++;
			}

			if(_url)form.action = _url;
			if(_method)form.method = _method;

			form.target = frameId;
			frame["_onload"] = function() {
				try {
					_shim_XHR2_object._fakeFormDataWithFileInputs_ondone(this.contentDocument.documentElement.innerText);
				}
				catch(e) {
					_shim_XHR2_object._fakeFormDataWithFileInputs_error({"status" : 404});
				}			
				document.body.removeChild(frame);
				frame = null;
			}
			frame["_onerror"] = function() {
				_shim_XHR2_object._fakeFormDataWithFileInputs_error();
				document.body.removeChild(frame);
				frame = null;
			}
			frame.setAttribute("onload", "this._onload()");
			frame.setAttribute("onerror", "this._onerror()");
			
			//form.style = "visibility:hidden;position:absolute;top:0;left:0;height:0px;width:0px";
			form.style.display = "none";
			document.body.appendChild(form);
			form.submit();
			document.body.removeChild(form);

			i = 0;
			while(i < l) {
				fileInput = this["_file" + i];
				prevs[i * 2].insertBefore(fileInput, prevs[i * 2 + 1]);
				i++;
			}

			return xhr = _shim_XHR2_object.XHR1 = null;
		}
		else {
			if(_method == "GET") {
				xhr.open(_method, _url + '?' + _FormData.encodeSimpleFormData(this._pairs));
				xhr.setRequestHeader('Content-Type', '*/*');
			}
			else {
				xhr.open(_method, _url);
				xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
				result = _FormData.encodeSimpleFormData(this._pairs);
			}

			return result;
		}
	}
	, toString : function() {return "[object FormData]"}
};
_FormData.encodeSimpleFormData = function(data) {
	if(!data)return false;

	var pairs = [],
		regexp = /%20/g;

	for(var name in data) {
		var value = data[name].toString(),
		pair = encodeURIComponent(name).replace(regexp, '+') + '=' + encodeURIComponent(value).replace(regexp, '+');

		pairs.push(pair);
	}

	return pairs.join('&');
};

/*
 * https://developer.mozilla.org/en/XMLHttpRequest
 * @constructor

function _XMLHttpRequestUpload() {
	this.onabort = null;
	this.onerror = null;
	this.onload = null;
	this.onloadend = null;
	this.onloadstart = null;
	this.onprogress = null;
}
_XMLHttpRequestUpload.prototype = {
	constructor : _XMLHttpRequestUpload
	, addEventListener : function(a, b, c) {
		document.documentElement.addEventListener.call(this, a, b, c);
	}
	, dispatchEvent : function(e) {
		if(this["on" + e.type])this["on" + e.type](e);

		document.documentElement.dispatchEvent.call(this, e);
	}
	, removeEventListener : function(a, b, c) {
		document.documentElement.removeEventListener.call(this, a, b, c);
	}
}*/



function CORS_test(uri) {
	var _link = CORS_test.link || (CORS_test.link = document.createElement("a")),
		currentDomain = document.domain,
		currentProtocol = location.protocol,
		targetDomain,
		targetProtocol,
		result = {},
		_state;

	_link.href = uri;

	targetDomain = _link.hostname;
	targetProtocol = _link.protocol;

	if(targetDomain == currentDomain && targetProtocol == currentProtocol) {
		//No CORS
		return null
	}
	else if(targetProtocol == currentProtocol) {
		//CORS with diff domain, but same protocol
		/*if(targetDomain.substr(-currentDomain.length) == currentDomain) {//NOTE:: substr() must work with negative value as first parameter. FOR IE<9 use shim
			//CORS with target subdomain
			_state = 1;
			?????
		}
		else */
		if(currentDomain.substr(-targetDomain.length) == targetDomain) {
			//CORS with current subdomain
			_state = 1;
		}
		_state = 2;
	}
	else if(targetDomain == currentDomain) {
		_state = 3;
	}

	return {
		"state" : _state,
		"from" : currentDomain,
		"to" : targetDomain,
		"fromProtocol" : currentProtocol,
		"toProtocol" : targetProtocol
	};

	//TODO::
	//1. Test if uri in subdoment or super domen -> change document.domain
	//2. Test if we need CORS to another domain -> Use some CORS hack
}

function checkIsXML(elem) {
	var doc = elem.ownerDocument || elem;

	return !!doc['xmlVersion'] || //XHTML browsers
		!!doc['xml'] || //IE
		"createElement" in doc && doc.createElement("div").tagName == "div"//Major browsers. At least FF10+
	;
}



/**
 * @constructor
 */
function XMLHttpRequest2() {
	var xhr = this.XHR1 = new XHR;

	this._supportTimeOut = "timeout" in xhr;
	this._supportUpload = "upload" in xhr;

	/** @type {Function} */
	this.onreadystatechange	= null;
	/** @type {number} */
	this.readyState = xhr.readyState;
	/** @type {(Object|String|ArrayBuffer|Blob|Document)} */
	this.response = xhr.response || null;

	/** @type {String} */
	this.responseText = "";
	/** @type {String} */
	this.responseType = "";
	/** @type {Object} */
	this.responseXML = null;
	/** @type {number} */
	this.status = 0;
	/** @type {String} */
	this.statusText = "";

	/** @type {number} */
	this.timeout = 0;
	/** @type {boolean} */
	this.withCredentials = xhr.withCredentials || false;

	if(this._supportUpload)this.upload = xhr.upload;// || new _XMLHttpRequestUpload;//TODO::
}
XMLHttpRequest2.prototype = {
	constructor : XMLHttpRequest2
	, addEventListener : function(a, b, c) {
		document.documentElement.addEventListener.call(this, a, b, c);
	}
	, dispatchEvent : function(e) {
		document.documentElement.dispatchEvent.call(this, e);
	}
	, removeEventListener : function(a, b, c) {
		document.documentElement.removeEventListener.call(this, a, b, c);
	}
	/** @this {XMLHttpRequest2} */
	, _onreadystatechange : function(e) {
		var xhr = this.XHR1,
			s;

		if(this.timeout !== 0 && !this._supportTimeOut) {
			if(this._time <= Date.now() - this.timeout)this.XHR1.abort();
		}

		if ((this.readyState = xhr.readyState) == 4) {
			s = this.status = xhr.status;
			this.statusText = xhr.statusText || s + "";

			if(s === 200) {
				this.responseText = xhr.responseText;
				this.responseXML = xhr.responseXML;

				switch(xhr.responseType) {
					case "arraybuffer":
						// Array Buffer Firefox
						if ('mozResponseArrayBuffer' in xhr) {
							this.response = xhr.mozResponseArrayBuffer;
						}
						// Array Buffer Chrome
						else if ('responseType' in xhr && xhr.response) {
							this.response = xhr.response;
						}
						// Internet Explorer (Byte array accessible through VBScript -- convert to text)
						// http://stackoverflow.com/questions/1919972/how-do-i-access-xhr-responsebody-for-binary-data-from-javascript-in-ie/3050364
						else if ('responseBody' in xhr) {
							this.response = convertResponseBodyToText(xhr.responseBody);
						}
						// Older Browsers
						else {
							throw new Error("Can't get response as ArrayBuffer")
						}
					break;

					case "json":
						try {
							this.response = JSON.parse(xhr.responseText);
						}
						catch(e) {
							this.response = "";
						}
					break;

					case "blob":
						//TODO::
					break;

					case "document":
						this.response = xhr.responseXML;
						if(!xhr.responseXML) {
							s = document.createElement("iframe")
							//TODO::
						}
					break;

					case "text":
					default:
						this.response = xhr.responseText;						
				}
			}
			else {
				this._onerror(e);
			}

			this._onload(e);
		}

		if(this.onreadystatechange)this.onreadystatechange(e);
	}
	, _onload : function(e) {
		if(!e || e.type != "load")e = new Event("load");
		this.dispatchEvent(e);
	}
	, _onerror : function(e) {
		if(!e || e.type != "error")e = new Event("error");
		this.dispatchEvent(e);
	}
	, _ontimeout : function(e) {
		if(!this._supportTimeOut) {
			if(!e || e.type != "timeout")e = new Event("timeout");
			this.dispatchEvent(e);
		}
		else {
			if(!("dispatchEvent" in this.XHR1))this.dispatchEvent(e);
			else this.ontimeout(e);
		}
	}
	, _fakeFormDataWithFileInputs_ondone : function(response) {
		var xhr = this.XHR1 = {};
		xhr.readyState = 4;
		xhr.status = 200;
		xhr.responseText = xhr.response = xhr.responseBody = response;
		xhr.responseXML = null;
		xhr._onreadystatechange({});
	}
	, _fakeFormDataWithFileInputs_error : function(response) {
		var xhr = this.XHR1 = {};
		xhr.readyState = 4;
		xhr.status = response["status"] || -1;//error
		xhr.responseText = xhr.response = xhr.responseBody = "";
		xhr.responseXML = null;
		xhr._onreadystatechange({});
	}
	, _prepeareDocumentDataForSending : function(_document, xhr) {
			var isXML = checkIsXML(_document)
				, charset = _document['charset'] || _document['characterSet'] || "utf-8"//TODO:: test for default "utf-8"
				, headerString = 
					(checkIsXML(_document) ? "application/xml" : "text/html") + 
					"; charset=" + charset
				, serializedDocument
				, serializedHead
				, serializedBody
				, _boolean_temp
			;

			//http://stackoverflow.com/a/817225 //there is no standard that supports this(outerHTML), although IE 6+ and more recently several other browsers now support it
			serializedDocument = _document.documentElement[(_boolean_temp = ("outerHTML" in _document.documentElement)) ? 'outerHTML' : 'innerHTML'];
			if(!serializedDocument) {
				 //Note:: In IE we must do document.head = document.getElementsByTagName('head')[0] before
				serializedHead = _document.head[(_boolean_temp = ("outerHTML" in _document.head)) ? 'outerHTML' : 'innerHTML'];
				serializedDocument = _boolean_temp ? serializedHead : "<head>" + serializedHead + "</head>";
				serializedBody = _document.body[(_boolean_temp = ("outerHTML" in _document.body)) ? 'outerHTML' : 'innerHTML'];
				serializedDocument += _boolean_temp ? serializedBody : "<body>" + serializedBody + "</body>";

				//Note: In particular, if the document cannot be serialized an "InvalidStateError" exception is thrown.
				if(!(serializedHead + serializedBody))throw new Error("InvalidStateError");
			}
			else if(!_boolean_temp)serializedDocument = "<html>" + serializedDocument + "</html>";

			if(isXML) {//Do we realy need this???
				serializedDocument = '<?xml version="1.0" encoding="' + charset + '"?>' + serializedDocument;
			}

			//Need?:
			//Let the request entity body be the result of getting the innerHTML attribute on data converted to Unicode and encoded as encoding
			//http://www.webtoolkit.info/javascript-utf8.html

   			xhr.setRequestHeader("Content-Type", headerString);

		   	return serializedDocument;
	}
	, "abort" : function() {
		this.XHR1.abort.apply(this.XHR1, arguments);
	}
	, "getAllResponseHeaders" : function () {
		return this.XHR1.getAllResponseHeaders();
	}
	, "getResponseHeader" : function (header) {
		return this.XHR1.getResponseHeader(header);
	}
	, "open" : function(method, uri) {
		var rt = this.responseType;
		if(rt == "arraybuffer" || rt == "blob")
			if("overrideMimeType" in this.XHR1)this.XHR1.overrideMimeType('text\/plain; charset=x-user-defined');

		this.__method__ = method;
		this.__uri__ = uri;
		this.XHR1.open(method, uri);
	}
	, "overrideMimeType" : function() {
		/*
			if (xml.overrideMimeType) {
			    xml.overrideMimeType('text/plain; charset=x-user-defined');
			} else {
			    xml.setRequestHeader('Accept-Charset', 'x-user-defined');
			}
		*/
		this.XHR1.overrideMimeType.apply(this.XHR1, arguments);
	}
	, "setRequestHeader" : function() {
		this.XHR1.setRequestHeader.apply(this.XHR1, arguments);
	}
	/**
	 * @param {(ArrayBuffer|Blob|Document|String|FormData)} data
	 */
	, "send" : function(data) {
		var thisObj = this,
			xhr = thisObj.XHR1,
			cors = CORS_test(thisObj.__uri__);

		if(cors && cors["state"] > 1)throw new Error("CORS with defferent protocol or domain (except subdomain) currently unsupporting");

		xhr.responseType = thisObj.responseType;
		xhr.withCredentials = thisObj.withCredentials;
		xhr.timeout = +thisObj.timeout >>> 0;
		if(thisObj.timeout !== 0 && !thisObj._supportTimeOut)thisObj._time = Date.now();
		xhr.ontimeout = thisObj._ontimeout;

		xhr.onreadystatechange = this._onreadystatechange.bind(this);

		if(typeof data == "object") {
			if(data instanceof _FormData) {
				data = data._send(thisObj, cors);

				if(!thisObj.XHR1)return;
			}
			if(data instanceof _Document ||
			   !_Document && "nodeType" in data && data.nodeType === 9//IE lt 8
			   ) {
			   	data = _prepeareDocumentDataForSending(data, thisObj.XHR1);
			}
			if(data instanceof _ArrayBuffer) {
				//TODO::
				//FF https://developer.mozilla.org/en/DOM/XMLHttpRequest#sendAsBinary() ??????
			}
		}

		if(cors["state"] == 1) {
			document.domain = cors["to"];
		}
		thisObj.XHR1.send(data);
		if(cors["state"] == 1) {
			document.domain = cors["from"];
		}
	}
}

if(!("overrideMimeType" in new XHR))delete XMLHttpRequest2.prototype["overrideMimeType"];

//EXPORT
global["FormData"] = _FormData;
global["XMLHttpRequest"] = XMLHttpRequest2;
global["XMLHttpRequestUpload"] = _XMLHttpRequestUpload;


}(window);

}