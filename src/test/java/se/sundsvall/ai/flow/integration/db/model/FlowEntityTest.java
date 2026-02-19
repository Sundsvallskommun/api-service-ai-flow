package se.sundsvall.ai.flow.integration.db.model;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class FlowEntityTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(FlowEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void setterAndGetter() {
		var id = "id";
		var version = 1;
		var name = "name";
		var description = "description";
		var content = "content";

		var flowEntity = new FlowEntity();
		flowEntity.setId(id);
		flowEntity.setVersion(version);
		flowEntity.setName(name);
		flowEntity.setDescription(description);
		flowEntity.setContent(content);

		assertThat(flowEntity.getId()).isEqualTo(id);
		assertThat(flowEntity.getVersion()).isEqualTo(version);
		assertThat(flowEntity.getName()).isEqualTo(name);
		assertThat(flowEntity.getDescription()).isEqualTo(description);
		assertThat(flowEntity.getContent()).isEqualTo(content);
	}

	@Test
	void builderPattern() {
		var id = "id";
		var version = 1;
		var name = "name";
		var description = "description";
		var content = "content";

		var flowEntity = new FlowEntity()
			.withId(id)
			.withVersion(version)
			.withName(name)
			.withDescription(description)
			.withContent(content);

		assertThat(flowEntity.getId()).isEqualTo(id);
		assertThat(flowEntity.getVersion()).isEqualTo(version);
		assertThat(flowEntity.getName()).isEqualTo(name);
		assertThat(flowEntity.getDescription()).isEqualTo(description);
		assertThat(flowEntity.getContent()).isEqualTo(content);
	}

	@Nested
	class IdAndVersionTest {

		@Test
		void constructorAndGetter() {
			var id = "tjansteskrivelse";
			var version = 123;

			var flowEntityId = new FlowEntity.IdAndVersion(id, version);

			assertThat(flowEntityId.id()).isEqualTo(id);
			assertThat(flowEntityId.version()).isEqualTo(version);
			assertThat(flowEntityId).hasNoNullFieldsOrProperties();
		}
	}
}
