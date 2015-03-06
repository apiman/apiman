/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
module Apiman {

  export var PlanOverviewController = _module.controller("Apiman.PlanOverviewController", ['$scope', '$location', 'OrgSvcs', 'ActionServices', ($scope, $location, OrgSvcs, ActionServices) => {
    var params = $location.search();

    OrgSvcs.get({organizationId: params.org}, function(org) {
        $scope.org = org;
    } , function(error) {
        alert("ERROR=" + error);
    });
    
    OrgSvcs.get({organizationId: params.org, entityType: 'plans', entityId: params.plan}, function(plan) {
        $scope.plan = plan;
    } , function(error) {
        alert("ERROR=" + error);
    });    

    $scope.setVersion = function(plan) {
      $scope.selectedPlanVersion = plan;
      $location.path( Apiman.pluginName + "/plan-overview.html").search('org', params.org).search('plan', params.plan).search('version', plan.version);
    };

    $scope.lockPlan = function(plan) {
      var lockAction = {
        type: 'lockPlan',
        entityId: plan.id,
        organizationId: plan.organizationId,
        entityVersion: plan.version
      };
      ActionServices.save(lockAction,function(reply) {
        alert("locked");
      } , function(error) {
        alert("ERROR=" + error);
    });
    }

    OrgSvcs.query({organizationId: params.org, entityType: 'plans', entityId: params.plan, versionsOrActivity: 'versions'}, function(versions) {
        $scope.versions = versions;
        if (params.version != null) { 
          for (var i=0; i<versions.length; i++) {
            if (params.version == versions[i].version) { 
              $scope.selectedPlanVersion = versions[i];
              break;
            }
          }
        } else {
           $scope.selectedPlanVersion = versions[0];
        }
    } , function(error) {
        alert("ERROR=" + error);
    });
    OrgSvcs.query({organizationId: params.org, entityType: 'members'}, function(members) {
        $scope.members = members;
    } , function(error) {
        alert("ERROR=" + error);
    });
  }])
  

}
