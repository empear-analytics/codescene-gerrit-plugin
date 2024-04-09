package com.codescene.gerrit;

import com.google.gerrit.entities.Project;
import com.google.gerrit.extensions.restapi.Response;
import com.google.gerrit.extensions.restapi.RestModifyView;
import com.google.gerrit.server.change.RevisionResource;
import com.google.inject.Inject;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

public class RerunCheck implements RestModifyView<RevisionResource, String> {

    private final Controller controller;

    @Override
    public Response<?> apply(RevisionResource revision, String input) throws Exception {
        Project.NameKey project = revision.getProject();
        RevisionLink link = new RevisionLink(controller, revision);
        Map<String, String> headers = link.getHeaders(project);
        URI url = link.getUrl(project);
        if (url != null && headers != null ) {
            if (!controller.canReadChangeSet(revision.getChange().getDest())) {
                return Response.ok(new CodeScenePrResponse(null, "Cannot access ref under review", 500));
            } else {
                try {
                    return Response.ok(CodeScenePrResponse.processResult(controller.patch(project, url, headers)));
                } catch (IOException e) {
                    System.out.println(e);
                    e.printStackTrace();
                    return Response.ok(new CodeScenePrResponse(null, e.getMessage(), 400));
                }
            }
        }
        return Response.ok(new CodeScenePrResponse());
    }

    @Inject
    public RerunCheck(Controller controller) {
        this.controller = controller;
    }


}
