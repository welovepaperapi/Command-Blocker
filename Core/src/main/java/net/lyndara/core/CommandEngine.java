package net.lyndara.core;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CommandEngine {

    private final List<Pattern> filterPatterns = new ArrayList<>();
    private boolean whitelistMode = false;

    // aktualisiert filter
    public void loadSettings(List<String> rawPatterns, boolean whitelist) {
        this.whitelistMode = whitelist;
        this.filterPatterns.clear();
        for (String regex : rawPatterns) {
            this.filterPatterns.add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
        }
    }

    public boolean isAllowed(String commandLine, UserContext user) {
        if (commandLine == null || commandLine.isEmpty()) return false;

        // / entfernen
        String label = commandLine.startsWith("/") ? commandLine.substring(1) : commandLine;
        label = label.split(" ")[0].toLowerCase();

        // bypass
        if (user.hasPermission("commandfilter.bypass." + label) ||
                user.hasPermission("commandfilter.bypass.*")) {
            return false;
        }

        // regex matcher prüfen
        String finalLabel = label;
        boolean matches = filterPatterns.stream()
                .anyMatch(p -> p.matcher(finalLabel).matches());

        //  Whitelist uberprüfenn
        return whitelistMode != matches;
    }

}