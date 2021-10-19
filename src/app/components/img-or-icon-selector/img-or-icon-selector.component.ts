import {AfterViewInit, Component, ElementRef, Input, OnInit, Renderer2, ViewChild} from '@angular/core';
import {ConfigService} from "../../services/config/config.service";

@Component({
  selector: 'app-img-or-icon-selector',
  templateUrl: './img-or-icon-selector.component.html',
  styleUrls: ['./img-or-icon-selector.component.scss']
})
export class ImgOrIconSelectorComponent implements AfterViewInit {
  @Input() imgUrl?: string;
  @Input() dimension?: string;

  constructor(public configService: ConfigService) { }

  ngAfterViewInit(): void {
    if (this.dimension)
      this.resize()
  }

  private resize() {
    if (this.imgUrl) {
      // Had to use document because @View Child could not do it with the ngIf in template
      // TODO: Must fix this later, as multiple elements with these ids could be in document because this component could get used multiple times
      document.getElementById('img')!.style.width = this.dimension + 'px';
      document.getElementById('img')!.style.height = this.dimension + 'px';
    }else{
      document.getElementById('icon')!.style.fontSize = this.dimension + 'px';
      document.getElementById('icon')!.style.height = this.dimension + 'px';
    }
  }
}
