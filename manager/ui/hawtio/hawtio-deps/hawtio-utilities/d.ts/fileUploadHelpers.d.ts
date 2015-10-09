/// <reference path="includes.d.ts" />
/// <reference path="baseHelpers.d.ts" />
/// <reference path="coreHelpers.d.ts" />
declare module FileUpload {
    interface IFileItem {
        url: string;
        alias?: string;
        headers: any;
        formData: Array<any>;
        method: string;
        withCredentials: boolean;
        removeAfterUpload: boolean;
        index: number;
        progress: number;
        isReady: boolean;
        isUploading: boolean;
        isUploaded: boolean;
        isSuccess: boolean;
        isCancel: boolean;
        isError: boolean;
        uploader: FileUploader;
        json?: string;
        remove: () => void;
        upload: () => void;
        cancel: () => void;
        onBeforeUpload: () => void;
        onProgress: (progress: number) => void;
        onSuccess: (response: any, status: number, headers: any) => void;
        onError: (response: any, status: number, headers: any) => void;
        onCancel: (response: any, status: number, headers: any) => void;
        onComplete: (response: any, status: number, headers: any) => void;
    }
    interface IFilter {
        name: String;
        fn: (item: IFileItem) => boolean;
    }
    interface IOptions {
        url: String;
        alias?: String;
        headers?: any;
        queue?: Array<IFileItem>;
        progress?: number;
        autoUpload?: boolean;
        removeAfterUpload?: boolean;
        method?: String;
        filters?: Array<IFilter>;
        formData?: Array<any>;
        queueLimit?: number;
        withCredentials?: boolean;
    }
    interface FileUploader {
        url: String;
        alias?: String;
        headers?: any;
        queue?: Array<any>;
        progress?: number;
        autoUpload?: boolean;
        removeAfterUpload?: boolean;
        method?: String;
        filters?: Array<IFilter>;
        formData?: Array<any>;
        queueLimit?: number;
        withCredentials?: boolean;
        addToQueue: (files: FileList, options: any, filters: String) => void;
        removeFromQueue: (item: IFileItem) => void;
        clearQueue: () => void;
        uploadItem: (item: any) => void;
        cancelItem: (item: any) => void;
        uploadAll: () => void;
        cancelAll: () => void;
        destroy: () => void;
        isFile: (value: any) => boolean;
        isFileLikeObject: (value: any) => boolean;
        getIndexOfItem: (item: IFileItem) => number;
        getReadyItems: () => Array<IFileItem>;
        getNotUploadedItems: () => Array<IFileItem>;
        onAfterAddingFile: (item: IFileItem) => void;
        onWhenAddingFileFailed: (item: IFileItem, filter: IFilter, options: any) => void;
        onAfterAddingAll: (addedItems: Array<IFileItem>) => void;
        onBeforeUploadItem: (item: IFileItem) => void;
        onProgressItem: (item: IFileItem, progress: number) => void;
        onSuccessItem: (item: IFileItem, response: any, status: number, headers: any) => void;
        onErrorItem: (item: IFileItem, response: any, status: number, headers: any) => void;
        onCancelItem: (item: IFileItem, response: any, status: number, headers: any) => void;
        onCompleteItem: (item: IFileItem, response: any, status: number, headers: any) => void;
        onProgressAll: (progress: number) => void;
        onCompleteAll: () => void;
    }
    interface RequestParameters {
        type: String;
        mbean: String;
        operation: String;
        arguments: Array<any>;
    }
    function useJolokiaTransport($scope: ng.IScope, uploader: FileUploader, jolokia: any, onLoad: (json: string) => RequestParameters): void;
}
