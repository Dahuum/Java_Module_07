package orm;

import annotations.OrmEntity;
import annotations.OrmColumn;
import annotations.OrmColumnId;
import models.User;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrmManager {
    
    private Map<String, List<Map<String, Object>>> database = new HashMap<>();
    private Map<String, Long> idCounters = new HashMap<>();
    
    public OrmManager() {
        System.out.println("🚀 Starting ORM Manager...");
        initializeTables();
    }
    
    private void initializeTables() {
        // Drop existing tables first
        dropTables();
        
        // Create tables for entity classes
        Class<?> userClass = User.class;
        
        if (userClass.isAnnotationPresent(OrmEntity.class)) {
            System.out.println("📋 Found entity class: " + userClass.getSimpleName());
            createTable(userClass);
        }
    }
    
    private void createTable(Class<?> clazz) {
        OrmEntity entityAnnotation = clazz.getAnnotation(OrmEntity.class);
        String tableName = entityAnnotation.table();
        
        System.out.println("🏗️ Creating table: " + tableName);
        
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ").append(tableName).append(" (\n");
        
        List<String> columnDefinitions = new ArrayList<>();
        
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            
            if (field.isAnnotationPresent(OrmColumnId.class)) {
                columnDefinitions.add("    id BIGINT AUTO_INCREMENT PRIMARY KEY");
                
            } else if (field.isAnnotationPresent(OrmColumn.class)) {
                OrmColumn column = field.getAnnotation(OrmColumn.class);
                String columnName = column.name();
                String columnType = getSqlType(field.getType(), column.length());
                columnDefinitions.add("    " + columnName + " " + columnType);
            }
        }
        
        sql.append(String.join(",\n", columnDefinitions));
        sql.append("\n);");
        
        System.out.println("📝 Generated SQL:");
        System.out.println(sql.toString());
        
        database.put(tableName, new ArrayList<>());
        idCounters.put(tableName, 1L);
        
        System.out.println("✅ Table created successfully!\n");
    }
    
    private String getSqlType(Class<?> fieldType, int length) {
        if (fieldType == String.class) {
            return "VARCHAR(" + length + ")";
        } else if (fieldType == Integer.class || fieldType == int.class) {
            return "INT";
        } else if (fieldType == Long.class || fieldType == long.class) {
            return "BIGINT";
        } else if (fieldType == Double.class || fieldType == double.class) {
            return "DOUBLE";
        } else if (fieldType == Boolean.class || fieldType == boolean.class) {
            return "BOOLEAN";
        }
        return "VARCHAR(255)";
    }
    
    public void save(Object entity) {
        Class<?> clazz = entity.getClass();
        
        if (!clazz.isAnnotationPresent(OrmEntity.class)) {
            throw new IllegalArgumentException("Class is not an entity: " + clazz.getSimpleName());
        }
        
        OrmEntity entityAnnotation = clazz.getAnnotation(OrmEntity.class);
        String tableName = entityAnnotation.table();
        
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(tableName).append(" (");
        
        List<String> columnNames = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        
        Field[] fields = clazz.getDeclaredFields();
        
        // First, handle ID field (auto-generate)
        for (Field field : fields) {
            if (field.isAnnotationPresent(OrmColumnId.class)) {
                field.setAccessible(true);
                try {
                    Long newId = idCounters.get(tableName);
                    field.set(entity, newId);
                    idCounters.put(tableName, newId + 1);
                    columnNames.add("id");
                    values.add(newId);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Cannot set ID field", e);
                }
                break;
            }
        }
        
        // Then handle regular columns
        for (Field field : fields) {
            if (field.isAnnotationPresent(OrmColumn.class)) {
                field.setAccessible(true);
                try {
                    OrmColumn column = field.getAnnotation(OrmColumn.class);
                    columnNames.add(column.name());
                    values.add(field.get(entity));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Cannot access field: " + field.getName(), e);
                }
            }
        }
        
        sql.append(String.join(", ", columnNames));
        sql.append(") VALUES (");
        
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) sql.append(", ");
            Object value = values.get(i);
            if (value instanceof String) {
                sql.append("'").append(value).append("'");
            } else {
                sql.append(value);
            }
        }
        sql.append(");");
        
        System.out.println("📝 Generated SQL:");
        System.out.println(sql.toString());
        
        // Save to simulated database
        Map<String, Object> row = new HashMap<>();
        for (int i = 0; i < columnNames.size(); i++) {
            row.put(columnNames.get(i), values.get(i));
        }
        database.get(tableName).add(row);
        
        System.out.println("💾 Entity saved: " + entity);
        System.out.println();
    }
    
    @SuppressWarnings("unchecked")
    public <T> T findById(Long id, Class<T> clazz) {
        if (!clazz.isAnnotationPresent(OrmEntity.class)) {
            throw new IllegalArgumentException("Class is not an entity: " + clazz.getSimpleName());
        }
        
        OrmEntity entityAnnotation = clazz.getAnnotation(OrmEntity.class);
        String tableName = entityAnnotation.table();
        
        System.out.println("📝 Generated SQL:");
        System.out.println("SELECT * FROM " + tableName + " WHERE id = " + id + ";");
        
        // Search in simulated database
        List<Map<String, Object>> table = database.get(tableName);
        if (table == null) {
            return null;
        }
        
        for (Map<String, Object> row : table) {
            if (id.equals(row.get("id"))) {
                try {
                    T instance = clazz.getDeclaredConstructor().newInstance();
                    
                    Field[] fields = clazz.getDeclaredFields();
                    for (Field field : fields) {
                        field.setAccessible(true);
                        
                        if (field.isAnnotationPresent(OrmColumnId.class)) {
                            field.set(instance, row.get("id"));
                        } else if (field.isAnnotationPresent(OrmColumn.class)) {
                            OrmColumn column = field.getAnnotation(OrmColumn.class);
                            field.set(instance, row.get(column.name()));
                        }
                    }
                    
                    System.out.println("🔍 Entity found: " + instance);
                    System.out.println();
                    return instance;
                } catch (Exception e) {
                    throw new RuntimeException("Cannot create instance of " + clazz.getSimpleName(), e);
                }
            }
        }
        
        System.out.println("❌ Entity not found with id: " + id);
        System.out.println();
        return null;
    }
    
    public void update(Object entity) {
        Class<?> clazz = entity.getClass();
        
        if (!clazz.isAnnotationPresent(OrmEntity.class)) {
            throw new IllegalArgumentException("Class is not an entity: " + clazz.getSimpleName());
        }
        
        OrmEntity entityAnnotation = clazz.getAnnotation(OrmEntity.class);
        String tableName = entityAnnotation.table();
        
        try {
            // Get the ID field
            Long id = null;
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(OrmColumnId.class)) {
                    id = (Long) field.get(entity);
                    break;
                }
            }
            
            if (id == null) {
                throw new RuntimeException("Entity has no ID - cannot update");
            }
            
            // Generate UPDATE SQL
            StringBuilder sql = new StringBuilder();
            sql.append("UPDATE ").append(tableName).append(" SET ");
            
            List<String> updates = new ArrayList<>();
            for (Field field : fields) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(OrmColumn.class)) {
                    OrmColumn column = field.getAnnotation(OrmColumn.class);
                    Object value = field.get(entity);
                    String valueStr = (value instanceof String) ? "'" + value + "'" : String.valueOf(value);
                    updates.add(column.name() + " = " + valueStr);
                }
            }
            
            sql.append(String.join(", ", updates));
            sql.append(" WHERE id = ").append(id).append(";");
            
            System.out.println("📝 Generated SQL:");
            System.out.println(sql.toString());
            
            // Update in simulated database
            List<Map<String, Object>> table = database.get(tableName);
            boolean found = false;
            for (Map<String, Object> row : table) {
                if (id.equals(row.get("id"))) {
                    for (Field field : fields) {
                        field.setAccessible(true);
                        if (field.isAnnotationPresent(OrmColumn.class)) {
                            OrmColumn column = field.getAnnotation(OrmColumn.class);
                            row.put(column.name(), field.get(entity));
                        }
                    }
                    found = true;
                    break;
                }
            }
            
            if (found) {
                System.out.println("🔄 Entity updated: " + entity);
            } else {
                System.out.println("❌ Entity not found for update with id: " + id);
            }
            System.out.println();
            
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot access fields for update", e);
        }
    }
    
    public void dropTables() {
        System.out.println("🗑️ Dropping tables...");
        
        for (String tableName : new ArrayList<>(database.keySet())) {
            System.out.println("📝 Generated SQL:");
            System.out.println("DROP TABLE IF EXISTS " + tableName + ";");
        }
        
        database.clear();
        idCounters.clear();
        
        System.out.println("✅ Tables dropped successfully!\n");
    }
}