import { Component, Input, OnInit } from '@angular/core';
import { IGaugeChartData } from '../../../interfaces/IGaugeChartData';
import { LegendPosition } from '@swimlane/ngx-charts';

@Component({
  selector: 'app-gauge-chart',
  templateUrl: './gauge-chart.component.html',
  styleUrls: ['./gauge-chart.component.scss'],
})
export class GaugeChartComponent implements OnInit {
  @Input() gaugeData!: IGaugeChartData;
  @Input() icon?: string;

  data: { name: string; value: number }[] = [{ name: '', value: 0 }];
  legend = false;
  currentValue = 0;
  legendPosition = LegendPosition.Below;
  period = '';
  percentage = '';
  colorScheme: any | undefined;

  disableValueLabel = () => '';

  ngOnInit(): void {
    this.getPrimaryColor();
    this.setupChart();
  }

  private setupChart() {
    if (this.gaugeData) {
      this.data = [
        {
          name: 'Currently Used',
          value: this.gaugeData.currentValue,
        },
      ];
      this.currentValue = this.gaugeData.currentValue;
      this.period = this.gaugeData.period;
      this.percentage = ((this.currentValue * 100) / this.gaugeData.limit).toFixed(1);
    }
  }

  /**
   * This method gets and sets the primary color for the charts by adding a element to the DOM
   */
  getPrimaryColor(): void {
    const p = document.createElement('p');
    p.classList.add('primary');
    document.body.appendChild(p);
    const color = getComputedStyle(p).color;
    document.body.removeChild(p);
    this.colorScheme = { domain: [color] };
  }
}
