package com.codescene.gerrit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gerrit.entities.Project;
import com.google.gerrit.extensions.restapi.Response;
import com.google.gerrit.extensions.restapi.RestReadView;
import com.google.gerrit.server.change.RevisionResource;
import com.google.gerrit.server.project.NoSuchProjectException;
import com.google.inject.Inject;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GetResults implements RestReadView<RevisionResource> {
    private final Controller controller;

    private static Cache<String, HttpResponseHandler.HttpResult> responses =
            Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).maximumSize(200).build();

    private HttpResponseHandler.HttpResult doGet(Project.NameKey project, URI url, Map<String, String> headers) throws IOException, NoSuchProjectException {
        long time = System.currentTimeMillis();
        HttpResponseHandler.HttpResult prevResponse = responses.getIfPresent(url.toString());
        if (prevResponse != null && prevResponse.headers.get("etag") != null && !"false".equals(controller.getConfigVal(project, "cache"))) {
            //headers.put("if-none-match", prevResponse.headers.get("etag"));
        }
        HttpResponseHandler.HttpResult response = controller.get(project, url, headers);
        if (response != null && response.status == 304) {
            response = prevResponse;
        } else if (response != null && response.status == 200 && response.headers.get("etag") != null) {
            responses.put(url.toString(), response);
        }
        return response;
    }

    @Override
    public Response<CodeScenePrResponse> apply(RevisionResource revision) throws Exception {
        Project.NameKey project = revision.getProject();
        RevisionLink link = new RevisionLink(controller, revision);
        Map<String, String> headers = link.getHeaders(project);
        URI url = link.getUrl(project);
        if (url == null || headers == null) {
            return Response.ok(new CodeScenePrResponse());
        }
        if (!controller.canReadChangeSet(revision.getChange().getDest())) {
            return Response.ok(new CodeScenePrResponse(null, "Cannot access ref under review", 500));
        }
        try {
            return Response.ok(CodeScenePrResponse.processResult(doGet(project, url, headers)));
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
            return Response.ok(new CodeScenePrResponse(null, e.getMessage(), 400));
        }
    }

    @Inject
    public GetResults(Controller controller) {
        this.controller = controller;
    }
}
