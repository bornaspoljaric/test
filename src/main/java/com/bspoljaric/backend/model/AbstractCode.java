package com.bspoljaric.backend.model;

import java.io.Serializable;

public abstract class AbstractCode {

    private Serializable id;

    private String name;

    private String code;

    public AbstractCode(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public Serializable getId() {
        return id;
    }

    public void setId(Serializable id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
