package com.example.jobportal.models;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonArray;
import java.lang.reflect.Type;

public class SkillsDeserializer implements JsonDeserializer<String> {
    @Override
    public String deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonNull()) {
            return "";
        }
        
        if (json.isJsonPrimitive()) {
            // If it's already a string, return it
            return json.getAsString();
        }
        
        if (json.isJsonArray()) {
            // If it's an array, join the elements with commas
            JsonArray array = json.getAsJsonArray();
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < array.size(); i++) {
                if (i > 0) {
                    result.append(", ");
                }
                result.append(array.get(i).getAsString());
            }
            return result.toString();
        }
        
        return "";
    }
}