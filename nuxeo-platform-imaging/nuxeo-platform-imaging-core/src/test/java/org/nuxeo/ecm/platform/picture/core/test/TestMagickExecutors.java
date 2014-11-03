/*
 * (C) Copyright 2006-2009 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Nuxeo - initial API and implementation
 *
 * $Id$
 */

package org.nuxeo.ecm.platform.picture.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.core.storage.sql.SQLRepositoryTestCase;
import org.nuxeo.ecm.platform.picture.api.ImageInfo;
import org.nuxeo.ecm.platform.picture.magick.utils.ImageConverter;
import org.nuxeo.ecm.platform.picture.magick.utils.ImageCropper;
import org.nuxeo.ecm.platform.picture.magick.utils.ImageCropperAndResizer;
import org.nuxeo.ecm.platform.picture.magick.utils.ImageIdentifier;
import org.nuxeo.ecm.platform.picture.magick.utils.ImageResizer;

public class TestMagickExecutors extends SQLRepositoryTestCase {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        deployBundle("org.nuxeo.ecm.platform.commandline.executor");
        deployContrib("org.nuxeo.ecm.platform.picture.core",
                "OSGI-INF/commandline-imagemagick-contrib.xml");
    }

    @Test
    public void testIdentify() throws Exception {
        File file = FileUtils.getResourceFileFromContext("images/test.jpg");

        ImageInfo info = ImageIdentifier.getInfo(file.getAbsolutePath());

        assertNotNull(info);
        assertEquals("JPEG", info.getFormat());
        assertFalse(info.getWidth() == 0);
        assertFalse(info.getHeight() == 0);
        assertTrue(info.getColorSpace().endsWith("RGB"));

        System.out.print(info);
    }

    @Test
    public void testJpegSimplier() throws Exception {
        File out = File.createTempFile("", "test_small.jpg");
        File file = FileUtils.getResourceFileFromContext("images/test.jpg");

        ImageInfo info = ImageResizer.resize(file.getAbsolutePath(),
                out.getAbsolutePath(), 20, 20, 8);
        assertNotNull(info);

        assertTrue(out.exists());
        assertTrue(out.length() < file.length());
        out.delete();
    }

    @Test
    public void testCropper() throws Exception {
        File out = File.createTempFile("", "/test_crop.jpg");
        File file = FileUtils.getResourceFileFromContext("images/test.jpg");

        ImageCropper.crop(file.getAbsolutePath(), out.getAbsolutePath(), 255,
                255, 10, 10);

        assertTrue(out.exists());
        assertTrue(out.length() < file.length());

        ImageInfo info = ImageIdentifier.getInfo(out.getAbsolutePath());
        assertNotNull(info);
        assertEquals(255, info.getWidth());
        assertEquals(255, info.getHeight());
        out.delete();
    }

    @Test
    public void testCropperAndResize() throws Exception {
        File out = File.createTempFile(null, "/test_crop_resized.jpg");
        File file = FileUtils.getResourceFileFromContext("images/test.jpg");

        ImageCropperAndResizer.cropAndResize(file.getAbsolutePath(),
                out.getAbsolutePath(), 255, 255, 10, 10, 200, 200);

        assertTrue(out.exists());
        assertTrue(out.length() < file.length());

        ImageInfo info = ImageIdentifier.getInfo(out.getAbsolutePath());
        assertNotNull(info);
        assertEquals(200, info.getWidth());
        assertEquals(200, info.getHeight());

        out.delete();
    }

    @Test
    public void testConverterWithBmp() throws Exception {
        File file = FileUtils.getResourceFileFromContext("images/andy.bmp");
        File out = File.createTempFile(null, "/andy.jpg");

        ImageConverter.convert(file.getAbsolutePath(), out.getAbsolutePath());

        ImageInfo info = ImageIdentifier.getInfo(out.getAbsolutePath());
        assertNotNull(info);
        assertEquals("JPEG", info.getFormat());

        out.delete();
    }

    @Test
    public void testConverterWithGif() throws Exception {
        File file = FileUtils.getResourceFileFromContext("images/cat.gif");
        File out = File.createTempFile(null, "/cat.jpg");

        ImageConverter.convert(file.getAbsolutePath(), out.getAbsolutePath());

        ImageInfo info = ImageIdentifier.getInfo(out.getAbsolutePath());
        assertNotNull(info);
        assertEquals("JPEG", info.getFormat());

        out.delete();
    }
}
