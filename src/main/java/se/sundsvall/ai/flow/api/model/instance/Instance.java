package se.sundsvall.ai.flow.api.model.instance;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.WRITE_ONLY;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

@Schema(description = "Instance model")
public class Instance {

	@Schema(description = "Instance ID", accessMode = READ_ONLY, example = "123e4567-e89b-12d3-a456-426614174000")
	private String id;

	@Schema(description = "Base URL of the instance", example = "https://example.com")
	private String baseUrl;

	@Schema(description = "Token URL for the instance", example = "https://example.com/token")
	private String tokenUrl;

	@Schema(description = "Username for the instance", accessMode = WRITE_ONLY, example = "user123")
	private String username;

	@Schema(description = "Password for the instance", accessMode = WRITE_ONLY, example = "pass123")
	private String password;

	@Schema(description = "Connection timeout in seconds", example = "5")
	private Integer connectTimeout;

	@Schema(description = "Read timeout in seconds", example = "60")
	private Integer readTimeout;

	public static Instance create() {
		return new Instance();
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public Instance withId(final String id) {
		this.id = id;
		return this;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(final String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public Instance withBaseUrl(final String baseUrl) {
		this.baseUrl = baseUrl;
		return this;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(final String username) {
		this.username = username;
	}

	public Instance withUsername(final String username) {
		this.username = username;
		return this;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	public Instance withPassword(final String password) {
		this.password = password;
		return this;
	}

	public Integer getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(final Integer connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public Instance withConnectTimeout(final Integer connectTimeout) {
		this.connectTimeout = connectTimeout;
		return this;
	}

	public Integer getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(final Integer readTimeout) {
		this.readTimeout = readTimeout;
	}

	public Instance withReadTimeout(final Integer readTimeout) {
		this.readTimeout = readTimeout;
		return this;
	}

	public Instance withTokenUrl(final String tokenUrl) {
		this.tokenUrl = tokenUrl;
		return this;
	}

	public String getTokenUrl() {
		return tokenUrl;
	}

	public void setTokenUrl(String tokenUrl) {
		this.tokenUrl = tokenUrl;
	}

	@Override
	public String toString() {
		return "Instance{" +
			"id='" + id + '\'' +
			", baseUrl='" + baseUrl + '\'' +
			", tokenUrl='" + tokenUrl + '\'' +
			", username='" + username + '\'' +
			", password='" + password + '\'' +
			", connectTimeout=" + connectTimeout +
			", readTimeout=" + readTimeout +
			'}';
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass())
			return false;
		Instance instance = (Instance) o;
		return Objects.equals(id, instance.id) && Objects.equals(baseUrl, instance.baseUrl) && Objects.equals(tokenUrl, instance.tokenUrl) && Objects.equals(username, instance.username) && Objects.equals(
			password, instance.password) && Objects.equals(connectTimeout, instance.connectTimeout) && Objects.equals(readTimeout, instance.readTimeout);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, baseUrl, tokenUrl, username, password, connectTimeout, readTimeout);
	}
}
