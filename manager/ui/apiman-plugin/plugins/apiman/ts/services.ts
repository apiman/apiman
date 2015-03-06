/// <reference path="../../includes.ts"/>
module ApimanServices {

    export var _module = angular.module("ApimanServices", ['ngResource', 'ApimanConfiguration']);

    export var ApimanServices = _module.factory('ApimanSvcs', ['$resource', 'Configuration',
        function($resource, Configuration) {
            var endpoint = Configuration.api.endpoint + '/apiman/:entityType';
            return $resource(endpoint,
                { entityType: '@entityType' });
        }]);

    export var UserServices = _module.factory('UserSvcs', ['$resource', 'Configuration',
        function($resource, Configuration) {
            var endpoint = Configuration.api.endpoint + '/apiman/users/admin/:entityType';
            return $resource(endpoint,
                { entityType: '@entityType' });
        }]);

    export var OrganizationServices = _module.factory('OrgSvcs', ['$resource', 'Configuration',
        function($resource, Configuration) {
            var endpoint = Configuration.api.endpoint + '/apiman/organizations/:organizationId/:entityType/:entityId/:versionsOrActivity/:version/:policiesOrActivity/:policyId';
            return $resource(endpoint,
                { organizationId: '@organizationId', entityType: '@entityType', entityId: '@entityId', versionsOrActivity: '@versionsOrActivity', version: '@version', policiesOrActivity: '@policiesOrActivity', policyId: '@policyId' });
        }]);

}
