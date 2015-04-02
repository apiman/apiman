/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

 export var ServiceActivityController = _module.controller("Apiman.ServiceActivityController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'ServiceEntityLoader', 'AuditSvcs', '$routeParams',
         ($q, $scope, $location, PageLifecycle, ServiceEntityLoader, AuditSvcs, $routeParams) => {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'activity';
            $scope.version = params.version;
            
            var getNextPage = function(successHandler, errorHandler) {
                $scope.currentPage = $scope.currentPage + 1;
                AuditSvcs.get({ organizationId: params.org, entityType: 'services', entityId: params.service, page: $scope.currentPage, count: 20 }, function(results) {
                    var entries = results.beans;
                    successHandler(entries);
                }, errorHandler);
            };
             
            var pageData = ServiceEntityLoader.getCommonData($scope, $location);
            pageData = angular.extend(pageData, {
                auditEntries: $q(function(resolve, reject) {
                    $scope.currentPage = 0;
                    getNextPage(resolve, reject);
                })
            });
            
            $scope.getNextPage = getNextPage;
            PageLifecycle.loadPage('ServiceActivity', pageData, $scope, function() {
                PageLifecycle.setPageTitle('service-activity', [ $scope.service.name ]);
            });
        }])

}
