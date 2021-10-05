/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>

import {Editor, HookCallback, HookMap} from "@toast-ui/editor/types/editor";

module Apiman {
  import ApiPlanSummaryBean = ApimanRPC.ApiPlanSummaryBean;
  import UpdateApiVersionBean = ApimanRPC.UpdateApiVersionBean;
  import ApiVersionBean = ApimanRPC.ApiVersionBean;
  import BlobRef = ApimanRPC.BlobRef;

  export var ApiDevPortalController = _module.controller( "Apiman.DevPortalController",
      ['$q', '$scope', '$location', 'PageLifecycle', 'ApiEntityLoader', 'OrgSvcs', 'ApimanSvcs', '$routeParams', 'Configuration', 'EntityStatusSvc', 'DevPortalService', 'BlobService', 'Logger', '$window',
        ($q, $scope, $location, PageLifecycle, ApiEntityLoader, OrgSvcs, ApimanSvcs, $routeParams, Configuration, EntityStatusSvc, DevPortalService, BlobService, Logger, $window) => {
          const params = $routeParams;
          $scope.organizationId = params.org;
          $scope.tab = 'devportal';
          $scope.version = params.version;
          $scope.showMetrics = Configuration.ui.metrics;
          $scope.isEntityDisabled = EntityStatusSvc.isEntityDisabled;
          $scope.isDirty = false;
          $scope.apiVersion = null as ApiVersionBean;
          let apiVersionCopy = null as ApiVersionBean;
          $scope.planSummaries = [] as ApiPlanSummaryBean[];

          // Load basic page data
          const pageData = ApiEntityLoader.getCommonData($scope, $location);
          PageLifecycle.loadPage('DevPortal', 'apiView', pageData, $scope, function () {
            //$scope.reset();
            PageLifecycle.setPageTitle('api-devportal', [$scope.api.name]);
          });

          const uploadImage = (blob: Blob | File, callback: HookCallback): void => {
            BlobService.uploadBlob(blob).then(
                (blobRef: BlobRef) => {
                  Logger.debug("Uploaded successfully: {0}", blobRef);
                  callback(blobRef.id);
                },
                (failed) => {
                  Logger.debug("Upload failed: {0}", failed);
                }
            );
          }

          // Load Markdown Editor
          const ed: Editor = new $window.editor({
            el: document.querySelector('#editor'),
            height: '500px',
            initialEditType: 'markdown',
            previewStyle: 'tab',
            usageStatistics: false,
            hooks: {
              addImageBlobHook: uploadImage
            } as HookMap,
            plugins: [[$window.codeSyntaxHighlightPlugin, { highlighter: $window.prism }]]
          }) as Editor;

          // Get the API Version Plan summaries
          DevPortalService.getApiVersionPlans(params.org, params.api, params.version).then(
              (apiPlans: ApiPlanSummaryBean[]) => {
                Logger.info("Got plans: {0}", apiPlans);
                $scope.planSummaries.push(...apiPlans);
              }
          )

          // Get the API Version and bind to $scope.apiVersion, and initialise MD Editor
          DevPortalService.getApiVersion(params.org, params.api, params.version).then(
              (apiVersion: ApiVersionBean) => {
                Logger.info("Got API Version {0}", apiVersion);
                $scope.apiVersion = apiVersion as ApiVersionBean;
                apiVersionCopy = angular.copy(apiVersion) as ApiVersionBean;

                Logger.info("Setting extended description to: {0}", apiVersion.extendedDescription);
                ed.setMarkdown(
                    apiVersion.extendedDescription,
                    true
                );

                // Dirty check after loading, otherwise we'll get spurious matches.
                $scope.$watch('apiVersion', (oldValue, newValue) => {
                  if (angular.equals(oldValue, newValue)) {
                    return;
                  }
                  // Logger.debug("Dirty set to true {0} vs {1}", oldValue, newValue);
                  $scope.isDirty = true;
                }, true);
              },
              (failure) => handleFailure(failure)
          );

          /** Save, and reset **/
          // Reset (copy saved pristine copy back over).
          $scope.reset = () => {
            $scope.apiVersion = angular.copy(apiVersionCopy) as ApiVersionBean;
            $scope.isDirty = false;
          };

          // Save
          $scope.doSave = (clickInfo) => {
            const markdown: string = ed.getMarkdown();
            const updateApiVersionBean = {
              extendedDescription: markdown,
              exposeInPortal: $scope.apiVersion.exposeInPortal,
              plans: $scope.apiVersion.plans
            } as UpdateApiVersionBean;

            DevPortalService.updateApiVersion(params.org, params.api, params.version, updateApiVersionBean).then(
                (ok) => {
                  Logger.info("Api Version update succeeded!");
                  $scope.isDirty = false;
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