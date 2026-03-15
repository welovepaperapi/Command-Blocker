package net.lyndara.core;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

public class CommandEngine {
    private final List<Pattern> filterPatterns = new CopyOnWriteArrayList<>();
    private volatile boolean whitelistMode = false;

    public void loadSettings(List<String> rawPatterns, boolean whitelist) {
        this.whitelistMode = whitelist;
        this.filterPatterns.clear();
        if (rawPatterns == null) return;

        for (String regex : rawPatterns) {
            try {
                this.filterPatterns.add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
            } catch (Exception e) {
                System.err.println("[CommandBlocker] Ungültiges Regex-Pattern: " + regex);
            }
        }
    }

    public boolean isAllowed(String commandLine, UserContext user) {
        if (commandLine == null || commandLine.isBlank()) return false;

        String cleanLine = commandLine.trim();
        if (cleanLine.startsWith("/")) cleanLine = cleanLine.substring(1);
        String label = cleanLine.split(" ")[0].toLowerCase();

        if (user.hasPermission("commandfilter.bypass." + label) ||
                user.hasPermission("commandfilter.bypass.*")) {
            return false;
        }

        boolean matches = filterPatterns.stream().anyMatch(p -> p.matcher(label).matches());
        return whitelistMode != matches;
    }
}