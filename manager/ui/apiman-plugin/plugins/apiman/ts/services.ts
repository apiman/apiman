/// <reference path="../../includes.ts"/>
module ApimanServices {

    export var _module = angular.module("ApimanServices", ['ngResource']);

    export var ActionServices = _module.factory('ActionServices', ['$resource',
        function($resource) {
            return $resource('http://127.0.0.1/apiman/actions');
        }]);

    export var UserServices = _module.factory('UserSvcs', ['$resource',
        function($resource) {
            return $resource('http://127.0.0.1/apiman/users/admin/:entityType',
                { entityType: '@entityType' });
        }]);

    export var OrganizationServices = _module.factory('OrgSvcs', ['$resource',
        function($resource) {
            return $resource('http://127.0.0.1/apiman/organizations/:organizationId/:entityType/:entityId/:versionsOrActivity/:version',
                { organizationId: '@organizationId', entityType: '@entityType', entityId: '@entityId', versionsOrActivity: '@versionsOrActivity', version: '@version' });
        }]);

}