package com.fasterxml.jackson.dataformat.xml.unwrapped;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class TestNestedUnwrappedLists  extends XmlTestBase
{
    // // // Test

    static class ServiceDelivery {
        public Date responseTimestamp;
        public List<VehicleMonitoringDelivery> vehicleMonitoringDelivery;    
    }

    static class VehicleMonitoringDelivery {
        public Date responseTimestamp;
        public Date validUntil;
        public List<VehicleActivity> vehicleActivity;
    }

    static class VehicleActivity {
        public Date recordedAtTime;    
    }

    /*
    /**********************************************************************
    /* Unit tests
    /**********************************************************************
     */

    public void testNested1_2() throws Exception
    {
        JacksonXmlModule module = new JacksonXmlModule();
        module.setDefaultUseWrapper(false);
        XmlMapper mapper = new XmlMapper(module);
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.PascalCaseStrategy());
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
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
        
        ServiceDelivery svc = mapper.readValue(XML, ServiceDelivery.class);
        assertNotNull(svc);
        assertNotNull(svc.vehicleMonitoringDelivery);
        assertEquals(1, svc.vehicleMonitoringDelivery.size());
        VehicleMonitoringDelivery del = svc.vehicleMonitoringDelivery.get(0);
        assertNotNull(del);
        assertNotNull(del.vehicleActivity);
        assertEquals(2, del.vehicleActivity.size());
    }

    public void testNested2_1() throws Exception
    {
        JacksonXmlModule module = new JacksonXmlModule();
        module.setDefaultUseWrapper(false);
        XmlMapper mapper = new XmlMapper(module);
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.PascalCaseStrategy());
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
        
        ServiceDelivery svc = mapper.readValue(XML, ServiceDelivery.class);
        assertNotNull(svc);
        assertNotNull(svc.vehicleMonitoringDelivery);
        assertEquals(2, svc.vehicleMonitoringDelivery.size());
        VehicleMonitoringDelivery del = svc.vehicleMonitoringDelivery.get(1);
        assertNotNull(del);
        assertNull(del.vehicleActivity);
    }
}
