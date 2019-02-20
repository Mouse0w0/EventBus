package com.github.mouse0w0.eventbus.misc;

import com.github.mouse0w0.eventbus.Event;

@FunctionalInterface
public interface EventExceptionHandler {

    void handle(ListenerList list, Event event, Exception e);
}
