package pandorum.comp;

import java.awt.*;
import java.io.*;

import pandorum.PandorumPlugin;

public class DiscordSender {

    //Эмбед без полей. Только название.
    public static void send(String name, String title, Color color) {
        if (PandorumPlugin.config.hasWebhookLink()) {
            new Thread(() -> {
                Webhook wh = new Webhook(PandorumPlugin.config.DiscordWebhookLink);
                wh.setUsername(name);
                wh.addEmbed(new Webhook.EmbedObject()
                         .setTitle(title)
                         .setColor(color));
                try {
                    wh.execute();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } finally {
                    Thread.currentThread().interrupt();
                }
                return;
            }).start();
        }
    }

    //Эмбед с одним полем.
    public static void send(String name, String title, String fieldname, String fieldcontent, Color color) {
        if (PandorumPlugin.config.hasWebhookLink()) {
            new Thread(() -> {
                Webhook wh = new Webhook(PandorumPlugin.config.DiscordWebhookLink);
                wh.setUsername(name);
                wh.addEmbed(new Webhook.EmbedObject()
                         .setTitle(title)
                         .addField(fieldname, fieldcontent, false)
                         .setColor(color));
                try {
                    wh.execute();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } finally {
                    Thread.currentThread().interrupt();
                }
                return;
            }).start();
        }
    }

    //Эмбед с двумя полями. 
    public static void send(String name, String title, String fieldname1, String fieldcontent1, String fieldname2, String fieldcontent2, Color color) {
        if (PandorumPlugin.config.hasWebhookLink()) {
            new Thread(() -> {
                Webhook wh = new Webhook(PandorumPlugin.config.DiscordWebhookLink);
                wh.setUsername(name);
                wh.addEmbed(new Webhook.EmbedObject()
                         .setTitle(title)
                         .addField(fieldname1, fieldcontent1, false)
                         .addField(fieldname2, fieldcontent2, false)
                         .setColor(color));
                try {
                    wh.execute();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } finally {
                    Thread.currentThread().interrupt();
                }
                return;
            }).start();
        }
    }

    public static void send(String name, String text, Color color) {
        if (PandorumPlugin.config.hasWebhookLink()) {
            new Thread(() -> {
                Webhook wh = new Webhook(PandorumPlugin.config.DiscordWebhookLink);
                wh.setUsername(name);
                wh.setContent(text);
                try {
                    wh.execute();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } finally {
                    Thread.currentThread().interrupt();
                }
                return;
            }).start();
        }
    }
}
