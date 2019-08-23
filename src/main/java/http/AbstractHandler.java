package http;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

//TODO Add error to http status supporting
public abstract class AbstractHandler implements HttpHandler {

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    protected static final int STATUS_OK = 200;
    protected static final int STATUS_CREATED = 201;
    protected static final int STATUS_BAD_REQUEST = 400;
    protected static final int STATUS_NOT_FOUND = 404;
    private static final int STATUS_METHOD_NOT_ALLOWED = 405;

    private static final int NO_RESPONSE_LENGTH = -1;

    private static final String HEADER_ALLOW = "Allow";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    private static final String METHOD_GET = "GET";
    private static final String METHOD_POST = "POST";
    private static final String ALLOWED_METHODS = METHOD_GET + "," + METHOD_POST;

    public void handle(HttpExchange he) throws IOException {
        try {
            final Headers headers = he.getResponseHeaders();
            final String requestMethod = he.getRequestMethod().toUpperCase();
            final Map<String, String> requestParameters = getRequestParameters(he.getRequestURI());
            switch (requestMethod) {
                case METHOD_GET:
                    processResponse(get(he, requestParameters), headers, he);
                    break;
                case METHOD_POST:
                    String requestBody = getRequestBody(he);
                    processResponse(post(he, requestParameters, requestBody), headers, he);
                    break;
                default:
                    headers.set(HEADER_ALLOW, ALLOWED_METHODS);
                    he.sendResponseHeaders(STATUS_METHOD_NOT_ALLOWED, NO_RESPONSE_LENGTH);
                    break;
            }
        } finally {
            he.close();
        }
    }

    private void processResponse(ResponseBody responseBody, Headers headers, HttpExchange he) throws IOException {
        headers.set(HEADER_CONTENT_TYPE, String.format("application/json; charset=%s", CHARSET));
        String body = responseBody.getResponseBody();
        final byte[] rawResponseBody = body == null ? new byte[]{} : body.getBytes(CHARSET);
        he.sendResponseHeaders(responseBody.getStatusCode(), rawResponseBody.length);
        he.getResponseBody().write(rawResponseBody);
    }

    protected ResponseBody get(HttpExchange he, Map<String, String> requestParameters) {
        throw new RuntimeException("Method GET is not implemented");
    }

    protected ResponseBody post(HttpExchange he, Map<String, String> requestParameters, String requestBody) {
        throw new RuntimeException("Method POST is not implemented");
    }

    private String getRequestBody(HttpExchange he) throws IOException {
        StringBuilder body = new StringBuilder();
        try (InputStreamReader reader = new InputStreamReader(he.getRequestBody(), CHARSET)) {
            char[] buffer = new char[256];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                body.append(buffer, 0, read);
            }
        }
        return body.toString();
    }

    private static Map<String, String> getRequestParameters(final URI requestUri) {
        final Map<String, String> requestParameters = new LinkedHashMap<>();
        final String requestQuery = requestUri.getRawQuery();
        if (requestQuery != null) {
            final String[] rawRequestParameters = requestQuery.split("[&;]", -1);
            for (final String rawRequestParameter : rawRequestParameters) {
                final String[] requestParameter = rawRequestParameter.split("=", 2);
                final String requestParameterName = decodeUrlComponent(requestParameter[0]);
                final String requestParameterValue = requestParameter.length > 1 ? decodeUrlComponent(requestParameter[1]) : null;
                requestParameters.put(requestParameterName, requestParameterValue);
            }
        }
        return requestParameters;
    }

    private static String decodeUrlComponent(final String urlComponent) {
        try {
            return URLDecoder.decode(urlComponent, CHARSET.name());
        } catch (final UnsupportedEncodingException ex) {
            throw new InternalError(ex);
        }
    }

    public static class ResponseBody {
        private final String responseBody;
        private final int statusCode;

        public ResponseBody(String responseBody, int statusCode) {
            this.responseBody = responseBody;
            this.statusCode = statusCode;
        }

        public ResponseBody(int statusCode) {
            this(null, statusCode);
        }

        public String getResponseBody() {
            return responseBody;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }

}
