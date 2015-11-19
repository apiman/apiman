/// <reference path="../apimanPlugin.ts"/>
module Apiman {

    export var NewContractController = _module.controller("Apiman.NewContractController",
        ['$q', '$location', '$scope', 'OrgSvcs', 'CurrentUserSvcs', 'PageLifecycle', 'Logger', '$rootScope', 'Dialogs',
        ($q, $location, $scope, OrgSvcs, CurrentUserSvcs, PageLifecycle, Logger, $rootScope, Dialogs) => {
            var params = $location.search();
            var svcId = params.svc;
            var svcOrgId = params.svcorg;
            var svcVer = params.svcv;
            var planId = params.planid;
            
            $scope.refreshAppVersions = function(organizationId, appId, onSuccess, onError) {
                OrgSvcs.query({ organizationId: organizationId, entityType: 'applications', entityId: appId, versionsOrActivity: 'versions' }, function(versions) {
                    var plainVersions = [];

                    angular.forEach(versions, function(version) {
                        if (version.status == 'Created' || version.status == 'Ready') {
                            plainVersions.push(version.version);
                        }
                    });

                    $scope.appVersions = plainVersions;

                    if (onSuccess) {
                        onSuccess(plainVersions);
                    }
                }, PageLifecycle.handleError);
            };
            
            var pageData = {
                apps: $q(function(resolve, reject) {
                    CurrentUserSvcs.query({ what: 'applications' }, function(apps) {
                        if ($rootScope.mruApp) {
                            for (var i = 0; i < apps.length; i++) {
                                var app = apps[i];

                                if (app.organizationId == $rootScope.mruApp.application.organization.id && app.id == $rootScope.mruApp.application.id) {
                                    $scope.selectedApp = app;
                                }
                            }
                        } else {
                            $scope.selectedApp = undefined;
                        }
                        resolve(apps);
                    }, reject);
                }),
                selectedService: $q(function(resolve, reject) {
                    if (svcId && svcOrgId && svcVer) {
                        Logger.debug('Loading service {0}/{1} version {2}.', svcOrgId, svcId, svcVer);

                        OrgSvcs.get({ organizationId: svcOrgId, entityType: 'services', entityId: svcId, versionsOrActivity: 'versions', version: svcVer }, function(serviceVersion) {
                            serviceVersion.organizationName = serviceVersion.service.organization.name;
                            serviceVersion.organizationId = serviceVersion.service.organization.id;
                            serviceVersion.name = serviceVersion.service.name;
                            serviceVersion.id = serviceVersion.service.id;
                            resolve(serviceVersion);
                        }, reject);
                    } else {
                        resolve(undefined);
                    }
                })
            };

            $scope.$watch('selectedApp', function(newValue) {
                Logger.debug("App selected: {0}", newValue);
                $scope.selectedAppVersion = undefined;
                $scope.appVersions = [];

                if (newValue) {
                    $scope.refreshAppVersions(newValue.organizationId, newValue.id, function(versions) {
                        Logger.debug("Versions: {0}", versions);

                        if ($rootScope.mruApp) {
                            if ($rootScope.mruApp.application.organization.id == newValue.organizationId && $rootScope.mruApp.application.id == newValue.id) {
                                $scope.selectedAppVersion = $rootScope.mruApp.version;
                            }
                        } else {
                            if (versions.length > 0) {
                                $scope.selectedAppVersion = versions[0];
                            }
                        }
                    });
                }
            });
            
            $scope.selectService = function() {
                Dialogs.selectService('Select a Service', function(serviceVersion) {
                    $scope.selectedService = serviceVersion;
                }, true);
            };

            $scope.$watch('selectedService', function(newValue) {
                if (!newValue) {
                    $scope.plans = undefined;
                    $scope.selectedPlan = undefined;

                    return;
                }

                Logger.debug('Service selection made, fetching plans.');

                OrgSvcs.query({ organizationId: newValue.organizationId, entityType: 'services', entityId: newValue.id, versionsOrActivity: 'versions', version: newValue.version, policiesOrActivity: 'plans' }, function(plans) {
                    $scope.plans = plans;
                    Logger.debug("Found {0} plans: {1}.", plans.length, plans);

                    if (plans.length > 0) {
                        if (planId) {
                            for (var i = 0; i < plans.length; i++) {
                                if (plans[i].planId == planId) {
                                    $scope.selectedPlan = plans[i];
                                }
                            }
                        } else {
                            $scope.selectedPlan = undefined;
                        }
                    } else {
                        $scope.plans = undefined;
                    }
                }, PageLifecycle.handleError);
            });

            $scope.createContract = function() {
                Logger.log("Creating new contract from {0}/{1} ({2}) to {3}/{4} ({5}) through the {6} plan!", 
                        $scope.selectedApp.organizationName, $scope.selectedApp.name, $scope.selectedAppVersion,
                        $scope.selectedService.organizationName, $scope.selectedService.name, $scope.selectedService.version,
                        $scope.selectedPlan.planName);

                $scope.createButton.state = 'in-progress';

                var newContract = {
                    serviceOrgId : $scope.selectedService.organizationId,
                    serviceId : $scope.selectedService.id,
                    serviceVersion : $scope.selectedService.version,
                    planId : $scope.selectedPlan.planId
                };

                OrgSvcs.save({ organizationId: $scope.selectedApp.organizationId, entityType: 'applications', entityId: $scope.selectedApp.id, versionsOrActivity: 'versions', version: $scope.selectedAppVersion, policiesOrActivity: 'contracts' }, newContract, function(reply) {
                    PageLifecycle.redirectTo('/orgs/{0}/apps/{1}/{2}/contracts', $scope.selectedApp.organizationId, $scope.selectedApp.id, $scope.selectedAppVersion);
                }, PageLifecycle.handleError);
            };
            
            PageLifecycle.loadPage('NewContract', undefined, pageData, $scope, function() {
                PageLifecycle.setPageTitle('new-contract');
            });
        }]);
}
