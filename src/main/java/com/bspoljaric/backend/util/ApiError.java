package com.bspoljaric.backend.util;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ApiError {
    @XmlElement(name = "status")
    int    status;
    @XmlElement(name = "message")
    String message;

    public ApiError(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public ApiError() {
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
