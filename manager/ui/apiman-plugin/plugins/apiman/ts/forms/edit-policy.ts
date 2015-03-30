/// <reference path="../apimanPlugin.ts"/>
module Apiman {
    
    export var EditPolicyController = _module.controller("Apiman.EditPolicyController",
        ['$q', '$location', '$scope', 'OrgSvcs', 'ApimanSvcs', 'PageLifecycle', 'Logger',
        ($q, $location, $scope, OrgSvcs, ApimanSvcs, PageLifecycle, Logger) => {
            var params = $location.search();
            
            var promise = $q.all({
                policy: $q(function(resolve, reject) {
                    OrgSvcs.get({
                        organizationId: params.org,
                        entityType: params.type,
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
                    }, function(error) {
                        reject(error);
                    });
                })
            });
            
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
                OrgSvcs.update({
                    organizationId: params.org,
                    entityType: params.type,
                    entityId: params.id,
                    versionsOrActivity: 'versions', 
                    version: params.ver,
                    policiesOrActivity: 'policies', 
                    policyId: params.policy
                }, updatedPolicy, function() {
                    var toPage = '/plan-policies.html';
                    var entityParam = 'plan';
                    if (params.type == 'services') {
                        toPage = '/service-policies.html';
                        entityParam = 'service';
                    }
                    if (params.type == 'applications') {
                        toPage = '/app-policies.html';
                        entityParam = 'app';
                    }
                    $location.url(Apiman.pluginName + toPage)
                        .search('org', params.org)
                        .search(entityParam, params.id)
                        .search('version', params.ver);
                }, function(error) {
                    if (error.status == 409) {
                        $location.url('apiman/error-409.html');
                    } else {
                        alert("ERROR=" + error.status + " " + error.statusText);
                    }
                    // TODO handle error better here
                    $scope.updateButton.state = 'error';
                });
            };
            
            PageLifecycle.loadPage('EditPolicy', promise, $scope, function() {
                PageLifecycle.setPageTitle('edit-policy');
                $('#apiman-description').focus();
            });
        }]);

}
