/*
 * Copyright 2015 JBoss Inc
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

package io.apiman.manager.api.core;

import io.apiman.manager.api.beans.download.DownloadBean;
import io.apiman.manager.api.beans.download.DownloadType;
import io.apiman.manager.api.core.exceptions.StorageException;

/**
 * Allows temporary download links to be created.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IDownloadManager {
    
    /**
     * Creates a download.
     * @param type
     * @param path
     */
    public DownloadBean createDownload(DownloadType type, String path) throws StorageException;

    /**
     * Gets the previously created download with the given ID.
     * @param downloadId
     * @throws StorageException
     */
    public DownloadBean getDownload(String downloadId) throws StorageException;

}
