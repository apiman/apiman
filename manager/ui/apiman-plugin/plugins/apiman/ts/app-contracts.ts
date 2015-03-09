/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
module Apiman {

    export var AppOverviewController = _module.controller("Apiman.AppOverviewController",
        ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', ($q, $scope, $location, OrgSvcs, PageLifecycle) => {
            var params = $location.search();
            $scope.version = params.version;
            var promise = $q.all({
                org: $q(function(resolve, reject) {
                    OrgSvcs.get({ organizationId: params.org }, function(org) {
                        resolve(org);
                    }, function(error) {
                        reject(error);
                    });
                }),
                app: $q(function(resolve, reject) {
                    OrgSvcs.get({ organizationId: params.org, entityType: 'applications', entityId: params.app }, function(app) {
                        resolve(app);
                    }, function(error) {
                        reject(error);
                    });
                }),
                versions: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'applications', entityId: params.app, versionsOrActivity: 'versions', version: $scope.version }, function(versions) {
                        if (params.version != null) {
                            for (var i = 0; i < versions.length; i++) {
                                if (params.version == versions[i].version) {
                                    $scope.selectedAppVersion = versions[i];
                                    break;
                                }
                            }
                        } else {
                            $scope.selectedAppVersion = versions[0];
                        }
                        resolve(versions);
                    }, function(error) {
                        reject(error);
                    });
                }),
                members: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'members' }, function(members) {
                        resolve(members);
                    }, function(error) {
                        reject(error);
                    });
                })
            });

            $scope.setVersion = function(app) {
                $scope.selectedAppVersion = app;
                $location.path(Apiman.pluginName + "/app-overview.html").search('org', params.org).search('app', params.app).search('version', app.version);
            };
            
            PageLifecycle.loadPage('AppContracts', promise, $scope);
        }])

}
