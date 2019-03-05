package de.fearnixx.jeak.reflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Designates a class to be loaded as a plugin by the framework.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JeakBotPlugin {
    /**
     * The plugin ID
     * Must match "^[a-z.]+$"
     */
    String id();

    /**
     * The plugin version will be used to determine improperly satisfied dependencies
     */
    String version() default "0.0.0";

    /**
     * Hard dependencies
     * Bot will refuse to start if unresolved
     * Bot will refuse to start if circular! You will need to avoid that.
     */
    String[] depends() default {};

    /**
     * Soft dependencies - Bot will try to load this plugin AFTER all of these
     * This cannot be 100% guaranteed though.
     * @deprecated Will be removed without replacement. There is currently no safe plan for handling soft dependencies.
     */
    @Deprecated
    String[] requireAfter() default {};

    /**
     * Bot version revisions!
     * Bot will refuse to start with plugins that won't work.
     * Bot will issue a warning if builtAgainst does not match with its version
     *
     * Empty string is a wildcard!
     * This means no breaking version and built against ANY.
     *
     * - Note: Currently unused. Will be added later
     */
    String builtAgainst() default "";

    /**
     * @deprecated use {@link #builtAgainst()}. Values will be inferred by semver rules.
     */
    @Deprecated
    String breaksBefore() default "";

    /**
     *
     * @deprecated use {@link #builtAgainst()}. Values will be inferred by semver rules.
     */
    @Deprecated
    String breaksAfter() default "";
}
