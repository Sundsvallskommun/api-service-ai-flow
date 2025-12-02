package se.sundsvall.ai.flow.test.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Test annotation for testing {@link org.springframework.boot.context.properties.ConfigurationProperties} classes
 * without starting the full application context.
 *
 * <p>
 * Usage:
 *
 * <pre>
 * {
 * 	&#64;code
 * 	&#64;PropertiesTest(TemplatingProperties.class)
 * 	class TemplatingPropertiesTest {
 * 		&#64;Autowired
 * 		private TemplatingProperties properties;
 * 	}
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
@EnableConfigurationProperties
@ActiveProfiles
public @interface PropertiesTest {

	/**
	 * The {@link org.springframework.boot.context.properties.ConfigurationProperties} class(es) to test.
	 */
	@AliasFor(annotation = EnableConfigurationProperties.class, attribute = "value")
	Class<?>[] value();

	/**
	 * The Spring profile(s) to activate. Defaults to "junit".
	 */
	@AliasFor(annotation = ActiveProfiles.class, attribute = "profiles")
	String[] profiles() default "junit";
}
