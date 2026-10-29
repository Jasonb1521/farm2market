package com.farm2market.core.protocol;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public class ProtocolSerializer {

    private static final Gson GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .create();

    private static final String MESSAGE_DELIMITER = "\n";

    public static String serialize(Request request) {
        return GSON.toJson(request) + MESSAGE_DELIMITER;
    }

    public static String serialize(Response response) {
        return GSON.toJson(response) + MESSAGE_DELIMITER;
    }

    public static Request deserializeRequest(String json) throws JsonSyntaxException {
        return GSON.fromJson(json.trim(), Request.class);
    }

    public static Response deserializeResponse(String json) throws JsonSyntaxException {
        return GSON.fromJson(json.trim(), Response.class);
    }

    public static <T> T deserializePayload(Object payload, Class<T> clazz) {
        if (payload == null) {
            return null;
        }
        String json = GSON.toJson(payload);
        return GSON.fromJson(json, clazz);
    }

    public static String toJson(Object obj) {
        return GSON.toJson(obj);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }

    public static Gson getGson() {
        return GSON;
    }
}
