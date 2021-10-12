import { _module } from "../apimanPlugin";
import {
  ApiPlanSummaryBean,
  ApiVersionBean,
  UpdateApiVersionBean,
} from "../model/api.model";
import Prism from "prismjs";
import { Editor, EditorOptions } from "@toast-ui/editor";
import { BlobRef } from "../model/blob.model";
import angular = require("angular");
// Use CommonJS syntax for tui stuff
const toast = require("@toast-ui/editor");
const codeSyntaxHighlight = require("@toast-ui/editor-plugin-code-syntax-highlight/dist/toastui-editor-plugin-code-syntax-highlight-all");
const colourSyntax = require("@toast-ui/editor-plugin-color-syntax");

_module.controller("Apiman.DevPortalController", [
  "$q",
  "$scope",
  "$rootScope",
  "$location",
  "PageLifecycle",
  "ApiEntityLoader",
  "OrgSvcs",
  "ApimanSvcs",
  "$routeParams",
  "Configuration",
  "EntityStatusSvc",
  "DevPortalService",
  "BlobService",
  "Logger",
  "$interval",
  function (
      $q,
      $scope,
      $rootScope,
      $location,
      PageLifecycle,
      ApiEntityLoader,
      OrgSvcs,
      ApimanSvcs,
      $routeParams,
      Configuration,
      EntityStatusSvc,
      DevPortalService,
      BlobService,
      Logger,
      $interval
  ) {
    const params = $routeParams;
    $scope.organizationId = params.org;
    $scope.tab = "devportal";
    $scope.version = params.version;
    $scope.showMetrics = Configuration.ui.metrics;
    $scope.isEntityDisabled = EntityStatusSvc.isEntityDisabled;
    $rootScope.isDirty = false;
    $scope.apiVersion = null as ApiVersionBean;
    let apiVersionCopy = null as ApiVersionBean;
    $scope.planSummaries = [] as ApiPlanSummaryBean[];
    let markdownEditor: Editor = null;

    // Load basic page data
    const pageData = ApiEntityLoader.getCommonData($scope, $location);
    PageLifecycle.loadPage(
        "DevPortal",
        "apiView",
        pageData,
        $scope,
        function () {
          //$scope.reset();
          PageLifecycle.setPageTitle("api-devportal", [$scope.api.name]);
        }
    );

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
    };

    /*** Markdown Editor ***/

    const initEditor = (): void => {
      const options: EditorOptions = {
        el: document.querySelector("#editor"),
        height: "500px",
        initialEditType: "markdown",
        previewStyle: "tab",
        usageStatistics: false,
        hooks: {
          addImageBlobHook: uploadImage,
        },
        events: {
          // blur: () => editorDirtyCheck,
        },
        plugins: [
          [codeSyntaxHighlight, {highlighter: Prism}],
          [colourSyntax, {}],
        ],
        linkAttributes: {
          target: "_blank",
          rel: "noopener",
        },
      };

      markdownEditor = new toast(options);
      Logger.info(
          "Setting extended description to: {0}",
          $scope.apiVersion.extendedDescription
      );
      markdownEditor.setMarkdown($scope.apiVersion.extendedDescription, true);
    };

    // Dirty check the MD editor pane every 2 seconds (avoids excessive checking).
    const intervalPromise: Promise<any> = $interval(() => {
      editorDirtyCheck();
    }, 1000);

    const editorDirtyCheck = (): void => {
      if (markdownEditor == null) {
        initEditor();
      }
      let latestDescription = markdownEditor.getMarkdown();
      // If original description != latestDescription, and latest poll != previous poll
      if (apiVersionCopy.extendedDescription !== latestDescription
          && $scope.apiVersion.extendedDescription !== latestDescription) {
        $rootScope.isDirty = true;
        $scope.apiVersion.extendedDescription = latestDescription;
      }
    };

    /*** End Markdown Editor ***/

    // Get the API Version Plan summaries
    DevPortalService.getApiVersionPlans(
        params.org,
        params.api,
        params.version
    ).then((apiPlans: ApiPlanSummaryBean[]) => {
      Logger.info("Got plans: {0}", apiPlans);
      $scope.planSummaries.push(...apiPlans);
    });

    // Get the API Version and bind to $scope.apiVersion, and initialise MD Editor
    DevPortalService.getApiVersion(params.org, params.api, params.version).then(
        (apiVersion: ApiVersionBean) => {
          Logger.info("Got API Version {0}", apiVersion);
          $scope.apiVersion = apiVersion as ApiVersionBean;
          apiVersionCopy = angular.copy(apiVersion) as ApiVersionBean;

          // Dirty check after loading, otherwise we'll get spurious matches.
          $scope.$watch(
              "apiVersion",
              (oldValue, newValue) => {
                if (!angular.equals(oldValue, newValue)) {
                  Logger.debug("Dirty set to true {0} vs {1}", oldValue, newValue);
                  $rootScope.isDirty = true;
                }
              },
              true
          );

          if (apiVersion.exposeInPortal) {
            Logger.info("Sending init to editor");
            initEditor();
          }
        },
        (failure) => handleFailure(failure)
    );

    /** Save, and reset **/
    // Reset (copy saved pristine copy back over).
    $scope.reset = () => {
      $scope.apiVersion = angular.copy(apiVersionCopy) as ApiVersionBean;
      markdownEditor.setMarkdown($scope.apiVersion.extendedDescription, true);
      $rootScope.isDirty = false;
    };

    $scope.$on('$destroy', function () {
      $interval.cancel(intervalPromise);
    });

    // Save
    $scope.doSave = (clickInfo) => {
      const markdown: string = markdownEditor.getMarkdown();
      const updateApiVersionBean = {
        extendedDescription: markdown,
        exposeInPortal: $scope.apiVersion.exposeInPortal,
        plans: $scope.apiVersion.plans,
      } as UpdateApiVersionBean;

      DevPortalService.updateApiVersion(
          params.org,
          params.api,
          params.version,
          updateApiVersionBean
      ).then(
          (_) => {
            Logger.info("Api Version update succeeded!");
            $rootScope.isDirty = false;
          },
          (failure) => handleFailure(failure)
      );
    };

    function handleFailure(failure: any): void {
      // TODO(msavy): do something useful here...
      Logger.error("failure {0}", failure.data || failure);
    }
  },
]);
