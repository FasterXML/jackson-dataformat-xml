package com.fasterxml.jackson.dataformat.xml.misc;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestUtil;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// for [dataformat-xml#286]: parser getting confused with unwrapped lists,
// object id
public class UnwrappedJsonIdentityConflict286Test extends XmlTestUtil
{
    static class Town
    {
        public School[] schools;
        public Student[] students;
    }

    static class Student
    {
        public School school;
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "year")
        public Year[] years;
    }

    static class Year
    {
        public String grade;
    }

    @JsonIdentityInfo(scope = School.class, generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "name")
    static class School
    {
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "staffMember")
        public String[] staffMembers;
        public String name;
    }

    @Test
    public void testCaseInsensitiveComplex() throws Exception
    {

        XmlMapper mapper = XmlMapper.builder().build();

        final String input =
"<town>\n"+
"  <schools>\n"+
"    <school name=\"Springfield Elementary\">\n"+
"      <staffMember>Principal Skinner</staffMember>\n"+
"      <staffMember>Groundskeeper Willie</staffMember>\n"+
"    </school>\n"+
"  </schools>\n"+
"  <students>\n"+
"    <student>\n"+
"      <school>Springfield Elementary</school>\n"+
"      <year>\n"+
"        <grade>3</grade>\n"+
"      </year>\n"+
"    </student>\n"+
"  </students>\n"+
"</town>";
        Town result = mapper.readValue(input, Town.class);
        assertNotNull(result);
        assertEquals("Principal Skinner", result.schools[0].staffMembers[0]);
        assertEquals("Principal Skinner", result.students[0].school.staffMembers[0]);
        assertEquals("3", result.students[0].years[0].grade);
    }
}
