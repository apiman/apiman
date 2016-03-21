/// <reference path="../../includes.ts"/>
module ApimanRPC {

    export var _module = angular.module("ApimanRPC", ['ngResource', 'ApimanConfiguration']);
    
    var formatEndpoint = function(endpoint, params) {
        return endpoint.replace(/:(\w+)/g, function(match, key) {
            return params[key] ? params[key] : (':' + key);
        });
    };

    export var ApimanSvcs = _module.factory('ApimanSvcs', ['$resource', 'Configuration',
        function($resource, Configuration) {
            var endpoint = Configuration.api.endpoint + '/:entityType/:secondaryType';

            /*
            // Intercept Errors
            function resourceErrorResponseHandler(response) {
                var charRegExp = /([\\]*)?"(pass|password)([\\]*)?":([\\]*)?"(.+?)([\\]*)?"/g;
                var regTest = charRegExp.test(JSON.stringify(response));

                if (regTest === true) {
                    var newResponse = JSON.stringify(response).replace(charRegExp, '\\"password\\":\\"*****\\"');
                    console.log('Formatted response: ' + newResponse);

                    return newResponse;
                }

                return response;
            }
            */

            return $resource(endpoint,
                { entityType: '@entityType', secondaryType: '@secondaryType' }, {
                update: {
                  method: 'PUT'//, // this method issues a PUT request
                    //interceptor : {responseError : resourceErrorResponseHandler }
                }});
        }]);

    export var UserSvcs = _module.factory('UserSvcs', ['$resource', 'Configuration',
        function($resource, Configuration) {
            var endpoint = Configuration.api.endpoint + '/users/:user/:entityType';
            return $resource(endpoint,
                { user: '@user', entityType: '@entityType' });
        }]);

