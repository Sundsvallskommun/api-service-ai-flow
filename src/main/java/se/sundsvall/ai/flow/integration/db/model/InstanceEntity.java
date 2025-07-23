package se.sundsvall.ai.flow.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "instance", indexes = {
	@Index(name = "idx_municipality_id", columnList = "municipality_id")
})
public class InstanceEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Column(name = "municipality_id")
	private String municipalityId;

	@Column(name = "base_url")
	private String baseUrl;

	@Column(name = "username")
	private String username;

	@Column(name = "password")
	private String password;

	@Column(name = "connect_timeout")
	private int connectTimeout;

	@Column(name = "read_timeout")
	private int readTimeout;

	public static InstanceEntity create() {
		return new InstanceEntity();
	}

	public InstanceEntity withId(final String id) {
		this.id = id;
		return this;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public InstanceEntity withMunicipalityId(final String municipalityId) {
		this.municipalityId = municipalityId;
		return this;
	}

	public String getMunicipalityId() {
		return municipalityId;
	}

	public void setMunicipalityId(String municipalityId) {
		this.municipalityId = municipalityId;
	}

	public InstanceEntity withBaseUrl(final String baseUrl) {
		this.baseUrl = baseUrl;
		return this;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public InstanceEntity withUsername(final String username) {
		this.username = username;
		return this;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public InstanceEntity withPassword(final String password) {
		this.password = password;
		return this;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public InstanceEntity withConnectTimeout(final int connectTimeout) {
		this.connectTimeout = connectTimeout;
		return this;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public InstanceEntity withReadTimeout(final int readTimeout) {
		this.readTimeout = readTimeout;
		return this;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	@Override
	public String toString() {
		return "InstanceEntity{" +
			"id='" + id + '\'' +
			", municipalityId='" + municipalityId + '\'' +
			", baseUrl='" + baseUrl + '\'' +
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
		InstanceEntity that = (InstanceEntity) o;
		return connectTimeout == that.connectTimeout && readTimeout == that.readTimeout && Objects.equals(id, that.id) && Objects.equals(municipalityId, that.municipalityId) && Objects.equals(baseUrl, that.baseUrl)
			&& Objects.equals(username, that.username) && Objects.equals(password, that.password);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, municipalityId, baseUrl, username, password, connectTimeout, readTimeout);
	}
}
