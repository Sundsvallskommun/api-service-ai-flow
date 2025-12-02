package se.sundsvall.ai.flow.service.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static se.sundsvall.ai.flow.model.flowdefinition.Step.Target.Type.SERVICE;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Problem;

@ExtendWith(MockitoExtension.class)
class TargetExecutorResolverTest {

	@Mock
	private TargetExecutor targetExecutor;

	@Test
	void resolve_returnsMatchingExecutor() {

		when(targetExecutor.supports(SERVICE)).thenReturn(true);

		final var resolver = new TargetExecutorResolver(List.of(targetExecutor));

		final var result = resolver.resolve(SERVICE);

		assertThat(result).isSameAs(targetExecutor);
	}

	@Test
	void resolve_throwsWhenNoExecutor() {
		final var resolver = new TargetExecutorResolver(List.of());

		assertThatThrownBy(() -> resolver.resolve(SERVICE))
			.isInstanceOf(Problem.class)
			.hasMessageContaining("No TargetExecutor for type");
	}
}
