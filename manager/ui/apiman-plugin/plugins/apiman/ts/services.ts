/// <reference path="../../includes.ts"/>
module ApimanServices {

    export var _module = angular.module("ApimanServices", ['ngResource', 'ApimanConfiguration']);

    export var ApimanServices = _module.factory('ApimanSvcs', ['$resource', 'Configuration',
        function($resource, Configuration) {
            var endpoint = Configuration.api.endpoint + '/apiman/:entityType/:secondaryType';
            return $resource(endpoint,
                { entityType: '@entityType', secondaryType: '@secondaryType' }, {
                update: {
                  method: 'PUT' // this method issues a PUT request
                }});
        }]);

    export var UserServices = _module.factory('UserSvcs', ['$resource', 'Configuration',
        function($resource, Configuration) {
            var endpoint = Configuration.api.endpoint + '/apiman/users/' + Configuration.user.username + '/:entityType';
            return $resource(endpoint,
                { entityType: '@entityType' });
        }]);

    export var OrganizationServices = _module.factory('OrgSvcs', ['$resource', 'Configuration',
        function($resource, Configuration) {
            var endpoint = Configuration.api.endpoint + '/apiman/organizations/:organizationId/:entityType/:entityId/:versionsOrActivity/:version/:policiesOrActivity/:policyId/:policyChain';
            return $resource(endpoint,
                { organizationId: '@organizationId', entityType: '@entityType', entityId: '@entityId', versionsOrActivity: '@versionsOrActivity', version: '@version', policiesOrActivity: '@policiesOrActivity', policyId: '@policyId', chain: '@policyChain' });
        }]);

    export var CurrentUserServices = _module.factory('CurrentUserSvcs', ['$resource', 'Configuration',
        function($resource, Configuration) {
            var endpoint = Configuration.api.endpoint + '/apiman/currentuser/:what';
            return $resource(endpoint,
                { entityType: '@what' });
        }]);

    export var ActionServices = _module.factory('ActionSvcs', ['$resource', 'Configuration',
        function($resource, Configuration) {
            var endpoint = Configuration.api.endpoint + '/apiman/actions';
            return $resource(endpoint);
        }]);

}
