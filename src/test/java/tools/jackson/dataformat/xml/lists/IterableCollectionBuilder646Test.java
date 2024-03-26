package tools.jackson.dataformat.xml.lists;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import tools.jackson.databind.annotation.JsonDeserialize;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestBase;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

// [dataformat-xml#646]
public class IterableCollectionBuilder646Test extends XmlTestBase
{
	@JsonDeserialize(builder = Parent.Builder.class)
	@JacksonXmlRootElement(localName = "parent")
	static class Parent {
		private final List<Child> children;

		Parent(List<Child> children) {
			this.children = children;
		}

		@JsonProperty("child")
		@JacksonXmlElementWrapper(useWrapping = false)
		public List<Child> getChildren() {
			return children;
		}

		static class Builder {
			private final List<Child> children = new ArrayList<>();

			@JsonProperty("child")
			@JacksonXmlElementWrapper(useWrapping = false)
			public Builder children(Iterable<Child> children) {
				for (Child c : children) {
					this.children.add(c);
				}
				return this;
			}

			public Parent build() {
				return new Parent(children);
			}
		}
	}

	@JsonDeserialize(builder = Child.Builder.class)
	@JacksonXmlRootElement(localName = "child")
	static class Child {
		private final String id;

		public Child(String id) {
			this.id = id;
		}

		@JsonProperty("id")
		public String getId() {
			return id;
		}

		static class Builder {
			private String id;

			@JsonProperty("id")
			public Builder id(String id) {
				this.id = id;
				return this;
			}

			public Child build() {
				return new Child(id);
			}
		}
	}

	// -- Test Methods --//
	private final XmlMapper MAPPER = newMapper();

	public void testIssue646() throws Exception {
		final String XML = "<parent><child><id>1</id></child></parent>";
		Parent parent = MAPPER.readValue(XML, Parent.class);
		assertNotNull(parent);
		assertNotNull(parent.getChildren());
		assertEquals(1, parent.getChildren().size());
	}
}
