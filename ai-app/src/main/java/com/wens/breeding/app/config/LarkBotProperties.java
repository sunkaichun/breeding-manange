package com.wens.breeding.app.config;

import java.time.Duration;

import com.wens.breeding.lark.bot.event.ImMessageReceiveV1Schema;
import com.wens.breeding.lark.bot.runner.LarkEventConsumerIdentity;

public final class LarkBotProperties {
    private final App app = new App();
    private final Consumer consumer = new Consumer();
    private Duration queueDelay = Duration.ofMillis(500);
    private int queueThreads = 2;

    public App getApp() {
        return app;
    }

    public Consumer getConsumer() {
        return consumer;
    }

    public Duration getQueueDelay() {
        return queueDelay;
    }

    public void setQueueDelay(Duration queueDelay) {
        this.queueDelay = queueDelay == null ? Duration.ZERO : queueDelay;
    }

    public int getQueueThreads() {
        return queueThreads;
    }

    public void setQueueThreads(int queueThreads) {
        this.queueThreads = queueThreads;
    }

    public int resolvedQueueThreads() {
        return Math.max(1, queueThreads);
    }

    public static final class App {
        private String appId = "";
        private String appSecret = "";
        private String verificationToken = "";
        private String encryptKey = "";
        private String botOpenId = "";

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = normalize(appId);
        }

        public String getAppSecret() {
            return appSecret;
        }

        public void setAppSecret(String appSecret) {
            this.appSecret = normalize(appSecret);
        }

        public String getVerificationToken() {
            return verificationToken;
        }

        public void setVerificationToken(String verificationToken) {
            this.verificationToken = normalize(verificationToken);
        }

        public String getEncryptKey() {
            return encryptKey;
        }

        public void setEncryptKey(String encryptKey) {
            this.encryptKey = normalize(encryptKey);
        }

        public String getBotOpenId() {
            return botOpenId;
        }

        public void setBotOpenId(String botOpenId) {
            this.botOpenId = normalize(botOpenId);
        }

        public boolean hasAppCredentials() {
            return !appId.isEmpty() && !appSecret.isEmpty();
        }
    }

    public static final class Consumer {
        private boolean enabled;
        private String cliPath = "lark-cli";
        private String eventKey = ImMessageReceiveV1Schema.EVENT_KEY;
        private LarkEventConsumerIdentity identity = LarkEventConsumerIdentity.BOT;
        private int maxEvents;
        private String timeout = "";
        private String jqExpression = "";
        private Duration readyTimeout = Duration.ofSeconds(10);

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getCliPath() {
            return cliPath;
        }

        public void setCliPath(String cliPath) {
            this.cliPath = defaultIfBlank(cliPath, "lark-cli");
        }

        public String getEventKey() {
            return eventKey;
        }

        public void setEventKey(String eventKey) {
            this.eventKey = defaultIfBlank(eventKey, ImMessageReceiveV1Schema.EVENT_KEY);
        }

        public LarkEventConsumerIdentity getIdentity() {
            return identity;
        }

        public void setIdentity(LarkEventConsumerIdentity identity) {
            this.identity = identity == null ? LarkEventConsumerIdentity.BOT : identity;
        }

        public int getMaxEvents() {
            return maxEvents;
        }

        public void setMaxEvents(int maxEvents) {
            this.maxEvents = Math.max(0, maxEvents);
        }

        public String getTimeout() {
            return timeout;
        }

        public void setTimeout(String timeout) {
            this.timeout = normalize(timeout);
        }

        public String getJqExpression() {
            return jqExpression;
        }

        public void setJqExpression(String jqExpression) {
            this.jqExpression = normalize(jqExpression);
        }

        public Duration getReadyTimeout() {
            return readyTimeout;
        }

        public void setReadyTimeout(Duration readyTimeout) {
            if (readyTimeout == null || readyTimeout.isNegative() || readyTimeout.isZero()) {
                this.readyTimeout = Duration.ofSeconds(10);
                return;
            }
            this.readyTimeout = readyTimeout;
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private static String defaultIfBlank(String value, String fallback) {
        String normalized = normalize(value);
        return normalized.isEmpty() ? fallback : normalized;
    }
}
