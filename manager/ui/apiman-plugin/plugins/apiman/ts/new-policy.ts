/// <reference path="apimanPlugin.ts"/>
module Apiman {

  export var NewPolicyController = _module.controller("Apiman.NewPolicyController", ['$location','$scope', 'UserSvcs', 'OrgSvcs', 'ApimanSvcs',  ($location,$scope, UserSvcs, OrgSvcs, ApimanSvcs) => {

    ApimanSvcs.query({ entityType: 'policyDefs' },function(policyDefs) {
        $scope.policyDefs = policyDefs;
        $scope.selectedDef = $scope.policyDefs[0];
        $scope.include = 'plugins/apiman/html/policyconfig-' + $scope.selectedDef.id + '.include';
    }, function(error) {
        alert("ERROR=" + error);
    });
    $scope.setPolicyDef = function(policyDef) {
      $scope.selectedDef = policyDef;
      $scope.include = 'plugins/apiman/html/policyconfig-' + $scope.selectedDef.id + '.include';
    };
    $scope.addPolicy = function() {
        OrgSvcs.save({organizationId: $scope.selectedOrg.id, entityType: 'plans'}, $scope.plan, function(reply) {
           $location.path(Apiman.pluginName + '/plan-overview.html').search('org',reply.organization.id).search('plan',reply.name).search('version',$scope.plan.initialVersion);
        }, function(error) {
           if (error.status == 409) {
              $location.path('apiman/error-409.html');          
           } else {
              alert("ERROR=" + error.status + " " + error.statusText);
           }
        });
    };
  }]);

}
