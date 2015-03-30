/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var OrgActivityController = _module.controller("Apiman.OrgActivityController",
        ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', '$rootScope', 'AuditSvcs',
        ($q, $scope, $location, OrgSvcs, PageLifecycle, $rootScope, AuditSvcs) => {
            var params = $location.search();
            $scope.organizationId = params.org;
            
            var getNextPage = function(successHandler, errorHandler) {
                $scope.currentPage = $scope.currentPage + 1;
                AuditSvcs.get({ organizationId: params.org, page: $scope.currentPage, count: 20 }, function(results) {
                    var entries = results.beans;
                    successHandler(entries);
                }, errorHandler);
            };
            
            var promise = $q.all({
                org: $q(function(resolve, reject) {
                    OrgSvcs.get({ organizationId: params.org, entityType: '' }, function(org) {
                        $rootScope.mruOrg = org;
                        resolve(org);
                    }, reject);
                }),
                members: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'members' }, function(members) {
                        resolve(members);
                    }, reject);
                }),
                auditEntries: $q(function(resolve, reject) {
                    $scope.currentPage = 0;
                    getNextPage(resolve, reject);
                })
            });
            $scope.getNextPage = getNextPage;
            PageLifecycle.loadPage('OrgActivity', promise, $scope, function() {
                PageLifecycle.setPageTitle('org-activity', [ $scope.org.name ]);
            });
        }])

}
