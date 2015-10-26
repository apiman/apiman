/// <reference path='apimanPlugin.ts'/>
/// <reference path='services.ts'/>
module Apiman {
    
    export var isRegexpValid = function(v) {
        var valid = true;

        try {
            new RegExp(v, '');
        } catch(e) {
            valid = false;
        }

        return valid;
    };
    
    _module.controller('Apiman.DefaultPolicyConfigFormController',
        ['$scope', 'Logger', 'EntityStatusService',
        ($scope, Logger, EntityStatusService) => {
            var validateRaw = function(config) {
                var valid = true;

                try {
                    var parsed = JSON.parse(config);
                    $scope.setConfig(parsed);
                } catch (e) {
                    valid = false;
                }

                $scope.setValid(valid);
            };

            $scope.isEntityDisabled = function() {
                var status = EntityStatusService.getEntityStatus();

                return (status !== 'Created' && status !== 'Ready');
            };

            if ($scope.getConfig()) {
                $scope.rawConfig = JSON.stringify($scope.getConfig(), null, 2);
            }

            $scope.$watch('rawConfig', validateRaw);
        }]);

    _module.controller('Apiman.JsonSchemaPolicyConfigFormController',
        ['$scope', 'Logger', 'PluginSvcs', 'EntityStatusService',
        ($scope, Logger, PluginSvcs, EntityStatusService) => {
            $scope.isEntityDisabled = function() {
                var status = EntityStatusService.getEntityStatus();

                return (status !== 'Created' && status !== 'Ready');
            };

            var initEditor = function(schema) {
                var holder = document.getElementById('json-editor-holder');

                var editor = new window['JSONEditor'](holder, {
                    // Disable fetching schemas via ajax
                    ajax: false,
                    // The schema for the editor
                    schema: schema,
                    // Disable additional properties
                    no_additional_properties: true,
                    // Require all properties by default
                    required_by_default: true,
                    disable_edit_json: true,
                    disable_properties: true,
                    iconlib: 'fontawesome4',
                    theme: 'bootstrap3'
                });

                editor.on('change', function() {
                    $scope.$apply(function() {
                        // Get an array of errors from the validator
                        var errors = editor.validate();

                        // Not valid
                        if (errors.length) {
                            $scope.setValid(false);
                        } else {
                            $scope.setValid(true);
                            $scope.setConfig($scope.editor.getValue());
                        }
                    });
                });

                if ($scope.isEntityDisabled() === true) {
                    editor.disable();
                }

                $scope.editor = editor;
            };

            var destroyEditor = function() {
                if ($scope.editor) {
                    $scope.editor.destroy();
                    $scope.editor = null;
                }
            };
            
            var loadSchema = function() {
                $scope.schemaState = 'loading';

                var pluginId = $scope.selectedDef.pluginId;
                var policyDefId = $scope.selectedDef.id;

                PluginSvcs.getPolicyForm(pluginId, policyDefId, function(schema) {
                    destroyEditor();
                    initEditor(schema);
                    $scope.editor.setValue($scope.config);
                    $scope.schemaState = 'loaded';
                }, function (error) {
                    // TODO handle the error better here!
                    Logger.error(error);
                    $scope.schemaState = 'loaded';
                });
            };

            // Watch for changes to selectedDef - if the user changes from one schema-based policy
            // to another schema-based policy, then the controller won't change.  The result is that
            // we need to refresh the schema when the selectedDef changes.
            $scope.$watch('selectedDef', function(newValue) {
                if (newValue && newValue.formType == 'JsonSchema') {
                    destroyEditor();
                    loadSchema();
                }
            });

            $scope.$on('$destroy', function() {
                destroyEditor();
            });
            
            // On first load of this controller, load the schema.
            loadSchema();
        }]);

    _module.controller('Apiman.RateLimitingFormController',
        ['$scope', 'Logger', 'EntityStatusService',
        ($scope, Logger, EntityStatusService) => {
            var validate = function(config) {
                var valid = true;

                if (config.limit) {
                    config.limit = Number(config.limit);
                }

                if (!config.limit || config.limit < 1) {
                    valid = false;
                }

                if (!config.granularity) {
                    valid = false;
                }

                if (!config.period) {
                    valid = false;
                }

                if (config.granularity == 'User' && !config.userHeader) {
                    valid = false;
                }

                $scope.isEntityDisabled = function() {
                    var status = EntityStatusService.getEntityStatus();

                    return (status !== 'Created' && status !== 'Ready');
                };

                $scope.setValid(valid);
            };

            $scope.$watch('config', validate, true);
        }]);

