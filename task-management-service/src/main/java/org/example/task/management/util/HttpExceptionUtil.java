package org.example.task.management.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

public class HttpExceptionUtil {
    public static HttpClientErrorException create(HttpStatus httpStatus) {
        return HttpClientErrorException.create(httpStatus, httpStatus.getReasonPhrase(), null, null, null);
    }
}