    export var OrganizationSvcs = _module.factory('OrgSvcs', ['$resource', 'Configuration',
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

    export var CurrentUserSvcs = _module.factory('CurrentUserSvcs', ['$resource', 'Configuration',
        function($resource, Configuration) {
            var endpoint = Configuration.api.endpoint + '/currentuser/:what';
            return $resource(endpoint,
                { entityType: '@what' }, {
                update: {
                  method: 'PUT' // this method issues a PUT request
                }});
        }]);

    export var ActionSvcs = _module.factory('ActionSvcs', ['$resource', 'Configuration',
        function($resource, Configuration) {
            var endpoint = Configuration.api.endpoint + '/actions';
            return $resource(endpoint);
        }]);

    export var AuditSvcs = _module.factory('AuditSvcs', ['$resource', 'Configuration',
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

    export var UserAuditSvcs = _module.factory('UserAuditSvcs', ['$resource', 'Configuration',
        function($resource, Configuration) {
            var endpoint = Configuration.api.endpoint + '/users/:user/activity';
            return $resource(endpoint,
                {
                    user: '@user',
                    
                    page: '@page',
                    count: '@count'
                });
        }]);

    
    export var PluginSvcs = _module.factory('PluginSvcs', ['$resource', 'Configuration',
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

    export var ApiSvcs = _module.factory('ApiSvcs', [
        '$resource',
        '$http',
        'Configuration', function($resource,
                                  $http,
                                  Configuration) {
            return {
                deleteApi: function(data) {
                    var endpoint = Configuration.api.endpoint + '/organizations/' + data.orgId + '/apis/' + data.apiId;

                    return $http({
                        method: 'DELETE',
                        url: endpoint,
                        data: {
                            organizationId: data.orgId,
                            apiId: data.apiId
                        }
                    });
                }
            }
        }]);

    export var ApiDefinitionSvcs = _module.factory('ApiDefinitionSvcs', ['$resource', '$http', 'Configuration',
        function($resource, $http, Configuration) {
            return {
                getApiDefinitionUrl: function(orgId, apiId, version) {
                    var endpoint = formatEndpoint(
                        Configuration.api.endpoint + '/organizations/:organizationId/apis/:apiId/versions/:version/definition',
                        { organizationId: orgId, apiId: apiId, version: version });
                    return endpoint;
                },
                getApiDefinition: function(orgId, apiId, version, handler, errorHandler) {
                    var endpoint = formatEndpoint(
                        Configuration.api.endpoint + '/organizations/:organizationId/apis/:apiId/versions/:version/definition',
                        { organizationId: orgId, apiId: apiId, version: version });
                    $http({
                        method: 'GET', 
                        url: endpoint, 
                        transformResponse: function(value) { return value; }
                    }).success(handler).error(errorHandler);
                },
                updateApiDefinition: function(orgId, apiId, version, definition, definitionType, handler, errorHandler) {
                    var ct = 'application/json';
                    if (definitionType == 'SwaggerYAML') {
                        ct = 'application/x-yaml';
                    }
                    var endpoint = formatEndpoint(
                        Configuration.api.endpoint + '/organizations/:organizationId/apis/:apiId/versions/:version/definition',
                        { organizationId: orgId, apiId: apiId, version: version });
                    $http({
                        method: 'PUT', 
                        url: endpoint,
                        headers: { 'Content-Type' : ct },
                        data: definition
                    }).success(handler).error(errorHandler);
                }
            }
        }]);

    export var MetricsSvcs = _module.factory('MetricsSvcs', ['$resource', 'Configuration',
        function($resource, Configuration) {
            return {
                getUsage: function(orgId, apiId, version, interval, from, to, handler, errorHandler) {
                    var endpoint = formatEndpoint(
                        Configuration.api.endpoint + '/organizations/:organizationId/apis/:apiId/versions/:version/metrics/usage',
                        { organizationId: orgId, apiId: apiId, version: version });
                    $resource(endpoint, {interval: interval, from: from, to: to}).get({}, handler, errorHandler);
                },
                getUsagePerClient: function(orgId, apiId, version, from, to, handler, errorHandler) {
                    var endpoint = formatEndpoint(
                        Configuration.api.endpoint + '/organizations/:organizationId/apis/:apiId/versions/:version/metrics/clientUsage',
                        { organizationId: orgId, apiId: apiId, version: version });
                    $resource(endpoint, {from: from, to: to}).get({}, handler, errorHandler);
                },
                getUsagePerPlan: function(orgId, apiId, version, from, to, handler, errorHandler) {
                    var endpoint = formatEndpoint(
                        Configuration.api.endpoint + '/organizations/:organizationId/apis/:apiId/versions/:version/metrics/planUsage',
                        { organizationId: orgId, apiId: apiId, version: version });
                    $resource(endpoint, {from: from, to: to}).get({}, handler, errorHandler);
                },
                getResponseStats: function(orgId, apiId, version, interval, from, to, handler, errorHandler) {
                    var endpoint = formatEndpoint(
                        Configuration.api.endpoint + '/organizations/:organizationId/apis/:apiId/versions/:version/metrics/responseStats',
                        { organizationId: orgId, apiId: apiId, version: version });
                    $resource(endpoint, {interval: interval, from: from, to: to}).get({}, handler, errorHandler);
                },
                getResponseStatsSummary: function(orgId, apiId, version, from, to, handler, errorHandler) {
                    var endpoint = formatEndpoint(
                        Configuration.api.endpoint + '/organizations/:organizationId/apis/:apiId/versions/:version/metrics/summaryResponseStats',
                        { organizationId: orgId, apiId: apiId, version: version });
                    $resource(endpoint, {from: from, to: to}).get({}, handler, errorHandler);
                },
                getResponseStatsPerClient: function(orgId, apiId, version, from, to, handler, errorHandler) {
                    var endpoint = formatEndpoint(
                        Configuration.api.endpoint + '/organizations/:organizationId/apis/:apiId/versions/:version/metrics/clientResponseStats',
                        { organizationId: orgId, apiId: apiId, version: version });
                    $resource(endpoint, {from: from, to: to}).get({}, handler, errorHandler);
                },
                getResponseStatsPerPlan: function(orgId, apiId, version, from, to, handler, errorHandler) {
                    var endpoint = formatEndpoint(
                        Configuration.api.endpoint + '/organizations/:organizationId/apis/:apiId/versions/:version/metrics/planResponseStats',
                        { organizationId: orgId, apiId: apiId, version: version });
                    $resource(endpoint, {from: from, to: to}).get({}, handler, errorHandler);
                },
                getClientUsagePerApi: function(orgId, clientId, version, from, to, handler, errorHandler) {
                    var endpoint = formatEndpoint(
                        Configuration.api.endpoint + '/organizations/:organizationId/clients/:clientId/versions/:version/metrics/apiUsage',
                        { organizationId: orgId, clientId: clientId, version: version });
                    $resource(endpoint, {from: from, to: to}).get({}, handler, errorHandler);
                },
            }
        }]);

    export var SystemSvcs = _module.factory('SystemSvcs', ['$resource', 'Configuration', 'Logger', 'Upload',
        function($resource, Configuration, Logger, Upload) {
            return {
                getStatus: function(handler, errorHandler) {
                    var endpoint = formatEndpoint(Configuration.api.endpoint + '/system/status', {});
                    $resource(endpoint).get({}, handler, errorHandler);
                },
                exportAsJson: function(handler, errorHandler) {
                    var endpoint = formatEndpoint(Configuration.api.endpoint + '/system/export?download=true', {});
                    $resource(endpoint).get({}, handler, errorHandler);
                },
                importJson: function(file, progressHandler, handler, errorHandler) {
	                var endpoint = formatEndpoint(Configuration.api.endpoint + '/system/import', {});
	                file.upload = Upload.http({
	                    url: endpoint,
	                    method: 'POST',
	                    headers: {
	                      'Content-Type': 'application/json',
	                    },
	                    data: file
	                  });
                  file.upload.then(handler, errorHandler);
                  file.upload.progress(progressHandler);
                }
            }
        }]);

    export var DownloadSvcs = _module.factory('DownloadSvcs', ['$resource', 'Configuration',
        function($resource, Configuration) {
            return {
                getDownloadLink: function(downloadId) {
                    var endpoint = formatEndpoint(Configuration.api.endpoint + '/downloads/:downloadId', {
                    	"downloadId" : downloadId
                    });
                    return endpoint;
                }
            }
        }]);

    export var ApiRegistrySvcs = _module.factory('ApiRegistrySvcs', ['$resource', 'Configuration',
        function($resource, Configuration) {
            return {
                exportApiRegistryAsJson: function(orgId, clientId, version, handler, errorHandler) {
                    var endpoint = formatEndpoint(Configuration.api.endpoint + '/organizations/:organizationId/clients/:clientId/versions/:version/apiregistry/json?download=true', {
                    	"organizationId" : orgId,
                    	"clientId" : clientId,
                    	"version" : version
                    });
                    $resource(endpoint).get({}, handler, errorHandler);
                },
                exportApiRegistryAsXml: function(orgId, clientId, version, handler, errorHandler) {
                    var endpoint = formatEndpoint(Configuration.api.endpoint + '/organizations/:organizationId/clients/:clientId/versions/:version/apiregistry/xml?download=true', {
                    	"organizationId" : orgId,
                    	"clientId" : clientId,
                    	"version" : version
                    });
                    $resource(endpoint).get({}, handler, errorHandler);
                }
            }
        }]);


    export var ApiCatalogSvcs = _module.factory('ApiCatalogSvcs', ['$resource', 'Configuration',
        function($resource, Configuration) {
            return {
                getNamespaces: function(handler, errorHandler) {
                    var endpoint = Configuration.api.endpoint + '/search/apiCatalog/namespaces';
                    $resource(endpoint, {}, {
                        "get" : { "action" : "GET", "isArray" : true }
                    }).get({}, handler, errorHandler);
                },
                search: function(criteria, handler, errorHandler) {
                    var endpoint = Configuration.api.endpoint + '/search/apiCatalog/entries';
                    $resource(endpoint).save({}, criteria, handler, errorHandler);
                }
            }
        }]);

}
