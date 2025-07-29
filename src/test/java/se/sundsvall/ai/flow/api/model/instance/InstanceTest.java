package se.sundsvall.ai.flow.api.model.instance;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class InstanceTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(Instance.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builder() {
		// Arrange
		final var id = "123e4567-e89b-12d3-a456-426614174000";
		final var baseUrl = "https://example.com";
		final var tokenUrl = "https://example.com/token";
		final var username = "user123";
		final var password = "pass123";
		final var connectTimeout = 5;
		final var readTimeout = 60;

		// Act
		final var result = Instance.create()
			.withId(id)
			.withBaseUrl(baseUrl)
			.withTokenUrl(tokenUrl)
			.withUsername(username)
			.withPassword(password)
			.withConnectTimeout(connectTimeout)
			.withReadTimeout(readTimeout);

		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(result.getId()).isEqualTo(id);
		assertThat(result.getBaseUrl()).isEqualTo(baseUrl);
		assertThat(result.getUsername()).isEqualTo(username);
		assertThat(result.getPassword()).isEqualTo(password);
		assertThat(result.getConnectTimeout()).isEqualTo(connectTimeout);
		assertThat(result.getReadTimeout()).isEqualTo(readTimeout);

	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Instance.create()).hasAllNullFieldsOrProperties();
		assertThat(new Instance()).hasAllNullFieldsOrProperties();
	}
}
