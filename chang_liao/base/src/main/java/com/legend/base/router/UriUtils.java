package com.legend.base.router;

import android.net.Uri;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class UriUtils {
    /**
     * Split query parameters
     * @param rawUri raw uri
     * @return map with params
     */
    public static Map<String, String> splitQueryParameters(Uri rawUri) {
        String query = rawUri.getEncodedQuery();

        if (query == null) {
            return Collections.emptyMap();
        }

        Map<String, String> paramMap = new LinkedHashMap<>();
        int start = 0;
        do {
            int next = query.indexOf('&', start);
            int end = (next == -1) ? query.length() : next;

            int separator = query.indexOf('=', start);
            if (separator > end || separator == -1) {
                separator = end;
            }

            String name = query.substring(start, separator);

            if (!android.text.TextUtils.isEmpty(name)) {
                String value = (separator == end ? "" : query.substring(separator + 1, end));
                paramMap.put(Uri.decode(name), Uri.decode(value));
            }

            // Move start to end of name.
            start = end + 1;
        } while (start < query.length());

        return Collections.unmodifiableMap(paramMap);
    }

    /**
     * 输出uri各部分内容
     * @param uri uri
     * @return format string
     */
    public static String toFormatString(Uri uri) {
        String scheme = uri.getScheme();
        String host = uri.getHost();
        String path = uri.getPath();
        String encodedPath = uri.getEncodedPath();
        String query = uri.getQuery();
        String encodedQuery = uri.getEncodedQuery();
        String fragment = uri.getFragment();
        String encodedFragment = uri.getEncodedFragment();
        String schemeSpecificPart = uri.getSchemeSpecificPart();
        String encodedSchemeSpecificPart = uri.getEncodedSchemeSpecificPart();

        return "scheme = " + scheme +
                ", \nhost = " + host +
                ", \npath = " + path +
                ", \nencodedPath = " + encodedPath +
                ", \nquery = " + query +
                ", \nencodedQuery = " + encodedQuery +
                ", \nfragment = " + fragment +
                ", \nencodedFragment = " + encodedFragment +
                ", \nschemeSpecificPart = " + schemeSpecificPart +
                ", \nencodedSchemeSpecificPart = " + encodedSchemeSpecificPart;
    }

}
