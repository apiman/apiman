/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var ImportPolicyDefsController = _module.controller("Apiman.ImportPolicyDefsController",
        ['$q', '$scope', '$location', 'ApimanSvcs', 'PageLifecycle',
        ($q, $scope, $location, ApimanSvcs, PageLifecycle) => {
            $scope.isData = true;
            $scope.isConfirm = false;
            $scope.isValid = false;
            
            $scope.parseJSON  = function() {
                var policiesImport = JSON.parse($scope.policyDefsJSON);
                var policyDefs:any = [];
                if (policiesImport.constructor === Array) {
                    policyDefs = policiesImport;
                } else {
                    policyDefs.push(policiesImport);
                }
                $scope.policyDefs = policyDefs;
                $scope.isData = false;
                $scope.isConfirm = true;
            }
            
            $scope.$watch('policyDefsJSON', function(newValue) {
                try {
                    JSON.parse($scope.policyDefsJSON);
                    $scope.isValid = true;
                } catch (e) {
                    $scope.isValid = false;
                }
            });
            
            $scope.importPolicyDefs = function() {
                $scope.yesButton.state = 'in-progress';
                var promises = [];
                angular.forEach($scope.policyDefs, function(def) {
                    promises.push($q(function(resolve, reject) {
                        ApimanSvcs.save({ entityType: 'policyDefs'}, def, resolve, reject);
                    }));
                });
                $q.all(promises).then(function() {
                    PageLifecycle.redirectTo('/admin/policyDefs');
                }, PageLifecycle.handleError);
            }
            
            PageLifecycle.loadPage('ImportPolicyDefs', undefined, $scope, function() {
                PageLifecycle.setPageTitle('import-policyDefs');
            });
    }])

}
