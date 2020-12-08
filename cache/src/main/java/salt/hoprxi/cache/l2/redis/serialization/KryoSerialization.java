/*
 * Copyright (c) 2019 www.hoprxi.com All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package salt.hoprxi.cache.l2.redis.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoPool;
import de.javakaffee.kryoserializers.SynchronizedCollectionsSerializer;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author <a href="www.hoprxi.com/author/guan xianghuang">guan xiangHuan</a>
 * @version 0.0.1 2019-03-18
 * @since JDK8.0
 */
public class KryoSerialization implements Serialization {
    private static KryoPool pool = new KryoPool.Builder(() -> {
        final Kryo kryo = new Kryo();
        kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(
                new StdInstantiatorStrategy()));
        SynchronizedCollectionsSerializer.registerSerializers(kryo);
        return kryo;
    }).softReferences().build();

    @Override
    public byte[] serialize(Object obj) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (Output output = new Output(baos);) {
            Kryo kryo = pool.borrow();
            kryo.writeClassAndObject(output, obj);
            pool.release(kryo);
            output.flush();
            return baos.toByteArray();
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0)
            return null;
        try (Input ois = new Input(new ByteArrayInputStream(bytes))) {
            Kryo kryo = pool.borrow();
            T t = (T) new Kryo().readClassAndObject(ois);
            pool.release(kryo);
            return t;
        }
    }
}
