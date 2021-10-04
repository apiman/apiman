import {IApiVersion} from "../interfaces/ICommunication";

export function flatArray(array: any[][]): any[] {
  let flattenedArray: any[] = []
  flattenedArray = flattenedArray.concat(...array)
  return flattenedArray;
}

export function formatBytes(bytes: number, decimals = 0): string {
  // Thankfully taken from https://stackoverflow.com/a/18650828
  if (bytes === 0) return '0 Bytes';

  const k = 1024;
  const dm = decimals < 0 ? 0 : decimals;
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];

  const i = Math.floor(Math.log(bytes) / Math.log(k));

  return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
}

export function isApiDocAvailable(apiVersion: IApiVersion): boolean {
  return (apiVersion.definitionType !== null &&
          apiVersion.definitionType !== 'None')
}
