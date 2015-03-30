/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var ServiceContractsController = _module.controller("Apiman.ServiceContractsController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'ServiceEntityLoader', 'OrgSvcs', 'Logger',
        ($q, $scope, $location, PageLifecycle, ServiceEntityLoader, OrgSvcs, Logger) => {
            var params = $location.search();
            $scope.organizationId = params.org;
            $scope.tab = 'contracts';
            $scope.version = params.version;

            var getNextPage = function(successHandler, errorHandler) {
                var maxCount = 10;
                $scope.currentPage = $scope.currentPage + 1;
                OrgSvcs.query({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'contracts', page: $scope.currentPage, count: maxCount  }, function(contracts) {
                   if (contracts.length == maxCount) {
                       $scope.hasMore = true;
                   } else {
                       $scope.hasMore = false;
                   }
                   successHandler(contracts);
                }, errorHandler);
            };

            var dataLoad = ServiceEntityLoader.getCommonData($scope, $location);
            dataLoad = angular.extend(dataLoad, {
                contracts: $q(function(resolve, reject) {
                    $scope.currentPage = 0;
                    getNextPage(resolve, reject);
                }),
                serviceVersion: $q(function(resolve, reject) {
                    OrgSvcs.get({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions', version: params.version }, function(serviceVersion) {
                        resolve(serviceVersion);
                    }, function(error) {
                        reject(error);
                    });
                })
            });
            var promise = $q.all(dataLoad);
            $scope.getNextPage = getNextPage;
            PageLifecycle.loadPage('ServiceContracts', promise, $scope, function() {
                Logger.debug("::: is public: {0}", $scope.serviceVersion.publicService);
                if ($scope.serviceVersion.publicService) {
                    Logger.debug("::: num plans: {0}", $scope.serviceVersion.plans.length);
                    if ($scope.serviceVersion.plans.length == 0) {
                        $scope.isPublicOnly = true;
                    }
                }
                PageLifecycle.setPageTitle('service-contracts', [ $scope.service.name ]);
            });
        }])

}
