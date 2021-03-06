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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.collections.StoredMap;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseException;

public class BdbMap<K, V> implements DataStore<K, V> {

    private final Database db;
    private final StoredMap<K, V> storedMap;
    private boolean isClosed;
    private EnvironmentInfo envInfo;

    BdbMap(EnvironmentInfo envInfo, Database database) throws DataStoreError {
        this.db = database;
        this.envInfo = envInfo;
        StoredClassCatalog catalog = envInfo.getClassCatalog();
        try {
            EntryBinding<K> kBinding = new SerialBinding<>(catalog, null);
            EntryBinding<V> vBinding = new SerialBinding<>(catalog, null);
            storedMap = new StoredMap<>(db, kBinding, vBinding, true);
        } catch (DatabaseException ex) {
            throw new DataStoreError(ex);
        }
    }

    @Override
    public void shutdown() {
        this.envInfo.closeAndRemoveDatabaseHandle(this.db);
        this.isClosed = true;
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public void clear() {
        this.storedMap.clear();
    }

    @Override
    public boolean containsKey(Object arg0) {
        return this.storedMap.containsKey(arg0);
    }

    @Override
    public boolean containsValue(Object arg0) {
        return this.storedMap.containsValue(arg0);
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return this.storedMap.entrySet();
    }

    @Override
    public V get(Object arg0) {
        return this.storedMap.get(arg0);
    }

    @Override
    public boolean isEmpty() {
        return this.storedMap.isEmpty();
    }

    @Override
    public Set<K> keySet() {
        return this.storedMap.keySet();
    }

    @Override
    public V put(K arg0, V arg1) {
        return this.storedMap.put(arg0, arg1);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> arg0) {
        this.storedMap.putAll(arg0);
    }

    @Override
    public V remove(Object arg0) {
        return this.storedMap.remove(arg0);
    }

    @Override
    public int size() {
        return this.storedMap.size();
    }

    @Override
    public Collection<V> values() {
        return this.storedMap.values();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        @SuppressWarnings("unchecked")
        final BdbMap<K, V> other = (BdbMap<K, V>) obj;
        if (this.storedMap != other.storedMap
                && (this.storedMap == null
                || !this.storedMap.equals(other.storedMap))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return this.storedMap.hashCode();
    }

    @Override
    protected void finalize() throws Throwable {
        shutdown();
    }
}
