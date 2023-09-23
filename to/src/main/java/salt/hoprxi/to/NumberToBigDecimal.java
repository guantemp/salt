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
package salt.hoprxi.to;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuan</a>
 * @version 0.0.2 2019-08-22
 * @since JDK8.0
 */
public enum NumberToBigDecimal {
    /**
     * Conversion from BigDecimal.
     */
    BIGDECIMAL {
        @Override
        BigDecimal toDecimal(Number num) {
            BigDecimal result = ((BigDecimal) num);
            return isScaleZero(result);
        }
    },
    /**
     * COnversion from BigDecimal, extended.
     */
    BIGDECIMAL_EXTENDS {
        @Override
        BigDecimal toDecimal(Number num) {
            BigDecimal result = ((BigDecimal) num).stripTrailingZeros();
            return isScaleZero(result);
        }
    },
    /**
     * Conversion from BigInteger.
     */
    BIGINTEGER {
        @Override
        BigDecimal toDecimal(Number num) {
            return new BigDecimal((BigInteger) num);
        }
    },
    /**
     * Default conversion based on String, if everything else failed.
     */
    DEFAULT {
        @Override
        BigDecimal toDecimal(Number num) {
            BigDecimal result = null;
            try {
                result = new BigDecimal(num.toString());
            } catch (NumberFormatException ignored) {
            }
            if (result == null)
                result = BigDecimal.valueOf(num.doubleValue());
            return isScaleZero(result);
        }
    },
    /**
     * Conversion for floating point numbers.
     */
    FLUCTUAGE {
        @Override
        BigDecimal toDecimal(Number num) {
            return new BigDecimal(num.toString());
        }
    },
    /**
     * Conversion from integral numeric types, short, int, long.
     */
    INTEGER {
        @Override
        BigDecimal toDecimal(Number num) {
            return BigDecimal.valueOf(num.longValue());
        }
    };

    private static final List<Class<? extends Number>> INSTEGERS = Arrays.asList(Long.class, Integer.class, Short.class,
            Byte.class, AtomicLong.class, AtomicInteger.class);

    private static NumberToBigDecimal factory(Number num) {
        if (INSTEGERS.contains(num.getClass())) {
            return INTEGER;
        }
        if (num.getClass() == Float.class || num.getClass() == Double.class) {
            return FLUCTUAGE;
        }
        if (BigDecimal.class.equals(num.getClass())) {
            return BIGDECIMAL;
        }
        if (num instanceof BigInteger) {
            return BIGINTEGER;
        }
        if (num instanceof BigDecimal) {
            return BIGDECIMAL_EXTENDS;
        }
        return DEFAULT;
    }

    private static BigDecimal isScaleZero(BigDecimal result) {
        if (result.signum() == 0) {
            return BigDecimal.ZERO;
        }
        if (result.scale() > 0) {
            return result.stripTrailingZeros();
        }
        return result;
    }

    public static BigDecimal to(Number num) {
        Objects.requireNonNull(num, "Number is required.");
        return factory(num).toDecimal(num);
    }

    abstract BigDecimal toDecimal(Number num);
}

