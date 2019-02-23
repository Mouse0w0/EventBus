package com.github.mouse0w0.eventbus.misc;

import com.github.mouse0w0.eventbus.Cancellable;
import com.github.mouse0w0.eventbus.Event;
import com.github.mouse0w0.eventbus.GenericEvent;
import com.github.mouse0w0.eventbus.Order;

import java.lang.reflect.Type;
import java.util.function.Predicate;

public class RegisteredListener {

    private final Class<?> eventType;
    private final Object owner;
    private final Order order;
    private final Type genericType;
    private final EventListener eventListener;
    private final Predicate<Event> filter;

    public RegisteredListener(Class<?> eventType, Object owner, Order order, boolean receiveCancelled, Type genericType, EventListener eventListener) {
        this.eventType = eventType;
        this.owner = owner;
        this.order = order;
        this.genericType = genericType;
        this.eventListener = eventListener;
        this.filter = createFilter(receiveCancelled, genericType != null);
    }

    private Predicate<Event> createFilter(boolean receiveCancelled, boolean isGeneric) {
        if (receiveCancelled) {
            if (isGeneric) {
                return this::checkGeneric;
            } else {
                return event -> true;
            }
        } else {
            if (isGeneric) {
                return event -> checkCancel(event) && checkGeneric(event);
            } else {
                return this::checkCancel;
            }
        }
    }

    public Class<?> getEventType() {
        return eventType;
    }

    public Object getOwner() {
        return owner;
    }

    public Order getOrder() {
        return order;
    }

    public EventListener getEventListener() {
        return eventListener;
    }

    public void post(Event event) throws Exception {
        if (filter.test(event)) {
            eventListener.post(event);
        }
    }

    private boolean checkCancel(Event event) {
        return !event.isCancellable() || !((Cancellable) event).isCancelled();
    }

    private boolean checkGeneric(Event event) {
        return ((GenericEvent) event).getGenericType() == genericType;
    }
}
