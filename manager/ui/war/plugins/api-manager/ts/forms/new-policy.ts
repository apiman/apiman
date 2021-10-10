import angular = require("angular");
import {_module} from "../apimanPlugin";

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
        CachingResourcesPolicy: 'caching-resources.include',
        TimeRestrictedAccessPolicy: 'time-restricted-access.include'
    };

    export var NewPolicyController = _module.controller("Apiman.NewPolicyController",
        ['$q', '$location', '$scope', 'OrgSvcs', 'ApimanSvcs', 'PageLifecycle', 'Logger', '$routeParams',
        function ($q, $location, $scope, OrgSvcs, ApimanSvcs, PageLifecycle, Logger, $routeParams) {
            var params = $routeParams;

            var etype = params.type;

            var pageData = {
                version: $q(function(resolve, reject) {
                    OrgSvcs.get({ organizationId: params.org, entityType: etype, entityId: params.id, versionsOrActivity: 'versions', version: params.ver }, resolve, reject);
                }),
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
