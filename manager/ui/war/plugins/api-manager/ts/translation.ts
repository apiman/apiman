import angular = require("angular");

angular.module("ApimanTranslation", [])
    .factory('TranslationSvc', ['$window',
      function ($window) {
        return {
          translate: function (key, defaultValue) {
            var translation = undefined;
            if ($window.APIMAN_TRANSLATION_DATA && $window.APIMAN_TRANSLATION_DATA[key]) {
              translation = $window.APIMAN_TRANSLATION_DATA[key];
            } else {
              translation = defaultValue;
            }
            return translation;
          }
        }
      }]);

