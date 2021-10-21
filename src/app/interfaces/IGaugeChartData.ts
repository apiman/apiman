export interface IGaugeChartData {
  name: string;
  currentVal: number;
  limit: number;
  unitSuffix: string
  period: string;
  remaining: number;
  infoHeader: string;
  bottomText: string;
}
