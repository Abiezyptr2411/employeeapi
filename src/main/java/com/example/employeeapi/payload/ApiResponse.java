package com.example.employeeapi.payload;

public class ApiResponse<T> {
    private String status;
    private String code; 
    private T data;

    public ApiResponse() {}

    public ApiResponse(String status, String code, T data) {
        this.status = status;
        this.code = code;
        this.data = data;
    }

    // Getters & Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
