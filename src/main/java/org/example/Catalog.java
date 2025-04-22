package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.File;
import java.util.*;

public class Catalog {
    private static final String IMAGE_PATH = "/Users/kuaijike/Desktop/A/001_compressed.jpg";

    private static final Map<String, Integer> PRODUCTS = new LinkedHashMap<>();

    static {
        PRODUCTS.put("Shirinov water 0.33L", 3000);
        PRODUCTS.put("Shirinov water 0.5L", 4500);
        PRODUCTS.put("Shirinov water 1L", 6000);
        PRODUCTS.put("Shirinov water 1.5L", 7500);
        PRODUCTS.put("Shirinov water 5L", 10000);
        PRODUCTS.put("Shirinov water 10L", 12000);
        PRODUCTS.put("Shirinov water 19.5L", 20000);
    }


    public static void sendCatalog(long chatId, TelegramLongPollingBot bot) {
        File img = new File(IMAGE_PATH);
        if (!img.exists()) {
            System.err.println("❌ Rasm topilmadi: " + IMAGE_PATH);
            return;
        }

        PRODUCTS.forEach((name, price) -> {
            SendPhoto photo = new SendPhoto();
            photo.setChatId(chatId);
            photo.setPhoto(new InputFile(img));
            photo.setCaption("📦 " + name + "\n💰 Narx: " + price + " so‘m");
            photo.setReplyMarkup(getButtons(name));

            try {
                bot.execute(photo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static InlineKeyboardMarkup getButtons(String name) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        int[] options = {1, 5, 10};

        for (int qty : options) {
            InlineKeyboardButton btn = new InlineKeyboardButton();
            btn.setText("+" + qty);
            btn.setCallbackData("add_" + qty + "_" + name.replace(" ", "-"));
            row.add(btn);
        }

        rows.add(row);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    public static int getPrice(String name) {
        return PRODUCTS.getOrDefault(name, 0);
    }
}
