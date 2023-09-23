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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuan</a>
 * @since JDK8.0
 * @version 0.0.1 2019-01-13
 */
public class ImageBytesConversion {
    private static final String FORMAT_NAME = "png";
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageBytesConversion.class);

    public static BufferedImage bytesToImage(byte[] bytes) {
        if (null != bytes) {
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            try {
                return ImageIO.read(in);
            } catch (IOException e) {
                LOGGER.error("Can't read image", e);
            }
        }
        return null;
    }

    public static byte[] imageToByte(BufferedImage image) {
        if (null != image) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                ImageIO.write(image, FORMAT_NAME, out);
                return out.toByteArray();
            } catch (IOException e) {
                LOGGER.error("Can't write image", e);
            }
        }
        return new byte[0];
    }
}
