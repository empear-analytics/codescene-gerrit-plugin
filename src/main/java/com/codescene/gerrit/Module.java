// Copyright (C) 2015 The Android Open Source Project
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

import com.google.gerrit.extensions.registration.DynamicSet;
import com.google.gerrit.extensions.restapi.RestApiModule;
import com.google.gerrit.extensions.webui.JavaScriptPlugin;
import com.google.gerrit.extensions.webui.WebUiPlugin;
import com.google.gerrit.server.events.EventListener;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import org.apache.http.impl.client.CloseableHttpClient;

import static com.codescene.gerrit.DefaultHttpClientProvider.DEFAULT;
import static com.codescene.gerrit.SslVerifyingHttpClientProvider.SSL_VERIFY;
import static com.google.gerrit.server.change.RevisionResource.REVISION_KIND;

class Module extends RestApiModule {

  @Override
  protected void configure() {
    DynamicSet.bind(binder(), WebUiPlugin.class).toInstance(new JavaScriptPlugin("codescene.js"));
    DynamicSet.bind(binder(), EventListener.class).to(EventHandler.class);
    bind(CloseableHttpClient.class).annotatedWith(Names.named(DEFAULT))
            .toProvider(DefaultHttpClientProvider.class).in(Scopes.SINGLETON);
    bind(CloseableHttpClient.class).annotatedWith(Names.named(SSL_VERIFY))
            .toProvider(SslVerifyingHttpClientProvider.class).in(Scopes.SINGLETON);

    get(REVISION_KIND, "result").to(GetResults.class);
    put(REVISION_KIND, "result").to(RerunCheck.class);
  }
}
