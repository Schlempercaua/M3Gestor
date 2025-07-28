package com.caua.madeira.util;

import javafx.util.StringConverter;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class NumberUtils {
    
    // Formata um double para string com 2 casas decimais
    public static String formatDouble(double value) {
        return String.format(Locale.US, "%.2f", value);
    }
    
    // Converte uma string para double, aceitando vírgula ou ponto como separador decimal
    public static double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0.0;
        }
        // Substitui vírgula por ponto para garantir o parse correto
        String normalizedValue = value.replace(',', '.');
        try {
            return Double.parseDouble(normalizedValue);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    // Cria um StringConverter para campos numéricos que aceita vírgula
    public static StringConverter<Double> createDoubleStringConverter() {
        return new StringConverter<Double>() {
            private final NumberFormat format = NumberFormat.getNumberInstance(new Locale("pt", "BR"));
            
            @Override
            public String toString(Double value) {
                if (value == null) {
                    return "";
                }
                return format.format(value);
            }
            
            @Override
            public Double fromString(String text) {
                try {
                    if (text == null || text.trim().isEmpty()) {
                        return 0.0;
                    }
                    return format.parse(text).doubleValue();
                } catch (ParseException e) {
                    return 0.0;
                }
            }
        };
    }
    
    // Cria um StringConverter para campos monetários (2 casas decimais)
    public static StringConverter<Double> createCurrencyStringConverter() {
        return new StringConverter<Double>() {
            private final NumberFormat format = NumberFormat.getNumberInstance(new Locale("pt", "BR"));
            
            {
                format.setMinimumFractionDigits(2);
                format.setMaximumFractionDigits(2);
            }
            
            @Override
            public String toString(Double value) {
                if (value == null) {
                    return "0,00";
                }
                return format.format(value);
            }
            
            @Override
            public Double fromString(String text) {
                try {
                    if (text == null || text.trim().isEmpty()) {
                        return 0.0;
                    }
                    // Garante que números inteiros tenham 2 casas decimais
                    String normalized = text.trim().replace(".", "").replace(',', '.');
                    if (!normalized.contains(".")) {
                        normalized += ".00";
                    }
                    return Double.parseDouble(normalized);
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            }
        };
    }
}
