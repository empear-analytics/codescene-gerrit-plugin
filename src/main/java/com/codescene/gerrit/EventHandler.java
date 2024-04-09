package com.codescene.gerrit;

import com.google.common.base.Supplier;
import com.google.gerrit.server.events.Event;
import com.google.gerrit.server.events.EventListener;
import com.google.gerrit.server.events.PatchSetCreatedEvent;
import com.google.gerrit.server.events.SupplierSerializer;
import com.google.gson.*;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Instant;

public class EventHandler implements EventListener {
    private static final Logger log = LoggerFactory
            .getLogger(EventHandler.class);

    private final Controller controller;

    @Inject
    public EventHandler(Controller controller) {

        this.controller = controller;
    }
    private static Gson GSON =
            new GsonBuilder()
                    .registerTypeAdapter(Supplier.class, new SupplierSerializer())
                    .registerTypeAdapter(Instant.class, new InstantSerializer())
                    .create();

    public static class InstantSerializer implements JsonSerializer<Instant> {
        public InstantSerializer() {
        }

        public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src.toString());
        }
    }

    @Override
    public void onEvent(Event event) {
        if ("patchset-created".equals(event.getType())) {
            try {
                controller.post(((PatchSetCreatedEvent)event).getProjectNameKey(), GSON.toJson(event));
            } catch (IOException e) {
                log.error("Error POSTing to CodeScene", e);
            }
        }
    }
}
