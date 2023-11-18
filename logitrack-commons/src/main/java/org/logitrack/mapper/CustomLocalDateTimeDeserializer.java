package org.logitrack.mapper;


import com.fasterxml.jackson.core.JsonParser;

import com.fasterxml.jackson.databind.DeserializationContext;

import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;


public class CustomLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override

    public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

        String dateStr = jsonParser.getValueAsString();

        return LocalDateTime.parse(dateStr, formatter);

    }

}
