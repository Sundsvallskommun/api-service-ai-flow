package se.sundsvall.ai.flow.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class InstanceEntityTest {

	private static final String ID = "664ff969-bb5f-42a6-8889-eb3af0e01e74";
	private static final String MUNICIPALITY_ID = "2281";
	private static final String BASE_URL = "https://api.example.com";
	private static final String TOKEN_URL = "https://api.example.com/token";
	private static final String USERNAME = "testUser";
	private static final String PASSWORD = "testPassword";
	private static final Integer CONNECT_TIMEOUT = 5;
	private static final Integer READ_TIMEOUT = 30;

	@Test
	void testBean() {
		MatcherAssert.assertThat(InstanceEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void getterAndSetterTest() {
		final var instanceEntity = new InstanceEntity();

		instanceEntity.setId(ID);
		instanceEntity.setMunicipalityId(MUNICIPALITY_ID);
		instanceEntity.setBaseUrl(BASE_URL);
		instanceEntity.setTokenUrl(TOKEN_URL);
		instanceEntity.setUsername(USERNAME);
		instanceEntity.setPassword(PASSWORD);
		instanceEntity.setConnectTimeout(CONNECT_TIMEOUT);
		instanceEntity.setReadTimeout(READ_TIMEOUT);

		assertThat(instanceEntity).hasNoNullFieldsOrProperties();
		assertThat(instanceEntity.getId()).isEqualTo(ID);
		assertThat(instanceEntity.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
		assertThat(instanceEntity.getBaseUrl()).isEqualTo(BASE_URL);
		assertThat(instanceEntity.getTokenUrl()).isEqualTo(TOKEN_URL);
		assertThat(instanceEntity.getUsername()).isEqualTo(USERNAME);
		assertThat(instanceEntity.getPassword()).isEqualTo(PASSWORD);
		assertThat(instanceEntity.getConnectTimeout()).isEqualTo(CONNECT_TIMEOUT);
		assertThat(instanceEntity.getReadTimeout()).isEqualTo(READ_TIMEOUT);
	}

	@Test
	void builderPatternTest() {
		final var instanceEntity = InstanceEntity.create()
			.withId(ID)
			.withMunicipalityId(MUNICIPALITY_ID)
			.withBaseUrl(BASE_URL)
			.withTokenUrl(TOKEN_URL)
			.withUsername(USERNAME)
			.withPassword(PASSWORD)
			.withConnectTimeout(CONNECT_TIMEOUT)
			.withReadTimeout(READ_TIMEOUT);

		assertThat(instanceEntity).hasNoNullFieldsOrProperties();
		assertThat(instanceEntity.getId()).isEqualTo(ID);
		assertThat(instanceEntity.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
		assertThat(instanceEntity.getBaseUrl()).isEqualTo(BASE_URL);
		assertThat(instanceEntity.getTokenUrl()).isEqualTo(TOKEN_URL);
		assertThat(instanceEntity.getUsername()).isEqualTo(USERNAME);
		assertThat(instanceEntity.getPassword()).isEqualTo(PASSWORD);
		assertThat(instanceEntity.getConnectTimeout()).isEqualTo(CONNECT_TIMEOUT);
		assertThat(instanceEntity.getReadTimeout()).isEqualTo(READ_TIMEOUT);
	}

	@Test
	void noDirtOnNewlyCreatedObject() {
		final var constructorEntity = new InstanceEntity();
		assertThat(constructorEntity).hasAllNullFieldsOrPropertiesExcept("connectTimeout", "readTimeout");
		assertThat(constructorEntity.getReadTimeout()).isZero();
		assertThat(constructorEntity.getConnectTimeout()).isZero();

		final var builderEntity = InstanceEntity.create();
		assertThat(builderEntity.getReadTimeout()).isZero();
		assertThat(builderEntity.getConnectTimeout()).isZero();
		assertThat(builderEntity).hasAllNullFieldsOrPropertiesExcept("connectTimeout", "readTimeout");
	}
}
