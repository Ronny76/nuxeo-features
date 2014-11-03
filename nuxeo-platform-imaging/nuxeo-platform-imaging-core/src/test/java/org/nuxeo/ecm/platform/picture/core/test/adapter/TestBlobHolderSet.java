/*
 * (C) Copyright 2007-2009 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     Laurent Doguin
 *     Florent Guillaume
 */

package org.nuxeo.ecm.platform.picture.core.test.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.DocumentBlobHolder;
import org.nuxeo.ecm.core.api.impl.DocumentModelImpl;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;

@RunWith(FeaturesRunner.class)
@Features({ AutomationFeature.class })
@Deploy({ "org.nuxeo.ecm.platform.picture.api", "org.nuxeo.ecm.core.convert",
        "org.nuxeo.ecm.platform.commandline.executor",
        "org.nuxeo.ecm.platform.picture.core",
        "org.nuxeo.ecm.platform.picture.convert" })
public class TestBlobHolderSet {

    protected DocumentModel root;

    @Inject
    protected CoreSession session;

    @Before
    public void init() throws Exception {
        root = session.getRootDocument();
    }

    private List<Map<String, Serializable>> createViews() {
        List<Map<String, Serializable>> views = new ArrayList<Map<String, Serializable>>();
        Map<String, Serializable> map = new HashMap<String, Serializable>();
        map.put("title", "Original");
        map.put("content",
                new FileBlob(
                        FileUtils.getResourceFileFromContext(ImagingResourcesHelper.TEST_DATA_FOLDER
                                + "test.jpg"), "image/jpeg", null, "test.jpg",
                        null));
        map.put("filename", "test.jpg");
        views.add(map);
        return views;
    }

    @Test
    public void testBlobHolderSet() throws Exception {
        DocumentModel picture = new DocumentModelImpl(root.getPathAsString(),
                "pic", "Picture");
        picture.setPropertyValue("picture:views", (Serializable) createViews());
        picture = session.createDocument(picture);
        session.save();

        BlobHolder bh = picture.getAdapter(BlobHolder.class);
        assertNotNull(bh);
        Blob blob = bh.getBlob();
        assertNotNull(blob);
        assertEquals(1, bh.getBlobs().size());

        // test write
        blob = new FileBlob(
                FileUtils.getResourceFileFromContext(ImagingResourcesHelper.TEST_DATA_FOLDER
                        + "test.jpg"), "image/jpeg", null, "logo.jpg", null);
        bh.setBlob(blob);
        session.saveDocument(picture);
        session.save();

        // reread
        bh = picture.getAdapter(BlobHolder.class);
        assertNotNull(bh);
        assertTrue(bh instanceof DocumentBlobHolder);
        assertEquals("/pic/logo.jpg", bh.getFilePath());
        blob = bh.getBlob();
        assertEquals("logo.jpg", blob.getFilename());
        assertEquals("image/jpeg", blob.getMimeType());
        byte[] bytes = FileUtils.readBytes(blob.getStream());
        assertEquals(2022140, bytes.length);
        bytes = null;

        // generated views
        assertEquals(5, bh.getBlobs().size());

        // test set null blob
        bh.setBlob(null);
        session.saveDocument(picture);
        session.save();
        blob = bh.getBlob();
        assertNull(blob);
    }

}
