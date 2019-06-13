package fr.univ_tours.info.im_olap.model;

import com.google.gson.*;

import java.lang.reflect.Type;


public class QueryPartDeserializer implements JsonDeserializer<QueryPart> {

    @Override
    public QueryPart deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject obj = jsonElement.getAsJsonObject();
        return null; //TODO
    }
}
