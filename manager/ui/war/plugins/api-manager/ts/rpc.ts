import angular = require("angular");
import { BlobRef } from "./model/blob.model";
import {
    ApiBean,
    ApiPlanBean,
    ApiPlanSummaryBean,
    ApiVersionBean, KeyValueTagDto, UpdateApiBean,
    UpdateApiVersionBean,
} from "./model/api.model";
import { ContractAction } from "./model/contract.model";

const _module = angular.module("ApimanRPC", [
  "ngResource",
  "ApimanConfiguration",
]);

const formatEndpoint = function(endpoint, params) {
    return endpoint.replace(/:(\w+)/g, function(match, key) {
        return params[key] ? params[key] : (':' + key);
    });
};

_module.factory('ApimanSvcs', ['$resource', 'Configuration',
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

_module.factory('UserSvcs', ['$resource', 'Configuration',
    function($resource, Configuration) {
        var endpoint = Configuration.api.endpoint + '/users/:user/:entityType';
        return $resource(endpoint,
            {user: '@user', entityType: '@entityType'}, {
                update: {
                    method: 'PUT' // this method issues a PUT request
                }
            });
    }]);

_module.factory('ContractService', ['$http', 'Configuration', '$q',
    ($http, Configuration, $q) => {
        return {
            approveContract: (contractId: number): Promise<any> => {
                return $http({
                    method: 'POST',
                    url: `${Configuration.api.endpoint}/actions/contracts`,
                    data: {
                        contractId: contractId,
                        status: 'AwaitingApproval'
                    } as ContractAction
                }).then(
                    success => $q.resolve(success.data),
                    failure => $q.reject(failure)
                );
            }
        }
    }]);

_module.factory('DevPortalService', ['$http', 'Configuration', '$q',
    ($http, Configuration, $q) => {
        return {
            updateApi: (orgId: string, apiId: string, bean: UpdateApiBean): Promise<any> => {
                return $http({
                    method: 'PUT',
                    url: `${Configuration.api.endpoint}/organizations/${orgId}/apis/${apiId}`,
                    data: bean
                });
            },
            tagApi: (orgId: string, apiId: string, bean: KeyValueTagDto): Promise<any> => {
                return $http({
                    method: 'PUT',
                    url: `${Configuration.api.endpoint}/organizations/${orgId}/apis/${apiId}/tags`,
                    data: bean
                })
            },
            getApiVersion: (orgId: string, apiId: string, apiVersion: string):  Promise<ApiVersionBean> => {
                return $http.get(`${Configuration.api.endpoint}/organizations/${orgId}/apis/${apiId}/versions/${apiVersion}`)
                .then(
                    success => $q.resolve(success.data),
                    failure => $q.reject(failure)
                );
            },
            updateApiVersion: (orgId: string, apiId: string, apiVersion: string, bean: UpdateApiVersionBean): Promise<any> => {
                return $http({
                    method: 'PUT',
                    url: `${Configuration.api.endpoint}/organizations/${orgId}/apis/${apiId}/versions/${apiVersion}`,
                    data: bean
                })
            },
            getApiVersionPlans: (orgId: string, apiId: string, apiVersion: string): Promise<ApiPlanSummaryBean[]> => {
                return $http({
                    method: 'GET',
                    url: `${Configuration.api.endpoint}/organizations/${orgId}/apis/${apiId}/versions/${apiVersion}/plans`
                }).then(
                    success => $q.resolve(success.data),
                    failure => $q.reject(failure)
                );
            },
            updateApiVersionPlan: (orgId: string, apiId: string, apiVersion: string, update: ApiPlanBean): Promise<any> => {
                return $http({
                    method: 'PUT',
                    url: `${Configuration.api.endpoint}/organizations/${orgId}/apis/${apiId}/versions/${apiVersion}/plans`,
                    data: update
                });
            }
        }
    }]);

_module.factory('BlobService', ['$http', 'Configuration', '$q',
    ($http, Configuration, $q) => {
        return {
            uploadBlob: (blob: Blob | File): Promise<BlobRef> => {
                const formData: FormData = new FormData();
                formData.append('image', blob);
                return $http({
                    method: 'POST',
                    headers: {"Content-Type": undefined }, // Otherwise Angular will set the content type as JSON
                    url: `${Configuration.api.endpoint}/blobs`,
                    data: formData
                }).then(
                    success => {
                        const data: any = success.data;
                        const blobRef: BlobRef = {
                            id: data.id,
                            name: data.name,
                            hash: data.hash,
                            mimeType: data.mimeType,
                            location: success.headers("Location")
                        }
                        return $q.resolve(blobRef);
                    },
                    failure => $q.reject(failure)
                )
            }
        }
    }]);

_module.factory('OrgSvcs', ['$resource', 'Configuration',
    function($resource, Configuration) {
        const endpoint = Configuration.api.endpoint + '/organizations/:organizationId/:entityType/:entityId/:versionsOrActivity/:version/:policiesOrActivity/:policyId/:policyChain';
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

_module.factory('CurrentUserSvcs', ['$resource', 'Configuration',
    function($resource, Configuration) {
        var endpoint = Configuration.api.endpoint + '/users/currentuser/:what';
        return $resource(endpoint,
            {entityType: '@what'});
    }]);

_module.factory('ActionSvcs', ['$resource', 'Configuration',
    function($resource, Configuration) {
        var endpoint = Configuration.api.endpoint + '/actions';
        return $resource(endpoint);
    }]);

_module.factory('AuditSvcs', ['$resource', 'Configuration',
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

_module.factory('UserAuditSvcs', ['$resource', 'Configuration',
    function($resource, Configuration) {
        var endpoint = Configuration.api.endpoint + '/users/:user/activity';
        return $resource(endpoint,
            {
                user: '@user',

                page: '@page',
                count: '@count'
            });
    }]);


_module.factory('PluginSvcs', ['$resource', 'Configuration',
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

_module.factory('ApiSvcs', [
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

_module.factory('ApiDefinitionSvcs', ['$resource', '$http', 'Configuration', '$q',
    function($resource, $http, Configuration, $q) {
        return {
            getApimanDefinitionUrl: function(orgId, apiId, version) {
                var endpoint = formatEndpoint(
                    Configuration.api.endpoint + '/organizations/:organizationId/apis/:apiId/versions/:version/definition',
                    { organizationId: orgId, apiId: apiId, version: version });
                return endpoint;
            },
            getApiDefinition: function(orgId, apiId, version, handler): Promise<string> {
                var endpoint = formatEndpoint(
                    Configuration.api.endpoint + '/organizations/:organizationId/apis/:apiId/versions/:version/definition',
                    { organizationId: orgId, apiId: apiId, version: version });
                return $http({
                    method: 'GET',
                    url: endpoint
                }).then(
                    ok => $q.resolve(JSON.stringify(ok.data)),
                    failure => $q.reject(failure)
                );
            },
            updateApiDefinition: function (orgId, apiId, version, definition, definitionType): Promise<any> {
                let ct = 'application/json';
                if (definitionType == 'SwaggerYAML') {
                    ct = 'application/x-yaml';
                } else if (definitionType == 'WSDL') {
                    ct = 'application/wsdl+xml';
                }
                let endpoint = formatEndpoint(
                    Configuration.api.endpoint + '/organizations/:organizationId/apis/:apiId/versions/:version/definition',
                    {organizationId: orgId, apiId: apiId, version: version});
                return $http({
                    method: 'PUT',
                    url: endpoint,
                    headers: {'Content-Type': ct},
                    data: definition
                }).then(
                    ok => $q.resolve(ok),
                    failure => $q.reject(failure)
                );
            },
            updateApiDefinitionFromUrl(orgId, apiId, version, definitionUrl, definitionType): Promise<any> {
                let ct = 'application/json';
                let endpoint = formatEndpoint(
                    Configuration.api.endpoint + '/organizations/:organizationId/apis/:apiId/versions/:version/definition',
                    {organizationId: orgId, apiId: apiId, version: version});
                ///let data = JSON.stringify();
                return $http({
                    method: 'POST',
                    url: endpoint,
                    headers: {'Content-Type': ct},
                    data: {
                        definitionUrl: definitionUrl,
                        definitionType: definitionType
                    }
                })
            }
        }
    }]);

_module.factory('MetricsSvcs', ['$resource', 'Configuration',
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

_module.factory('SystemSvcs', ['$resource', 'Configuration', 'Logger', 'Upload',
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

_module.factory('DownloadSvcs', ['$resource', 'Configuration',
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

_module.factory('ApiRegistrySvcs', ['$resource', 'Configuration',
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

_module.factory('ApiCatalogSvcs', ['$resource', 'Configuration',
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

/** searching, paging, and ordering **/
export interface PagingBean {
    page: number;
    pageSize: number;
}

export interface OrderByBean {
    ascending: boolean;
    name: string;
}

export interface SearchCriteriaFilterBean {
    name: string;
    value: string;
    operator: SearchCriteriaFilterOperator;
}

export type SearchCriteriaFilterOperator = 'bool_eq' | 'eq' | 'neq' | 'gt' | 'gte' | 'lt' | 'lte' | 'like';

export interface SearchResultsBean<T> {
    beans: T[];
    totalSize: number;
}