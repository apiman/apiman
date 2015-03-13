/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
module Apiman {
    
    export var AppContractsController = _module.controller("Apiman.AppContractsController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'AppEntityLoader', 'OrgSvcs', 'Logger', 'Dialogs',
        ($q, $scope, $location, PageLifecycle, AppEntityLoader, OrgSvcs, Logger, Dialogs) => {
            var params = $location.search();
            $scope.organizationId = params.org;
            $scope.tab = 'contracts';
            $scope.version = params.version;
            var dataLoad = AppEntityLoader.getCommonData($scope, $location);
            dataLoad = angular.extend(dataLoad, {
                contracts: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'applications', entityId: params.app, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'contracts' }, function(contracts) {
                        $scope.filteredContracts = contracts;
                        resolve(contracts);
                    }, function(error) {
                        reject(error);
                    });
                })
            });
            var promise = $q.all(dataLoad);
            
            function removeContractFromArray(contract, carray) {
                var idx = -1;
                for (var i = 0; i < carray.length; i++) {
                    if (carray[i].contractId == contract.contractId) {
                        idx = i;
                        break;
                    }
                }
                if (idx > -1) {
                    carray.splice(idx, 1);
                }
            };
            
            $scope.filterContracts = function(value) {
                Logger.debug('Called filterContracts!');
                if (!value) {
                    $scope.filteredContracts = $scope.contracts;
                } else {
                    var fc = [];
                    for (var i = 0; i < $scope.contracts.length; i++) {
                        var contract = $scope.contracts[i];
                        if (contract.serviceOrganizationName.toLowerCase().indexOf(value) > -1 || contract.serviceName.toLowerCase().indexOf(value) > -1) {
                            fc.push(contract);
                        }
                    }
                    $scope.filteredContracts = fc;
                }
            };
            
            $scope.breakAll = function() {
                Dialogs.confirm('Break All Contracts?', 'Do you really want to break all contracts with all services?', function() {
                    OrgSvcs.delete({ organizationId: params.org, entityType: 'applications', entityId: params.app, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'contracts' }, function() {
                        $scope.contracts = [];
                        $scope.filteredContracts = [];
                    }, function(error) {
                        alert(error);
                    });
                });
            };
            
            $scope.break = function(contract) {
                Logger.debug("Called break() with {0}.", contract);
                Dialogs.confirm('Break Contract', 'Do you really want to break this contract?', function() {
                    OrgSvcs.delete({ organizationId: params.org, entityType: 'applications', entityId: params.app, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'contracts', policyId: contract.contractId }, function() {
                        removeContractFromArray(contract, $scope.contracts);
                        removeContractFromArray(contract, $scope.filteredContracts);
                    }, function(error) {
                        alert(error);
                    });
                });
            };
            
            PageLifecycle.loadPage('AppContracts', promise, $scope);
        }])

}
