export interface IFormattedBytes{
  value: number;
  unit: string;
}

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

export function formatBytesAsObject(bytes: number, decimals = 0): IFormattedBytes{
  const formattedBytes = formatBytes(bytes, decimals).split(' ');

  return {
    value: Number.parseInt(formattedBytes[0]),
    unit: formattedBytes[1]
  }
}