    _module.controller('Apiman.QuotaFormController',
        ['$scope', 'Logger', 'EntityStatusService',
        ($scope, Logger, EntityStatusService) => {
            var validate = function(config) {
                var valid = true;
                if (config.limit) {
                    config.limit = Number(config.limit);
                }

                if (!config.limit || config.limit < 1) {
                    valid = false;
                }

                if (!config.granularity) {
                    valid = false;
                }

                if (!config.period) {
                    valid = false;
                }

                if (config.granularity == 'User' && !config.userHeader) {
                    valid = false;
                }

                $scope.setValid(valid);
            };

            $scope.isEntityDisabled = function() {
                var status = EntityStatusService.getEntityStatus();

                return (status !== 'Created' && status !== 'Ready');
            };

            $scope.$watch('config', validate, true);
        }]);
    
    export var KB = 1024;
    export var MB = 1024 * 1024;
    export var GB = 1024 * 1024 * 1024;

    _module.controller('Apiman.TransferQuotaFormController',
        ['$scope', 'Logger', 'EntityStatusService',
        ($scope, Logger, EntityStatusService) => {
            $scope.limitDenomination = 'B';
            
            if ($scope.config && $scope.config.limit) {
                var limit = Number($scope.config.limit);

                if (limit > GB && ((limit % GB) == 0)) {
                    $scope.limitAmount = limit / GB;
                    $scope.limitDenomination = 'GB';
                } else if (limit > MB && ((limit % MB) == 0)) {
                    $scope.limitAmount = limit / MB;
                    $scope.limitDenomination = 'MB';
                } else if (limit > KB && ((limit % KB) == 0)) {
                    $scope.limitAmount = limit / KB;
                    $scope.limitDenomination = 'KB';
                } else {
                    $scope.limitAmount = limit;
                }
            }

            var validate = function(config) {
                var valid = true;

                if (!config.limit || config.limit < 1) {
                    valid = false;
                }

                if (!config.granularity) {
                    valid = false;
                }

                if (!config.period) {
                    valid = false;
                }

                if (config.granularity == 'User' && !config.userHeader) {
                    valid = false;
                }

                if (!config.direction) {
                    valid = false;
                }

                $scope.setValid(valid);
            };

            var onLimitChange = function() {
                var amt = $scope.limitAmount;

                if (amt) {
                    var den = $scope.limitDenomination;
                    var denFact = 1;

                    if (den == 'KB') {
                        denFact = 1024;
                    }

                    if (den == 'MB') {
                        denFact = 1024 * 1024;
                    }

                    if (den == 'GB') {
                        denFact = 1024 * 1024 * 1024;
                    }

                    try {
                        $scope.config.limit = Number(amt) * denFact;
                    } catch (e) {
                        $scope.config.limit = null;
                    }
                } else {
                    $scope.config.limit = null;
                }
            };

            $scope.isEntityDisabled = function() {
                var status = EntityStatusService.getEntityStatus();

                return (status !== 'Created' && status !== 'Ready');
            };

            $scope.$watch('config', validate, true);
            $scope.$watch('limitDenomination', onLimitChange, false);
            $scope.$watch('limitAmount', onLimitChange, false);
        }]);

    _module.controller('Apiman.IPListFormController',
        ['$scope', 'Logger', 'EntityStatusService',
        ($scope, Logger, EntityStatusService) => {
            var validate = function(config) {
                var valid = true;
                $scope.setValid(valid);
            };
            
            $scope.$watch('config', validate, true);
            
            if (!$scope.config.ipList) {
                $scope.config.ipList = [];
            }

            if (!$scope.config.responseCode) {
                $scope.config.responseCode = '500';
            }
            
            $scope.add = function(ip) {
                $scope.remove(ip);
                $scope.config.ipList.push(ip);
                $scope.selectedIP =  [ ip ];
                $scope.ipAddress = undefined;
                $('#ip-address').focus();
            };
            
            $scope.remove = function(ips) {
                angular.forEach(ips, function(ip) {
                    var idx = -1;

                    angular.forEach($scope.config.ipList, function(item, index) {
                        if (item == ip) {
                            idx = index;
                        }
                    });

                    if (idx != -1) {
                        $scope.config.ipList.splice(idx, 1);
                    }
                });

                $scope.selectedIP = undefined;
            };
            
            $scope.clear = function() {
                $scope.config.ipList = [];
                $scope.selectedIP = undefined;
            };

            $scope.isEntityDisabled = function() {
                var status = EntityStatusService.getEntityStatus();

                return (status !== 'Created' && status !== 'Ready');
            };
        }]);

