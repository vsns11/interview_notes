package com.example.hello;

/** The JSON body we return. Jackson turns record components into JSON fields. */
public record Greeting(String message, int length) {
}
