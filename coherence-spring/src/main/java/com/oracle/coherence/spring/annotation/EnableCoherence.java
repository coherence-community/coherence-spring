/**
 *
 */
package com.oracle.coherence.spring.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.oracle.coherence.spring.configuration.CoherenceConfiguration;

/**
 *
 * @author Gunnar Hillert
 *
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
@Import(CoherenceConfiguration.class)
public @interface EnableCoherence {

}
