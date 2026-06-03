package com.agridirect.common.dto;

public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    public ApiResponse() {}

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<T>(true, "Success", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<T>(true, message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<T>(false, message, null);
    }

    public boolean isSuccess()  { return success; }
    public String getMessage()  { return message; }
    public T getData()          { return data; }

    public void setSuccess(boolean v)  { this.success = v; }
    public void setMessage(String v)   { this.message = v; }
    public void setData(T v)           { this.data = v; }
}
