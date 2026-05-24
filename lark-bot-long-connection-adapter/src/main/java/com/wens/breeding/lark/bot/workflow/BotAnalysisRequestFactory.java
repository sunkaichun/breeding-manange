package com.wens.breeding.lark.bot.workflow;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

import com.wens.breeding.analysis.model.AnalysisRequest;
import com.wens.breeding.analysis.model.RequestSource;
import com.wens.breeding.lark.bot.command.BotCommand;
import com.wens.breeding.lark.bot.event.BotMessageEvent;

public final class BotAnalysisRequestFactory {
    private static final int MIN_RECENT_DAYS = 1;

    private final Clock clock;

    public BotAnalysisRequestFactory() {
        this(Clock.systemDefaultZone());
    }

    public BotAnalysisRequestFactory(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public AnalysisRequest create(BotMessageEvent event, BotCommand command) {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(command, "command");
        if (!command.hasBatchId()) {
            throw new IllegalArgumentException("batchId is required for bot analysis requests");
        }

        LocalDate endDate = LocalDate.now(clock);
        int recentDays = Math.max(MIN_RECENT_DAYS, command.getRecentDays());
        LocalDate startDate = endDate.minusDays(recentDays - 1L);
        return new AnalysisRequest(
                requestId(event),
                RequestSource.LARK_BOT,
                event.getSenderOpenId(),
                command.getBatchId(),
                command.getAnalysisType(),
                startDate,
                endDate,
                command.getRawText());
    }

    private static String requestId(BotMessageEvent event) {
        if (event.getEventId() != null && !event.getEventId().trim().isEmpty()) {
            return "BOT-" + event.getEventId();
        }
        return "BOT-" + event.getMessageId();
    }
}
