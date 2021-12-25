package pandorum;

import arc.struct.Seq;
import arc.struct.StringMap;
import arc.util.Log;
import mindustry.gen.Iconc;
import mindustry.gen.Player;
import mindustry.net.Administration;
import mindustry.net.NetConnection;
import pandorum.annotations.commands.ClientCommand;
import pandorum.annotations.commands.ServerCommand;
import pandorum.annotations.commands.gamemodes.*;
import pandorum.annotations.commands.gamemodes.containers.DisabledGamemodes;
import pandorum.annotations.commands.gamemodes.containers.RequiredGamemodes;
import pandorum.annotations.events.EventListener;
import pandorum.annotations.filters.ActionFilter;
import pandorum.annotations.filters.ChatFilter;
import pandorum.annotations.handlers.PacketHandler;
import pandorum.annotations.events.TriggerListener;
import pandorum.comp.Config;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

public class Reflection {
    @FunctionalInterface
    private interface ReceiverParams{
        Class<?>[] Receive(Method method);
    }

    private static Seq<Class<?>> getClasses(String basePackage) {
        String packageName = basePackage.replace('.', '/');
        URL pckg = PandorumPlugin.class.getClassLoader().getResource(packageName);

        if (pckg == null)
            throw new IllegalArgumentException("Cannot get resources to package " + basePackage);

        String pckgStr = pckg.toString();

        try {
            ZipFile zipFile = new ZipFile(pckgStr.substring("jar:file:".length(), pckgStr.lastIndexOf("!/")));
            return Seq.with(
                    zipFile.stream()
                    .filter(e -> e.getName().startsWith(packageName)
                            && e.getName().endsWith(".class"))
                    .<Class<?>>map(e -> {
                        try{
                            String name = e.getName().replace('/', '.');
                            name = name.substring(0, name.lastIndexOf('.'));
                            return Class.forName(name);
                        }catch(ClassNotFoundException ex){
                            Log.err(ex.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toArray(Class<?>[]::new)
            );
        } catch (IOException e) {
            Log.err(e.getMessage());
            return new Seq<Class<?>>();
        }
    }

    private static Seq<Method> getAnnotatedMethods(String basePackage, Class<? extends Annotation> annotationClass) {
        return getClasses(basePackage).<Stream<Method>>map(classObject ->
                Arrays.stream(classObject.getMethods()).filter(method -> method.isAnnotationPresent(annotationClass))
        ).reduce(
                new Seq<Method>(),
                (Stream<Method> initial, Seq<Method> elem) -> {
                    initial.forEach(elem::add);
                    return elem;
                }
        );
    }

    private static Seq<Method> getStaticAnnotatedMethods(String basePackage, Class<? extends Annotation> annotationClass) {
        return getAnnotatedMethods(basePackage, annotationClass).filter(
                method -> Modifier.isStatic(method.getModifiers())
        );
    }

    private static Seq<Method> getStaticAnnotatedMethods(String basePackage, Class<? extends Annotation> annotationClass, Class<?>[] params) {
        return getStaticAnnotatedMethods(basePackage, annotationClass).filter(
                method -> Arrays.equals(method.getParameterTypes(), params)
        );
    }

    private static Seq<Method> getStaticAnnotatedMethods(String basePackage, Class<? extends Annotation> annotationClass, Class<?>[] params, Class<?> returnType) {
        return getStaticAnnotatedMethods(basePackage, annotationClass, params).filter(
                method -> method.getReturnType().isAssignableFrom(returnType) //TODO: fixit
        );
    }

    private static Seq<Method> getStaticAnnotatedVoids(String basePackage, Class<? extends Annotation> annotationClass) {
        return getStaticAnnotatedMethods(basePackage, annotationClass).filter(
                method -> method.getReturnType().equals(Void.TYPE)
        );
    }

    private static Seq<Method> getStaticAnnotatedVoids(String basePackage, Class<? extends Annotation> annotationClass, Class<?>[] params) {
        return getStaticAnnotatedVoids(basePackage, annotationClass).filter(
                method -> Arrays.equals(method.getParameterTypes(), params)
        );
    }

    private static Seq<Method> getStaticAnnotatedVoidsWithoutParams(String basePackage, Class<? extends Annotation> annotationClass) {
        return getStaticAnnotatedVoids(basePackage, annotationClass).filter(
                method -> method.getParameterCount() == 0
        );
    }

    /** Вспомогательный метод. Получает аннотированные классы с нужными пармеметами */
    private static Seq<Method> getSuitableMethods(String basePackage, Class<? extends Annotation> annotation, ReceiverParams consumer) {
        return getStaticAnnotatedVoids(basePackage, annotation).filter(
                method -> Arrays.equals(method.getParameterTypes(), consumer.Receive(method))
        );
    }

    public static Seq<Method> getClientCommands(Config.Gamemode gamemode) {
        return getStaticAnnotatedVoids(PandorumPlugin.ClientCommandsBasePackage, ClientCommand.class, new Class<?>[] { String[].class, Player.class })
                .map(method -> {
                    Seq<Config.Gamemode> disabledGamemodes = new Seq<>();
                    Seq<Config.Gamemode> reqiuredGamemodes = new Seq<>();

                    boolean requireSimpleGamemode = method.isAnnotationPresent(RequireSimpleGamemode.class);
                    boolean hasRequiredGamemodes = method.isAnnotationPresent(DisabledGamemodes.class);
                    boolean hasDisabledGamemodes = method.isAnnotationPresent(RequiredGamemodes.class);
                    boolean requirePvP = method.isAnnotationPresent(RequirePvP.class);
                    boolean disablePvp = method.isAnnotationPresent(DisablePvP.class);

                    if (hasRequiredGamemodes) {
                        reqiuredGamemodes = Seq.with(
                                Arrays.stream(method.getAnnotation(DisabledGamemodes.class).value())
                                        .map(DisableGamemode::Gamemode).toArray(Config.Gamemode[]::new)
                        );
                    }

                    if (hasDisabledGamemodes) {
                        disabledGamemodes = Seq.with(
                                Arrays.stream(method.getAnnotation(RequiredGamemodes.class).value())
                                        .map(RequireGamemode::Gamemode).toArray(Config.Gamemode[]::new)
                        );
                    }

                    if(!(
                            (hasRequiredGamemodes && hasDisabledGamemodes)
                         || (hasRequiredGamemodes && !reqiuredGamemodes.contains(gamemode))
                         || (hasDisabledGamemodes && disabledGamemodes.contains(gamemode))
                         || (hasRequiredGamemodes && requireSimpleGamemode)
                         || (requireSimpleGamemode && !gamemode.isSimple)
                         || (requirePvP && disablePvp)
                         || (requirePvP && !gamemode.isPvP)
                         || (disablePvp && gamemode.isPvP)
                    )) return method;
                    return null;
                })
                .filter(Objects::nonNull);
    }

    public static Seq<Method> getServerCommands() {
        return getStaticAnnotatedVoids(PandorumPlugin.ServerCommandsBasePackage, ServerCommand.class, new Class<?>[] { String[].class });
    }

    public static Seq<Method> getTriggerListenersMethods() {
        return getStaticAnnotatedVoidsWithoutParams(PandorumPlugin.TriggerListenersBasePackage, TriggerListener.class);
    }

    public static Seq<Method> getEventListenersMethods() {
        return getSuitableMethods(PandorumPlugin.EventListenersBasePackage, EventListener.class, method -> new Class<?>[] { method.getAnnotation(EventListener.class).eventType() });
    }

    public static Seq<Method> getPacketHandlersMethods() {
        return getSuitableMethods(PandorumPlugin.PacketHandlersBasePackage, PacketHandler.class, method -> new Class<?>[] { NetConnection.class, method.getAnnotation(PacketHandler.class).packetType() });
    }

    public static Seq<Method> getActionFiltersMethods() {
        return getStaticAnnotatedMethods(PandorumPlugin.ActionFiltersBasePackage, ActionFilter.class, new Class<?>[] { Administration.PlayerAction.class }, boolean.class);
    }

    public static Seq<Method> getChatFiltersMethods() {
        return getStaticAnnotatedMethods(PandorumPlugin.ChatFiltersBasePackage, ChatFilter.class, new Class<?>[] { Player.class, String.class }, String.class);
    }
}
