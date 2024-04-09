package com.codescene.gerrit;

import com.google.gson.annotations.SerializedName;

public class CodeScenePrResponse {
    public CodeScenePrResponse(String body, String error, int code) {
        this.body = body;
        this.error = error;
        this.code = code;
    }

    public CodeScenePrResponse() {
    }

    @SerializedName("body")
    String body;

    @SerializedName("error")
    String error;

    @SerializedName("code")
    int code;

    static CodeScenePrResponse processResult(HttpResponseHandler.HttpResult httpResult) {
        CodeScenePrResponse response = new CodeScenePrResponse();
        if (httpResult == null) {
            return response;
        }
        if (httpResult.successful) {
            response.body = httpResult.message;
            response.code = 200;
        } else if (httpResult.status == 304) {
            response.code = 304;
        } else {
            switch (httpResult.status) {
                case 404: {
                    response.error = "No review available";
                    response.code = 400;
                    break;
                }
                case 401: {
                    response.error = "Configured user cannot authenticate with CodeScene";
                    response.code = 500;
                    break;
                }
                case 403: {
                    response.error = "Configured user cannot access project";
                    response.code = 500;
                    break;
                }
                case 500: {
                    response.error = "Server Error, check logs";
                    response.code = 500;
                    break;
                }
                case 502: {
                    response.error = "Gateway Error";
                    response.code = 500;
                    break;
                }
                default: {
                    response.error = httpResult.message;
                    response.code = 400;
                }
            }
        }
        return response;
    }
}
