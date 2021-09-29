import { Component, Input, OnInit } from '@angular/core';
import { IGaugeChartData } from '../../../interfaces/IGaugeChartData';
import { LegendPosition } from '@swimlane/ngx-charts';

@Component({
  selector: 'app-gauge-chart',
  templateUrl: './gauge-chart.component.html',
  styleUrls: ['./gauge-chart.component.scss'],
})
export class GaugeChartComponent implements OnInit {
  @Input() gaugeData?: IGaugeChartData;

  data: { name: string; value: number }[] = [{ name: '', value: 0 }];
  legend = false;
  limit = 0;
  currentValue = 0;
  legendPosition = LegendPosition.Below;
  period = '';
  percentage = '';
  name: string | undefined;
  colorScheme: any | undefined;

  ngOnInit(): void {
    this.getPrimaryColor();
    this.setupChart();
  }

  private setupChart() {
    if (this.gaugeData) {
      this.name = this.gaugeData.name;
      this.data = [
        {
          name: 'Currently Used',
          value: this.gaugeData.currentValue,
        },
      ];
      this.currentValue = this.gaugeData.currentValue;
      this.limit = this.gaugeData.limit;
      this.period = this.gaugeData.period;
      this.percentage = ((this.currentValue * 100) / this.limit).toFixed(1);
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
