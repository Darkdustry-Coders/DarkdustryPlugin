package rewrite.components;

import rewrite.DarkdustryPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Database {

    public static Connection connection;

    public static void connect(String url, String user, String password) {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException | SQLException e) {
            DarkdustryPlugin.err("Не удалось подключиться к базе данных: @", e.getMessage());
        }
    }

    public static PlayerData getPlayerData(String uuid) {
        return null;
    }

    public static void setPlayerData(String uuid, PlayerData data) {

    }

    public static class PlayerData {
        public String uuid;
        public String translatorLanguage = "off";

        public boolean welcomeMessage = true;
        public boolean alertsEnabled = true;

        public int playTime = 0;
        public int buildingsBuilt = 0;
        public int gamesPlayed = 0;

        public int rank = 0;

        public PlayerData(String uuid) {
            this.uuid = uuid;
        }

        public PlayerData(ResultSet set) {

        }
    }
}
