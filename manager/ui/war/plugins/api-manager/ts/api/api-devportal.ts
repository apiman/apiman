import { _module } from "../apimanPlugin";
import {
  ApiBean,
  ApiPlanSummaryBean,
  ApiVersionBean, KeyValueTagDto, UpdateApiBean,
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
import Cropper from "cropperjs/dist/cropper.esm";
import "cropperjs/dist/cropper.css"
import {clone} from "lodash";


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
          PageLifecycle.setPageTitle("api-devportal", []);
          // Magic bindings:
          // $scope.versions = ApiSummaryBean[]
          // $scope.version = ApiVersionBean

          devPortalBuisnessLogic(
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
              params
          );
        }
    );
  },
]);


function devPortalBuisnessLogic(
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
    params
) {
  let dataClone: any; // This will be set after summaries have loaded.

  $rootScope.isDirty = false;

  // Data
  $scope.data = {
    apiVersion: $scope.version as ApiVersionBean, // It's bound magically... Maybe pass in as an arg.
    planSummaries: [] as ApiPlanSummaryBean[],
    isFeaturedApi: isFeaturedApi($scope.version.api)
  }

  // Functions
  $scope.updateFeaturedApi = invertFeaturedApi;

  // Markdown and cropping
  let markdownEditor: Editor = null;
  $scope.openImageCropper = openImageCropper;

  /*** Markdown Editor ***/
  function initEditor(): void {
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
        [codeSyntaxHighlight, { highlighter: Prism }],
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
      $scope.data.apiVersion.extendedDescription
    );
    markdownEditor.setMarkdown($scope.data.apiVersion.extendedDescription, true);
  }

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
    if (
      dataClone.apiVersion.extendedDescription !== latestDescription &&
      $scope.data.apiVersion.extendedDescription !== latestDescription
    ) {
      $rootScope.isDirty = true;
      $scope.data.apiVersion.extendedDescription = latestDescription;
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
    $scope.data.planSummaries.push(...apiPlans);
    Logger.info("Cloning pristine $scope.data -> dataClone (for restore/reset).")
    dataClone = angular.copy($scope.data);
  });

  $scope.$watch(
    "data.apiVersion",
    (oldValue, newValue) => {
      if (!angular.equals(oldValue, newValue)) {
        Logger.debug("Dirty set to true {0} vs {1}", oldValue, newValue);
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

  // // Get the API Version and bind to $scope.apiVersion, and initialise MD Editor
  // DevPortalService.getApiVersion(params.org, params.api, params.version).then(
  //     (apiVersion: ApiVersionBean) => {
  //       Logger.info("Got API Version {0}", apiVersion);
  //       $scope.apiVersion = apiVersion as ApiVersionBean;
  //
  //       // Dirty check after loading, otherwise we'll get spurious matches.
  //
  //       if (apiVersion.exposeInPortal) {
  //         Logger.info("Sending init to editor");
  //         initEditor();
  //       }
  //     },
  //     (failure) => handleFailure(failure)
  // );

  /** Save, and reset **/
  // Reset (copy saved pristine copy back over).
  $scope.reset = () => {
    $scope.data = angular.copy(dataClone);
    markdownEditor.setMarkdown($scope.data.apiVersion.extendedDescription, true);
    $rootScope.isDirty = false;
  };

  $scope.$on("$destroy", function () {
    $interval.cancel(intervalPromise);
  });

  // Save
  $scope.doSave = () => {
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
      DevPortalService.updateApi(params.org, params.api, updateApiBean)
      .then(
        () => Logger.info("Api update succeeded!"),
        (failure) => handleFailure(failure)
      );
    }
  };

  function uploadImage(blob: Blob | File, callback): void {
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

  function handleFailure(failure: any): void {
    // TODO(msavy): do something useful here...
    Logger.error("failure {0}", failure.data || failure);
  }

  function openImageCropper(): void {
    const modalInstance = $uibModal.open({
      animation: true,
      templateUrl:
        "plugins/api-manager/html/api/api-devportal-cropper-modal.html",
      size: "md",
      controller: "Apiman.DevPortalImageCropper",
    });

    modalInstance.result.then(
      (uploadedImage: BlobModalReturn) => {
        assignCanvas(uploadedImage);
        $scope.data.apiVersion.api.image = uploadedImage.blobRef;
      },
      (dismissed) => {}
    );
  }

  function invertFeaturedApi($event): void {
    const api = $scope.data.apiVersion.api;
    const isFeatured: boolean = isFeaturedApi(api);
    const tagsArray: KeyValueTagDto[] = api.tags;
    // If featured remove featured entry, and vince versa...
    if (isFeatured) {
      tagsArray.forEach((candidate: KeyValueTagDto, index) => {
        if (candidate.key === "featured") {
          tagsArray.splice(index, 1);
        }
      });
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

  function assignCanvas(canvas: BlobModalReturn) {
    $scope.data.apiVersion.api.image = canvas.croppedCanvas.toDataURL(
      canvas.type,
      100
    );
  }
}

_module.controller("Apiman.DevPortalImageCropper",
    ['$scope', 'BlobService', '$uibModalInstance', 'Logger', 'Modals',
      function($scope, BlobService, $uibModalInstance, Logger, Modals) {
        $scope.uploadImage = uploadImage;
        $scope.closeModal = closeModal;
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
      }]);

interface BlobModalReturn {
  blobRef: BlobRef,
  croppedCanvas: HTMLCanvasElement,
  type: string
}
