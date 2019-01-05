package com.zlin.translate.model;

import com.google.gson.Gson;

import java.io.Serializable;

/**
 * Created by zhanglin03 on 2019/1/3.
 */

public class BaseModel implements Serializable {
    /**
     * errorMessage :
     * hasErrors : false
     * success : true
     */
    private String errorMessage;
    private boolean hasErrors;
    private boolean success;

    public static <T> T fromJson(Class<T> c, String jsonStr) {
        return new Gson().fromJson(jsonStr, c);
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setHasErrors(boolean hasErrors) {
        this.hasErrors = hasErrors;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isHasErrors() {
        return hasErrors;
    }

    public boolean isSuccess() {
        return success;
    }
}
