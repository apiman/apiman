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
            var endpoint = Configuration.api.endpoint + '/apiman/users/:user/:entityType';
            return $resource(endpoint,
                { user: '@user', entityType: '@entityType' });
        }]);

    export var OrganizationServices = _module.factory('OrgSvcs', ['$resource', 'Configuration',
        function($resource, Configuration) {
            var endpoint = Configuration.api.endpoint + '/apiman/organizations/:organizationId/:entityType/:entityId/:versionsOrActivity/:version/:policiesOrActivity/:policyId/:policyChain';
            return $resource(endpoint,
                {
                    organizationId: '@organizationId', 
                    entityType: '@entityType', 
                    entityId: '@entityId', 
                    versionsOrActivity: '@versionsOrActivity', 
                    version: '@version', 
                    policiesOrActivity: '@policiesOrActivity', 
                    policyId: '@policyId', 
                    chain: '@policyChain',
                    
                    page: '@page',
                    count: '@count'
                }, {
                update: {
                  method: 'PUT' // update issues a PUT request
                }});
        }]);

    export var CurrentUserServices = _module.factory('CurrentUserSvcs', ['$resource', 'Configuration',
        function($resource, Configuration) {
            var endpoint = Configuration.api.endpoint + '/apiman/currentuser/:what';
            return $resource(endpoint,
                { entityType: '@what' }, {
                update: {
                  method: 'PUT' // this method issues a PUT request
                }});
        }]);

    export var ActionServices = _module.factory('ActionSvcs', ['$resource', 'Configuration',
        function($resource, Configuration) {
            var endpoint = Configuration.api.endpoint + '/apiman/actions';
            return $resource(endpoint);
        }]);

    export var AuditServices = _module.factory('AuditSvcs', ['$resource', 'Configuration',
        function($resource, Configuration) {
            var endpoint = Configuration.api.endpoint + '/apiman/organizations/:organizationId/:entityType/:entityId/activity';
            return $resource(endpoint,
                {
                    organizationId: '@organizationId',
                    entityType: '@entityType',
                    entityId: '@entityId',
                    
                    page: '@page',
                    count: '@count'
                });
        }]);

    export var UserAuditServices = _module.factory('UserAuditSvcs', ['$resource', 'Configuration',
        function($resource, Configuration) {
            var endpoint = Configuration.api.endpoint + '/apiman/users/:user/activity';
            return $resource(endpoint,
                {
                    user: '@user',
                    
                    page: '@page',
                    count: '@count'
                });
        }]);

    
    export var PluginServices = _module.factory('PluginSvcs', ['$resource', 'Configuration',
        function($resource, Configuration) {
            return {
                getPolicyForm: function(pluginId, policyDefId, handler, errorHandler) {
                    var endpoint = Configuration.api.endpoint + '/apiman/plugins/:pluginId/policyDefs/:policyDefId/form';
                    $resource(endpoint, { pluginId: '@pluginId', policyDefId: '@policyDefId' }).get(
                        {pluginId: pluginId, policyDefId: policyDefId}, 
                        handler, errorHandler);
                }
            }
        }]);

}
