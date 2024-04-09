package com.codescene.gerrit;

import com.google.gerrit.entities.Project;
import com.google.gerrit.server.change.RevisionResource;
import com.google.gerrit.server.project.NoSuchProjectException;
import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class RevisionLink {

    private final Controller controller;

    public RevisionLink(Controller controller, RevisionResource revision) {
        this.controller = controller;
        String sha = revision.getPatchSet().commitId().getName();
        String repo = revision.getProject().toString();
        String reviewId = revision.getChange().getId().toString();
        String branch = revision.getChange().getDest().shortName();
        String changeId = repo + "~" + branch + "~" + revision.getChange().getKey();
        int patch = revision.getPatchSet().id().get();
        params = new HashMap<>();
        params.put("repo", repo);
        params.put("review-id", reviewId);
        params.put("sha", sha);
        params.put("change-id", changeId);
        params.put("patch", "" + patch);
    }

    public URI getUrl(Project.NameKey projectKey) throws URISyntaxException, NoSuchProjectException {
        String base = controller.getUrl(projectKey);
        if (base != null) {
            URIBuilder builder = new URIBuilder(base);
            for (Map.Entry<String, String> qparam : params.entrySet()) {
                builder.addParameter(qparam.getKey(), qparam.getValue());
            }
            return builder.build();
        } else {
            return null;
        }
    }

    public Map<String, String> getHeaders(Project.NameKey project) throws NoSuchProjectException {
        String username = controller.getConfigVal(project, "username");
        String password = controller.getConfigVal(project, "password");
        if (username != null && password != null) {
            Map<String, String> m = new HashMap<>();
            byte[] bytes = (username + ":" + password).getBytes(StandardCharsets.UTF_8);
            m.put("authorization", "Basic " + new String(Base64.getEncoder().encode(bytes), StandardCharsets.UTF_8));
            return m;
        } else {
            return null;
        }
    }
    private final Map<String, String> params;

}
