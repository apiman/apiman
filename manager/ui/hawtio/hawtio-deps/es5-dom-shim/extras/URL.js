
;(function(global) {

	//Mini alternative https://gist.github.com/2428561
	
	//Uses
	//https://gist.github.com/1384398
	//https://gist.github.com/1235332

	function parseSearch(s) {
		var result = [];
		var k = 0;
		var parts = s.slice(1).split('&');
		for (var i = 0; i < parts.length; i++) {
		  var part = parts[i];
		  var key = part.split('=', 1)[0];
		  if (key) {
			var value = part.slice(key.length + 1);
			result[k++] = [key, value];
		  }
		}
		return result;
	}

	function serializeParsed(array) {
		return array.map(function(pair) {
		  return pair[1] !== '' ? pair.join('=') : pair[0];
		}).join('&');
	}

	function _URL(url, base) {
		if(this === global && this === null)
			throw new TypeError();
			//throw new TypeError("DOM object constructor cannot be called as a function");

		if (!url)
			throw new TypeError('Invalid argument');

		var thisObj = this,
			//https://gist.github.com/1235332
			m = String(url).replace(/^\s+|\s+$/g, '').match(/^([^:\/?#]+:)?(\/\/(?:[^:@]*(?::[^:@]*)?@)?(([^:\/?#]*)(?::(\d*))?))?([^?#]*)(\?[^#]*)?(#[\s\S]*)?/);

		thisObj.href = m[0];
		if (thisObj.href === '')
			throw new TypeError('Invalid URL');
		thisObj.protocol = m[1] || "";
		thisObj.authority = m[2] || "";
		thisObj.host = m[3] || "";
		thisObj.hostname = m[4] || "";
		thisObj.port = m[5] || "";
		thisObj.pathname = m[6] || "";
		thisObj.search = m[7] || "";
		thisObj.hash = m[8] || "";
		
		var match;
		if ((match = thisObj.pathname.match(/\/([^\/]+)$/)))
			thisObj.filename = match[1];
		thisObj.filename = '';
		
		thisObj.parameterNames = Object.create(null);
		parseSearch(thisObj.search).map(function(pair) {
			return pair[0];
		}).filter(function(key) {
			if(key in thisObj.parameterNames)return false;
			thisObj.parameterNames[key] = true;
			return true;
		});
	}

	_URL.prototype = {
		toString: function() {
		  return this.href;
		},
  
    // NOT IMPLEMENTED
    // get username() {
    //   return this._anchorElement.username;
    // },
    // set username(value) {
    //   this._anchorElement.username = value;
    // },

    // get password() {
    //   return this._anchorElement.password;
    // },
    // set password(value) {
    //   this._anchorElement.password = value;
    // },

    // get origin() {
    //   return this._anchorElement.origin;
    // },


		getParameter: function(name) {
		  return this.getParameterAll(name).pop();
		},

		getParameterAll: function(name) {
		  name = String(name);
		  var result = [];
		  var k = 0;
		  parseSearch(this.search).forEach(function(pair) {
			if (pair[0] === name)
			  result[k++] = pair[1];
		  });
		  return result;
		},

		appendParameter: function(name, values) {
		  if (!Array.isArray(values))
			values = [values];
		  var parsed = parseSearch(this.search);
		  for (var i = 0; i < values.length; i++) {
			parsed.push([name, values[i]]);
		  }
		  this.search = serializeParsed(parsed);
		},

		clearParameter: function(name) {
		  this.search = serializeParsed(
			  parseSearch(this.search).filter(function(pair) {
				return pair[0] !== name;
			  }));
		}
	};

	var oldURL = global.URL || global.webkitURL || global.mozURL;

	_URL.createObjectURL = function(blob) {
		return oldURL.createObjectURL.apply(oldURL, arguments);
	};

	_URL.revokeObjectURL = function(url) {
		return oldURL.revokeObjectURL.apply(oldURL, arguments);
	};
/*
  // Methods should not be enumerable.
  Object.defineProperty(_URL.prototype, 'toString', {enumerable: false});
  Object.defineProperty(_URL.prototype, 'getParameter', {enumerable: false});
  Object.defineProperty(_URL.prototype, 'getParameterAll', {enumerable: false});
  Object.defineProperty(_URL.prototype, 'appendParameter', {enumerable: false});
  Object.defineProperty(_URL.prototype, 'clearParameter', {enumerable: false});
  Object.defineProperty(_URL, 'createObjectURL', {enumerable: false});
  Object.defineProperty(_URL, 'revokeObjectURL', {enumerable: false});*/

 
//EXPORT
	global.URL = _URL;
  
})(window);