    _module.controller('Apiman.IgnoredResourcesFormController',
        ['$scope', 'Logger', 'EntityStatusService',
        ($scope, Logger, EntityStatusService) => {
            var validate = function(config) {
                var valid = true;

                $scope.setValid(valid);
            };

            $scope.$watch('config', validate, true);
            
            $scope.add = function(path) {
                if (!$scope.config.pathsToIgnore) {
                    $scope.config.pathsToIgnore = [];
                }

                $scope.remove(path);
                $scope.config.pathsToIgnore.push(path);
                $scope.selectedPath =  [ path ];
                $scope.path = undefined;
                $('#path').focus();
            };
            
            $scope.remove = function(paths) {
                angular.forEach(paths, function(path) {
                    var idx = -1;

                    angular.forEach($scope.config.pathsToIgnore, function(item, index) {
                        if (item == path) {
                            idx = index;
                        }
                    });

                    if (idx != -1) {
                        $scope.config.pathsToIgnore.splice(idx, 1);
                    }
                });

                $scope.selectedPath = undefined;
            };
            
            $scope.clear = function() {
                $scope.config.pathsToIgnore = [];
                $scope.selectedPath = undefined;
            };

            $scope.isEntityDisabled = function() {
                var status = EntityStatusService.getEntityStatus();

                return (status !== 'Created' && status !== 'Ready');
            };
        }]);

    _module.controller('Apiman.BasicAuthFormController',
        ['$scope', 'Logger', 'EntityStatusService',
        ($scope, Logger, EntityStatusService) => {
            var validate = function(config) {
                if (!config) {
                    return;
                }

                var valid = true;

                if (!config.realm) {
                    valid = false;
                }
                
                if (!config.staticIdentity && !config.ldapIdentity && !config.jdbcIdentity) {
                    valid = false;
                }
                
                if (config.staticIdentity) {
                    if (!config.staticIdentity.identities) {
                        valid = false;
                    }
                }

                if (config.ldapIdentity) {
                    if (!config.ldapIdentity.url) {
                        valid = false;
                    }

                    if (!config.ldapIdentity.dnPattern) {
                        valid = false;
                    }

                    if (config.ldapIdentity.bindAs == 'ServiceAccount') {
                        if (!config.ldapIdentity.credentials || !config.ldapIdentity.credentials.username || !config.ldapIdentity.credentials.password) {
                            valid = false;
                        }

                        if (config.ldapIdentity.credentials) {
                            if (config.ldapIdentity.credentials.password != $scope.repeatPassword) {
                                valid = false;
                            }
                        }

                        if (!config.ldapIdentity.userSearch || !config.ldapIdentity.userSearch.baseDn || !config.ldapIdentity.userSearch.expression) {
                            valid = false;
                        }
                    }

                    if (config.ldapIdentity.extractRoles) {
                        if (!config.ldapIdentity.membershipAttribute) {
                            valid = false;
                        }

                        if (!config.ldapIdentity.rolenameAttribute) {
                            valid = false;
                        }
                    }
                }

                if (config.jdbcIdentity) {
                    if (!config.jdbcIdentity.datasourcePath) {
                        valid = false;
                    }

                    if (!config.jdbcIdentity.query) {
                        valid = false;
                    }

                    if (config.jdbcIdentity.extractRoles && !config.jdbcIdentity.roleQuery) {
                        valid = false;
                    }
                }

                $scope.setValid(valid);
            };

            $scope.$watch('config', validate, true);

            $scope.$watch('repeatPassword', function() {
                validate($scope.config);
            });
            
            if ($scope.config) {
                if ($scope.config.staticIdentity) {
                    $scope.identitySourceType = 'static';
                } else if ($scope.config.ldapIdentity && $scope.config.ldapIdentity.credentials) {
                    $scope.identitySourceType = 'ldap';
                    $scope.repeatPassword = $scope.config.ldapIdentity.credentials.password;
                } else if ($scope.config.jdbcIdentity) {
                    $scope.identitySourceType = 'jdbc';
                }
            }
            
            $scope.$watch('identitySourceType', function(newValue) {
                if (newValue) {
                    if (newValue == 'static' && !$scope.config.staticIdentity) {
                        $scope.config.staticIdentity = new Object();
                        delete $scope.config.ldapIdentity;
                        delete $scope.config.jdbcIdentity;
                    } else if (newValue == 'jdbc' && !$scope.config.jdbcIdentity) {
                        $scope.config.jdbcIdentity = new Object();
                        $scope.config.jdbcIdentity.hashAlgorithm = 'SHA1';
                        delete $scope.config.staticIdentity;
                        delete $scope.config.ldapIdentity;
                    } else if (newValue == 'ldap' && !$scope.config.ldapIdentity) {
                        $scope.config.ldapIdentity = new Object();
                        $scope.config.ldapIdentity.bindAs = 'UserAccount';
                        delete $scope.config.staticIdentity;
                        delete $scope.config.jdbcIdentity;
                    }
                }
            });

            $scope.add = function(username, password) {
                var item = {
                    username: username,
                    password: password
                };

                if (!$scope.config.staticIdentity.identities) {
                    $scope.config.staticIdentity.identities = [];
                }

                $scope.remove([ item ]);
                $scope.config.staticIdentity.identities.push(item);
                $scope.selectedIdentity =  [ item ];
                $scope.username = undefined;
                $scope.password = undefined;

                $('#username').focus();
            };
            
            $scope.remove = function(selectedIdentities) {
                angular.forEach(selectedIdentities, function(identity) {
                    var idx = -1;

                    angular.forEach($scope.config.staticIdentity.identities, function(item, index) {
                        if (item.username == identity.username) {
                            idx = index;
                        }
                    });

                    if (idx != -1) {
                        $scope.config.staticIdentity.identities.splice(idx, 1);
                    }
                });

                $scope.selectedIdentity = undefined;
            };
            
            $scope.clear = function() {
                $scope.config.staticIdentity.identities = [];
                $scope.selectedIdentity = undefined;
            };

            $scope.isEntityDisabled = function() {
                var status = EntityStatusService.getEntityStatus();

                return (status !== 'Created' && status !== 'Ready');
            };
        }]);

