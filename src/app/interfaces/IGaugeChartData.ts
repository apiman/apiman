export interface IGaugeChartData {
  name: string;
  currentVal: number;
  limit?: number;
  dividendSuffix?: string
  divisorSuffix?: string
  period: string;
  infoHeader: string;
  bottomText?: string;
}
