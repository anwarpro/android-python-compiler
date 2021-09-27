package com.helloanwar.androidprocessbuilder.app.models;

public enum UserAction {

    ABOUT("about"),
    CRASH_REPORT("crash report"),
    PLUGIN_EXECUTION_COMMAND("plugin execution command"),
    REPORT_ISSUE_FROM_TRANSCRIPT("report issue from transcript");

    private final String name;

    UserAction(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
