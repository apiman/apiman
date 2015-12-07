/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>
module Apiman {

    export var ApiContractsController = _module.controller("Apiman.ApiContractsController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'ApiEntityLoader', 'OrgSvcs', 'Logger', '$routeParams', 'Configuration',
        ($q, $scope, $location, PageLifecycle, ApiEntityLoader, OrgSvcs, Logger, $routeParams, Configuration) => {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'contracts';
            $scope.version = params.version;
            $scope.showMetrics = Configuration.ui.metrics;

            var getNextPage = function(successHandler, errorHandler) {
                var maxCount = 10;
                $scope.currentPage = $scope.currentPage + 1;

                OrgSvcs.query({ organizationId: params.org, entityType: 'apis', entityId: params.api, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'contracts', page: $scope.currentPage, count: maxCount  }, function(contracts) {
                   if (contracts.length == maxCount) {
                       $scope.hasMore = true;
                   } else {
                       $scope.hasMore = false;
                   }
                   successHandler(contracts);
                }, errorHandler);
            };

            var pageData = ApiEntityLoader.getCommonData($scope, $location);

            pageData = angular.extend(pageData, {
                contracts: $q(function(resolve, reject) {
                    $scope.currentPage = 0;
                    getNextPage(resolve, reject);
                })
            });
            
            $scope.getNextPage = getNextPage;

            PageLifecycle.loadPage('ApiContracts', 'apiView', pageData, $scope, function() {
                Logger.debug("::: is public: {0}", $scope.version.publicAPI);

                if ($scope.version.publicAPI) {
                    Logger.debug("::: num plans: {0}", $scope.version.plans.length);
                    if ($scope.version.plans.length == 0) {
                        $scope.isPublicOnly = true;
                    }
                }

                PageLifecycle.setPageTitle('api-contracts', [ $scope.api.name ]);
            });
        }])

}
