package apptest;

import static org.awaitility.Awaitility.await;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import java.io.FileNotFoundException;
import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriComponentsBuilder;

import se.sundsvall.ai.flow.Application;
import se.sundsvall.ai.flow.model.session.Session;
import se.sundsvall.ai.flow.service.SessionService;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/AppSessionResourceIT/", classes = Application.class)
class AppSessionResourceIT extends AbstractAppTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String BASE_PATH = "/" + MUNICIPALITY_ID + "/session";
	private static final String REQUEST_FILE = "request.json";

	@Autowired
	private SessionService sessionService;

	@Test
	void test1_pictureTranscriberApp() throws FileNotFoundException {
		final var inputId = "indata-bild";
		final var stepId = "tolka-bild";

		// Create a session
		var responseHeaders = setupCall()
			.withServicePath(BASE_PATH)
			.withRequest(REQUEST_FILE)
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.sendRequest()
			.getResponseHeaders();

		final var sessionId = getSessionId(responseHeaders);

		// Add a file to the session
		setupCall()
			.withServicePath(UriComponentsBuilder.newInstance().replacePath(BASE_PATH)
				.pathSegment(sessionId, "input", inputId, "file")
				.toUriString())
			.withContentType(MediaType.MULTIPART_FORM_DATA)
			.withRequestFile("file", "picture1.jpeg")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("add-file-response.json")
			.sendRequest();

		// Trigger 
		setupCall()
			.withServicePath(UriComponentsBuilder.newInstance().replacePath(BASE_PATH)
				.pathSegment(sessionId)
				.toUriString())
			.withHttpMethod(POST)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequest();

		// Wait for the async execution to complete
		final var session = sessionService.getSession(UUID.fromString(sessionId));
		await().atMost(Duration.ofSeconds(30)).until(() ->
			(session.getState() == Session.State.FINISHED || session.getState() == Session.State.ERROR));

		// Get step execution results and verify
		setupCall()
			.withServicePath(UriComponentsBuilder.newInstance().replacePath(BASE_PATH)
				.pathSegment(sessionId, "step", stepId)
				.toUriString())
			.withHttpMethod(org.springframework.http.HttpMethod.GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("step-execution-response.json")
			.sendRequestAndVerifyResponse();
	}

	private String getSessionId(final HttpHeaders headers) {
		// Extract just the session ID from the location header
		final var location = headers.getFirst(HttpHeaders.LOCATION);
		return location.substring(location.lastIndexOf('/') + 1);
	}

}
