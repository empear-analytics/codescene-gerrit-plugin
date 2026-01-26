package com.codescene.gerrit;

import com.google.gerrit.entities.BranchNameKey;
import com.google.gerrit.entities.Project;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.server.CurrentUser;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.gerrit.server.permissions.PermissionBackend;
import com.google.gerrit.server.permissions.RefPermission;
import com.google.gerrit.server.project.NoSuchProjectException;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static com.codescene.gerrit.DefaultHttpClientProvider.DEFAULT;
import static com.codescene.gerrit.SslVerifyingHttpClientProvider.SSL_VERIFY;

@Singleton
public class Controller {

    private static final Logger log = LoggerFactory
            .getLogger(Controller.class);
    private final String pluginName;
    private final PluginConfigFactory config;

    private final PermissionBackend permissionBackend;
    private final Provider<CurrentUser> currentUser;
    private final Provider<CloseableHttpClient> defaultClientProvider;
    private final Provider<CloseableHttpClient> sslVerifyingClientProvider;

    @Inject
    public Controller(PluginConfigFactory config, @PluginName String pluginName,
                            PermissionBackend permissionBackend,
                            Provider<CurrentUser> currentUser,
                            @Named(DEFAULT) Provider<CloseableHttpClient> defaultClientProvider,
                            @Named(SSL_VERIFY) Provider<CloseableHttpClient> sslVerifyingClientProvider) {
        this.config = config;
        this.pluginName = pluginName;
        this.permissionBackend = permissionBackend;
        this.currentUser = currentUser;
        this.defaultClientProvider = defaultClientProvider;
        this.sslVerifyingClientProvider = sslVerifyingClientProvider;
    }

    public String getConfigVal(Project.NameKey project, String prop) throws NoSuchProjectException {
        String val = config.getFromProjectConfigWithInheritance(project, pluginName).getString(prop);
        val = val == null ? config.getGlobalPluginConfig(pluginName).getString("server", null, prop) : val;
        val = val == null ? config.getFromGerritConfig(pluginName).getString(prop) : val;
        return val;
    }

    private CloseableHttpClient getClient(Project.NameKey project) throws NoSuchProjectException {
        if ("false".equals(getConfigVal(project, "ssl_verify"))) {
            return defaultClientProvider.get();
        } else {
            return sslVerifyingClientProvider.get();
        }
    }
    public String getUrl(Project.NameKey project) throws NoSuchProjectException {
        String codeSceneUrl = getConfigVal(project, "url");
        return codeSceneUrl == null ? null : codeSceneUrl + "/hooks/plugin-gerrit/review";
    }

    HttpResponseHandler.HttpResult post(Project.NameKey projectKey, String payload) throws IOException {
        try {
            String url = getUrl(projectKey);
            log.info("Posting to url: " + url);
            if (url != null) {
                HttpPost post = new HttpPost(url);
                post.addHeader("Content-Type", "application/json");
                post.setConfig(getRequestConfig());
                post.setEntity(new StringEntity(payload, StandardCharsets.UTF_8));
                return getClient(projectKey).execute(post, new HttpResponseHandler());
            }
        } catch (NoSuchProjectException e) {
            log.error("Unexpected couldn't find project during hook post of " + payload);
        }
        return null;
    }


    HttpResponseHandler.HttpResult get(Project.NameKey projectKey,
                                       URI url,
                                       Map<String, String> headers) throws IOException {
        try {
            HttpGet get = new HttpGet(url);
            for(Map.Entry<String, String> header : headers.entrySet()) {
                get.addHeader(header.getKey(), header.getValue());
            }
            get.setConfig(getRequestConfig());
            return getClient(projectKey).execute(get, new HttpResponseHandler());
        } catch (NoSuchProjectException e) {
            log.error("Unexpected: couldn't find project during GET " + projectKey);
        }
        return null;
    }

    HttpResponseHandler.HttpResult patch(Project.NameKey projectKey,
                                         URI url,
                                         Map<String, String> headers) throws IOException {
        try {
            HttpPatch patch = new HttpPatch(url);
            for(Map.Entry<String, String> header : headers.entrySet()) {
                patch.addHeader(header.getKey(), header.getValue());
            }
            patch.setConfig(getRequestConfig());
            return getClient(projectKey).execute(patch, new HttpResponseHandler());
        } catch (NoSuchProjectException e) {
            log.error("Unexpected: couldn't find project during PATCH " + projectKey);
        }
        return null;
    }

    public RequestConfig getRequestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(10000)
                .setConnectionRequestTimeout(10000)
                .setSocketTimeout(10000)
                .build();
    }

    public boolean canReadChangeSet(BranchNameKey branchNameKey) {
        return permissionBackend.user(currentUser.get()).ref(branchNameKey).testOrFalse(RefPermission.READ);
    }




}
