/// <reference path="../../includes.ts"/>
module ApimanServices {

    export var _module = angular.module("ApimanServices", ['ngResource', 'ApimanConfiguration']);
    
    var formatEndpoint = function(endpoint, params) {
        return endpoint.replace(/:(\w+)/g, function(match, key) {
            return params[key] ? params[key] : (':' + key);
        });
    };

    export var ApimanServices = _module.factory('ApimanSvcs', ['$resource', 'Configuration',
        function($resource, Configuration) {
            var endpoint = Configuration.api.endpoint + '/:entityType/:secondaryType';
            return $resource(endpoint,
                { entityType: '@entityType', secondaryType: '@secondaryType' }, {
                update: {
                  method: 'PUT' // this method issues a PUT request
                }});
        }]);

    export var UserServices = _module.factory('UserSvcs', ['$resource', 'Configuration',
        function($resource, Configuration) {
            var endpoint = Configuration.api.endpoint + '/users/:user/:entityType';
            return $resource(endpoint,
                { user: '@user', entityType: '@entityType' });
        }]);

    export var OrganizationServices = _module.factory('OrgSvcs', ['$resource', 'Configuration',
        function($resource, Configuration) {
            var endpoint = Configuration.api.endpoint + '/organizations/:organizationId/:entityType/:entityId/:versionsOrActivity/:version/:policiesOrActivity/:policyId/:policyChain';
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
            var endpoint = Configuration.api.endpoint + '/currentuser/:what';
            return $resource(endpoint,
                { entityType: '@what' }, {
                update: {
                  method: 'PUT' // this method issues a PUT request
                }});
        }]);

    export var ActionServices = _module.factory('ActionSvcs', ['$resource', 'Configuration',
        function($resource, Configuration) {
            var endpoint = Configuration.api.endpoint + '/actions';
            return $resource(endpoint);
        }]);

    export var AuditServices = _module.factory('AuditSvcs', ['$resource', 'Configuration',
        function($resource, Configuration) {
            var endpoint = Configuration.api.endpoint + '/organizations/:organizationId/:entityType/:entityId/activity';
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
            var endpoint = Configuration.api.endpoint + '/users/:user/activity';
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
                    var endpoint = Configuration.api.endpoint + '/plugins/:pluginId/policyDefs/:policyDefId/form';
                    $resource(endpoint, { pluginId: '@pluginId', policyDefId: '@policyDefId' }).get(
                        {pluginId: pluginId, policyDefId: policyDefId}, 
                        handler, errorHandler);
                }
            }
        }]);

    export var ServiceDefinitionServices = _module.factory('ServiceDefinitionSvcs', ['$resource', '$http', 'Configuration',
        function($resource, $http, Configuration) {
            return {
                getServiceDefinitionUrl: function(orgId, serviceId, version) {
                    var endpoint = formatEndpoint(
                        Configuration.api.endpoint + '/organizations/:organizationId/services/:serviceId/versions/:version/definition',
                        { organizationId: orgId, serviceId: serviceId, version: version });
                    return endpoint;
                },
                getServiceDefinition: function(orgId, serviceId, version, handler, errorHandler) {
                    var endpoint = formatEndpoint(
                        Configuration.api.endpoint + '/organizations/:organizationId/services/:serviceId/versions/:version/definition',
                        { organizationId: orgId, serviceId: serviceId, version: version });
                    $http({
                        method: 'GET', 
                        url: endpoint, 
                        transformResponse: function(value) { return value; }
                    }).success(handler).error(errorHandler);
                },
                updateServiceDefinition: function(orgId, serviceId, version, definition, definitionType, handler, errorHandler) {
                    var ct = 'application/json';
                    if (definitionType == 'SwaggerYAML') {
                        ct = 'application/x-yaml';
                    }
                    var endpoint = formatEndpoint(
                        Configuration.api.endpoint + '/organizations/:organizationId/services/:serviceId/versions/:version/definition',
                        { organizationId: orgId, serviceId: serviceId, version: version });
                    $http({
                        method: 'PUT', 
                        url: endpoint,
                        headers: { 'Content-Type' : ct },
                        data: definition
                    }).success(handler).error(errorHandler);
                }
            }
        }]);
    
    export var SystemServices = _module.factory('SystemSvcs', ['$resource', 'Configuration',
        function($resource, Configuration) {
            return {
                getStatus: function(handler, errorHandler) {
                    var endpoint = formatEndpoint(Configuration.api.endpoint + '/system/status', {});
                    $resource(endpoint).get({}, handler, errorHandler);
                }
            }
        }]);

}
