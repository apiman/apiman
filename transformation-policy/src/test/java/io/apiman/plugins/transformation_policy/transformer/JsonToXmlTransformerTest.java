package io.apiman.plugins.transformation_policy.transformer;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class JsonToXmlTransformerTest {

	private JsonToXmlTransformer transformer = new JsonToXmlTransformer();
	
    @Test
    public void test_jsonToXml_1() throws Exception {
        test("jsonToXml-input1.json", "jsonToXml-output1.xml");
    }

    @Test
    public void test_jsonToXml_2() throws Exception {
        test("jsonToXml-input2.json", "jsonToXml-output2.xml");
    }

    @Test
    public void test_jsonToXml_3() throws Exception {
        test("jsonToXml-input3.json", "jsonToXml-output3.xml");
    }

	private void test(String jsonFileName, String xmlFileName) throws Exception {
		String json = readFile(jsonFileName);
		String expectedXml = readFile(xmlFileName);
		
		String actualXml = transformer.transform(json);
		
		ObjectMapper mapper = new XmlMapper();
        JsonNode expectedXmlNode = mapper.readTree(expectedXml);
        JsonNode actualXmlNode = mapper.readTree(actualXml);
        assertTrue(expectedXmlNode.equals(actualXmlNode));
	}
	
	private String readFile(String fileName) throws IOException {
		return IOUtils.toString(getClass().getClassLoader().getResource("jsonToXml/" + fileName), "UTF-8");
	}
	
}
