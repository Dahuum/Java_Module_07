package fr.school.reflection;

import java.util.*;
import java.lang.reflect.*;

public class Program 
{
    private static final String CLASSES_PACKAGES = "fr.school.reflection.classes";
    private static final String[] AVAILABLE_CLASSES = {"User", "Car"};
    
    public static void main( String[] args )
    {
        try {
            
            Scanner scanner = new Scanner(System.in);
            
            displayAvailableClasses();
            
            System.out.println("Enter class name:");
            System.out.print("-> ");
            String className = scanner.nextLine();
            
            Class<?> selectedClass = loadClass(className);
            if (selectedClass == null) {
                System.out.println("Class not found");
                return ;
            }
            
            displayClassInfo(selectedClass);
            
            Object createdObject = createObjectInteractively(selectedClass, scanner);
            System.out.println("Object created: " + createdObject);
            System.out.println("---------------------");
            
            updateFieldInteractively(createdObject, selectedClass, scanner);
            
            callMethodInteractively(createdObject, selectedClass, scanner);
        } catch (Exception e) { System.err.println(e.getMessage()); }
    }
    
    private static void displayAvailableClasses() 
    {
        System.out.println("Classes:");
        for (String clazz: AVAILABLE_CLASSES)
            System.out.println(clazz);
        System.out.println("---------------------");
    }
    
    private static Class<?> loadClass(String className) {
        try {
            return Class.forName(CLASSES_PACKAGES + '.' + className);
        } catch (Exception e) { return null; }
    }
    
    private static void displayClassInfo(Class<?> clazz) {
        System.out.println("---------------------");
        
        Field[] fields = clazz.getDeclaredFields();
        Method[] methods = clazz.getDeclaredMethods();
        System.out.println("Fields:");
        for (Field field: fields) 
            System.out.println("    " + field.getType().getSimpleName() + " " + field.getName());
        
        System.out.println("Methods:");
        for (Method method: methods) {
            Class<?>[] params = method.getParameterTypes();
            StringBuilder paramStr = new StringBuilder();
            for (Class<?> param: params) {
                if (paramStr.length() > 0) paramStr.append(", ");
                paramStr.append(param.getSimpleName());
            }
            System.out.println("    " + method.getReturnType().getSimpleName() + " " + method.getName() + "(" +  paramStr + ")");
        }
        System.out.println("---------------------");
    }
    
    private static Object createObjectInteractively(Class<?> clazz, Scanner scanner) throws Exception {
            System.out.println("Let's create an object.");
            
            Field[] fields = clazz.getDeclaredFields();
            
            Constructor<?> constructor = null;
            Class<?>[] paramTypes = new Class[fields.length];
            
            for (int i = 0; i < fields.length; i++) {
                paramTypes[i] = fields[i].getType();
            }
            
            try {
                constructor = clazz.getDeclaredConstructor(paramTypes);
            } catch (NoSuchMethodException e) {
                constructor = clazz.getDeclaredConstructor();
                return constructor.newInstance();
            }
            
            Object[] args = new Object[fields.length];
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                System.out.println(field.getName() + ":");
                System.out.print("-> ");
                String input = scanner.nextLine().trim();
                args[i] = convertStringToType(input, field.getType());
            }
            
            return constructor.newInstance(args);
        }
        
    private static Object convertStringToType(String value, Class<?> type) {
        if (type == String.class) {
            return value;
        } else if (type == Integer.class || type == int.class) {
            return Integer.parseInt(value);
        } else if (type == Double.class || type == double.class) {
            return Double.parseDouble(value);
        } else if (type == Boolean.class || type == boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (type == Long.class || type == long.class) {
            return Long.parseLong(value);
        }
              
        throw new IllegalArgumentException("Unsupported type: " + type);
    }
        
    private static void updateFieldInteractively(Object obj, Class<?> clazz, Scanner scanner) throws Exception {
        System.out.println("Enter name of the field for changing:");
        System.out.print("-> ");
        String fieldName = scanner.nextLine().trim();
              
        Field field;
        try {
            field = clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            System.out.println("Field not found!");
            return;
        }
              
        field.setAccessible(true);
              
        System.out.printf("Enter %s value:%n", field.getType().getSimpleName());
        System.out.print("-> ");
        String input = scanner.nextLine().trim();
              
        Object value = convertStringToType(input, field.getType());
        field.set(obj, value);
              
        System.out.println("Object updated: " + obj);
        System.out.println("---------------------");
    }
    
    private static void callMethodInteractively(Object obj, Class<?> clazz, Scanner scanner) throws Exception {
        System.out.println("Enter name of the method for call:");
        System.out.print("-> ");
        String methodInput = scanner.nextLine().trim();
        
        String methodName;
        Class<?>[] paramTypes;
        
        if (methodInput.contains("(")) {
            methodName = methodInput.substring(0, methodInput.indexOf("("));
            String paramString = methodInput.substring(methodInput.indexOf("(") + 1, methodInput.indexOf(")"));
            
            if (paramString.trim().isEmpty()) {
                paramTypes = new Class<?>[0];
            } else {
                String[] paramTypeNames = paramString.split(",");
                paramTypes = new Class<?>[paramTypeNames.length];
                for (int i = 0; i < paramTypeNames.length; i++) {
                    paramTypes[i] = getClassForSimpleName(paramTypeNames[i].trim());
                }
            }
        } else {
            methodName = methodInput;
            paramTypes = new Class<?>[0];
        }
        
        Method method;
        try {
            method = clazz.getDeclaredMethod(methodName, paramTypes);
        } catch (NoSuchMethodException e) {
            System.out.println("Method not found!");
            return;
        }
        
        Object[] args = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            System.out.printf("Enter %s value:%n", paramTypes[i].getSimpleName());
            System.out.print("-> ");
            String input = scanner.nextLine().trim();
            args[i] = convertStringToType(input, paramTypes[i]);
        }
        
        Object result = method.invoke(obj, args);
        
        if (!method.getReturnType().equals(void.class)) {
            System.out.println("Method returned:");
            System.out.println(result);
        }
    }
    
    private static Class<?> getClassForSimpleName(String simpleName) {
        switch (simpleName.toLowerCase()) {
            case "int":
            case "integer":
                return Integer.class;
            case "double":
                return Double.class;
            case "string":
                return String.class;
            case "boolean":
                return Boolean.class;
            case "long":
                return Long.class;
            default:
                throw new IllegalArgumentException("Unknown type: " + simpleName);
        }
    }
}
