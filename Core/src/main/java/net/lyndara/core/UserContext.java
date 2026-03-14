package net.lyndara.core;

public interface UserContext {
    boolean hasPermission(String permission);
    void sendMessage(String message);
}