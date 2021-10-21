import {AfterViewInit, Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import { IGaugeChartData } from '../../../interfaces/IGaugeChartData';
import { LegendPosition } from '@swimlane/ngx-charts';

@Component({
  selector: 'app-gauge-chart',
  templateUrl: './gauge-chart.component.html',
  styleUrls: ['./gauge-chart.component.scss'],
})
export class GaugeChartComponent implements OnInit, OnChanges {
  @Input() gaugeData!: IGaugeChartData;
  @Input() icon?: string;
  @Input() bottomText?: string;

  data: { name: string; value: number }[] = [{ name: '', value: 0 }];
  legend = false;
  legendPosition = LegendPosition.Below;
  period = '';
  colorScheme: any | undefined;

  disableValueLabel = () => '';

  ngOnInit(): void {
    this.getPrimaryColor();
    this.setupChart();
  }

  private setupChart() {
    if (this.gaugeData) {
      this.data = [{name: 'Currently Used', value: this.gaugeData.currentVal}]
      this.period = this.gaugeData.period;
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

  /**
   * Because the policy probes are loaded async we have to listen to the changes and update the diagram
   * @param changes
   */
  ngOnChanges(changes: SimpleChanges): void {
    this.setupChart();
  }
}
