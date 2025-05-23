package se.sundsvall.ai.flow;

import static org.springframework.boot.SpringApplication.run;

import se.sundsvall.dept44.ServiceApplication;
import se.sundsvall.dept44.util.jacoco.ExcludeFromJacocoGeneratedCoverageReport;

@ServiceApplication
@ExcludeFromJacocoGeneratedCoverageReport
public class Application {

	public static void main(String... args) {
		run(Application.class, args);
	}
}
