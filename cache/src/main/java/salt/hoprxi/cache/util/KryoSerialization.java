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

package salt.hoprxi.cache.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.Pool;

import java.io.ByteArrayInputStream;

/**
 * @author <a href="www.hoprxi.com/author/guan xianghuang">guan xiangHuan</a>
 * @version 0.0.1 2020-03-18
 * @since JDK8.0
 */
public class KryoSerialization implements Serialization {
    /*
    private static final KryoPool pool = new KryoPool.Builder(() -> {
        final Kryo kryo = new Kryo();
        kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(
                new StdInstantiatorStrategy()));
        //SynchronizedCollectionsSerializer.registerSerializers(kryo);
        return kryo;
    }).softReferences().build();
     */

    /**
     * 1. 参数详解：池构造函数参数：线程安全、软引用、最大容量 ：
     * public Pool(boolean threadSafe, boolean softReferences, final int maximumCapacity)
     * threadSafe: 这个参数是制定是否需要再POOL内部同步，如果设置为true，则可以被多个线程并发访问。
     * softReferences： 这个参数是是否使用softReferences进行存储对象，如果设置为true，则Kryo 池将会使用 java.lang.ref.SoftReference 来存储对象。这允许池中的对象在 JVM 的内存压力大时被垃圾回收。
     */
    private final Pool<Kryo> kryoPool = new Pool<Kryo>(true, false, 8) {
        protected Kryo create() {
            Kryo kryo = new Kryo();
            //kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
            kryo.setRegistrationRequired(false);
            return kryo;
        }
    };
    private static final int KRYO_OUTPUT_BUFFER_SIZE = 1024 * 1024;
    private static final int KRYO_OUTPUT_MAX_BUFFER_SIZE = Integer.MAX_VALUE;

    @Override
    public byte[] serialize(Object obj) {
        Kryo kryo = null;
        try (Output outPut = new Output(KRYO_OUTPUT_BUFFER_SIZE, KRYO_OUTPUT_MAX_BUFFER_SIZE)) {
            // 5.5.0已经移除了pool.borrow();方法替换使用obtain()
            kryo = kryoPool.obtain();
            kryo.writeClassAndObject(outPut, obj);
            outPut.flush();
            return outPut.toBytes();
        } finally {
            // 5.5.0已经移除了pool.release();方法替换使用free()
            kryoPool.free(kryo);
        }

    }

    @Override
    public <T> T deserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0)
            return null;
        Kryo kryo = null;
        try (Input in = new Input(new ByteArrayInputStream(bytes))) {
            kryo = kryoPool.obtain();
            T t = (T) kryo.readClassAndObject(in);
            return t;
        } finally {
            // 5.5.0已经移除了pool.release();方法替换使用free()
            kryoPool.free(kryo);
        }
    }
}
