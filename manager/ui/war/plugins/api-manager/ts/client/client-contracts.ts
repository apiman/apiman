/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>
module Apiman {
    
    export var ClientContractsController = _module.controller('Apiman.ClientContractsController',
        [
            '$q',
            '$scope',
            '$location',
            '$uibModal',
            'PageLifecycle',
            'ClientEntityLoader',
            'OrgSvcs',
            'Logger',
            '$routeParams',
            'Configuration',
        ($q, $scope, $location, $uibModal, PageLifecycle, ClientEntityLoader, OrgSvcs, Logger, $routeParams, Configuration) => {
            var params = $routeParams;

            $scope.organizationId = params.org;
            $scope.tab = 'contracts';
            $scope.version = params.version;
            $scope.showMetrics = Configuration.ui.metrics;

            var pageData = ClientEntityLoader.getCommonData($scope, $location);

            pageData = angular.extend(pageData, {
                contracts: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'clients', entityId: params.client, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'contracts' }, function(contracts) {
                        $scope.filteredContracts = contracts;
                        resolve(contracts);
                    }, reject);
                })
            });
            
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
                    angular.forEach($scope.contracts, function(contract) {
                        if (contract.apiOrganizationName.toLowerCase().indexOf(value.toLowerCase()) > -1 || contract.apiName.toLowerCase().indexOf(value.toLowerCase()) > -1) {
                            fc.push(contract);
                        }
                    });
                    $scope.filteredContracts = fc;
                }
            };
            
            $scope.breakAll = function(size) {
                var options = {
                    title: 'Break All Contracts?',
                    message: 'Do you really want to break all contracts with all APIs?'
                };
    
                $scope.animationsEnabled = true;
    
                $scope.toggleAnimation = function () {
                    $scope.animationsEnabled = !$scope.animationsEnabled;
                };
    
                var modalInstance = $uibModal.open({
                    animation: $scope.animationsEnabled,
                    templateUrl: 'confirmModal.html',
                    controller: 'ModalConfirmCtrl',
                    size: size,
                    resolve: {
                        options: function () {
                            return options;
                        }
                    }
                });
    
                modalInstance.result.then(function () {
                    OrgSvcs.delete({ organizationId: params.org, entityType: 'clients', entityId: params.client, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'contracts' }, function() {
                        $scope.contracts = [];
                        $scope.filteredContracts = [];
                        $scope.version.modifiedOn = Date.now();
                    }, PageLifecycle.handleError);
                }, function () {
                    //console.log('Modal dismissed at: ' + new Date());
                });
            };
            
            $scope.break = function(contract, size) {
                Logger.debug('Called break() with {0}.', contract);
    
    
                var options = {
                    title: 'Break Contract',
                    message: 'Do you really want to break this contract?'
                };
    
                $scope.animationsEnabled = true;
    
                $scope.toggleAnimation = function () {
                    $scope.animationsEnabled = !$scope.animationsEnabled;
                };
    
                var modalInstance = $uibModal.open({
                    animation: $scope.animationsEnabled,
                    templateUrl: 'confirmModal.html',
                    controller: 'ModalConfirmCtrl',
                    size: size,
                    resolve: {
                        options: function () {
                            return options;
                        }
                    }
                });
    
                modalInstance.result.then(function () {
                    OrgSvcs.delete({ organizationId: params.org, entityType: 'clients', entityId: params.client, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'contracts', policyId: contract.contractId }, function() {
                        removeContractFromArray(contract, $scope.contracts);
                        removeContractFromArray(contract, $scope.filteredContracts);
                        $scope.version.modifiedOn = Date.now();
                    }, PageLifecycle.handleError);
                }, function () {
                    //console.log('Modal dismissed at: ' + new Date());
                });
            };
            
            PageLifecycle.loadPage('ClientContracts', 'clientView', pageData, $scope, function() {
                PageLifecycle.setPageTitle('client-contracts', [ $scope.client.name ]);
            });
        }])

}
