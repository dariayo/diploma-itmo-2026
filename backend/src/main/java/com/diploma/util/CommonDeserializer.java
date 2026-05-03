package com.diploma.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.diploma.models.xml.Common;

import java.io.IOException;

public class CommonDeserializer extends JsonDeserializer<Common> {
    @Override
    public Common deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);

        Common common = new Common();
        if (node.has("to")) common.setTo(node.get("to").asText());
        if (node.has("freq")) common.setFreq(node.get("freq").asText());
        if (node.has("fir")) common.setFir(node.get("fir").asText());
        if (node.has("awy")) common.setAwy(node.get("awy").asText());
        if (node.has("stb")) common.setStb(node.get("stb").asInt());
        if (node.has("gmora")) common.setGmora(node.get("gmora").asInt());
        if (node.has("fl")) common.setFl(node.get("fl").asText());
        if (node.has("pointType")) common.setPointType(node.get("pointType").asText());


        String wind = node.get("wind").asText();
        if (wind != null && wind.length() >= 3) {
            common.setWindSpeed(wind.substring(0, 3));
            common.setWindAngle(wind.substring(3));
        } else {
            common.setWindSpeed(wind);
            common.setWindAngle("");
        }
        String temp = node.get("temp").asText();

        if (temp.isEmpty()) {
            common.setTemp(null);
        } else {
            common.setTemp(-1 * Integer.parseInt(temp.substring(1)));
        }
        return common;
    }
}
