package darkdustry;

import arc.struct.Seq;
import arc.util.CommandHandler;
import darkdustry.features.votes.VoteKick;
import darkdustry.features.votes.VoteSession;
import mindustry.core.Version;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.temporal.ChronoUnit;
import java.util.regex.Pattern;

import static java.time.temporal.ChronoUnit.*;
import static mindustry.Vars.tilesize;

@SuppressWarnings("unchecked")
public class PluginVars {

    /** Необходимое количество игроков для успешного завершения голосования. */
    public static final float voteRatio = 0.55f;

    /** Время, после которого голосование автоматически завершится. В секундах. */
    public static final int voteDuration = 50;

    /** Стандартная длительность кика игрока. В миллисекундах. */
    public static final int kickDuration = 30 * 60 * 1000;

    /** Расстояние до ядра, в котором отслеживаются опасные блоки. Интервал оповещений об опасных блоках. */
    public static final int alertsDistance = 16 * tilesize, alertsTimer = 3;

    /** Максимальное количество пропущенных волн. */
    public static final int maxVnwAmount = 5;

    /** Максимальное количество выдаваемых ресурсов. */
    public static final int maxGiveAmount = 100000;

    /** Максимальное количество создаваемых юнитов. */
    public static final int maxSpawnAmount = 25;

    /** Максимальная длительность применяемого эффекта статуса. В секундах. */
    public static final int maxEffectDuration = 60 * 60;

    /** Максимальная площадь для заливки. */
    public static final int maxFillArea = 512;

    /** Количество элементов на одной странице списка. */
    public static final int maxPerPage = 6;

    /** Максимальное количество записей истории на один тайл. */
    public static final int maxHistorySize = 8;

    /** Максимально допустимое количество игроков с одинаковыми IP адресами. */
    public static final int maxIdenticalIPs = 3;

    /** Версия Mindustry, запущенная на сервере. */
    public static final int mindustryVersion = Version.build;

    /** Путь к файлу, в котором хранится конфигурация сервера. */
    public static final String configFile = "config.json";

    /** Путь к файлу, в котором хранится конфигурация discord-бота. */
    public static final String discordConfigFile = "discord-config.json";

    /** Ссылка на наш Discord сервер. */
    public static final String discordServerUrl = "https://discord.gg/uPUZHp7xQn";

    /** Ссылка на API для перевода текста. */
    public static final String translationApiUrl = "https://clients5.google.com/translate_a/t?client=dict-chrome-ex&dt=t";

    /** Используется для перевода строки в длительность. */
    public static final Pattern durationPattern = Pattern.compile("(\\d+)\\s*?([a-zA-Zа-яА-Я]+)");

    /** Используются для перевода строки в длительность. */
    public static final Seq<Tuple2<Pattern, ChronoUnit>> durationPatterns = Seq.with(
            Tuples.of(Pattern.compile("(mon|month|months|мес|месяц|месяца|месяцев)"), MONTHS),
            Tuples.of(Pattern.compile("(w|week|weeks|н|нед|неделя|недели|недель)"), WEEKS),
            Tuples.of(Pattern.compile("(d|day|days|д|день|дня|дней)"), DAYS),
            Tuples.of(Pattern.compile("(h|hour|hours|ч|час|часа|часов)"), HOURS),
            Tuples.of(Pattern.compile("(m|min|mins|minute|minutes|м|мин|минута|минуты|минут)"), MINUTES),
            Tuples.of(Pattern.compile("(s|sec|secs|second|seconds|с|сек|секунда|секунды|секунд)"), SECONDS)
    );

    /** Текущее голосование. */
    public static VoteSession vote;

    /** Текущее голосование за кик игрока. */
    public static VoteKick voteKick;

    /** Обработчики команд, которые используются для их регистрации. */
    public static CommandHandler serverHandler, discordHandler;
}