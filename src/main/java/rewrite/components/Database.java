package rewrite.components;

import arc.util.Log;
import rewrite.DarkdustryPlugin;

import java.lang.reflect.Field;
import java.sql.*;

public class Database {

    public static final String table = "players";

    public static Connection connection;

    public static void connect(String url, String user, String password) {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException | SQLException e) {
            DarkdustryPlugin.err("Не удалось подключиться к базе данных: @", e.getMessage());
        }
    }

    // TODO упростить, переделать
    public static PlayerData getPlayerData(String uuid) {

        // это просто тест
        getFields: {
            Field[] fields = PlayerData.class.getDeclaredFields();
            if (fields.length == 0) {
                break getFields;
            }

            StringBuilder request = new StringBuilder();

            for (Field field : fields) {
                request.append(field.getName()).append(" ");
            }

            Log.info(request.toString());
        }

        String sql = "SELECT uuid, translatorLanguage, welcomeMessage, alertsEnabled, playTime, buildingsBuilt, gamesPlayed, rank FROM players WHERE uuid = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, uuid);

            ResultSet set = statement.executeQuery();
            if (set.next()) {
                PlayerData data = new PlayerData(set);
                set.close();
                return data;
            }
            set.close();
        } catch (SQLException e) {
            Log.err(e);
        }

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
