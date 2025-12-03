package apptest;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.ai.flow.Application;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/FlowResourceIT/", classes = Application.class)
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata.sql"
})
class FlowResourceIT extends AbstractAppTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String PATH = "/" + MUNICIPALITY_ID + "/flow";
	private static final String RESPONSE_FILE = "response.json";

	@Test
	void test1_getAllFlows() {
		setupCall()
			.withServicePath(PATH)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test2_getFlowByIdAndVersion() {
		setupCall()
			.withServicePath(PATH + "/tjansteskrivelse/1")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test3_getLatestFlowVersion() {
		setupCall()
			.withServicePath(PATH + "/tjansteskrivelse")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test4_createFlow() {
		setupCall()
			.withServicePath(PATH)
			.withHttpMethod(POST)
			.withRequest("request.json")
			.withExpectedResponseStatus(CREATED)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test5_deleteFlow() {
		setupCall()
			.withServicePath(PATH + "/tjansteskrivelse")
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test6_deleteFlowVersion() {
		setupCall()
			.withServicePath(PATH + "/tjansteskrivelse/1")
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse();
	}

}
