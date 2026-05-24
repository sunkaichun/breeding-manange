package com.wens.breeding.lark.im;

public interface LarkImClient {
    MessageDeliveryResult sendText(String chatId, String text);

    MessageDeliveryResult sendCard(String chatId, LarkCardMessage cardMessage);

    MessageDeliveryResult sendError(String chatId, String title, String detail);
}
