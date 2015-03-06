/// <reference path="../../includes.ts"/>
module ApimanConfiguration {

    export var _module = angular.module("ApimanConfiguration", []);

    export var Configuration = _module.factory('Configuration', ['$window',
        function($window) {
            return $window.APIMAN_CONFIG_DATA;
        }]);

}
