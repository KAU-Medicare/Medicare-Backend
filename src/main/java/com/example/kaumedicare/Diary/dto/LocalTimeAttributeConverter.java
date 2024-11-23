package com.example.kaumedicare.Diary.dto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Converter(autoApply = true)
public class LocalTimeAttributeConverter implements AttributeConverter<LocalTime, String> {

    @Override
    public String convertToDatabaseColumn(LocalTime localTime) {
        return localTime != null ? localTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")) : null;
    }

    @Override
    public LocalTime convertToEntityAttribute(String dbData) {
        return dbData != null ? LocalTime.parse(dbData, DateTimeFormatter.ofPattern("HH:mm:ss")) : null;
    }
}
