export type IApiStatus = 'Created' | 'Ready' | 'Retired' | 'Published';
export type IClientStatus =
  | 'Created'
  | 'Ready'
  | 'Retired'
  | 'AwaitingApproval'
  | 'Registered'
  | 'Unregistered';
export type IStatusColors = 'mediumseagreen' | 'darkred' | 'coral';

export type ApiStatusColorMap = Map<IApiStatus | IClientStatus, IStatusColors>;

export const statusColorMap: ApiStatusColorMap = new Map<
  IApiStatus | IClientStatus,
  IStatusColors
>([
  ['Created', 'coral'],
  ['Ready', 'mediumseagreen'],
  ['Retired', 'darkred'],
  ['Unregistered', 'darkred'],
  ['Published', 'mediumseagreen'],
  ['AwaitingApproval', 'coral'],
  ['Registered', 'mediumseagreen'],
]);
