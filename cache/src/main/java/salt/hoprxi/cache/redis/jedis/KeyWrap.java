/*
 * Copyright (c) 2022. www.hoprxi.com All Rights Reserved.
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

package salt.hoprxi.cache.redis.jedis;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author <a href="www.hoprxi.com/author/guan xianghuang">guan xiangHuan</a>
 * @version 0.0.1 2020-09-13
 * @since JDK8.0
 */
@Deprecated
public class KeyWrap<T> implements Serializable {
    private final String region;
    private final T t;

    public KeyWrap(String region, T t) {
        this.region = Objects.requireNonNull(region, "region is required").trim();
        this.t = Objects.requireNonNull(t, "t is required");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KeyWrap)) return false;

        KeyWrap<?> keyWrap = (KeyWrap<?>) o;

        if (!region.equals(keyWrap.region)) return false;
        return t.equals(keyWrap.t);
    }

    @Override
    public int hashCode() {
        int result = region.hashCode();
        result = 31 * result + t.hashCode();
        return result;
    }
}