    _module.controller('Apiman.AuthorizationFormController',
        ['$scope', 'Logger', 'EntityStatusService',
        ($scope, Logger, EntityStatusService) => {
            var validate = function(config) {
                var valid = config.rules && config.rules.length > 0;

                if (!config.requestUnmatched) {
                    config.requestUnmatched = 'fail';
                }

                if (!config.multiMatch) {
                    config.multiMatch = 'all';
                }
                
                $scope.setValid(valid);
            };

            $scope.$watch('config', validate, true);
            
            $scope.currentItemInvalid = function() {
                return !$scope.path || !$scope.verb || !$scope.role || !isRegexpValid($scope.path);
            };
            
            $scope.add = function(path, verb, role) {
                if (!$scope.config.rules) {
                    $scope.config.rules = [];
                }

                var rule = {
                    'verb' : verb,
                    'pathPattern' : path,
                    'role' : role
                };

                $scope.config.rules.push(rule);
                $scope.path = undefined;
                $scope.verb = undefined;
                $scope.role = undefined;

                $('#path').focus();
            };
            
            $scope.remove = function(selectedRule) {
                var idx = -1;

                angular.forEach($scope.config.rules, function(item, index) {
                    if (item == selectedRule) {
                        idx = index;
                    }
                });

                if (idx != -1) {
                    $scope.config.rules.splice(idx, 1);
                }
            };
            
            $scope.clear = function() {
                $scope.config.rules = [];
            };

            $scope.isEntityDisabled = function() {
                var status = EntityStatusService.getEntityStatus();

                return (status !== 'Created' && status !== 'Ready');
            };
        }]);

    _module.controller('Apiman.CachingFormController',
        ['$scope', 'Logger', 'EntityStatusService',
        ($scope, Logger, EntityStatusService) => {
            var validate = function(config) {
                var valid = false;

                if (config.ttl) {
                    config.ttl = Number(config.ttl);

                    if (config.ttl && config.ttl > 0) {
                        valid = true;
                    }
                }

                $scope.setValid(valid);
            };

            $scope.isEntityDisabled = function() {
                var status = EntityStatusService.getEntityStatus();

                return (status !== 'Created' && status !== 'Ready');
            };

            $scope.$watch('config', validate, true);
        }]);

    _module.controller('Apiman.URLRewritingFormController',
        ['$scope', 'Logger', 'EntityStatusService',
        ($scope, Logger, EntityStatusService) => {
            var validate = function(config) {
                var valid = true;

                if (!config.fromRegex) {
                    valid = false;
                } else {
                    if (!isRegexpValid(config.fromRegex)) {
                        valid = false;
                    }
                }

                if (!config.toReplacement) {
                    valid = false;
                }

                if (!config.processBody && !config.processHeaders) {
                    valid = false;
                }

                $scope.setValid(valid);
            };

            $scope.isEntityDisabled = function() {
                var status = EntityStatusService.getEntityStatus();

                return (status !== 'Created' && status !== 'Ready');
            };

            $scope.$watch('config', validate, true);
        }]);

}
