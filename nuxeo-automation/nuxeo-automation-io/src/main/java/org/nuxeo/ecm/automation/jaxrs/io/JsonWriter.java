/*
 * (C) Copyright 2006-2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     bstefanescu
 */
package org.nuxeo.ecm.automation.jaxrs.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationDocumentation;
import org.nuxeo.ecm.automation.OperationDocumentation.Param;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.io.services.codec.ObjectCodec;
import org.nuxeo.ecm.automation.io.services.codec.ObjectCodecService;
import org.nuxeo.ecm.automation.jaxrs.LoginInfo;
import org.nuxeo.ecm.automation.jaxrs.io.operations.AutomationInfo;
import org.nuxeo.ecm.platform.forms.layout.api.WidgetDefinition;
import org.nuxeo.ecm.platform.forms.layout.io.JSONLayoutExporter;
import org.nuxeo.ecm.webengine.JsonFactoryManager;
import org.nuxeo.ecm.webengine.WebException;
import org.nuxeo.runtime.api.Framework;

/**
 * Json writer for operations export.
 *
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 * @author <a href="mailto:grenard@nuxeo.com">Guillaume Renard</a>
 */
public class JsonWriter {

    private static JsonFactory getFactory() {
        return Framework.getLocalService(JsonFactoryManager.class).getJsonFactory();
    }

    private static JsonGenerator createGenerator(OutputStream out)
            throws IOException {
        return getFactory().createJsonGenerator(out, JsonEncoding.UTF8);
    }

    public static void writeAutomationInfo(OutputStream out,
            AutomationInfo info, boolean prettyPrint) throws IOException {
        writeAutomationInfo(createGenerator(out), info, prettyPrint);
    }

    public static void writeAutomationInfo(JsonGenerator jg,
            AutomationInfo info, boolean prettyPrint) throws IOException {
        if (prettyPrint) {
            jg.useDefaultPrettyPrinter();
        }
        jg.writeStartObject();
        writePaths(jg);
        writeCodecs(jg);
        writeOperations(jg, info);
        writeChains(jg, info);
        jg.writeEndObject();
        jg.flush();
    }

    private static void writePaths(JsonGenerator jg) throws IOException {
        jg.writeObjectFieldStart("paths");
        jg.writeStringField("login", "login");
        jg.writeEndObject();
    }

    private static void writeCodecs(JsonGenerator jg) throws IOException {
        jg.writeArrayFieldStart("codecs");
        ObjectCodecService codecs = Framework.getLocalService(ObjectCodecService.class);
        for (ObjectCodec<?> codec : codecs.getCodecs()) {
            if (!codec.isBuiltin()) {
                jg.writeString(codec.getClass().getName());
            }
        }
        jg.writeEndArray();
    }

    /**
     * Used to export operations to studio.
     */
    public static String exportOperations() throws IOException,
            OperationException {
        return exportOperations(false);
    }

    /**
     * Used to export operations to studio.
     *
     * @param filterNotInStudio if true, operation types not exposed in Studio
     *            will be filtered.
     * @since 5.9.1
     */
    public static String exportOperations(boolean filterNotInStudio)
            throws IOException, OperationException {
        List<OperationDocumentation> ops = Framework.getLocalService(
                AutomationService.class).getDocumentation();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator jg = getFactory().createJsonGenerator(out);
        jg.useDefaultPrettyPrinter();
        jg.writeStartObject();
        jg.writeArrayFieldStart("operations");
        for (OperationDocumentation op : ops) {
            if (filterNotInStudio) {
                if (op.addToStudio && !Constants.CAT_CHAIN.equals(op.category)) {
                    writeOperation(jg, op);
                }
            } else {
                writeOperation(jg, op);
            }
        }
        jg.writeEndArray();
        jg.writeEndObject();
        jg.flush();
        return out.toString("UTF-8");
    }

    private static void writeOperations(JsonGenerator jg, AutomationInfo info)
            throws IOException {
        jg.writeArrayFieldStart("operations");
        for (OperationDocumentation op : info.getOperations()) {
            writeOperation(jg, op);
        }
        jg.writeEndArray();
    }

    private static void writeChains(JsonGenerator jg, AutomationInfo info)
            throws IOException {
        jg.writeArrayFieldStart("chains");
        for (OperationDocumentation op : info.getChains()) {
            writeOperation(jg, op, Constants.CHAIN_ID_PREFIX + op.id);
        }
        jg.writeEndArray();
    }

    public static void writeOperation(OutputStream out,
            OperationDocumentation op) throws IOException {
        writeOperation(out, op, false);
    }

    /**
     * @since 5.9.4
     */
    public static void writeOperation(OutputStream out,
            OperationDocumentation op, boolean prettyPrint) throws IOException {
        writeOperation(createGenerator(out), op, prettyPrint);
    }

    public static void writeOperation(JsonGenerator jg,
            OperationDocumentation op) throws IOException {
        writeOperation(jg, op, false);
    }

    /**
     * @since 5.9.4
     */
    public static void writeOperation(JsonGenerator jg,
            OperationDocumentation op, boolean prettyPrint) throws IOException {
        writeOperation(jg, op, op.url, prettyPrint);
    }

