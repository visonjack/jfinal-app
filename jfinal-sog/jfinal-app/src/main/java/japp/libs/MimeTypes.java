/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2014 sagyf Yang. The Four Group.
 */

package japp.libs;

import japp.JApp;
import japp.StringPool;
import japp.Logger;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * .
 * </p>
 *
 * @author sagyf yang
 * @version 1.0 2014-04-04 10:24
 * @since JDK 1.6
 */
public class MimeTypes {


    private static Properties mimetypes = null;
    private static Pattern extPattern;

    static {
        extPattern = Pattern.compile("^.*\\.([^.]+)$");
    }

    /**
     * return the mimetype from a file name
     *
     * @param filename the file name
     * @return the mimetype or the empty string if not found
     */
    public static String getMimeType(String filename) {
        return getMimeType(filename, "");
    }

    /**
     * return the mimetype from a file name.<br/>
     *
     * @param filename        the file name
     * @param defaultMimeType the default mime type to return when no matching mimetype is found
     * @return the mimetype
     */
    public static String getMimeType(String filename, String defaultMimeType) {
        Matcher matcher = extPattern.matcher(filename.toLowerCase());
        String ext = "";
        if (matcher.matches()) {
            ext = matcher.group(1);
        }
        if (ext.length() > 0) {
            String mimeType = mimetypes().getProperty(ext);
            if (mimeType == null) {
                return defaultMimeType;
            }
            return mimeType;
        }
        return defaultMimeType;
    }

    /**
     * return the content-type from a file name. If none is found returning application/octet-stream<br/>
     * For a text-based content-type, also return the encoding suffix eg. <em>"text/plain; charset=utf-8"</em>
     *
     * @param filename the file name
     * @return the content-type deduced from the file extension.
     */
    public static String getContentType(String filename) {
        return getContentType(filename, "application/octet-stream");
    }

    /**
     * return the content-type from a file name.<br/>
     * For a text-based content-type, also return the encoding suffix eg. <em>"text/plain; charset=utf-8"</em>
     *
     * @param filename           the file name
     * @param defaultContentType the default content-type to return when no matching content-type is found
     * @return the content-type deduced from the file extension.
     */
    public static String getContentType(String filename, String defaultContentType) {
        String contentType = getMimeType(filename, null);
        if (contentType == null) {
            contentType = defaultContentType;
        }
        if (contentType != null && contentType.startsWith("text/")) {
            return contentType + "; charset=" + StringPool.UTF_8;
        }
        return contentType;
    }

    /**
     * check the mimetype is referenced in the mimetypes database
     *
     * @param mimeType the mimeType to verify
     */
    public static boolean isValidMimeType(String mimeType) {
        if (mimeType == null) {
            return false;
        } else if (mimeType.contains(";")) {
            return mimetypes().contains(mimeType.split(";")[0]);
        } else {
            return mimetypes().contains(mimeType);
        }
    }


    private static synchronized void initMimetypes() {
        if (mimetypes != null) return;
        // Load default mimetypes from the framework
        try {
            InputStream is = MimeTypes.class.getClassLoader().getResourceAsStream("libs/mime-types.properties");
            mimetypes = new Properties();
            mimetypes.load(is);
        } catch (Exception ex) {
            Logger.warn(ex.getMessage());
        }
        // Load custom mimetypes from the application configuration
        if (JApp.initlization) {
            Enumeration<Object> confenum = JApp.configuration.keys();
            while (confenum.hasMoreElements()) {
                String key = (String) confenum.nextElement();
                if (key.startsWith("mimetype.")) {
                    String type = key.substring(key.indexOf('.') + 1).toLowerCase();
                    String value = (String) JApp.configuration.get(key);
                    mimetypes.setProperty(type, value);
                }
            }
        }
    }

    private static Properties mimetypes() {
        if (mimetypes == null) {
            initMimetypes();
        }
        return mimetypes;
    }

}
