/*
 * Copyright (c) 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     bstefanescu
 */
package org.nuxeo.ecm.platform.picture.core.test;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationParameters;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.platform.picture.convert.operation.PictureResize;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;

@RunWith(FeaturesRunner.class)
@Features(AutomationFeature.class)
@Deploy({ "org.nuxeo.ecm.platform.picture.api",
        "org.nuxeo.ecm.platform.picture.convert",
        "org.nuxeo.ecm.platform.picture.core",
        "org.nuxeo.ecm.platform.commandline.executor" })
public class TestPictureResize {

    @Inject
    AutomationService service;

    @Inject
    CoreSession session;

    @Test
    public void testResizer1() throws Exception {
        Blob source = new FileBlob(
                FileUtils.getResourceFileFromContext("images/test.jpg"));

        OperationContext ctx = new OperationContext(session);

        ctx.setInput(source);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("maxWidth", 150);
        params.put("maxHeight", 300);
        OperationChain chain = new OperationChain("fakeChain");
        OperationParameters oparams = new OperationParameters(PictureResize.ID,
                params);
        chain.add(oparams);

        Blob result = (Blob) service.run(ctx, chain);

        assertNotNull(result);
        // FileUtils.copyToFile(result.getStream(), new
        // File("/tmp/convert.test"));
    }
}
