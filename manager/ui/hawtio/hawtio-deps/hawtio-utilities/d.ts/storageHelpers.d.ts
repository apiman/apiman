/// <reference path="coreHelpers.d.ts" />
/// <reference path="controllerHelpers.d.ts" />
declare module StorageHelpers {
    interface BindModelToLocalStorageOptions {
        $scope: any;
        $location: ng.ILocationService;
        localStorage: WindowLocalStorage;
        modelName: string;
        paramName: string;
        initialValue?: any;
        to?: (value: any) => any;
        from?: (value: any) => any;
        onChange?: (value: any) => void;
    }
    function bindModelToLocalStorage(options: BindModelToLocalStorageOptions): void;
}
