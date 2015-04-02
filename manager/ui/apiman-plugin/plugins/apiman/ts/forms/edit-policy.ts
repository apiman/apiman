/// <reference path="../apimanPlugin.ts"/>
module Apiman {
    
    export var EditPolicyController = _module.controller("Apiman.EditPolicyController",
        ['$q', '$location', '$scope', 'OrgSvcs', 'ApimanSvcs', 'PageLifecycle', 'Logger', '$routeParams',
        ($q, $location, $scope, OrgSvcs, ApimanSvcs, PageLifecycle, Logger, $routeParams) => {
            var params = $routeParams;
            
            var pageData = {
                policy: $q(function(resolve, reject) {
                var etype = params.type;
                    if (etype == 'apps') {
                        etype = 'applications';
                    }
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
                            $scope.include = 'plugins/apiman/html/policyForms/JsonSchema.include';
                        } else {
                            var inc = ConfigForms[policy.definition.id];
                            if (!inc) {
                                inc = 'Default.include';
                            }
                            $scope.include = 'plugins/apiman/html/policyForms/' + inc;
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
            
            $scope.updatePolicy = function() {
                $scope.updateButton.state = 'in-progress';
                var updatedPolicy = {
                    configuration: JSON.stringify($scope.config)
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
                PageLifecycle.setPageTitle('edit-policy');
                $('#apiman-description').focus();
            });
        }]);

}
