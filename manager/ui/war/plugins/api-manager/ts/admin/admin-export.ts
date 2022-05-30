import {_module} from "../apimanPlugin";

_module.controller("Apiman.AdminExportImportController",
        ['$q', '$scope', 'SystemSvcs', 'DownloadSvcs', 'PageLifecycle', 'Logger', '$window',
        function ($q, $scope, SystemSvcs, DownloadSvcs, PageLifecycle, Logger, $window) {
            $scope.tab = 'export';
            var pageData = {
            };
            
            $scope.importStatus = 'none';
            $scope.exportStatus = 'none';
            
            $scope.doExport = function() {
              $scope.exportButton.state = 'in-progress';
            	Logger.info('Starting download of export data.');
            	SystemSvcs.exportAsJson(function(download) {
            		Logger.info('Download: {0}', download);
            		var downloadLink = DownloadSvcs.getDownloadLink(download.id);
            		Logger.info('Downloading export data from: {0}', downloadLink);
                $scope.exportStatus = 'downloading';
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
                if (response.data.search(/^ERROR:/gm) === -1) {
                  Logger.info("ok")
                  $scope.importStatus = 'imported';
                } else {
                  Logger.error("Looks like import had errors :-(");
                  $scope.importStatus = 'error';
                }
              }, function (error) {
                $scope.importError = error;
                $scope.importStatus = 'error';
              });
            };

            $scope.selectFile = function(files) {
                if(files && files.length) {
                    $scope.importFile = files[0];
                }
            };
            
            PageLifecycle.loadPage('AdminExport', 'admin', pageData, $scope, function() {
                PageLifecycle.setPageTitle('admin-export');
            });
    }]);