import { _module } from "../apimanPlugin";
import {
  ApiBean,
  ApiPlanSummaryBean,
  ApiVersionBean,
  KeyValueTagDto,
  UpdateApiBean,
  UpdateApiVersionBean,
} from "../model/api.model";

// CSS
import 'prismjs/themes/prism.css'
import '@toast-ui/editor-plugin-code-syntax-highlight/dist/toastui-editor-plugin-code-syntax-highlight.css'
import '@toast-ui/editor/dist/toastui-editor.css'
import 'tui-color-picker/dist/tui-color-picker.css'
import '@toast-ui/editor-plugin-color-syntax/dist/toastui-editor-plugin-color-syntax.css'

// Markdown editor and code highlight plugin (for editor)
import '@toast-ui/editor';
import '@toast-ui/editor-plugin-code-syntax-highlight/dist/toastui-editor-plugin-code-syntax-highlight-all';
import 'prismjs';

import Prism from "prismjs";
import { Editor, EditorOptions } from "@toast-ui/editor";
import { BlobRef } from "../model/blob.model";
import Cropper from "cropperjs/dist/cropper.esm";
import "cropperjs/dist/cropper.css";
import angular = require("angular");
import { remove as _remove } from "lodash-es";
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
  "$uibModal",
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
      $interval,
      $uibModal
  ) {
    const pageData = ApiEntityLoader.getCommonData($scope, $location);
    const params = $routeParams;
    $scope.organizationId = params.org;
    $scope.tab = "devportal";
    $scope.version = params.version;
    $scope.showMetrics = Configuration.ui.metrics;
    $scope.isEntityDisabled = EntityStatusSvc.isEntityDisabled;
    $scope.organizationId = params.org;
    $scope.tab = "devportal";
    $scope.version = params.version;
    $scope.showMetrics = Configuration.ui.metrics;
    $scope.isEntityDisabled = EntityStatusSvc.isEntityDisabled;
    $rootScope.isDirty = false;

    PageLifecycle.loadPage(
        "DevPortal",
        "apiView",
        pageData,
        $scope,
        function () {
          PageLifecycle.setPageTitle("api-devportal", [$scope.version.api.name, $scope.version.version]);
          // Magic bindings:
          // $scope.versions = ApiSummaryBean[]
          // $scope.version = ApiVersionBean

          // Get the API Version Plan summaries
          DevPortalService.getApiVersionPlans(
              params.org,
              params.api,
              params.version
          ).then((apiPlans: ApiPlanSummaryBean[]) => {
            Logger.info("Got plan summaries: {0}", apiPlans);


            devPortalBusinessLogic(
                pageData,
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
                $interval,
                $uibModal,
                params,
                apiPlans
            );
          });
        }
    );
  },
]);


