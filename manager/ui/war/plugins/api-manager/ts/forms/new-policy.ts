/// <reference path="../apimanPlugin.ts"/>
module Apiman {
    
    export var ConfigForms = {
        BASICAuthenticationPolicy: 'basic-auth.include',
        IgnoredResourcesPolicy: 'ignored-resources.include',
        IPBlacklistPolicy: 'ip-list.include',
        IPWhitelistPolicy: 'ip-list.include',
        RateLimitingPolicy: 'rate-limiting.include',
        QuotaPolicy: 'quota.include',
        TransferQuotaPolicy: 'transfer-quota.include',
        AuthorizationPolicy: 'authorization.include',
        URLRewritingPolicy: 'url-rewriting.include',
        CachingPolicy: 'caching.include',
        TimeRestrictedAccessPolicy: 'time-restricted-access.include'
    };

    export var NewPolicyController = _module.controller("Apiman.NewPolicyController",
        ['$q', '$location', '$scope', 'OrgSvcs', 'ApimanSvcs', 'CustomTemplateSvcs', 'PageLifecycle', 'Logger', '$routeParams',
        ($q, $location, $scope, OrgSvcs, ApimanSvcs, CustomTemplateSvcs, PageLifecycle, Logger, $routeParams) => {
            var params = $routeParams;
            
            var pageData = {
                policyDefs: $q(function(resolve, reject) {
                    ApimanSvcs.query({ entityType: 'policyDefs' }, function(policyDefs) {
                        $scope.selectedDefId = null;
                        resolve(policyDefs);
                    }, reject);
                })
            };

            function loadTemplate(newValue) {
                if (!newValue) {
                    $scope.include = undefined;
                } else {
                    $scope.config = new Object();
                    if ($scope.selectedDef.formType == 'JsonSchema') {
                        $scope.include = 'plugins/api-manager/html/policyForms/JsonSchema.include';
                    } else if($scope.selectedDef.formType == 'AngularTemplate') {
                        // Fetch HTML file
                        $q(function (resolve, reject) {
                            ApimanSvcs.query({ entityType: 'policyDefs' }, function (response) {
                                resolve(response);
                                console.log('Response: ' + JSON.stringify(response));
                            }, reject);
                        });

                        // Set template
                        //ConfigForms[$scope.selectedDef.name] = '';

                        //CustomTemplateSvcs.setTemplate();
                        //CustomTemplateSvcs.setTemplate('plugins/api-manager/html/policyForms/' + $scope.selectedDef.id + '.html');

                        // Set include scope to custom HTML file that will include set template
                        //$scope.include = 'plugins/api-manager/html/forms/custom.html';
                    } else {
                        var inc = ConfigForms[$scope.selectedDef.id];

                        if (!inc) {
                            inc = 'Default.include';
                        }

                        $scope.include = 'plugins/api-manager/html/policyForms/' + inc;
                    }
                }
            }

            $scope.changeSelectedDefId = function(newValue) {
                if (newValue.id) {
                    var newDef = undefined;

                    angular.forEach($scope.policyDefs, function(def) {
                        if (def.id == newValue.id) {
                            newDef = def;
                        }
                    });

                    $scope.selectedDef = newDef;

                    loadTemplate(newValue);
                }
            };
            
            $scope.$watch('selectedDef', function(newValue) {
                if (!newValue) {
                    $scope.include = undefined;
                } else {
                    $scope.config = new Object();
                    if ($scope.selectedDef.formType == 'JsonSchema') {
                        $scope.include = 'plugins/api-manager/html/policyForms/JsonSchema.include';
                    } else if($scope.selectedDef.formType == 'AngularTemplate') {
                        //$scope.include = 'plugins/api-manager/html/forms/custom.html';
                    } else {
                        var inc = ConfigForms[$scope.selectedDef.id];
                        if (!inc) {
                            inc = 'Default.include';
                        }
                        $scope.include = 'plugins/api-manager/html/policyForms/' + inc;
                    }
                }
            });
            
            $scope.setValid = function(valid) {
                $scope.isValid = valid;
            };

            $scope.setConfig = function(config) {
                $scope.config = config;
            };

            $scope.getConfig = function() {
                return $scope.config;
            };
            
            $scope.addPolicy = function() {
                $scope.createButton.state = 'in-progress';
                var newPolicy = {
                    definitionId: $scope.selectedDef.id,
                    configuration: angular.toJson($scope.config)
                };
                var etype = params.type;
                OrgSvcs.save({ organizationId: params.org, entityType: etype, entityId: params.id, versionsOrActivity: 'versions', version: params.ver, policiesOrActivity: 'policies' }, newPolicy, function(reply) {
                    PageLifecycle.redirectTo('/orgs/{0}/{1}/{2}/{3}/policies', params.org, params.type, params.id, params.ver);
                }, PageLifecycle.handleError);
            };
            
            PageLifecycle.loadPage('NewPolicy', undefined, pageData, $scope, function() {
                PageLifecycle.setPageTitle('new-policy');
            });
        }]);

}
