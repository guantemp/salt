/*
 * Copyright (c) 2023. www.hoprxi.com All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package salt.hoprxi.utils;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="www.hoprxi.com/authors/guan xianghuang">guan xiangHuan</a>
 * @version 0.0.2 2019-03-23
 * @since JDK8.0
 */
public abstract class FileWatchDog {
    // check file
    protected File file;
    // file last modified time
    private long lastModified;
    private final int interval;

    /**
     * @param file
     */
    protected FileWatchDog(File file) {
        this(file, 2000);
    }

    /**
     * @param file
     * @param interval Refresh rate in milliseconds
     */
    protected FileWatchDog(File file, int interval) {
        this.file = Objects.requireNonNull(file, "file is required");
        this.interval = interval;
        ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        scheduler.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        scheduler.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        scheduler.scheduleWithFixedDelay(this::execute, interval, interval, TimeUnit.MILLISECONDS);
    }

    /**
     * @param uri
     */
    protected FileWatchDog(URI uri, int frequency) {
        this(new File(uri), frequency);
    }

    /**
     * @param fileName
     */
    protected FileWatchDog(String fileName) {
        this(new File(fileName));
    }

    /**
     * @param url
     */
    protected FileWatchDog(URL url, int frequency) throws URISyntaxException {
        this(new File(url.toURI()), frequency);
    }

    public long interval() {
        return interval;
    }

    /**
     * if monitor file changed,this is back
     */
    public abstract void onModify();

    private void execute() {
        boolean fileExists = file.exists();
        if (fileExists) {
            long l = file.lastModified();
            if (lastModified < l) {
                lastModified = l;
                onModify();
            }
        }
    }
}
