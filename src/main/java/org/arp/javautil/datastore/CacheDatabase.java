/*
 * #%L
 * JavaUtil
 * %%
 * Copyright (C) 2012 - 2013 Emory University
 * %%
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
 * #L%
 */
package org.arp.javautil.datastore;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import com.sleepycat.bind.serial.ClassCatalog;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

/**
 * @deprecated Implement the {@link DataStore} interface to define new caches and
 *             permanent stores. Clients should use {@link DataStoreFactory} to get
 *             stores.
 */
@Deprecated
class CacheDatabase {

    private static final String CLASS_CATALOG = "java_class_catalog";
    private static StoredClassCatalog classCatalog;
    private static Environment env;
    private static File location;
    private static Map<String, Database> maps = Collections
            .synchronizedMap(new HashMap<String, Database>());

    static {
        location = new File(System.getProperty("java.io.tmpdir"), 
                UUID.randomUUID().toString());
        Runtime.getRuntime().addShutdownHook(new Thread("CacheDBShutdownHook") {

            @Override
            public void run() {
                if (env != null) {
                    try {
                        for (Database m : maps.values()) {
                            m.close();
                        }
                        classCatalog.close();
                        env.close();
                    } catch (DatabaseException e) {
                        e.printStackTrace();
                    }
                    for (File f : location.listFiles()) {
                        f.delete();
                    }
                    location.delete();
                }
            }
        });
    }

    static Database createDatabase(String dbName) throws DatabaseException {
        createEnvironmentIfNeeded();

        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setAllowCreate(true);
        dbConfig.setTemporary(true);

        Database result = env.openDatabase(null, dbName, dbConfig);
        if (maps.put(dbName, result) != null) {
            throw new AssertionError("This shouldn't happen");
        }
        return result;
    }

    static ClassCatalog createOrGetClassCatalog() throws DatabaseException {
        createEnvironmentIfNeeded();
        createClassCatalogIfNeeded();

        return classCatalog;
    }

    private synchronized static void createClassCatalogIfNeeded()
            throws IllegalArgumentException, DatabaseException {
        if (classCatalog == null) {
            DatabaseConfig dbConfig = new DatabaseConfig();
            dbConfig.setTemporary(true);
            dbConfig.setAllowCreate(true);
            Database catalogDb = env
                    .openDatabase(null, CLASS_CATALOG, dbConfig);
            classCatalog = new StoredClassCatalog(catalogDb);
        }
    }

    private synchronized static void createEnvironmentIfNeeded()
            throws DatabaseException {
        if (env == null) {
            EnvironmentConfig envConf = new EnvironmentConfig();
            envConf.setAllowCreate(true);
            envConf.setTransactional(true);
            MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage memoryUsage = memoryMXBean.getHeapMemoryUsage();
            long max = memoryUsage.getMax();
            long used = memoryUsage.getUsed();
            long available = max - used;
            long cacheSize = Math.round(available / 2.0);
            envConf.setCacheSize(cacheSize);
            DataStoreUtil.logger().log(Level.FINE, "Cache size set to {0} bytes",
                    cacheSize);
            
            if (!location.exists()) {
                location.mkdirs();
            }

            env = new Environment(location, envConf);
        }
    }
}
