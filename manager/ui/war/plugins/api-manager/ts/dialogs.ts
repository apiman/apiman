/// <reference path="../../includes.ts"/>
module ApimanDialogs {

    export var _module = angular.module("ApimanDialogs", ["ApimanLogger", "ApimanRPC"]);

    export var Dialogs = _module.factory('Dialogs', 
        ['Logger', '$compile', '$rootScope', '$timeout', 'ApimanSvcs', 'OrgSvcs',
        function(Logger, $compile, $rootScope, $timeout, ApimanSvcs, OrgSvcs) {
            return {
                // Simple data entry dialog
                ///////////////////////////
                getValue: function(title, message, label, initialValue, okCallback, cancelCallback) {
                  var modalScope = $rootScope.$new(true);
                  
                  modalScope.onOK = function() {
                      if (okCallback) { okCallback(modalScope.value); }
                      cancelCallback = null;
                  };
                  
                  modalScope.onCancel = function() {
                      if (cancelCallback) { cancelCallback(); }
                      cancelCallback = null;
                  };
                  
                  modalScope.title = title;
                  modalScope.message = message;
                  modalScope.label = label;
                  modalScope.value = initialValue;
                  
                  $('body').append($compile('<apiman-getvalue-modal modal-title="{{ title }}" />')(modalScope));

                  $timeout(function() {
                      $('#valueModal').on('hidden.bs.modal', function () {
                          if (cancelCallback) { $rootScope.$apply(cancelCallback); }
                          cancelCallback = null;
                      });
                      
                      $('#valueModal')['modal']({'keyboard': true, 'backdrop': 'static'});
                  }, 50);

                },
                // A standard confirmation dialog
                /////////////////////////////////
                confirm: function(title, message, yesCallback, noCallback) {
                    var modalScope = $rootScope.$new(true);
                    
                    modalScope.onYes = function() {
                        if (yesCallback) { yesCallback(); }
                    };
                    
                    modalScope.onNo = function() {
                        if (noCallback) { noCallback(); }
                        noCallback = null;
                    };
                    
                    modalScope.title = title;
                    modalScope.message = message;
                    
                    $('body').append($compile('<apiman-confirm-modal modal-title="{{ title }}">{{ message }}</apiman-confirm-modal>')(modalScope));

                    $timeout(function() {
                        $('#confirmModal').on('hidden.bs.modal', function () {
                            if (noCallback) { $rootScope.$apply(noCallback); }
                            noCallback = null;
                        });
                        
                        $('#confirmModal')['modal']({'keyboard': true, 'backdrop': 'static'});
                    }, 50);
                },
                
                // A simple "Select an API" dialog (allows selecting a single api + version
                //////////////////////////////////////////////////////////////////////////////////
                selectApi: function(title, handler, publishedOnly) {
                    var modalScope = $rootScope.$new(true);

                    modalScope.selectedApi = undefined;
                    modalScope.selectedApiVersion = undefined;
                    modalScope.title = title;

                    $('body').append($compile('<apiman-select-api-modal modal-title="{{ title }}"></apiman-select-api-modal>')(modalScope));

                    $timeout(function() {
                        $('#selectApiModal')['modal']({'keyboard': true, 'backdrop': 'static'});
                        $('#selectApiModal').on('shown.bs.modal', function () {
                            $('#selectApiModal .input-search').focus();
                        });
                    }, 50);

                    modalScope.search = function() {
                        modalScope.selectedApi = undefined;

                        if (!modalScope.searchText) {
                            modalScope.criteria = undefined;
                            modalScope.apis = undefined;
                        } else {
                            modalScope.searchButton.state = 'in-progress';
                            
                            var body:any = {};
                            body.filters = [];

                            body.filters.push({
                                'name' : 'name',
                                'value' : '%' + modalScope.searchText + '%',
                                'operator' : 'like'
                            });

                            var searchStr = angular.toJson(body);

                            Logger.log('Searching for apis: {0}', modalScope.searchText);

                            ApimanSvcs.save({ entityType: 'search', secondaryType: 'apis' }, searchStr, function(reply) {
                                if (reply.beans.length > 0) {
                                    modalScope.apis = reply.beans;
                                } else {
                                    modalScope.apis = undefined;
                                }

                                modalScope.criteria = modalScope.searchText;

                                Logger.log('Found {0} apis.', reply.beans.length);

                                modalScope.searchButton.state = 'complete';
                            }, function(error) {
                                Logger.error(error);
                                
                                // TODO do something interesting with the error
                                modalScope.apis = undefined;
                                modalScope.criteria = modalScope.searchText;
                                modalScope.searchButton.state = 'error';
                            });
                        }
                    };

                    modalScope.onApiSelected = function(api) {
                        if (modalScope.selectedApi) {
                            modalScope.selectedApi.selected = false;
                        }

                        modalScope.selectedApi = api;
                        api.selected = true;
                        modalScope.selectedApiVersion = undefined;

                        OrgSvcs.query({ organizationId: api.organizationId, entityType: 'apis', entityId: api.id, versionsOrActivity: 'versions' }, function(versions) {
                            if (publishedOnly) {
                                var validVersions = [];

                                angular.forEach(versions, function(version) {
                                    if (version.status == 'Published') {
                                        validVersions.push(version);
                                    }
                                });

                                modalScope.apiVersions = validVersions;
                            } else {
                                modalScope.apiVersions = versions;
                            }

                            if (modalScope.apiVersions.length > 0) {
                                modalScope.selectedApiVersion = modalScope.apiVersions[0];
                            }
                        }, function(error) {
                            modalScope.apiVersions = [];
                            modalScope.selectedApiVersion = undefined;
                        });
                    };
                    
                    modalScope.onOK = function() {
                        if (handler) {
                            handler(modalScope.selectedApiVersion);
                        }
                    };
                }
            };
        }]);

}