package me.hektortm.woSSystems.database.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@link me.hektortm.woSSystems.utils.model.BaseEntity} subclass
 * as the source of truth for a database table.
 *
 * SchemaManager will:
 *  - CREATE TABLE IF NOT EXISTS using the annotated fields
 *  - ALTER TABLE ADD COLUMN for any fields not yet present in the DB
 *
 * Example:
 * <pre>
 * {@literal @}Table("stats")
 * public class Stat extends BaseEntity { ... }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Table {
    /** The SQL table name. */
    String value();
}
