package com.example.jobportal.network;

import android.util.Log;
import okhttp3.*;
import okio.Buffer;
import okio.BufferedSource;
import java.io.IOException;

/**
 * Interceptor to handle non-JSON responses and convert them to proper error responses
 */
public class ResponseInterceptor implements Interceptor {
    private static final String TAG = "ResponseInterceptor";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        
        // Only process responses for our API endpoints
        if (!request.url().toString().contains("emps.co.in/api/")) {
            return response;
        }
        
        // Check if response body exists
        ResponseBody responseBody = response.body();
        if (responseBody == null) {
            return response;
        }
        
        // Read the response body
        BufferedSource source = responseBody.source();
        source.request(Long.MAX_VALUE);
        Buffer buffer = source.getBuffer();
        String responseString = buffer.clone().readUtf8();
        
        Log.d(TAG, "API Response for " + request.url() + ": " + responseString);
        
        // Check if the response is not JSON
        String trimmedResponse = responseString.trim();
        boolean isJson = (trimmedResponse.startsWith("{") && trimmedResponse.endsWith("}")) ||
                        (trimmedResponse.startsWith("[") && trimmedResponse.endsWith("]"));
        
        if (!isJson && response.isSuccessful()) {
            Log.w(TAG, "Server returned non-JSON response: " + trimmedResponse);
            
            // Create a proper JSON error response
            String jsonErrorResponse;
            if (trimmedResponse.startsWith("<")) {
                // HTML response (likely an error page)
                jsonErrorResponse = "{\"success\":false,\"message\":\"Server error - please try again later\",\"data\":null}";
            } else {
                // Plain text response
                jsonErrorResponse = "{\"success\":false,\"message\":\"" + trimmedResponse.replace("\"", "\\\"") + "\",\"data\":null}";
            }
            
            // Create new response body with JSON
            ResponseBody newResponseBody = ResponseBody.create(
                MediaType.parse("application/json"),
                jsonErrorResponse
            );
            
            // Return modified response
            return response.newBuilder()
                    .body(newResponseBody)
                    .build();
        }
        
        return response;
    }
}