package com.wens.breeding.lark.bot.event;

public final class EventFieldDescriptor {
    private final String name;
    private final String format;
    private final String description;

    public EventFieldDescriptor(String name, String format, String description) {
        this.name = Texts.requireText(name, "name");
        this.format = format == null ? "" : format;
        this.description = Texts.requireText(description, "description");
    }

    public String getName() {
        return name;
    }

    public String getFormat() {
        return format;
    }

    public String getDescription() {
        return description;
    }
}
