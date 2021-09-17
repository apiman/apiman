export type IStatus = 'Created' | 'Ready' | 'Retired' | 'Published';
export type IClientStatus =
  | 'Created'
  | 'Ready'
  | 'Retired'
  | 'AwaitingApproval'
  | 'Registered';
export type IStatusColors = 'mediumseagreen' | 'darkred' | 'coral';

export type ApiStatusColorMap = Map<IStatus | IClientStatus, IStatusColors>;

export const statusColorMap: ApiStatusColorMap = new Map<
  IStatus | IClientStatus,
  IStatusColors
>([
  ['Created', 'coral'],
  ['Ready', 'mediumseagreen'],
  ['Retired', 'darkred'],
  ['Published', 'mediumseagreen'],
  ['AwaitingApproval', 'coral'],
  ['Registered', 'mediumseagreen'],
]);
