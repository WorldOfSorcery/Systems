package me.hektortm.woSSystems.database.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Maps a field to a database column.
 *
 * <ul>
 *   <li>{@link #name}         — SQL column name; defaults to snake_case of the field name</li>
 *   <li>{@link #type}         — SQL type; auto-detected from the Java type when left empty</li>
 *   <li>{@link #primaryKey}   — mark this column as the PRIMARY KEY</li>
 *   <li>{@link #notNull}      — add NOT NULL constraint</li>
 *   <li>{@link #defaultValue} — DEFAULT clause value (e.g. {@code "0"}, {@code "FALSE"})</li>
 * </ul>
 *
 * Auto-detected type mapping:
 * <pre>
 *   String        → VARCHAR(255)
 *   long / Long   → BIGINT
 *   int / Integer → INT
 *   boolean/…     → BOOLEAN
 *   double/Double → DOUBLE
 *   List          → TEXT  (JSON-serialised by the DAO)
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
    /** Override the column name. Defaults to snake_case of the field name. */
    String name() default "";

    /** Override the SQL type. Auto-detected from the Java type when empty. */
    String type() default "";

    /** Make this the PRIMARY KEY. */
    boolean primaryKey() default false;

    /** Add NOT NULL constraint. */
    boolean notNull() default false;

    /** DEFAULT value written into the DDL, e.g. {@code "0"} or {@code "FALSE"}. */
    String defaultValue() default "";
}
