package org.plus.net;

import retrofit2.Response;

public interface ResponseDelegate<T> {
    void run(Response<T> response, APIError apiError);
}
