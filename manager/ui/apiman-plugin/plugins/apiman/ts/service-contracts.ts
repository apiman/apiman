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
            
            var dataLoad = ServiceEntityLoader.getCommonData($scope, $location);
            if (params.version != null) {
                dataLoad = angular.extend(dataLoad, {
                    contracts: $q(function(resolve, reject) {
                        OrgSvcs.query({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'contracts' }, function(contracts) {
                            resolve(contracts);
                        }, function(error) {
                            reject(error);
                        });
                    })
                });
            }
            var promise = $q.all(dataLoad);
         
            PageLifecycle.loadPage('ServiceContracts', promise, $scope);
        }])

}
