package fr.school.annotations;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface HtmlInput {
    String type();
    String name();
    String placeholder();
}