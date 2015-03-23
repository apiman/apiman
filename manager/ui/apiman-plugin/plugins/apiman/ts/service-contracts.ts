/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
module Apiman {

 export var ServiceContractsController = _module.controller("Apiman.ServiceContractsController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'ServiceEntityLoader', 'OrgSvcs',
         ($q, $scope, $location, PageLifecycle, ServiceEntityLoader, OrgSvcs) => {
            var params = $location.search();
            $scope.organizationId = params.org;
            $scope.tab = 'contracts';
            $scope.version = params.version;
            
            var getNextPage = function(successHandler, errorHandler) {
                var maxCount = 10;
                $scope.currentPage = $scope.currentPage + 1;
                OrgSvcs.query({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'contracts', page: $scope.currentPage, count: maxCount  }, function(contracts) {
                   if (contracts.length == maxCount) $scope.hasMore = true;
                   else $scope.hasMore = false;
                   successHandler(contracts);
                }, errorHandler);
            };
             
            var dataLoad = ServiceEntityLoader.getCommonData($scope, $location);
            if (params.version != null) {
                dataLoad = angular.extend(dataLoad, {
                    contracts: $q(function(resolve, reject) {
                        $scope.currentPage = 0;
                        getNextPage(resolve, reject);
                    })
                });
            }
            var promise = $q.all(dataLoad);
            $scope.getNextPage = getNextPage;
            PageLifecycle.loadPage('ServiceContracts', promise, $scope);
        }])

}
