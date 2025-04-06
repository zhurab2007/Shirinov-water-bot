package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

public class BotMain extends TelegramLongPollingBot {
    private final Map<Long, Map<String, Integer>> cart = new HashMap<>();
    private final Map<Long, Contact> userContacts = new HashMap<>();
    private final Map<Long, Location> userLocations = new HashMap<>();

    private static final long ADMIN_GROUP_CHAT_ID = -4758170620L;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            handleMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
        }
    }

    private void handleMessage(Message message) {
        long chatId = message.getChatId();
        Chat chat = message.getChat();

        if (message.hasText()) {
            switch (message.getText()) {
                case "/start" -> {
                    sendMessage(chatId, "👋 Assalomu alaykum! Botga xush kelibsiz.");
                    if (chat.isUserChat()) {
                        askForPhoneNumber(chatId);
                    } else {
                        sendMessage(chatId, "📱 Iltimos, telefon raqamingizni yuborish uchun botga shaxsiy chatda yozing: @Shirinov_waters_bot");
                    }
                }
                case "📁 Katalog" -> CatalogHandler.sendCatalog(chatId, this);
                case "🛒 Savatcha" -> sendCart(chatId);
                case "📦 Buyurtmalar" -> sendMessage(chatId, "📝 Buyurtmalaringiz ro‘yxati bo‘sh.");
                case "❓ Yordam" -> sendMessage(chatId, "☎️ Aloqa: +998 94 128 10 14 yoki @Shirinov_M");
                default -> sendMessage(chatId, "❌ Noto‘g‘ri buyruq. Iltimos menyudan foydalaning.");
            }
        } else if (message.hasContact()) {
            userContacts.put(chatId, message.getContact());
            sendMessage(chatId, "📲 Telefon raqamingiz qabul qilindi.");
            notifyAdminNewUser(message.getContact(), message.getFrom());
            askForLocation(chatId);
        } else if (message.hasLocation()) {
            userLocations.put(chatId, message.getLocation());
            sendMessage(chatId, "📍 Manzilingiz saqlandi.");
            sendMainMenu(chatId);
        }
    }

    private void handleCallback(CallbackQuery callback) {
        String data = callback.getData();
        long chatId = callback.getMessage().getChatId();

        if (data.startsWith("add_")) {
            String[] parts = data.split("_");
            int qty = Integer.parseInt(parts[1]);
            String name = parts[2].replace("-", " ");

            cart.computeIfAbsent(chatId, k -> new HashMap<>())
                    .merge(name, qty, Integer::sum);

            sendMessage(chatId, "✅ " + name + " " + qty + " dona savatchaga qo‘shildi.");
        } else if (data.equals("order_cart")) {
            confirmOrder(chatId);
        }
    }

    private void askForPhoneNumber(long chatId) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText("📞 Telefon raqamingizni ulashing:");

        KeyboardButton btn = new KeyboardButton("📲 Raqamni yuborish");
        btn.setRequestContact(true);

        KeyboardRow row = new KeyboardRow();
        row.add(btn);

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setKeyboard(List.of(row));
        markup.setResizeKeyboard(true);
        msg.setReplyMarkup(markup);

        send(msg);
    }

    private void askForLocation(long chatId) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText("📍 Manzilingizni yuboring:");

        KeyboardButton locBtn = new KeyboardButton("📍 Manzilni yuborish");
        locBtn.setRequestLocation(true);

        KeyboardRow row = new KeyboardRow();
        row.add(locBtn);

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setKeyboard(List.of(row));
        markup.setResizeKeyboard(true);
        msg.setReplyMarkup(markup);

        send(msg);
    }

    private void sendMainMenu(long chatId) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText("🔘 Asosiy menyu:");
        msg.setReplyMarkup(KeyboardHelper.getMainMenuKeyboard());
        send(msg);
    }

    private void sendCart(long chatId) {
        Map<String, Integer> userCart = cart.get(chatId);
        if (userCart == null || userCart.isEmpty()) {
            sendMessage(chatId, "🛒 Savatchangiz bo‘sh.");
            return;
        }

        StringBuilder text = new StringBuilder("📦 Savatchangiz:\n");
        int total = 0;

        for (var entry : userCart.entrySet()) {
            int price = CatalogHandler.getPrice(entry.getKey());
            int qty = entry.getValue();
            total += price * qty;
            text.append("🔹 ").append(entry.getKey()).append(" - ")
                    .append(qty).append(" dona (").append(price * qty).append(" so‘m)\n");
        }

        text.append("\n💰 Umumiy: ").append(total).append(" so‘m");

        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(text.toString());
        msg.setReplyMarkup(KeyboardHelper.getCartButtons());
        send(msg);
    }

    private void confirmOrder(long chatId) {
        if (!cart.containsKey(chatId) || cart.get(chatId).isEmpty()) {
            sendMessage(chatId, "⚠️ Buyurtma berish uchun avval savatchani to‘ldiring.");
            return;
        }

        sendMessage(chatId, "✅ Buyurtmangiz qabul qilindi. Operator siz bilan tez orada bog‘lanadi.");

        sendOrderToGroup(chatId);
        cart.remove(chatId);
    }

    private void notifyAdminNewUser(Contact contact, User user) {
        String fullName = user.getFirstName() + (user.getLastName() != null ? " " + user.getLastName() : "");
        String username = user.getUserName() != null ? "@" + user.getUserName() : "—";
        String phone = contact.getPhoneNumber();

        String msg = "🆕 Yangi foydalanuvchi:\n" +
                "👤 Ism: " + fullName + "\n" +
                "🔗 Username: " + username + "\n" +
                "📞 Tel: " + phone;

        sendMessage(ADMIN_GROUP_CHAT_ID, msg);
    }

    private void sendOrderToGroup(long chatId) {
        Contact contact = userContacts.get(chatId);
        Location location = userLocations.get(chatId);
        Map<String, Integer> userCart = cart.get(chatId);

        if (contact == null || userCart == null) return;

        StringBuilder text = new StringBuilder("📥 Yangi buyurtma!\n");

        text.append("👤 Ism: ").append(contact.getFirstName()).append("\n");
        text.append("📞 Tel: ").append(contact.getPhoneNumber()).append("\n");

        User user = getUser(chatId);
        if (user != null && user.getUserName() != null) {
            text.append("🔗 Username: @").append(user.getUserName()).append("\n");
        }

        if (location != null) {
            text.append("📍 Manzil: https://maps.google.com/?q=")
                    .append(location.getLatitude()).append(",")
                    .append(location.getLongitude()).append("\n");
        }

        text.append("\n📦 Buyurtma:\n");
        int total = 0;
        for (Map.Entry<String, Integer> entry : userCart.entrySet()) {
            int price = CatalogHandler.getPrice(entry.getKey());
            int qty = entry.getValue();
            total += price * qty;
            text.append("• ").append(entry.getKey())
                    .append(" - ").append(qty)
                    .append(" dona (").append(price * qty).append(" so‘m)\n");
        }

        text.append("\n💰 Umumiy: ").append(total).append(" so‘m");

        sendMessage(ADMIN_GROUP_CHAT_ID, text.toString());
    }

    private User getUser(long chatId) {
        // TODO: Optionally cache User info if available from Update
        return null; // You may extend logic to keep user info from Message.getFrom()
    }

    private void sendMessage(long chatId, String text) {
        SendMessage msg = new SendMessage(String.valueOf(chatId), text);
        send(msg);
    }

    private void send(SendMessage msg) {
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "Shirinov_waters_bot";
    }

    @Override
    public String getBotToken() {
        return "7889953547:AAE7XvHPAy03f-wL9YItZFlSDZuUM3tsy-s"; // 🛡 Tokenni real kodda yashiring
    }
}
