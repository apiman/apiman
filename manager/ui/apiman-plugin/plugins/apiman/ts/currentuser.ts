/// <reference path='../../includes.ts'/>
module ApimanCurrentUser {

    export var _module = angular.module('ApimanCurrentUser', ['ApimanServices']);

    export var CurrentUser = _module.factory('CurrentUser', ['UserSvcs',
        function(UserSvcs) {
            return {};
        }]);

}
