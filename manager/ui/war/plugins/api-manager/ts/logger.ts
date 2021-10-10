import angular = require("angular");

const stringifyIfObject = function(candidate) {
    return (typeof candidate === 'object' || Array.isArray(candidate) ) ? angular.toJson(candidate, true) : candidate;
}

const _formatMessage = function(theArgs) {
    var now = new Date();
    var msg = theArgs[0];
    if (theArgs.length > 1) {
        for (var i = 1; i < theArgs.length; i++) {
            msg = msg.replace('{'+(i-1)+'}', stringifyIfObject(theArgs[i]));
        }
    } else {
        msg = stringifyIfObject(msg);
    }

    return 'apiman [' + now.toLocaleTimeString() + ']>>  ' + msg;
};

angular.module("ApimanLogger", [])
    .factory('Logger', [
    function() {
        return {
            debug: function() {
                console.debug(_formatMessage(arguments));
            },
            info: function() {
                console.info(_formatMessage(arguments));
            },
            log: function() {
                console.info(_formatMessage(arguments));
            },
            warn: function() {
                console.warn(_formatMessage(arguments));
            },
            error: function() {
                console.error(_formatMessage(arguments));
            }
        };
    }]);

