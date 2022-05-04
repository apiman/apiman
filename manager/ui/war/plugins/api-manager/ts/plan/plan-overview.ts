import {_module} from "../apimanPlugin";

_module.controller("Apiman.PlanOverviewController",
      ['$q', '$scope', '$location', 'PageLifecycle', 'PlanEntityLoader', '$routeParams',
      function ($q, $scope, $location, PageLifecycle, PlanEntityLoader, $routeParams) {
          var params = $routeParams;
          $scope.organizationId = params.org;
          $scope.tab = 'overview';
          var pageData = PlanEntityLoader.getCommonData($scope, $location);

          PageLifecycle.loadPage('PlanOverview', 'planView', pageData, $scope, function() {
              PageLifecycle.setPageTitle('plan-overview', [ $scope.plan.name ]);
          });
      }]);
