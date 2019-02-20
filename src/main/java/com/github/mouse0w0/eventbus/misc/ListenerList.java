package com.github.mouse0w0.eventbus.misc;

import com.github.mouse0w0.eventbus.Order;

import java.util.*;

public class ListenerList {

    private final Class<?> eventType;
    private final List<ListenerList> parents = new ArrayList<>();

    private final EnumMap<Order, List<RegisteredListener>> listeners = new EnumMap<>(Order.class);

    public ListenerList(Class<?> eventType) {
        this.eventType = eventType;
    }

    public Class<?> getEventType() {
        return eventType;
    }

    public void register(RegisteredListener listener) {
        listeners.computeIfAbsent(listener.getOrder(), order -> new ArrayList<>()).add(listener);
    }

    public void unregister(RegisteredListener listener) {
        listeners.computeIfAbsent(listener.getOrder(), order -> new ArrayList<>()).remove(listener);
    }

    public void addParent(ListenerList list) {
        parents.add(list);
    }

    public List<ListenerList> getParents() {
        return parents;
    }

    public EnumMap<Order, List<RegisteredListener>> getListeners() {
        return listeners;
    }
}
