package com.symphony.bdk.core.api.invoker;

import lombok.Getter;
import org.apiguardian.api.API;

import javax.ws.rs.core.GenericType;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

/**
 * Main exception raised when invoking {@link ApiClient#invokeAPI(String, String, List, Object, Map, Map, Map, String, String, String[], GenericType)}.
 *
 * Initially generated by the OpenAPI Maven Generator
 */
@Getter
@API(status = API.Status.STABLE)
public class ApiException extends Exception {

    private int code = 0;
    private Map<String, List<String>> responseHeaders = null;
    private String responseBody = null;

    /**
     * Creates new {@link ApiException} instance.
     *
     * @param message the detail message.
     * @param throwable the cause.
     */
    public ApiException(String message, Throwable throwable) {
        super(message, throwable);
    }

    /**
     * Creates new {@link ApiException} instance.
     *
     * @param code the HTTP response status code.
     * @param message the detail message.
     */
    public ApiException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * Creates new {@link ApiException} instance.
     *
     * @param code the HTTP response status code.
     * @param message the detail message.
     * @param responseHeaders list of headers returned by the server.
     * @param responseBody content of the response sent back by the server.
     */
    public ApiException(int code, String message, Map<String, List<String>> responseHeaders, String responseBody) {
        this(code, message);
        this.responseHeaders = responseHeaders;
        this.responseBody = responseBody;
    }

    /**
     * Indicates if the error is not a fatal one and if the api call can be subsequently retried.
     *
     * @return true if it error code is 401 unauthorized or 429 too many requests or 5xx greater than 500.
     */
    public boolean isMinorError() {
        return isServerError() || isUnauthorized() || isTooManyRequestsError();
    }

    /**
     * Check if response status is unauthorized or not.
     *
     * @return true if response status is 401, false otherwise
     */
    public boolean isUnauthorized() {
        return this.code == HttpURLConnection.HTTP_UNAUTHORIZED;
    }

    /**
     * Check if response status is client error or not
     *
     * @return true if response status is 400, false otherwise
     */
    public boolean isClientError() {
        return this.code == HttpURLConnection.HTTP_BAD_REQUEST;
    }

    /**
     * Check if response status is a server error (5xx) but not an internal server error (500)
     *
     * @return true if response status strictly greater than 500, false otherwise
     */
    public boolean isServerError() {
        return this.code > HttpURLConnection.HTTP_INTERNAL_ERROR;
    }

    /**
     * Check if response status corresponds to a too many requests error (429)
     *
     * @return true if error code is 429
     */
    public boolean isTooManyRequestsError() {
        return this.code == 429;
    }
}
