/// <reference path="../../includes.ts"/>
module ApimanTranslation {

    export var _module = angular.module("ApimanTranslation", []);

    export var Translation = _module.factory('TranslationService', ['$window',
        function($window) {
            return {
                translate: function(key, defaultValue) {
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

}
