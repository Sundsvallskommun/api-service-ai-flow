package apptest;

import static org.awaitility.Awaitility.await;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import se.sundsvall.ai.flow.Application;
import se.sundsvall.ai.flow.model.session.Session;
import se.sundsvall.ai.flow.model.session.StepExecution;
import se.sundsvall.ai.flow.service.Executor;
import se.sundsvall.ai.flow.service.FlowService;
import se.sundsvall.ai.flow.service.SessionService;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/SessionResourceIT/", classes = Application.class)
class SessionResourceIT extends AbstractAppTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String PATH = "/" + MUNICIPALITY_ID + "/session";
	private static final String RESPONSE_FILE = "response.json";
	private static final String REQUEST_FILE = "request.json";
	// Base64-encoded minimal PDF file for testing (contains "Hello World!")
	private static final String TEST_PDF_BASE64 = "JVBERi0xLjEKJcOiw6MKMSAwIG9iago8PC9UeXBlL0NhdGFsb2cvUGFnZXM8PC9UeXBlL1BhZ2VzL0NvdW50IDEvS2lkc1syIDAgUl0+Pj4+CmVuZG9iagoyIDAgb2JqCjw8L1R5cGUvUGFnZS9QYXJlbnQgMSAwIFIvTWVkaWFCb3hbMCAwIDU5NCA3OTJdL1Jlc291cmNlczw8L0ZvbnQ8PC9GMSAzIDAgUj4+L1Byb2NTZXRbL1BERi9UZXh0XT4+L0NvbnRlbnRzIDQgMCBSPj4KZW5kb2JqCjMgMCBvYmoKPDwvVHlwZS9Gb250L1N1YnR5cGUvVHlwZTEvTmFtZS9GMS9CYXNlRm9udC9IZWx2ZXRpY2E+PgplbmRvYmoKNCAwIG9iago8PC9MZW5ndGggNSAwIFI+PgpzdHJlYW0KQlQKL0YxIDM2IFRmCjEgMCAwIDEgMjU1IDc1MiBUbQo0OCBUTAooIEhlbGxvKScKKFdvcmxkISknCkVUCmVuZHN0cmVhbQplbmRvYmoKNSAwIG9iago3OAplbmRvYmoKeHJlZgowIDYKMDAwMDAwMDAwMCA2NTUzNiBmCjAwMDAwMDAwMTcgMDAwMDAgbgowMDAwMDAwMDk0IDAwMDAwIG4KMDAwMDAwMDIyOCAwMDAwMCBuCjAwMDAwMDAzMDIgMDAwMDAgbgowMDAwMDAwNDI1IDAwMDAwIG4KdHJhaWxlcgo8PC9TaXplIDYvSW5mbyA8PC9DcmVhdGlvbkRhdGUoRDoyMDIzKS9Qcm9kdWNlcihjbWQycGRmKS9UaXRsZShtaW5pLnBkZik+Pi9Sb290IDEgMCBSPj4Kc3RhcnR4cmVmCjQ0NgolJUVPRgoK";

	private Session session;

	@Autowired
	private SessionService sessionService;

	@Autowired
	private Executor executor;

	@Autowired
	private FlowService flowService;

	@BeforeEach
	void setup() {
		final var flow = flowService.getFlowVersion("tjansteskrivelse", 2);
		session = sessionService.createSession(MUNICIPALITY_ID, flow);
	}

	@Test
	void test1_getSession() {
		setupCall()
			.withServicePath(PATH + "/" + session.getId())
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test2_createSession() {
		setupCall()
			.withServicePath(PATH)
			.withRequest(REQUEST_FILE)
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test3_addInput() {
		setupCall()
			.withServicePath(PATH + "/" + session.getId() + "/input/arendenummer/simple")
			.withRequest(REQUEST_FILE)
			.withHttpMethod(POST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test4_replaceInput() {
		session.addSimpleInput("arendenummer", "VGhpcyBpcyBhbiBvcmlnaW5hbCBhcmVuZGVudW1tZXIgdmFsdWU=");

		setupCall()
			.withServicePath(PATH + "/" + session.getId() + "/input/arendenummer/simple")
			.withRequest(REQUEST_FILE)
			.withHttpMethod(POST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	@DirtiesContext
	void test5_runStep() {
		// Add all required inputs for the session to be executable
		session.addSimpleInput("arendenummer", "12345");
		session.addSimpleInput("uppdraget-till-tjansten", "VGhpcyBpcyBhIGFyZW5kZW51bW1lciB2YWx1ZQ==");
		session.addSimpleInput("forvaltningens-input", "VGhpcyBpcyBhIGFyZW5kZW51bW1lciB2YWx1ZQ==");
		session.addSimpleInput("bakgrundsmaterial", TEST_PDF_BASE64);
		session.addSimpleInput("relaterade-styrdokument", TEST_PDF_BASE64);

		setupCall()
			.withServicePath(PATH + "/" + session.getId())
			.withHttpMethod(POST)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequest();

		// Wait for the session to complete
		await().atMost(Duration.ofSeconds(30)).until(() ->
			session.getState() == Session.State.FINISHED);

		// Runs the step.
		setupCall()
			.withServicePath(PATH + "/" + session.getId() + "/step/arendet")
			.withRequest("request2.json")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.sendRequest();

		final var stepExecution = session.getStepExecution("arendet");

		await().atMost(Duration.ofSeconds(30)).until(() -> stepExecution.getState() == StepExecution.State.DONE);
		verifyAllStubs();
	}

	@Test
	@DirtiesContext
	void test6_getStepExecution() {
		// Add all required inputs for the session to be executable
		session.addSimpleInput("arendenummer", "12345");
		session.addSimpleInput("uppdraget-till-tjansten", "VGhpcyBpcyBhIGFyZW5kZW51bW1lciB2YWx1ZQ==");
		session.addSimpleInput("forvaltningens-input", "VGhpcyBpcyBhIGFyZW5kZW51bW1lciB2YWx1ZQ==");
		session.addSimpleInput("bakgrundsmaterial", TEST_PDF_BASE64);
		session.addSimpleInput("relaterade-styrdokument", TEST_PDF_BASE64);

		setupCall()
			.withServicePath(PATH + "/" + session.getId())
			.withHttpMethod(POST)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequest();

		// Wait for the session to complete
		await().atMost(Duration.ofSeconds(30)).until(() ->
			session.getState() == Session.State.FINISHED);

		// Runs the step.
		setupCall()
			.withServicePath(PATH + "/" + session.getId() + "/step/arendet")
			.withRequest("request2.json")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.sendRequest();

		final var stepExecution = session.getStepExecution("arendet");

		await().atMost(Duration.ofSeconds(30)).until(() -> stepExecution.getState() == StepExecution.State.DONE);

		setupCall()
			.withServicePath(PATH + "/" + session.getId() + "/step/arendet")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("response3.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test7_generateSessionOutput() {
		setupCall()
			.withServicePath(PATH + "/" + session.getId() + "/generate")
			.withRequest(REQUEST_FILE)
			.withHttpMethod(POST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	@DirtiesContext
	void test8_runSession() {
		// Add all required inputs for the session to be executable
		session.addSimpleInput("arendenummer", "12345");
		session.addSimpleInput("uppdraget-till-tjansten", "VGhpcyBpcyBhIGFyZW5kZW51bW1lciB2YWx1ZQ==");
		session.addSimpleInput("forvaltningens-input", "VGhpcyBpcyBhIGFyZW5kZW51bW1lciB2YWx1ZQ==");
		session.addSimpleInput("bakgrundsmaterial", TEST_PDF_BASE64);
		session.addSimpleInput("relaterade-styrdokument", TEST_PDF_BASE64);

		// Run the entire session (all steps)
		setupCall()
			.withServicePath(PATH + "/" + session.getId())
			.withHttpMethod(POST)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequest();

		// Wait for the session to complete
		await().atMost(Duration.ofSeconds(30)).until(() ->
			session.getState() == Session.State.FINISHED);
		verifyAllStubs();
	}

	@Test
	void test9_deleteSession() {

		setupCall()
			.withServicePath(PATH + "/" + session.getId())
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();

	}

	@Test
	void test10_clearInputInSession() {
		session.addSimpleInput("arendenummer", "12345");

		setupCall()
			.withServicePath(PATH + "/" + session.getId() + "/input/arendenummer")
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();

	}
}
