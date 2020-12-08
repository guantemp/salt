/*
 * Copyright (c) 2020. www.hoprxi.com All Rights Reserved.
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

package salt.hoprxi.cache.l2.redis;

import java.io.Serializable;

/**
 * @author <a href="www.hoprxi.com/author/guan xianghuang">guan xiangHuan</a>
 * @version 0.0.1 2020-09-13
 * @since JDK8.0
 */
public final class KeyWrapping<T> implements Serializable {
    private String region;
    private T key;

    KeyWrapping(String region, T key) {
        this.region = region;
        this.key = key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KeyWrapping<?> that = (KeyWrapping<?>) o;

        if (region != null ? !region.equals(that.region) : that.region != null) return false;
        return key != null ? key.equals(that.key) : that.key == null;
    }

    @Override
    public int hashCode() {
        int result = region != null ? region.hashCode() : 0;
        result = 31 * result + (key != null ? key.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "KeyWrapping{" +
                "region='" + region + '\'' +
                ", t=" + key +
                '}';
    }
}
