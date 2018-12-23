package de.fearnixx.t3.plugin.persistent;

import de.fearnixx.t3.event.IEvent;
import de.fearnixx.t3.plugin.PluginContainer;
import de.fearnixx.t3.reflect.Inject;
import de.fearnixx.t3.reflect.Listener;
import de.fearnixx.t3.reflect.T3BotPlugin;
import de.fearnixx.t3.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Created by MarkL4YG on 01.06.17.
 */
public class PluginRegistry {

    private static final Logger logger = LoggerFactory.getLogger(PluginRegistry.class);

    public static Optional<PluginRegistry> getFor(Class<?> pluginClass) {
        PluginRegistry pr = new PluginRegistry(pluginClass);
        if (!pr.analyze()) return Optional.empty();
        return Optional.of(pr);
    }

    private Class<?> pluginClass;
    private T3BotPlugin tag;

    private String id;
    private String version;
    private String buildAgainst;
    private String breaksBefore;
    private String breaksAfter;
    private List<String> HARD_depends;
    private List<String> SOFT_depends;

    private List<Method> listeners;
    private Map<Class<?>, List<Field>> injections;

    private PluginRegistry(Class<?> pluginClass) {
        this.pluginClass = pluginClass;
    }

    protected boolean analyze() {
        logger.debug("Analyzing class: {} from {}",
                pluginClass.toGenericString(), pluginClass.getProtectionDomain().getCodeSource().getLocation().getPath());

        tag = pluginClass.getAnnotation(T3BotPlugin.class);
        if (tag == null) {
            logger.error("Attempt to analyze untagged plugin class: {}", pluginClass.toGenericString());
            return false;
        }
        id = tag.id();
        if (!id.matches("^[a-z0-9.]+$")) {
            logger.error("Plugin ID: {} is invalid!",  this.id);
            return false;
        }
        version = Common.stripVersion(tag.version());
        if ("0".equals(version)) {
            logger.warn("Plugin ID: {} is using an invalid version: {}", this.id, tag.version());
            version = null;
        }

        // TODO: Bot version verification implementation
        buildAgainst = tag.builtAgainst();
        breaksAfter = tag.breaksAfter();
        breaksBefore = tag.breaksBefore();

        if (tag.depends().length == 0) {
            HARD_depends = Collections.emptyList();
        } else {
            HARD_depends = Collections.unmodifiableList(Arrays.asList(tag.depends()));
        }
        if (tag.requireAfter().length == 0) {
            SOFT_depends = Collections.emptyList();
        } else {
            SOFT_depends = Collections.unmodifiableList(Arrays.asList(tag.requireAfter()));
        }


        logger.debug("Pre-processing listeners");
        listeners = new ArrayList<>();

        Method[] methods = pluginClass.getDeclaredMethods();
        for (Method method : methods) {
            Annotation anno = method.getAnnotation(Listener.class);
            if (anno == null) continue;
            if (method.getParameterCount() != 1) {
                logger.debug("Wrong parameter count for method: {}", method.getName());
                continue;
            }
            if (!Modifier.isPublic(method.getModifiers())) {
                logger.debug("Wrong visibility for method: {}", method.getName());
                continue;
            }
            if (!IEvent.class.isAssignableFrom(method.getParameterTypes()[0])) {
                logger.debug("Wrong parameterType for method: {}", method.getName());
                continue;
            }

            listeners.add(method);
        }

        logger.debug("Pre-processing injections");
        injections = new HashMap<>();
        Field[] fields = pluginClass.getFields();
        for (Field field : fields) {
            if (field.getAnnotation(Inject.class) == null) continue;
            int mod = field.getModifiers();
            if (!Modifier.isPublic(mod) || Modifier.isAbstract(mod) || Modifier.isFinal(mod) || Modifier.isVolatile(mod)) {
                logger.debug("Wrong modifiers for field: {}", field.getName());
            }
            List<Field> l = injections.getOrDefault(field.getType(), null);
            if (l == null) {
                l = new ArrayList<>();
                injections.put(field.getType(), l);
            }
            l.add(field);
        }

        logger.debug("Plugin class {} analysed", pluginClass.toGenericString());
        final String logMessage = String.format("ID: %s Version: %s HDependencies: %d SDependencies: %d Build-INFO:[%s,%s,%s]", this.id, this.version, HARD_depends.size(), SOFT_depends.size(), breaksBefore, buildAgainst, breaksAfter);
        logger.debug(logMessage);
        return true;
    }

    public Class<?> getPluginClass() {
        return pluginClass;
    }

    public String getID() {
        return id;
    }

    public List<String> getHARD_depends() {
        return HARD_depends;
    }

    public List<String> getSOFT_depends() {
        return SOFT_depends;
    }

    public List<Method> getListeners() {
        return listeners;
    }

    public Map<Class<?>, List<Field>> getInjections() {
        return injections;
    }

    public PluginContainer newContainer() {
        return new PluginContainer(this);
    }
}
