/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

 export var ServiceActivityController = _module.controller("Apiman.ServiceActivityController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'ServiceEntityLoader', 'AuditSvcs',
         ($q, $scope, $location, PageLifecycle, ServiceEntityLoader, AuditSvcs) => {
            var params = $location.search();
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
             
            var dataLoad = ServiceEntityLoader.getCommonData($scope, $location);
            dataLoad = angular.extend(dataLoad, {
                auditEntries: $q(function(resolve, reject) {
                    $scope.currentPage = 0;
                    getNextPage(resolve, reject);
                })
            });
            var promise = $q.all(dataLoad);
            $scope.getNextPage = getNextPage;
            PageLifecycle.loadPage('ServiceActivity', promise, $scope, function() {
                PageLifecycle.setPageTitle('service-activity', [ $scope.service.name ]);
            });
        }])

}
