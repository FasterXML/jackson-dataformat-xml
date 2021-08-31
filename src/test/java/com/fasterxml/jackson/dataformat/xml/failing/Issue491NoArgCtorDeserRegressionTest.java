package com.fasterxml.jackson.dataformat.xml.failing;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Reproduces <i>no default no-arg ctor found</i> deserialization regression introduced to {@link XmlMapper} in 2.12.0.
 *
 * @see <a href="https://github.com/FasterXML/jackson-dataformat-xml/issues/491">jackson-dataformat-xml issue 491</a>
 */
public class Issue491NoArgCtorDeserRegressionTest extends XmlTestBase {

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
        DefaultProblem(String type, Integer status) {
            this.type = type != null ? type : Problem.DEFAULT_TYPE;
            this.status = status != null ? status : Problem.DEFAULT_STATUS;
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public int getStatus() {
            return status;
        }

    }

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.EXISTING_PROPERTY,
            property = "type",
            defaultImpl = DefaultProblem.class,
            visible = true)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonRootName("problem")
    interface ProblemMixIn extends Problem {

        @Override
        @JsonProperty("type")
        String getType();

        @Override
        @JsonProperty("status")
        int getStatus();

    }

    abstract static class DefaultProblemMixIn extends DefaultProblem {

        @JsonCreator
        DefaultProblemMixIn(
                @JsonProperty("type") String type,
                @JsonProperty("status") Integer status) {
            super(type, status);
            throw new IllegalStateException(
                    "mix-in constructor is there only for extracting the JSON mapping, " +
                            "it should not have been called");
        }

    }

    static class ProblemModule extends SimpleModule {

        @Override
        public void setupModule(SetupContext context) {
            super.setupModule(context);
            registerMixIns(context);
        }

        private static void registerMixIns(SetupContext context) {
            context.setMixInAnnotations(DefaultProblem.class, DefaultProblemMixIn.class);
            context.setMixInAnnotations(Problem.class, ProblemMixIn.class);
        }

    }

    private static final ProblemModule MODULE = new ProblemModule();

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper().registerModule(MODULE);

    private static final XmlMapper XML_MAPPER = (XmlMapper) new XmlMapper().registerModule(MODULE);

    /**
     * Passes on 2.11.4 and 2.12.{0..4}.
     */
    public void test_empty_Problem_JSON_deserialization() throws IOException {
        byte[] problemJsonBytes = "{}".getBytes(StandardCharsets.UTF_8);
        Problem problem = JSON_MAPPER.readValue(problemJsonBytes, Problem.class);
        assertEquals(Problem.DEFAULT_TYPE, problem.getType());
        assertEquals(Problem.DEFAULT_STATUS, problem.getStatus());
    }

    /**
     * Passes on 2.11.4, but fails on 2.12.{0..4}.
     */
    public void test_empty_Problem_XML_deserialization() throws IOException {
        byte[] problemXmlBytes = "<problem/>".getBytes(StandardCharsets.UTF_8);
        Problem problem = XML_MAPPER.readValue(problemXmlBytes, Problem.class);
        assertEquals(Problem.DEFAULT_TYPE, problem.getType());
        assertEquals(Problem.DEFAULT_STATUS, problem.getStatus());
    }

}
