package org.example;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.*;

import java.util.*;

public class KeyboardHelper {
    public static ReplyKeyboardMarkup getMainMenuKeyboard() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();

        KeyboardRow row1 = new KeyboardRow(List.of(
                new KeyboardButton("📁 Katalog"),
                new KeyboardButton("🛒 Savatcha")
        ));
        KeyboardRow row2 = new KeyboardRow(List.of(
                new KeyboardButton("📦 Buyurtmalar"),
                new KeyboardButton("❓ Yordam")
        ));

        markup.setKeyboard(List.of(row1, row2));
        markup.setResizeKeyboard(true);
        return markup;
    }

    public static InlineKeyboardMarkup getCartButtons() {
        InlineKeyboardButton btn = new InlineKeyboardButton("🛍 Buyurtma berish");
        btn.setCallbackData("order_cart");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(List.of(List.of(btn)));
        return markup;
    }
}
