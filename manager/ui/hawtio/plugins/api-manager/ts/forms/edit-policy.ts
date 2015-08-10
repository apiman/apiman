/// <reference path="../apimanPlugin.ts"/>
module Apiman {
    
    export var EditPolicyController = _module.controller("Apiman.EditPolicyController",
        ['$q', '$location', '$scope', 'OrgSvcs', 'ApimanSvcs', 'PageLifecycle', 'Logger', '$routeParams', 'EntityStatusService', 'CurrentUser',
        ($q, $location, $scope, OrgSvcs, ApimanSvcs, PageLifecycle, Logger, $routeParams, EntityStatusService, CurrentUser) => {
            var params = $routeParams;
            $scope.organizationId = params.org;
            var requiredPermissionMap = {
                applications: 'appEdit',
                services: 'svcEdit',
                plans: 'planEdit'
            };
            var etype = params.type;
            if (etype == 'apps') {
                etype = 'applications';
            }
            
            var pageData = {
                version: $q(function(resolve, reject) {
                    OrgSvcs.get({ organizationId: params.org, entityType: etype, entityId: params.id, versionsOrActivity: 'versions', version: params.ver }, resolve, reject);
                }),
                policy: $q(function(resolve, reject) {
                    OrgSvcs.get({
                        organizationId: params.org,
                        entityType: etype,
                        entityId: params.id,
                        versionsOrActivity: 'versions', 
                        version: params.ver,
                        policiesOrActivity: 'policies', 
                        policyId: params.policy
                    }, function(policy) {
                        var config = new Object();
                        try {
                            config = JSON.parse(policy.configuration);
                        } catch (e) {
                            // TODO handle this error better!
                        }
                        $scope.config = config;
                        if (policy.definition.formType == 'JsonSchema') {
                            $scope.include = 'plugins/api-manager/html/policyForms/JsonSchema.include';
                        } else {
                            var inc = ConfigForms[policy.definition.id];
                            if (!inc) {
                                inc = 'Default.include';
                            }
                            $scope.include = 'plugins/api-manager/html/policyForms/' + inc;
                        }
                        $scope.selectedDef = policy.definition;
                        resolve(policy);
                    }, reject);
                })
            };
            
            $scope.setValid = function(valid) {
                $scope.isValid = valid;
            };
            
            $scope.setConfig = function(config) {
                $scope.config = config;
            };
            $scope.getConfig = function() {
                return $scope.config;
            };
            
            $scope.updatePolicy = function() {
                $scope.updateButton.state = 'in-progress';
                var updatedPolicy = {
                    configuration: angular.toJson($scope.config)
                };
                var etype = params.type;
                if (etype == 'apps') {
                    etype = 'applications';
                }
                OrgSvcs.update({
                    organizationId: params.org,
                    entityType: etype,
                    entityId: params.id,
                    versionsOrActivity: 'versions', 
                    version: params.ver,
                    policiesOrActivity: 'policies', 
                    policyId: params.policy
                }, updatedPolicy, function() {
                    PageLifecycle.redirectTo('/orgs/{0}/{1}/{2}/{3}/policies', params.org, params.type, params.id, params.ver);
                }, PageLifecycle.handleError);
            };
            
            PageLifecycle.loadPage('EditPolicy', pageData, $scope, function() {
                EntityStatusService.setEntityStatus($scope.version.status);
                // Note: not using the apiman-permission directive in the template for this page because
                // we cannot hard-code the required permission.  The required permission changes depending
                // on the entity type of the parent of the policy.  Instead we figure it out and set it here.
                $scope.hasPermission = CurrentUser.hasPermission(params.org, requiredPermissionMap[etype]);
                PageLifecycle.setPageTitle('edit-policy');
                $('#apiman-description').focus();
            });
        }]);

}
