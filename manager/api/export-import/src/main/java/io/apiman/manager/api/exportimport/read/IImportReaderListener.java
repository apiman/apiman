/*
 * Copyright 2013 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apiman.manager.api.exportimport.read;

/**
 * Used to listen to the reading of an apiman import file.  The import
 * reader will parse the import file and then fire events for each
 * entity it finds in the import.  This listener is the callback for 
 * those events.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IImportReaderListener {

}
