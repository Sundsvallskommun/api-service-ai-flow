package se.sundsvall.ai.flow.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(StaleSessionReaperProperties.class)
class StaleSessionReaperConfiguration {

	@Bean
	TaskScheduler taskScheduler(final StaleSessionReaperProperties properties) {
		var scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(properties.threadPoolSize());
		scheduler.setThreadNamePrefix("stale-session-reaper-");
		scheduler.setWaitForTasksToCompleteOnShutdown(true);
		return scheduler;
	}
}
