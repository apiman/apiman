/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var ImportPolicyDefsController = _module.controller("Apiman.ImportPolicyDefsController",
        ['$q', '$scope', '$location', 'ApimanSvcs', 'PageLifecycle', ($q, $scope, $location, ApimanSvcs, PageLifecycle) => {
            
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
                for (var i=0; i<$scope.policyDefs.length; i++) {
                    ApimanSvcs.save({ entityType: 'policyDefs'}, $scope.policyDefs[i], function(reply) {
                       
                    }, function(error) {
                        if (error.status == 409) {
                            $location.path('apiman/error-409.html');
                        } else {
                            $scope.createButton.state = 'error';
                            alert("ERROR=" + error.status + " " + error.statusText);
                        }
                    });
                    $location.path(pluginName + '/admin-policyDefs.html');
                }
            }
            
            PageLifecycle.loadPage('ImportPolicyDefs', undefined, $scope);
    }])

}
