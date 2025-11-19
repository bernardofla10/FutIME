package com.futime.labprog.futimeapi.exception;

import java.io.Serializable;

public record FieldMessage(String fieldName, String message) implements Serializable {
}
