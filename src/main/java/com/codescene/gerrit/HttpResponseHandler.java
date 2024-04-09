// Copyright (C) 2017 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.codescene.gerrit;

import com.google.common.flogger.FluentLogger;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static javax.servlet.http.HttpServletResponse.*;

class HttpResponseHandler implements ResponseHandler<HttpResponseHandler.HttpResult> {

  static class HttpResult {
    final boolean successful;
    final String message;

    final int status;

    final Map<String, String> headers;

    HttpResult(boolean successful, String message, int status, Map<String, String> headers) {
      this.successful = successful;
      this.message = message;
      this.status = status;
      this.headers = headers;
    }

    @Override
    public String toString() {
      return "HttpResult{" +
              "successful=" + successful +
              ", message='" + message + '\'' +
              ", status=" + status +
              ", headers=" + headers +
              '}';
    }
  }

  private static final FluentLogger log = FluentLogger.forEnclosingClass();

  @Override
  public HttpResult handleResponse(HttpResponse response) {
    return new HttpResult(isSuccessful(response),
            parseResponse(response),
            response.getStatusLine().getStatusCode(),
            parseHeaders(response));
  }

  private boolean isSuccessful(HttpResponse response) {
    int sc = response.getStatusLine().getStatusCode();
    return sc == SC_CREATED || sc == SC_ACCEPTED || sc == SC_NO_CONTENT || sc == SC_OK;
  }

  private String parseResponse(HttpResponse response) {
    HttpEntity entity = response.getEntity();
    if (entity != null) {
      try {
        return EntityUtils.toString(entity, StandardCharsets.UTF_8);
      } catch (IOException e) {
        log.atSevere().withCause(e).log("Error parsing entity");
      }
    }
    return "";
  }

  private Map<String, String> parseHeaders(HttpResponse response) {
    Map<String, String> ret = new HashMap<>();
    for (Header h : response.getAllHeaders()) {
      ret.put(h.getName().toLowerCase(), h.getValue());
    }
    return ret;
  }
}