    public static void writeOperation(JsonGenerator jg,
            OperationDocumentation op, String url) throws IOException {
        writeOperation(jg, op, url, false);
    }

    /**
     * @since 5.9.4
     */
    public static void writeOperation(JsonGenerator jg,
            OperationDocumentation op, String url, boolean prettyPrint)
            throws IOException {
        if (prettyPrint) {
            jg.useDefaultPrettyPrinter();
        }
        jg.writeStartObject();
        jg.writeStringField("id", op.id);
        jg.writeStringField("label", op.label);
        jg.writeStringField("category", op.category);
        jg.writeStringField("requires", op.requires);
        jg.writeStringField("description", op.description);
        if (op.since != null && op.since.length() > 0) {
            jg.writeStringField("since", op.since);
        }
        jg.writeStringField("url", url);
        jg.writeArrayFieldStart("signature");
        for (String s : op.signature) {
            jg.writeString(s);
        }
        jg.writeEndArray();
        writeParams(jg, Arrays.asList(op.params));
        if (op.widgetDefinitions != null && op.widgetDefinitions.length > 0) {
            jg.writeArrayFieldStart("widgets");
            for (WidgetDefinition wdef : op.widgetDefinitions) {
                jg.writeObject(JSONLayoutExporter.exportToJson(wdef, null, null));
            }
            jg.writeEndArray();
        }
        jg.writeEndObject();
        jg.flush();
    }

    private static void writeParams(JsonGenerator jg, List<Param> params)
            throws IOException {
        jg.writeArrayFieldStart("params");
        for (Param p : params) {
            jg.writeStartObject();
            jg.writeStringField("name", p.name);
            jg.writeStringField("description", p.description);
            jg.writeStringField("type", p.type);
            jg.writeBooleanField("required", p.isRequired);

            jg.writeStringField("widget", p.widget);
            jg.writeNumberField("order", p.order);
            jg.writeArrayFieldStart("values");
            for (String value : p.values) {
                jg.writeString(value);
            }
            jg.writeEndArray();
            jg.writeEndObject();
        }
        jg.writeEndArray();
    }

    public static void writeLogin(OutputStream out, LoginInfo login)
            throws IOException {
        writeLogin(createGenerator(out), login);
    }

    public static void writeLogin(JsonGenerator jg, LoginInfo login)
            throws IOException {
        jg.writeStartObject();
        jg.writeStringField("entity-type", "login");
        jg.writeStringField("username", login.getUsername());
        jg.writeBooleanField("isAdministrator", login.isAdministrator());
        jg.writeArrayFieldStart("groups");
        for (String group : login.getGroups()) {
            jg.writeString(group);
        }
        jg.writeEndArray();
        jg.writeEndObject();
        jg.flush();
    }

    public static void writePrimitive(OutputStream out, Object value)
            throws IOException {
        writePrimitive(createGenerator(out), value);
    }

    public static void writePrimitive(JsonGenerator jg, Object value)
            throws IOException {
        jg.writeStartObject();
        jg.writeStringField("entity-type", "primitive");
        if (value != null) {
            Class<?> type = value.getClass();
            if (type == String.class) {
                jg.writeStringField("value", (String) value);
            } else if (type == Boolean.class) {
                jg.writeBooleanField("value", (Boolean) value);
            } else if (type == Long.class) {
                jg.writeNumberField("value", ((Number) value).longValue());
            } else if (type == Double.class) {
                jg.writeNumberField("value", ((Number) value).doubleValue());
            } else if (type == Integer.class) {
                jg.writeNumberField("value", ((Number) value).intValue());
            } else if (type == Float.class) {
                jg.writeNumberField("value", ((Number) value).floatValue());
            }
        } else {
            jg.writeNullField("value");
        }
        jg.writeEndObject();
        jg.flush();
    }

    /**
     * @deprecated since 6.0 - use {@link org.nuxeo.ecm.webengine.app
     * .JsonWebengineWriter#writeException(java.io.OutputStream,
     * org.nuxeo.ecm.webengine.WebException)} instead
     */
    @Deprecated
    public static void writeException(OutputStream out, WebException eh)
            throws IOException {
        writeException(createGenerator(out), eh);
    }

    /**
     * @deprecated since 6.0 - use {@link org.nuxeo.ecm.webengine.app
     * .JsonWebengineWriter#writeException(org.codehaus.jackson.JsonGenerator,
     * org.nuxeo.ecm.webengine.WebException)} instead
     */
    @Deprecated
    public static void writeException(JsonGenerator jg, WebException eh)
            throws IOException {
        jg.writeStartObject();
        jg.writeStringField("entity-type", "exception");
        jg.writeStringField("type", eh.getType());
        jg.writeNumberField("status", eh.getStatus());
        jg.writeStringField("message", eh.getMessage());
        jg.writeStringField("stack", eh.getStackTraceString());
        jg.writeObjectField("cause", eh.getCause());
        jg.writeEndObject();
        jg.flush();
    }

}
