package rewrite.components;

import arc.util.Log;
import rewrite.DarkdustryPlugin;

import java.lang.reflect.Field;
import java.sql.*;

public class Database {

    public static Connection connection;

    public static void connect(String url, String user, String password) {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(url, user, password);

            createTables();
        } catch (ClassNotFoundException | SQLException e) {
            DarkdustryPlugin.error("Не удалось подключиться к базе данных: @", e.getMessage());
        }
    }

    private static void createTables() {
        String sql = """
        CREATE TABLE IF NOT EXISTS players (
          uuid varchar(32) primary key,
          translatorLanguage varchar(32) not null,
          welcomeMessage bool not null,
          alertsEnabled bool not null,
          playTime integer not null,
          buildingsBuilt integer not null,
          gamesPlayed integer not null,
          rank integer not null
        )
        """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            Log.err(e);
        }
    }

    private static PreparedStatement createPreparedStatement(String sql, Object... params) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
        return statement;
    }

    private static boolean existsPlayerData(String uuid) {
        String sql = "SELECT uuid FROM players WHERE uuid = ?";
        try (PreparedStatement statement = createPreparedStatement(sql, uuid); ResultSet rs = statement.executeQuery()) {
            return rs.next();
        } catch (SQLException e) {
            Log.err(e);
        }
        return false;
    }

    public static PlayerData getPlayerData(String uuid) {
        String sql = "SELECT * FROM players WHERE uuid = ?";
        try (PreparedStatement statement = createPreparedStatement(sql, uuid); ResultSet set = statement.executeQuery()) {
            if (set.next()) return new PlayerData(set);
        } catch (IllegalAccessException | SQLException e) {
            Log.err(e);
        }

        return null;
    }

    public static void setPlayerData(PlayerData data) {
        // есть конечно on conflict (uuid) do update, но jdbc не даёт использовать параметры с определенным индексом,
        // т.е. придётся продублировать колбасу из 7 параметров (как ниже) чтобы описать логику обновления
        if (existsPlayerData(data.uuid)) {
            String sql = """
            UPDATE players set translatorLanguage = ?, welcomeMessage = ?, alertsEnabled = ?,
              playTime = ?, buildingsBuilt = ?, gamesPlayed = ?, rank = ? WHERE uuid = ?
            """;
            try (PreparedStatement statement = createPreparedStatement(sql, data.language,
                    data.welcomeMessage, data.alertsEnabled, data.playTime,
                    data.buildingsBuilt, data.gamesPlayed, data.rank, data.uuid)) {

                statement.executeUpdate();
            } catch (SQLException e) {
                Log.err(e);
            }
        } else {
            String sql = """
            INSERT INTO players (uuid, translatorLanguage, welcomeMessage, alertsEnabled,
              playTime, buildingsBuilt, gamesPlayed, rank) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
            try (PreparedStatement statement = createPreparedStatement(sql,
                    data.uuid, data.language, data.welcomeMessage, data.alertsEnabled, data.playTime,
                    data.buildingsBuilt, data.gamesPlayed, data.rank)) {

                statement.executeUpdate();
            } catch (SQLException e) {
                Log.err(e);
            }
        }
    }

    public static class PlayerData {
        public String uuid;
        public String language = "off";

        public boolean welcomeMessage = true;
        public boolean alertsEnabled = true;

        public int playTime = 0;
        public int buildingsBuilt = 0;
        public int gamesPlayed = 0;

        public int rank = 0;

        public PlayerData(ResultSet set) throws IllegalAccessException, SQLException {
            for (Field field : PlayerData.class.getDeclaredFields())
                field.set(this, set.getObject(field.getName()));
        }
    }
}
