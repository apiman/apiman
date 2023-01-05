/*
 * Copyright 2022 Scheer PAS Schweiz AG
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  imitations under the License.
 */

export interface IFormattedBytes {
  value: number;
  unit: string;
}
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function flatArray(array: any[][]): any[] {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  let flattenedArray: any[] = [];
  flattenedArray = flattenedArray.concat(...array);
  return flattenedArray;
}

export function formatBytes(bytes: number, decimals = 0): string {
  // Thankfully taken from https://stackoverflow.com/a/18650828
  if (bytes === 0) return '0 Bytes';

  const k = 1024;
  const dm = decimals < 0 ? 0 : decimals;
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];

  const i = Math.floor(Math.log(bytes) / Math.log(k));

  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(dm))} ${sizes[i]}`;
}

export function formatBytesAsObject(
  bytes: number,
  decimals = 0
): IFormattedBytes {
  const formattedBytes = formatBytes(bytes, decimals).split(' ');

  return {
    value: Number.parseInt(formattedBytes[0]),
    unit: formattedBytes[1]
  };
}

export function removeCssClass(selctor: string, className: string): void {
  // const elements = Array.from(document.getElementsByClassName(className));
  const elements = Array.from(document.querySelectorAll(selctor));
  elements.forEach((element: Element) => {
    element.classList.remove(className);
  });
}

/**
 * Checks if the user has all required roles, Apiman users and admins with the default roles also pass
 *
 * @param requiredRoles - string array of roles a user must have
 * @param actualRoles - string array of roles a user currently has
 * @returns True if the user has one of the configured roles or if the user is a full Apiman user (based on IDM default roles)
 */
export function hasRequiredAuthRoles(
  requiredRoles: string[],
  actualRoles: string[]
) {
  const apimanAdminFallback: string[] = ['apiuser', 'apiadmin'];

  return (
    requiredRoles.every((role: string) => actualRoles.includes(role)) ||
    (actualRoles.includes('view-profile') &&
      apimanAdminFallback.some((role: string) => actualRoles.includes(role)))
  );
}
