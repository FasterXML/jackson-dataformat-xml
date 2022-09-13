package com.fasterxml.jackson.dataformat.xml.deser.creator;

import com.fasterxml.jackson.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

/**
 * Reproduces <i>no default no-arg ctor found</i> deserialization regression
 * introduced to {@link XmlMapper} in 2.12.0.
 *
 * @see <a href="https://github.com/FasterXML/jackson-dataformat-xml/issues/491">jackson-dataformat-xml issue 491</a>
 *<p>
 * The underlying problem is due to the empty root element being recognized as a String
 * token (for consistency with how XML is mapper to tokens); this leads to deserialization
 * attempting to use "empty Object" construction which expects availability of the
 * default constructor.
 *<p>
 * Jackson 2.14 solved this issue by reverting earlier changes so that empty element
 * is exposed as simple START_OBJECT/END_OBJECT sequence (that is, empty Object).
 * Use cases where (non-empty) element needs to map to Scalar types is now handled
 * with mechanism introduced in 2.13.
 */
public class NoArgCtorDeser491Test extends XmlTestBase
{
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.EXISTING_PROPERTY,
            property = "type",
            defaultImpl = DefaultProblem.class,
            visible = true)
    @JsonRootName("problem")
    interface Problem {
        String DEFAULT_TYPE = "about:blank";
        int DEFAULT_STATUS = 500;
        String getType();
        int getStatus();
    }

    static class DefaultProblem implements Problem {

        private final String type;

        private final int status;

        /**
         * This is required to workaround Jackson's missing support for static
         * {@link JsonCreator}s in mix-ins. That is, we need to define the
         * creator on a constructor in the mix-in that is matching with a
         * constructor here too.
         *
         * @see <a href="https://github.com/FasterXML/jackson-databind/issues/1820">jackson-databind issue 1820</a>
         */
        @JsonCreator
        DefaultProblem(@JsonProperty("type") String type, @JsonProperty("status") Integer status) {
            this.type = type != null ? type : Problem.DEFAULT_TYPE;
            this.status = status != null ? status : Problem.DEFAULT_STATUS;
        }

        // Adding this would work around the issue
//        DefaultProblem() {
//            this(null, null);
//        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public int getStatus() {
            return status;
        }
    }

    private static final ObjectMapper JSON_MAPPER = new JsonMapper();

    private static final XmlMapper XML_MAPPER = newMapper();

    /**
     * Passes on 2.11.4 and 2.12.{0..4}.
     */
    public void test_empty_Problem_JSON_deserialization() throws Exception
    {
        Problem problem = JSON_MAPPER.readValue("{}", Problem.class);
        assertEquals(Problem.DEFAULT_TYPE, problem.getType());
        assertEquals(Problem.DEFAULT_STATUS, problem.getStatus());
    }

    /**
     * Passes on 2.11.4, but fails on 2.12.{0..4}.
     */
    public void test_empty_Problem_XML_deserialization() throws Exception
    {
        Problem problem = XML_MAPPER.readValue(
                "<problem />",
                Problem.class);
        assertEquals(Problem.DEFAULT_TYPE, problem.getType());
        assertEquals(Problem.DEFAULT_STATUS, problem.getStatus());

        // Also ensure variations of empty do not vary
        problem = XML_MAPPER.readValue(
                "<problem>\n</problem>",
                Problem.class);
        assertEquals(Problem.DEFAULT_TYPE, problem.getType());
        assertEquals(Problem.DEFAULT_STATUS, problem.getStatus());
    }
}
