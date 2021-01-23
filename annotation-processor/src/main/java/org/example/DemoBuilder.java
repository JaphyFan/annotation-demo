package org.example;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Japhy
 * @since 2021/1/17 01:51
 */
@Retention(RetentionPolicy.CLASS)
@Documented
@Target(ElementType.METHOD)
public @interface DemoBuilder {

}
