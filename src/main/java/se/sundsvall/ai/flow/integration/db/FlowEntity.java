package se.sundsvall.ai.flow.integration.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "flow")
@IdClass(FlowEntityId.class)
public class FlowEntity {

	@Id
	@Column(name = "name")
	private String name;

	@Id
	@Column(name = "version")
	private Integer version;

	@Column(name = "content", columnDefinition = "text")
	private String content;

	public FlowEntity(String name, Integer version, String content) {
		this.name = name;
		this.version = version;
		this.content = content;
	}

	public FlowEntity() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public FlowEntity withName(String name) {
		this.name = name;
		return this;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public FlowEntity withVersion(Integer version) {
		this.version = version;
		return this;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public FlowEntity withContent(String content) {
		this.content = content;
		return this;
	}
}
