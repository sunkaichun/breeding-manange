package com.wens.breeding.lark.bot.command;

import com.wens.breeding.lark.bot.event.BotMessageEvent;

public interface BotCommandRouter {
    BotCommand route(BotMessageEvent event);
}