function devPortalBusinessLogic(
    pageData,
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
    $interval,
    $uibModal,
    params,
    apiPlans: ApiPlanSummaryBean[]
) {
  $rootScope.isDirty = false;

  /** Data **/
  $scope.data = {
    apiVersion: $scope.version as ApiVersionBean, // $scope.version is bound magically by PageLifecycle... Maybe pass in as an arg.
    planSummaries: apiPlans,
    isFeaturedApi: isFeaturedApi($scope.version.api),
    latestImage: $scope.version.api.image
  }

  // Original clean copy.
  const dataClone = angular.copy($scope.data);

  /** Functions **/
  $scope.updateFeaturedApi = invertFeaturedApi;
  $scope.openImageCropper = openImageCropperModal;
  $scope.getImageEndpoint = getImageEndpoint;

  // TUI Markdown editor. Will initialise
  let markdownEditor: Editor = null;

  /** Start biz logic **/

  /**
   *  Watch `data.apiVersion` for changes by doing a deep comparison
   */
  $scope.$watch(
      "data",
      (oldValue, newValue) => {
        if (!angular.equals(oldValue, newValue)) {
          Logger.info("Dirty set to true {0} vs {1}", oldValue, newValue);
          $rootScope.isDirty = true;

          if (newValue.exposeInPortal) {
            Logger.info("Sending init to editor");
            if (markdownEditor == null) {
              initEditor();
            }
          }
        }
      },
      true
  );

  /** Markdown Editor **/
  function initEditor(): void {
    const options: EditorOptions = {
      el: document.querySelector("#editor"),
      height: "500px",
      initialEditType: "markdown",
      previewStyle: "tab",
      usageStatistics: false,
      hooks: {
        addImageBlobHook: uploadImageFromMarkdownEditor,
      },
      events: {
        // blur: () => editorDirtyCheck,
      },
      plugins: [
        [codeSyntaxHighlight, { highlighter: Prism }],
        [colourSyntax, {}],
      ],
      linkAttributes: {
        target: "_blank",
        rel: "noopener",
      },
    };

    markdownEditor = new toast(options);
    markdownEditor.setMarkdown("");

    if ($scope.data.apiVersion.extendedDescription) {
      Logger.info(
          "Setting extended description to: {0}",
          $scope.data.apiVersion.extendedDescription
      );
      markdownEditor.setMarkdown($scope.data.apiVersion.extendedDescription, true);
    }
  }

  // Dirty check the MD editor pane every 2 seconds (avoids excessive checking).
  const intervalPromise: Promise<any> = $interval(function () {
    if ($scope.data.apiVersion.exposeInPortal) {
      editorDirtyCheck();
    }
  }, 1000);

  const editorDirtyCheck = function (): void {
    if (!markdownEditor) {
      initEditor();
    }
    let latestDescription = markdownEditor.getMarkdown();
    // If original description != latestDescription, and latest poll != previous poll
    if (normaliseString(dataClone.apiVersion.extendedDescription) !== normaliseString(latestDescription) &&
        normaliseString($scope.data.apiVersion.extendedDescription) !== normaliseString(latestDescription)) {
      Logger.info("Setting dirty true");
      $rootScope.isDirty = true;
      $scope.data.apiVersion.extendedDescription = latestDescription;
    }
  };
  /** End Markdown Editor **/
  
  /** Save, and reset **/
  // Reset (copy saved pristine copy back over).
  $scope.reset = function () {
    Logger.info("Resetting");
    $scope.data = angular.copy(dataClone);
    markdownEditor.setMarkdown(
      $scope.data.apiVersion.extendedDescription,
      true
    );
    $rootScope.isDirty = false;
  };

  $scope.$on("$destroy", function () {
    console.log("destroy");
    $interval.cancel(intervalPromise);
    markdownEditor && markdownEditor.reset();
    markdownEditor && markdownEditor.destroy();
  });

  // Save
  $scope.doSave = function () {
    const markdown: string = markdownEditor.getMarkdown();
    const updateApiVersionBean = {
      extendedDescription: markdown,
      exposeInPortal: $scope.data.apiVersion.exposeInPortal,
      plans: $scope.data.apiVersion.plans,
    } as UpdateApiVersionBean;

    DevPortalService.updateApiVersion(
      params.org,
      params.api,
      params.version,
      updateApiVersionBean
    ).then(
      () => {
        Logger.info("Api Version update succeeded!");
        $rootScope.isDirty = false;
      },
      (failure) => handleFailure(failure)
    );

    // If identical to clone, don't bother saving anything.
    if (!angular.equals($scope.data.apiVersion.api, dataClone.apiVersion.api)) {
      const api: ApiBean = $scope.data.apiVersion.api;
      const updateApiBean: UpdateApiBean = {
        image: api.image,
        tags: api.tags,
      };
      DevPortalService.updateApi(params.org, params.api, updateApiBean).then(
        () => Logger.info("Api update succeeded!"),
        (failure) => handleFailure(failure)
      );
    }
  };

  function uploadImageFromMarkdownEditor(blob: Blob | File, callback): void {
    BlobService.uploadBlob(blob)
    .then(
      (blobRef: BlobRef) => {
        Logger.debug("Uploaded successfully: {0}", blobRef);
        callback(blobRef.id);
      },
      (failed) => {
        Logger.debug("Upload failed: {0}", failed);
      }
    );
  }

  function getImageEndpoint(): string {
    if (!$scope.data.latestImage) {
      return null;
    } else if ($scope.data.latestImage.startsWith("data")) {
        return $scope.data.latestImage;
    } else {
        return `${Configuration.api.endpoint}/${$scope.data.latestImage}`;
    }
  }

  function handleFailure(failure: any): void {
    // TODO(msavy): do something useful here...
    Logger.error("failure {0}", failure.data || failure);
  }

  function openImageCropperModal(): void {
    const modalInstance = $uibModal.open({
      animation: true,
      templateUrl: "plugins/api-manager/html/api/api-devportal-cropper-modal.html",
      size: "md",
      controller: "Apiman.DevPortalImageCropper",
    });

    modalInstance.result.then(
      (bmr: BlobModalReturn) => {
        $scope.data.latestImage = bmr.croppedCanvas.toDataURL(bmr.type, 100);
        console.log("$scope.data.apiVersion.api.image=" + bmr.blobRef.id);
        $scope.data.apiVersion.api.image = bmr.blobRef.id;
      },
      (dismissed) => {}
    );
  }

  function invertFeaturedApi(): void {
    const api = $scope.data.apiVersion.api;
    const isFeatured: boolean = isFeaturedApi(api);
    const tagsArray: KeyValueTagDto[] = api.tags;
    // If featured remove featured entry, and vince versa...
    if (isFeatured) {
      _remove(tagsArray, (candidate) => {
        return candidate.key === "featured";
      })
    } else {
      tagsArray.push({ key: "featured" } as KeyValueTagDto);
    }
  }

  function isFeaturedApi(apiBean: ApiBean): boolean {
    const tagsArray: KeyValueTagDto[] = apiBean.tags;
    const answer = tagsArray.findIndex((elem: KeyValueTagDto) => elem.key === "featured") !== -1;
    console.log(answer);
    return answer;
  }

  function normaliseString(str: string): string {
    if (!str || str.length === 0) {
      return "";
    } else {
      return str;
    }
  }
}

