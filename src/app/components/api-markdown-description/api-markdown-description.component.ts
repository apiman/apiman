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
  Component,
  Input,
  OnInit,
  ViewChild,
  ViewEncapsulation
} from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import * as Prism from 'prismjs';
import 'prismjs/components';

@Component({
  selector: 'app-api-markdown-description',
  templateUrl: './api-markdown-description.component.html',
  styleUrls: ['./api-markdown-description.component.scss'],
  // Enable a ShadowRoot to apply styles in a isolated manner
  // This is necessary to apply prism styling and syntax highlighting.
  encapsulation: ViewEncapsulation.ShadowDom
})
export class ApiMarkdownDescriptionComponent implements OnInit, AfterViewInit {
  @ViewChild('markdown-container') markdownContainer!: ParentNode;
  @Input() markdownText = '';

  constructor(private translator: TranslateService) {}

  ngOnInit(): void {
    if (!this.markdownText) {
      this.markdownText = this.translator.instant(
        'API_DETAILS.NO_EXT_DESCRIPTION'
      ) as string;
    }
  }

  // Activate syntax highlighting as soon as the page is rendered to avoid initialize errors
  ngAfterViewInit(): void {
    Prism.highlightAllUnder(this.markdownContainer);
  }
}
