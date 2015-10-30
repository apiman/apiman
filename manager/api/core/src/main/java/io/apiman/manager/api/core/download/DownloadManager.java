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

package io.apiman.manager.api.core.download;

import io.apiman.manager.api.beans.download.DownloadBean;
import io.apiman.manager.api.beans.download.DownloadType;
import io.apiman.manager.api.core.IDownloadManager;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.exceptions.StorageException;

import java.util.Date;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Default implementation of the {@link IDownloadManager} interface.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class DownloadManager implements IDownloadManager {
    
    private static final int DEFAULT_EXPIRES_LENGTH = 120000;

    @Inject
    private IStorage storage;
    
    /**
     * Constructor.
     */
    public DownloadManager() {
    }
    
    /**
     * @see io.apiman.manager.api.core.IDownloadManager#createDownload(io.apiman.manager.api.beans.download.DownloadType, java.lang.String)
     */
    @Override
    public DownloadBean createDownload(DownloadType type, String path) throws StorageException {
        DownloadBean download = new DownloadBean();
        download.setExpires(new Date(System.currentTimeMillis() + DEFAULT_EXPIRES_LENGTH));
        download.setId(UUID.randomUUID().toString());
        download.setType(type);
        download.setPath(path);

        storage.beginTx();
        try {
            storage.createDownload(download);
            storage.commitTx();
        } catch (StorageException e) {
            storage.rollbackTx();
            throw e;
        }
        return download;
    }
    
    /**
     * @see io.apiman.manager.api.core.IDownloadManager#getDownload(java.lang.String)
     */
    @Override
    public DownloadBean getDownload(String downloadId) throws StorageException {
        storage.beginTx();
        try {
            DownloadBean download = storage.getDownload(downloadId);
            if (download != null) {
                storage.deleteDownload(download);
                storage.commitTx();
                Date now = new Date();
                // Check if the download expired.  If so, return null instead of the download.
                if (now.getTime() > download.getExpires().getTime()) {
                    download = null;
                }
            }
            return download;
        } catch (StorageException e) {
            storage.rollbackTx();
            throw e;
        }
    }
    
}
