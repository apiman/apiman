/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var AdminRolesController = _module.controller("Apiman.AdminExportImportController",
        ['$q', '$scope', 'SystemSvcs', 'DownloadSvcs', 'PageLifecycle', 'Logger', '$window',
        ($q, $scope, SystemSvcs, DownloadSvcs, PageLifecycle, Logger, $window) => {
            $scope.tab = 'export';
            var pageData = {
            };
            
            $scope.importStatus = 'none';
            
            $scope.doExport = function() {
              $scope.exportButton.state = 'in-progress';
            	Logger.info('Starting download of export data.');
            	SystemSvcs.exportAsJson(function(download) {
            		Logger.info('Download: {0}', download);
            		var downloadLink = DownloadSvcs.getDownloadLink(download.id);
            		Logger.info('Downloading export data from: {0}', downloadLink);
            		$window.open(downloadLink, "_self");
            	}, PageLifecycle.handleError);
            };
            
            $scope.doImport = function(file) {
            	Logger.info('Uploading file for import.');
              $scope.importStatus = 'uploading';
              $scope.uploadPercentage = 0;
            	SystemSvcs.importJson(file, function(progress) {
                var perc = (100.0 * Number(progress.loaded)) / Number(progress.total);
                perc = (~~perc) + 1;
                if (perc >= 100) {
                  perc = 100;
                  $scope.importStatus = 'importing';
                }
                $scope.uploadPercentage = perc;
                Logger.info('Upload perc: {0}', perc);
            	}, function (response) {
                Logger.info('Import file successfully uploaded (and imported?).');
                $scope.importResult = response.data;
                $scope.importStatus = 'imported';
              }, function (error) {
                $scope.importError = error;
                $scope.importStatus = 'error';
              });
            };
            
            PageLifecycle.loadPage('AdminExport', pageData, $scope, function() {
                PageLifecycle.setPageTitle('admin-export');
            });
    }])

}
