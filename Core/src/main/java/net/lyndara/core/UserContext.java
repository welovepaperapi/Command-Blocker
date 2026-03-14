package net.lyndara.core;

import java.util.UUID;

public interface UserContext {
    boolean hasPermission(String permission);
    void sendMessage(String message);
    UUID getUniqueId();
}