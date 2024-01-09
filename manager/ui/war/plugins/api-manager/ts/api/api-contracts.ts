import {_module} from "../apimanPlugin";
import angular = require("angular");
import {ContractSummaryBean} from "../model/contract.model";

_module.controller("Apiman.ApiContractsController",
    ['$q', '$scope', '$location', 'PageLifecycle', 'ApiEntityLoader', 'OrgSvcs', 'Logger', '$routeParams', 'Configuration', 'ContractService', '$uibModal',
    function ($q, $scope, $location, PageLifecycle, ApiEntityLoader, OrgSvcs, Logger, $routeParams, Configuration, ContractService, $uibModal) {
        var params = $routeParams;
        $scope.organizationId = params.org;
        $scope.tab = 'contracts';
        $scope.version = params.version;
        $scope.showMetrics = Configuration.ui.metrics;

        var getNextPage = function(successHandler, errorHandler) {
            var maxCount = 10;
            $scope.currentPage = $scope.currentPage + 1;

            OrgSvcs.query({ organizationId: params.org, entityType: 'apis', entityId: params.api, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'contracts', page: $scope.currentPage, count: maxCount  }, function(contracts) {
               $scope.hasMore = contracts.length == maxCount;
               successHandler(contracts);
            }, errorHandler);
        };

        var pageData = ApiEntityLoader.getCommonData($scope, $location);

        pageData = angular.extend(pageData, {
            contracts: $q(function(resolve, reject) {
                $scope.currentPage = 0;
                getNextPage(resolve, reject);
                Logger.error('Debugging: {0}', "Henry");
            })
        });

        let handleError = function (err) {
            if (err !== 'cancel') {
                Logger.error('Error while approve/reject: {0}', err)
                PageLifecycle.handleError(err);
            } else {
                Logger.debug('Modal dismissed at: {0}', new Date());
            }
        }

        $scope.approveRequest = (contract: ContractSummaryBean) => {
            Logger.debug("Attempting to approve contract: {0}", contract);

            const options = {
                title: 'Confirm API Signup Request',
                message: 'Do you really want to approve this API Signup request?'
            };

            let modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'confirmModal.html',
                controller: 'ModalConfirmCtrl',
                resolve: {
                    options: function () {
                        return options;
                    }
                }
            });

            modalInstance.result
            .then(() => ContractService.approveContract(contract.contractId))
            .then(() => contract.status = 'Created')
            .catch((err) => handleError(err));
        };

        $scope.rejectRequest = (contract: ContractSummaryBean) => {
            Logger.debug("Attempting to reject contract: {0}", contract);

            const options = {
                title: 'Reject API Signup Request',
                message: 'Do you really want to reject this API Signup request?' +
                    '\nRejected contracts will be automatically deleted.' +
                    '\n\nYou can optionally pass a message to the user.',
                label: 'Rejection Message',
                initialValue: ''
            };

            let modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'getValueModal.html',
                controller: 'ModalGetValueCtrl',
                resolve: {
                    options: function () {
                        return options;
                    }
                }
            });

            modalInstance.result
                .then((rejectionReason) => ContractService.rejectContract(contract.contractId, rejectionReason))
                .then(() => contract.status = 'Rejected')
                .catch((err) => handleError(err));
        };

        $scope.getNextPage = function() {
          getNextPage(function(data) {
            $scope.contracts.push.apply($scope.contracts, data);
          }, function(){
          });
        };

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
    }]);
