/*
            Annotation Definition = Creating a Form Template
            ┌─────────────────────────┐
            │  OrmColumn Form         │
            │  ─────────────────────  │
            │  Name: [_____________]  │ ← required field
            │  Length: [____255____]  │ ← optional field with default
            └─────────────────────────┘
            
            Using Annotation = Filling Out the Form
            ┌─────────────────────────┐
            │  OrmColumn Form         │
            │  ─────────────────────  │
            │  Name: [first_name   ]  │ ← you filled this
            │  Length: [____10____]   │ ← you filled this (overrode default)
            └─────────────────────────┘
*/


// Step 1: Creating an annotation definition
public @interface MyAnnotation {
    String value();              // Required parameter
    int number() default 100;    // Optional parameter (has default)
}

// Step 2: Using the annotation
public class Example {
    
    @MyAnnotation(value = "hello")              // number will be 100 (default)
    private String field1;
    
    @MyAnnotation(value = "world", number = 50) // number will be 50 (specified)
    private String field2;
    
    // This would be an ERROR because 'value' is required:
    // @MyAnnotation(number = 50)  // Missing required 'value' parameter
}

// Step 3: How Java processes this internally
// When you write @MyAnnotation(value = "hello"), Java creates something like:
// MyAnnotation annotation = new MyAnnotation() {
//     public String value() { return "hello"; }
//     public int number() { return 100; }  // default value used
// };

// Step 4: Your OrmColumn annotation breakdown
public @interface OrmColumn {
    String name();           // REQUIRED parameter - must be provided
    int length() default 255; // OPTIONAL parameter - 255 if not provided
}

// When you use it:
@OrmColumn(name = "first_name")           // length = 255 (default)
@OrmColumn(name = "last_name", length = 10) // length = 10 (specified)