_module.controller("Apiman.DevPortalImageCropper",
    ['$scope', 'BlobService', '$uibModalInstance', 'Logger', 'Modals',
      function($scope, BlobService, $uibModalInstance, Logger, Modals) {
        $scope.uploadImage = uploadImage;
        $scope.closeModal = closeModal;
        $scope.dismissUibModal = dismissUibModal;
        $scope.inputImage = inputImage;
        $scope.uploading = false;
        let cropper: Cropper = null;
        let selectedFile: File = null;

        function inputImage($files) {
          if (!$files || $files.length == 0) {
            return;
          }
          console.log("input image");
          if (cropper) {
            cropper.destroy();
          }
          selectedFile = $files[0];

          const canvas = (<HTMLCanvasElement>document.getElementById('portal-image-cropper'));
          const context: CanvasRenderingContext2D = canvas.getContext('2d');
          let img = new Image();
          // Ensure you set onload first, as sometimes it fires before you have time to set img.src. Weird.
          img.onload = function () {
            scaleImageToCanvas(context, img);
            runCropper(context, canvas);
          };
          img.src = URL.createObjectURL(selectedFile);
        }

        function runCropper(context: CanvasRenderingContext2D, canvas: HTMLCanvasElement) {
          cropper = new Cropper(canvas, {
            aspectRatio: 1,
            viewMode: 1,
            ready: function () {
            }
          });
        }

        function uploadImage() {
          $scope.uploading = true;
          const croppedCanvas: HTMLCanvasElement = generatedScaledCanvas(cropper.getCroppedCanvas());
          croppedCanvas.toBlob(blob => {
            BlobService.uploadBlob(blob).then(
                blobRef => {
                  closeModal(blobRef, croppedCanvas, selectedFile.type);
                },
                error => {
                  $scope.uploading = false;
                  Logger.error("Error uploading: {0}", error);
                  Modals.error("Image upload didn't work", error.message, () => {});
                }
            );
          }, selectedFile.type, 100);
        }

        function scaleImageToCanvas(context: CanvasRenderingContext2D, img: HTMLImageElement|HTMLCanvasElement) {
          const canvas = context.canvas;
          const hRatio = canvas.width / img.width;
          const vRatio =  canvas.height / img.height ;
          const ratio  = Math.min (hRatio, vRatio);
          const centerShift_x = (canvas.width - img.width * ratio) / 2;
          const centerShift_y = (canvas.height - img.height * ratio) / 2;
          context.clearRect(0,0,canvas.width, canvas.height);
          context.drawImage(img, 0,0, img.width, img.height, centerShift_x, centerShift_y,img.width * ratio, img.height * ratio);
        }

        function generatedScaledCanvas(inputCanvas: HTMLCanvasElement): HTMLCanvasElement {
          const snapshotCanvas: HTMLCanvasElement = (<HTMLCanvasElement> document.createElement('canvas'));
          const context: CanvasRenderingContext2D = snapshotCanvas.getContext('2d');
          // canvas elements have a special height and width value that does not use an explicit unit type
          const width = 150;
          const height = 150;
          snapshotCanvas.width = width;
          snapshotCanvas.height = height;
          context.imageSmoothingEnabled = true;
          scaleImageToCanvas(context, inputCanvas);
          return snapshotCanvas;
        }

        function closeModal(blobRef: BlobRef, croppedCanvas: HTMLCanvasElement, type: string) {
          const returnValue: BlobModalReturn = {
            blobRef: blobRef,
            type: type,
            croppedCanvas: croppedCanvas
          };
          $uibModalInstance.close(returnValue);
        }

        function dismissUibModal() {
          $uibModalInstance.dismiss();
        }
      }]);

interface BlobModalReturn {
  blobRef: BlobRef,
  croppedCanvas: HTMLCanvasElement,
  type: string
}
