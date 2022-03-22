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

import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  Input,
  OnChanges,
  SimpleChanges
} from '@angular/core';
import { ConfigService } from '../../services/config/config.service';

@Component({
  selector: 'app-img-or-icon-selector',
  templateUrl: './img-or-icon-selector.component.html',
  styleUrls: ['./img-or-icon-selector.component.scss']
})
export class ImgOrIconSelectorComponent implements AfterViewInit, OnChanges {
  @Input() imgUrl: string | undefined = '';
  @Input() dimension = '';

  constructor(
    public configService: ConfigService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    this.imgUrl = changes.imgUrl.currentValue
      ? (changes.imgUrl.currentValue as string)
      : '';
    this.cdr.detectChanges();
    this.resize();
  }

  ngAfterViewInit(): void {
    if (this.dimension) this.resize();
  }

  private resize() {
    if (this.imgUrl) {
      const images = Array.from(
        document.getElementsByClassName(
          'api-img'
        ) as HTMLCollectionOf<HTMLElement>
      );

      images.forEach((img: HTMLElement) => {
        img.style.width = this.dimension + 'px';
        img.style.height = this.dimension + 'px';
      });
    } else {
      const icons = Array.from(
        document.getElementsByClassName(
          'api-icon'
        ) as HTMLCollectionOf<HTMLElement>
      );

      icons.forEach((icon: HTMLElement) => {
        icon.style.fontSize = this.dimension + 'px';
        icon.style.height = this.dimension + 'px';
      });
    }
  }

  showFallbackIcon() {
    this.imgUrl = '';
  }
}
