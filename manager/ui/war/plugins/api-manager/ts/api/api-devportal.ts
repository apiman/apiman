/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>
module Apiman {
  import ApiPlanSummaryBean = ApimanRPC.ApiPlanSummaryBean;

  export var ApiDevPortalController = _module.controller( "Apiman.DevPortalController",
      ['$q', '$scope', '$location', 'PageLifecycle', 'ApiEntityLoader', 'OrgSvcs', 'ApimanSvcs', '$routeParams', 'Configuration', 'EntityStatusSvc', 'OrgService2', 'Logger',
        ($q, $scope, $location, PageLifecycle, ApiEntityLoader, OrgSvcs, ApimanSvcs, $routeParams, Configuration, EntityStatusSvc, OrgService2, Logger) => {
          const params = $routeParams;
          $scope.organizationId = params.org;
          $scope.tab = 'devportal';
          $scope.version = params.version;
          $scope.updatedApi = new Object();
          $scope.showMetrics = Configuration.ui.metrics;
          $scope.isEntityDisabled = EntityStatusSvc.isEntityDisabled;

          var pageData = ApiEntityLoader.getCommonData($scope, $location);
          $scope.plans = {
            summaries: Array<ApiPlanSummaryBean>
          };

          if (params.version != null) {
            OrgService2.getApiVersionPlans(params.org, params.api, params.version).then(
                (apiPlans: ApiPlanSummaryBean[]) => {
                    Logger.info("Got plans: {0}", apiPlans);
                    $scope.plans.summaries = apiPlans;
                },
                (failure) => handleFailure(failure)
            )
          }

          function handleFailure(failure: any) {
            // do something useful...
          }
        }]);
}