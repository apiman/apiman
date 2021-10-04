/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>
//const Editor = require('@toast-ui/editor');

module Apiman {
  import ApiPlanSummaryBean = ApimanRPC.ApiPlanSummaryBean;
  import UpdateApiVersionBean = ApimanRPC.UpdateApiVersionBean;
  import ApiVersionBean = ApimanRPC.ApiVersionBean;
  import ApiPlanBean = ApimanRPC.ApiPlanBean;

  export var ApiDevPortalController = _module.controller( "Apiman.DevPortalController",
      ['$q', '$scope', '$location', 'PageLifecycle', 'ApiEntityLoader', 'OrgSvcs', 'ApimanSvcs', '$routeParams', 'Configuration', 'EntityStatusSvc', 'DevPortalService', 'Logger', '$window',
        ($q, $scope, $location, PageLifecycle, ApiEntityLoader, OrgSvcs, ApimanSvcs, $routeParams, Configuration, EntityStatusSvc, DevPortalService, Logger, $window) => {
          const params = $routeParams;
          $scope.organizationId = params.org;
          $scope.tab = 'devportal';
          $scope.version = params.version;
          $scope.showMetrics = Configuration.ui.metrics;
          $scope.isEntityDisabled = EntityStatusSvc.isEntityDisabled;

          var pageData = ApiEntityLoader.getCommonData($scope, $location);

          $scope.plans = {
            summaries: [] as ApiPlanSummaryBean[]
          };
          $scope.apiVersion = null as ApiVersionBean;

          const ed = new $window.editor({
            el: document.querySelector('#editor'),
            height: '500px',
            initialEditType: 'markdown',
            previewStyle: 'tab',
            usageStatistics: false
          });

          // Get the API Version and bind to $scope.apiVersion, and initialise MD Editor
          DevPortalService.getApiVersion(params.org, params.api, params.version).then(
              (apiVersion) => {
                Logger.info("Got API Version {0}", apiVersion);
                $scope.apiVersion = apiVersion;

                Logger.info("Setting extended description to: {0}", apiVersion.extendedDescription);
                ed.setMarkdown(
                    apiVersion.extendedDescription,
                    true
                )
              },
              (failure) => handleFailure(failure)
          );

          // Get the API Version Plan summaries
          DevPortalService.getApiVersionPlans(params.org, params.api, params.version).then(
              (apiPlans: ApiPlanSummaryBean[]) => {
                  Logger.info("Got plans: {0}", apiPlans);
                  $scope.plans.summaries = apiPlans;
              },
              (failure) => handleFailure(failure)
          )

          $scope.doSave = (clickInfo) => {
            // Plans to send to backend. Look at the summaries (where changes were applied) and make a corresponding ApiPlanBean
            // Which we will then PUT to the backend.
            const updatedPlans: ApiPlanBean[] = $scope.plans.summaries.map((apiPlanSummary: ApiPlanSummaryBean) => {
              return {
                planId: apiPlanSummary.planId,
                exposeInPortal: apiPlanSummary.exposeInPortal,
                version: apiPlanSummary.version,
                requiresApproval: apiPlanSummary.requiresApproval
              } as ApiPlanBean
            })

            // TODO(msavy): check for weird states in editor?
            const markdown: string = ed.getMarkdown();
            const updateApiVersionBean = {
              extendedDescription: markdown,
              exposeInPortal: $scope.apiVersion.exposeInPortal,
              plans: updatedPlans
            } as UpdateApiVersionBean;

            DevPortalService.updateApiVersion(params.org, params.api, params.version, updateApiVersionBean).then(
                (ok) => {
                  Logger.info("Api Version update succeeded!");
                },
                (failure) => handleFailure(failure)
            );
          }

          function handleFailure(failure: any) {
            // TODO(msavy): do something useful here...
            Logger.error("failure {0}", failure.data || failure);
          }
        }]);
}