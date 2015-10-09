declare module ControllerHelpers {
    function createClassSelector(config: any): (selection: any, model: any) => string;
    function createValueClassSelector(config: any): (model: any) => string;
    /**
     * Binds a $location.search() property to a model on a scope; so that its initialised correctly on startup
     * and its then watched so as the model changes, the $location.search() is updated to reflect its new value
     * @method bindModelToSearchParam
     * @for Core
     * @static
     * @param {*} $scope
     * @param {ng.ILocationService} $location
     * @param {String} modelName
     * @param {String} paramName
     * @param {Object} initialValue
     */
    function bindModelToSearchParam($scope: any, $location: any, modelName: string, paramName: string, initialValue?: any, to?: (value: any) => any, from?: (value: any) => any): void;
    /**
     * For controllers where reloading is disabled via "reloadOnSearch: false" on the registration; lets pick which
     * query parameters need to change to force the reload. We default to the JMX selection parameter 'nid'
     * @method reloadWhenParametersChange
     * @for Core
     * @static
     * @param {Object} $route
     * @param {*} $scope
     * @param {ng.ILocationService} $location
     * @param {Array[String]} parameters
     */
    function reloadWhenParametersChange($route: any, $scope: any, $location: any, parameters?: string[]): void;
}
