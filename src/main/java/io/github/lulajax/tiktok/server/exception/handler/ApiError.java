package io.github.lulajax.tiktok.server.exception.handler;

import lombok.Data;
@Data
class ApiError {

    private long code = 400;
    private String message;

    public static ApiError error(String message){
        ApiError apiError = new ApiError();
        apiError.setMessage(message);
        return apiError;
    }

    public static ApiError error(Integer status, String message){
        ApiError apiError = new ApiError();
        apiError.setCode(status);
        apiError.setMessage(message);
        return apiError;
    }
}


