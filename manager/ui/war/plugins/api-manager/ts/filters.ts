/// <reference path="../../includes.ts"/>
module ApimanFilters {

    export var _module = angular.module('ApimanFilters', ['ApimanConfiguration']);

    export var checklist = _module.filter('urlEncode', function() {
        return encodeURIComponent;
    });

    // Checklist Filter:
    // This filter processes and formats data returned by the /status endpoint.
    export var checklist = _module.filter('checklist', function() {
        // Map the IDs to the Tab Names
        // This is used when checking if the user is on the same tab
        // defined on the checklist.
        var tabMapping = {
            'endpoint': 'impl',
            'gateways': 'impl',
            'plans': 'plans',
            'policies': 'policies'
        };

        return function(input) {
            var filtered = [];

            angular.forEach(input, function(item: ChecklistConfig) {
                if(item && item.id) {
                    // Add path for tabs with more than one path (ie: endpoint and gateways)
                    item.path = tabMapping[item.id];

                    // Check if item is completed (no need to check for this)
                    var status = (item.done === true) ? 'complete' : 'incomplete';

                    // Determine status icon class
                    item.iconClass = (item.done === true) ? 'fa fa-check-circle-o' : 'fa fa-circle-o';

                    // Determine table row class (ie: active complete, inactive incomplete)
                    item.rowClass = status;

                    filtered.push(item);
                }
            });

            return filtered;
        };
    });

    export var propsFilter = _module.filter('propsFilter', function() {
        return function(items, props) {
            var out = [];

            if (angular.isArray(items)) {
                items.forEach(function(item) {
                    var itemMatches = false;

                    var keys = Object.keys(props);
                    for (var i = 0; i < keys.length; i++) {
                        var prop = keys[i];
                        var text = props[prop].toLowerCase();
                        if (item[prop].toString().toLowerCase().indexOf(text) !== -1) {
                            itemMatches = true;
                            break;
                        }
                    }

                    if (itemMatches) {
                        out.push(item);
                    }
                });
            } else {
                // Let the output be the input untouched
                out = items;
            }

            return out;
        };
    });

    export var selectedTags = _module.filter('selectedTags', ['Logger', function(Logger) {
        return function(array, selectedTags) {
            if(array && selectedTags.length >= 1) {
                return array.filter(function(item) {
                    var itemTags = [];

                    _.map(selectedTags, function(tag) {
                        if(_.includes(item.tags, tag)) {
                            itemTags.push(tag);
                        }
                    });

                    return _.difference(selectedTags, itemTags).length === 0;
                });
            } else {
                return array;
            }
        };
    }]);

    export var startFrom = _module.filter('startFrom', function() {
        return function(data, index:number) {

            if (data != null)
            {
                return data.slice(index);
            }

            return null;
        }
    });


}

