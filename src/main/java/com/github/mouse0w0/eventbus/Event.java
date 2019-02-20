package com.github.mouse0w0.eventbus;

/**
 * Every event class should implement this interface.
 */
public interface Event {

	default boolean isCancellable() {
		return false;
	}

	default boolean isCancelled() { return false; }

	default void setCancelled(boolean cancelled) {
		throw new UnsupportedOperationException("Cannot cancel this event.");
	}
}
