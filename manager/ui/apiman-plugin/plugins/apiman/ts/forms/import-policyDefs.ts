/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var ImportPolicyDefsController = _module.controller("Apiman.ImportPolicyDefsController",
        ['$q', '$scope', '$location', 'ApimanSvcs', 'PageLifecycle',
        ($q, $scope, $location, ApimanSvcs, PageLifecycle) => {
            var params = $location.search();
            $scope.isData = true;
            $scope.isConfirm = false;
            
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
            
            $scope.importPolicyDefs = function() {
                $scope.yesButton.state = 'in-progress';
                var promises = [];
                angular.forEach($scope.policyDefs, function(def) {
                    promises.push($q(function(resolve, reject) {
                        ApimanSvcs.save({ entityType: 'policyDefs'}, def, resolve, reject);
                    }));
                });
                $q.all(promises).then(function() {
                    $location.url(pluginName + '/admin-policyDefs.html');
                }, PageLifecycle.handleError);
            }
            
            PageLifecycle.loadPage('ImportPolicyDefs', undefined, $scope, function() {
                PageLifecycle.setPageTitle('import-policyDefs');
            });
    }])

}
