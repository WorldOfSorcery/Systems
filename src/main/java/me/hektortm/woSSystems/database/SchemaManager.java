package me.hektortm.woSSystems.database;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.annotation.Column;
import me.hektortm.woSSystems.database.annotation.Table;
import me.hektortm.wosCore.database.DatabaseManager;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Automatically creates and migrates tables from annotated {@link BaseEntity} subclasses.
 *
 * <h3>How it works</h3>
 * <ol>
 *   <li>Reads the {@link Table} annotation to get the table name.</li>
 *   <li>Collects every field annotated with {@link Column} (walks the full class hierarchy
 *       so parent fields — id, tag — come first).</li>
 *   <li>Issues {@code CREATE TABLE IF NOT EXISTS} with the full column list.</li>
 *   <li>Queries the live schema and {@code ALTER TABLE ADD COLUMN} for every new field.</li>
 * </ol>
 *
 * <h3>What it never does</h3>
 * <ul>
 *   <li>Remove or rename columns (data-safe)</li>
 *   <li>Change column types</li>
 *   <li>Touch tables that have no {@link Table} annotation</li>
 * </ul>
 */
public final class SchemaManager {

    private SchemaManager() {}

    /**
     * Synchronise the database table for the given entity class.
     * Safe to call on every startup — runs only DDL, never touches rows.
     * Accepts any class annotated with {@link Table} — not limited to {@link BaseEntity} subclasses.
     */
    public static void syncTable(DatabaseManager db, Class<?> clazz) {
        Table tableAnn = clazz.getAnnotation(Table.class);
        if (tableAnn == null) {
            throw new IllegalArgumentException(
                    "Class " + clazz.getSimpleName() + " has no @Table annotation");
        }

        String tableName = tableAnn.value();
        List<ColumnDef> columns = collectColumns(clazz);
        Logger log = WoSSystems.getPlugin(WoSSystems.class).getLogger();

        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {

            // 1 — create table (no-op if it already exists)
            stmt.execute(buildCreateTable(tableName, columns));

            // 2 — add any columns that don't exist yet (forward migration)
            Set<String> existing = existingColumns(conn, tableName);
            for (ColumnDef col : columns) {
                if (!existing.contains(col.name.toLowerCase())) {
                    StringBuilder alter = new StringBuilder(
                            "ALTER TABLE " + tableName + " ADD COLUMN " + col.name + " " + col.sqlType);
                    if (!col.defaultValue.isEmpty()) {
                        alter.append(" DEFAULT ").append(col.defaultValue);
                    }
                    stmt.execute(alter.toString());
                    log.info("[SchemaManager] " + tableName + ": added column '" + col.name + "'");
                }
            }

        } catch (SQLException e) {
            WoSSystems.getPlugin(WoSSystems.class).getLogger().severe(
                    "[SchemaManager] Failed to sync table '" + tableName + "': " + e.getMessage());
        }
    }

    // ─── Internals ──────────────────────────────────────────────────────────────

    private static Set<String> existingColumns(Connection conn, String tableName) throws SQLException {
        Set<String> cols = new HashSet<>();
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getColumns(null, null, tableName, null)) {
            while (rs.next()) {
                cols.add(rs.getString("COLUMN_NAME").toLowerCase());
            }
        }
        return cols;
    }

    /**
     * Walks the class hierarchy (most-derived → Object) and collects @Column fields,
     * reversing so parent fields (id, tag) appear first.
     */
    private static List<ColumnDef> collectColumns(Class<?> clazz) {
        List<List<ColumnDef>> layers = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            List<ColumnDef> layer = new ArrayList<>();
            for (Field f : current.getDeclaredFields()) {
                Column col = f.getAnnotation(Column.class);
                if (col != null) {
                    layer.add(new ColumnDef(f, col));
                }
            }
            layers.add(0, layer); // prepend → parent fields first after reversal
            current = current.getSuperclass();
        }
        List<ColumnDef> result = new ArrayList<>();
        for (List<ColumnDef> layer : layers) result.addAll(layer);
        return result;
    }

    private static String buildCreateTable(String tableName, List<ColumnDef> columns) {
        StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                .append(tableName).append(" (\n");
        List<String> defs = new ArrayList<>();
        List<String> pks = new ArrayList<>();

        for (ColumnDef col : columns) {
            StringBuilder def = new StringBuilder("    ")
                    .append(col.name).append(" ").append(col.sqlType);
            if (col.notNull)              def.append(" NOT NULL");
            if (!col.defaultValue.isEmpty()) def.append(" DEFAULT ").append(col.defaultValue);
            defs.add(def.toString());
            if (col.primaryKey) pks.add(col.name);
        }

        sb.append(String.join(",\n", defs));
        if (!pks.isEmpty()) {
            sb.append(",\n    PRIMARY KEY (").append(String.join(", ", pks)).append(")");
        }
        return sb.append("\n)").toString();
    }

    // ─── Type inference ──────────────────────────────────────────────────────────

    private static String inferType(Field field) {
        Class<?> t = field.getType();
        if (t == String.class)                            return "VARCHAR(255)";
        if (t == long.class   || t == Long.class)         return "BIGINT";
        if (t == int.class    || t == Integer.class)      return "INT";
        if (t == boolean.class || t == Boolean.class)     return "BOOLEAN";
        if (t == double.class  || t == Double.class)      return "DOUBLE";
        if (t == float.class   || t == Float.class)       return "FLOAT";
        if (t == UUID.class)                              return "CHAR(36)";
        if (List.class.isAssignableFrom(t))               return "TEXT";
        return "TEXT";
    }

    private static String toSnakeCase(String camel) {
        return camel.replaceAll("([A-Z])", "_$1").toLowerCase().replaceAll("^_", "");
    }

    // ─── ColumnDef ───────────────────────────────────────────────────────────────

    private static final class ColumnDef {
        final String  name;
        final String  sqlType;
        final boolean primaryKey;
        final boolean notNull;
        final String  defaultValue;

        ColumnDef(Field field, Column col) {
            this.name         = col.name().isEmpty() ? toSnakeCase(field.getName()) : col.name();
            this.sqlType      = col.type().isEmpty() ? inferType(field) : col.type();
            this.primaryKey   = col.primaryKey();
            this.notNull      = col.notNull();
            this.defaultValue = col.defaultValue();
        }
    }
}
