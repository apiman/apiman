/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>

//import {Editor, HookCallback, HookMap} from "@toast-ui/editor/types/editor";

module Apiman {
  import ApiPlanSummaryBean = ApimanRPC.ApiPlanSummaryBean;
  import UpdateApiVersionBean = ApimanRPC.UpdateApiVersionBean;
  import ApiVersionBean = ApimanRPC.ApiVersionBean;
  import BlobRef = ApimanRPC.BlobRef;

  export var ApiDevPortalController = _module.controller( 'Apiman.DevPortalController',
      ['$q', '$scope', '$rootScope', '$location', 'PageLifecycle', 'ApiEntityLoader', 'OrgSvcs', 'ApimanSvcs', '$routeParams', 'Configuration', 'EntityStatusSvc', 'DevPortalService', 'BlobService', 'Logger', '$window', '$interval',
        ($q, $scope, $rootScope, $location, PageLifecycle, ApiEntityLoader, OrgSvcs, ApimanSvcs, $routeParams, Configuration, EntityStatusSvc, DevPortalService, BlobService, Logger, $window, $interval) => {
          const params = $routeParams;
          $scope.organizationId = params.org;
          $scope.tab = 'devportal';
          $scope.version = params.version;
          $scope.showMetrics = Configuration.ui.metrics;
          $scope.isEntityDisabled = EntityStatusSvc.isEntityDisabled;
          $rootScope.isDirty = false;
          $scope.apiVersion = null as ApiVersionBean;
          let apiVersionCopy = null as ApiVersionBean;
          $scope.planSummaries = [] as ApiPlanSummaryBean[];

          // Load basic page data
          const pageData = ApiEntityLoader.getCommonData($scope, $location);
          PageLifecycle.loadPage('DevPortal', 'apiView', pageData, $scope, function () {
            //$scope.reset();
            PageLifecycle.setPageTitle('api-devportal', [$scope.api.name]);
          });

          const uploadImage = (blob: Blob | File, callback): void => {
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

          /*** Markdown Editor ***/
          // Load Markdown Editor
          const ed = new $window.editor({
            el: document.querySelector('#editor'),
            height: '500px',
            initialEditType: 'markdown',
            previewStyle: 'tab',
            usageStatistics: false,
            hooks: {
              addImageBlobHook: uploadImage
            },
            events: {
              blur: () => editorDirtyCheck
            },
            plugins: [[$window.codeSyntaxHighlightPlugin, { highlighter: $window.prism }]]
          });

          // Dirty check the MD editor pane every 2 seconds (avoids excessive checking).
          $interval(() => {
            editorDirtyCheck();
          }, 1000);

          const editorDirtyCheck = (): void => {
            if ($scope.apiVersion.extendedDescription !== ed.getMarkdown()) {
              $rootScope.isDirty = true;
            }
          }

          /*** End Markdown Editor ***/

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
                  $rootScope.isDirty = true;
                }, true);
              },
              (failure) => handleFailure(failure)
          );

          /** Save, and reset **/
          // Reset (copy saved pristine copy back over).
          $scope.reset = () => {
            $scope.apiVersion = angular.copy(apiVersionCopy) as ApiVersionBean;
            ed.setMarkdown($scope.apiVersion.extendedDescription, true);
            $rootScope.isDirty = false;
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
                (_) => {
                  Logger.info("Api Version update succeeded!");
                  $rootScope.isDirty = false;
                },
                (failure) => handleFailure(failure)
            );
          }

          function handleFailure(failure: any): void {
            // TODO(msavy): do something useful here...
            Logger.error("failure {0}", failure.data || failure);
          }
        }]);
}