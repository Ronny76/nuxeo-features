/*
 * (C) Copyright 2007 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nuxeo.ecm.platform.picture.ExifHelper;
import org.nuxeo.ecm.platform.picture.core.mistral.MistralMetadataUtils;

/**
 *
 * @author btatar
 *
 */
public class TestExifHelper {

    MistralMetadataUtils service;

    @Before
    public void setUp() throws Exception {
        service = new MistralMetadataUtils();
    }

    @After
    public void tearDown() throws Exception {
        service = null;
    }

    @Test
    public void testExtractBytes() {
        // ASCII string as an byte array
        byte[] bytes = { 65, 83, 67, 73, 73, 0, 0, 0 };
        byte[] extractedBytes = ExifHelper.extractBytes(bytes, 0, 3);
        String s = new String(new byte[] { 65, 83, 67, 73, 73 });
        assertEquals("ASCII", s);
        assertTrue(Arrays.equals(new byte[] { 65, 83, 67 }, extractedBytes));
    }

    @Test
    public void testDecodeUndefined() {
        byte[] rawBytes = { 65, 83, 67, 73, 73, 0, 0, 0, 66, 65, 66, 65 };
        String decodedString = ExifHelper.decodeUndefined(rawBytes);
        String rawString = new String(rawBytes);
        assertNotSame(decodedString, rawString);
    }

}
