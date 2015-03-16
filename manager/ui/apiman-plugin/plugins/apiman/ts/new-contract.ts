/// <reference path="apimanPlugin.ts"/>
module Apiman {

    export var NewContractController = _module.controller("Apiman.NewContractController",
        ['$q', '$location', '$scope', 'OrgSvcs', 'CurrentUserSvcs', 'PageLifecycle', 'Logger', '$rootScope',
        ($q, $location, $scope, OrgSvcs, CurrentUserSvcs, PageLifecycle, Logger, $rootScope) => {
            $scope.refreshAppVersions = function(organizationId, appId, onSuccess, onError) {
                OrgSvcs.query({ organizationId: organizationId, entityType: 'applications', entityId: appId, versionsOrActivity: 'versions' }, function(versions) {
                    var plainVersions = new Array();
                    for (var i = 0; i < versions.length; i++) {
                        plainVersions.push(versions[i].version);
                    }
                    $scope.appVersions = plainVersions;
                    if (onSuccess) {
                        onSuccess(plainVersions);
                    }
                }, function(error) {
                    if (onError) {
                        onError(error);
                    } else {
                        // nothing provided - do something interesting?
                    }
                });
            };
            
            var promise = $q.all({
                apps: $q(function(resolve, reject) {
                    CurrentUserSvcs.query({ what: 'applications' }, function(apps) {
                        if ($rootScope.mruApp) {
                            for (var i = 0; i < apps.length; i++) {
                                var app = apps[i];
                                if (app.organizationId == $rootScope.mruApp.organizationId && app.id == $rootScope.mruApp.id) {
                                    $scope.selectedApp = app;
                                }
                            }
                        } else {
                            if (apps.length > 0) {
                                $scope.selectedApp = apps[0];
                            }
                        }
                        resolve(apps);
                    }, function(error) {
                        reject(error);
                    });
                })
            });

            $scope.$watch('selectedApp', function(newValue) {
                Logger.debug("App selected: {0}", newValue);
                $scope.selectedAppVersion = undefined;
                $scope.appVersions = [];
                $scope.refreshAppVersions(newValue.organizationId, newValue.id, function(versions) {
                    Logger.debug("Versions: {0}", versions);
                    if ($rootScope.mruApp) {
                        if ($rootScope.mruApp.organizationId == newValue.organizationId && $rootScope.mruApp.id == newValue.id) {
                            $scope.selectedAppVersion = $rootScope.mruApp.version;
                        }
                    } else {
                        if (versions.length > 0) {
                            $scope.selectedAppVersion = versions[0];
                        }
                    }
                });
            });
            $scope.$watch('selectedAppVersion', function(newValue) {
                Logger.debug("App version selected: {0}", newValue);
            });
            
            $scope.createContract = function() {
                Logger.info("Creating new contract from {0}/{1} ({2}) to {3}/{4} ({5}) through the {6} plan!", 
                        $scope.selectedApp.organizationName, $scope.selectedApp.name, $scope.selectedAppVersion,
                        $scope.selectedService.organizationName, $scope.selectedService.name, $scope.selectedService.version,
                        $scope.selectedPlan);
            };
            
            PageLifecycle.loadPage('NewContract', promise, $scope);
        }]);

}
