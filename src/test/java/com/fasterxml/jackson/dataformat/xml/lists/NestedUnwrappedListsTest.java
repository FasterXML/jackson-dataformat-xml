package com.fasterxml.jackson.dataformat.xml.lists;

import java.util.List;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class NestedUnwrappedListsTest  extends XmlTestBase
{
    // // // Test

    static class ServiceDelivery {
        public String responseTimestamp;
        public List<VehicleMonitoringDelivery> vehicleMonitoringDelivery;    
    }

    static class VehicleMonitoringDelivery {
        public String responseTimestamp;
        public String validUntil;
        public List<VehicleActivity> vehicleActivity;
    }

    static class VehicleActivity {
        public String recordedAtTime;    
    }

    /*
    /**********************************************************************
    /* Set up
    /**********************************************************************
     */

    protected XmlMapper _xmlMapper = mapperBuilder()
            .propertyNamingStrategy(new PropertyNamingStrategies.UpperCamelCaseStrategy())
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .defaultUseWrapper(false)
            .build();

    /*
    /**********************************************************************
    /* Unit tests
    /**********************************************************************
     */

    public void testNested1_2() throws Exception
    {
        final String XML =
"<ServiceDelivery>\n"
+"  <ResponseTimestamp>2012-09-12T09:28:17.213-04:00</ResponseTimestamp>\n"
+"  <VehicleMonitoringDelivery>\n"
+"    <ResponseTimestamp>2012-09-12T09:28:17.213-04:00</ResponseTimestamp>\n"
+"    <ValidUntil>2012-09-12T09:29:17.213-04:00</ValidUntil>\n"
+"    <VehicleActivity>\n"
+"      <RecordedAtTime>2012-09-12T09:28:07.536-04:00</RecordedAtTime>\n"
+"    </VehicleActivity>\n"
+"    <VehicleActivity>\n"
+"      <RecordedAtTime>2013-09-12T09:29:07.536-04:00</RecordedAtTime>\n"
+"    </VehicleActivity>\n"
+"  </VehicleMonitoringDelivery>\n"
+"</ServiceDelivery>\n"
                ;
        
        ServiceDelivery svc = _xmlMapper.readValue(XML, ServiceDelivery.class);
        assertNotNull(svc);
        assertNotNull(svc.vehicleMonitoringDelivery);
        assertEquals(1, svc.vehicleMonitoringDelivery.size());
        VehicleMonitoringDelivery del = svc.vehicleMonitoringDelivery.get(0);
        assertEquals("2012-09-12T09:28:17.213-04:00", del.responseTimestamp);
        assertNotNull(del);
        assertNotNull(del.vehicleActivity);
        assertEquals(2, del.vehicleActivity.size());
        VehicleActivity act = del.vehicleActivity.get(1);
        assertNotNull(act);
        assertEquals("2013-09-12T09:29:07.536-04:00", act.recordedAtTime);
    }

    public void testNestedWithEmpty() throws Exception
    {
        final String XML =
"<ServiceDelivery>\n"
+"  <ResponseTimestamp>2012-09-12T09:28:17.213-04:00</ResponseTimestamp>\n"
+"  <VehicleMonitoringDelivery>\n"
+"  </VehicleMonitoringDelivery>\n"
+"</ServiceDelivery>\n"
                ;
        
        ServiceDelivery svc = _xmlMapper.readValue(XML, ServiceDelivery.class);
        assertNotNull(svc);
        assertNotNull(svc.vehicleMonitoringDelivery);
        // 06-Jan-2015, tatu: Really should have one empty entry, not 0
        assertEquals(1, svc.vehicleMonitoringDelivery.size());
    }

    public void testNestedWithEmpty2() throws Exception
    {
        final String XML =
"<ServiceDelivery>\n"
+"  <ResponseTimestamp>2012-09-12T09:28:17.213-04:00</ResponseTimestamp>\n"
+"  <VehicleMonitoringDelivery>\n"
+"    <VehicleActivity>\n"
+"    </VehicleActivity>\n"
+"  </VehicleMonitoringDelivery>\n"
+"</ServiceDelivery>\n"
                ;
        
        ServiceDelivery svc = _xmlMapper.readValue(XML, ServiceDelivery.class);
        assertNotNull(svc);
        assertNotNull(svc.vehicleMonitoringDelivery);
        assertEquals(1, svc.vehicleMonitoringDelivery.size());
        VehicleMonitoringDelivery del = svc.vehicleMonitoringDelivery.get(0);
        assertNotNull(del.vehicleActivity);
        // 06-Jan-2015, tatu: Really should have one empty entry, not 0
        assertEquals(1, del.vehicleActivity.size());
    }
    
    public void testNested1_2b() throws Exception
    {
        final String XML =
"<ServiceDelivery>\n"
+"  <ResponseTimestamp>2012-09-12T09:28:17.213-04:00</ResponseTimestamp>\n"
+"  <VehicleMonitoringDelivery>\n"
+"    <VehicleActivity>\n"
+"      <RecordedAtTime>2012-09-12T09:28:07.536-04:00</RecordedAtTime>\n"
+"    </VehicleActivity>\n"
+"    <VehicleActivity>\n"
+"      <RecordedAtTime>2013-09-12T09:29:07.536-04:00</RecordedAtTime>\n"
+"    </VehicleActivity>\n"
+"    <ResponseTimestamp>2012-09-12T09:28:17.213-04:00</ResponseTimestamp>\n"
+"    <ValidUntil>2012-09-12T09:29:17.213-04:00</ValidUntil>\n"
+"  </VehicleMonitoringDelivery>\n"
+"</ServiceDelivery>\n"
                ;
        
        ServiceDelivery svc = _xmlMapper.readValue(XML, ServiceDelivery.class);
        assertNotNull(svc);
        assertEquals("2012-09-12T09:28:17.213-04:00", svc.responseTimestamp);
        assertNotNull(svc.vehicleMonitoringDelivery);
        assertEquals(1, svc.vehicleMonitoringDelivery.size());
        VehicleMonitoringDelivery del = svc.vehicleMonitoringDelivery.get(0);
        assertEquals("2012-09-12T09:29:17.213-04:00", del.validUntil);
        assertNotNull(del);
        assertNotNull(del.vehicleActivity);
        assertEquals(2, del.vehicleActivity.size());
        VehicleActivity act = del.vehicleActivity.get(1);
        assertNotNull(act);
        assertEquals("2013-09-12T09:29:07.536-04:00", act.recordedAtTime);
    }

    public void testNested2_1() throws Exception
    {
        final String XML =
"<ServiceDelivery>\n"
+"  <ResponseTimestamp>2012-09-12T09:28:17.213-04:00</ResponseTimestamp>\n"
+"  <VehicleMonitoringDelivery>\n"
+"    <ResponseTimestamp>2012-09-12T09:28:17.213-04:00</ResponseTimestamp>\n"
+"    <ValidUntil>2012-09-12T09:29:17.213-04:00</ValidUntil>\n"
+"    <VehicleActivity>\n"
+"      <RecordedAtTime>2012-09-12T09:28:07.536-04:00</RecordedAtTime>\n"
+"    </VehicleActivity>\n"
+"  </VehicleMonitoringDelivery>\n"
+"  <VehicleMonitoringDelivery>\n"
+"    <ResponseTimestamp>2012-09-12T09:28:17.213-04:00</ResponseTimestamp>\n"
+"    <ValidUntil>2012-09-12T09:29:17.213-04:00</ValidUntil>\n"
+"    <VehicleActivity>\n"
+"      <RecordedAtTime>2012-09-12T09:28:07.536-04:00</RecordedAtTime>\n"
+"    </VehicleActivity>\n"
+"  </VehicleMonitoringDelivery>\n"
+"</ServiceDelivery>\n"
                ;
        
        ServiceDelivery svc = _xmlMapper.readValue(XML, ServiceDelivery.class);
        assertNotNull(svc);
        assertEquals("2012-09-12T09:28:17.213-04:00", svc.responseTimestamp);
        assertNotNull(svc.vehicleMonitoringDelivery);
        assertEquals(2, svc.vehicleMonitoringDelivery.size());
        VehicleMonitoringDelivery del = svc.vehicleMonitoringDelivery.get(1);
        assertNotNull(del);
        assertNotNull(del.vehicleActivity);
        assertEquals(1, del.vehicleActivity.size());
        assertEquals("2012-09-12T09:28:07.536-04:00", del.vehicleActivity.get(0).recordedAtTime);
    }
}
