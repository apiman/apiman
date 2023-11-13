package io.apiman.plugins.transformation_policy.transformer;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

@SuppressWarnings("nls")
public class XmlToJsonTransformerTest {

	private XmlToJsonTransformer transformer = new XmlToJsonTransformer();

    @Test
    public void test_xmlToJson_1() throws Exception {
        test("xmlToJson-input1.xml", "xmlToJson-output1.json");
    }

	@Test
	public void test_xmlToJson_2() throws Exception {
        test("xmlToJson-input2.xml", "xmlToJson-output2.json");
	}

	@Test
	public void test_xmlToJson_3() throws Exception {
        test("xmlToJson-input3.xml", "xmlToJson-output3.json");
	}

	private void test(String xmlFileName, String jsonFileName) throws Exception {
		String xml = readFile(xmlFileName);
		String expectedJson = readFile(jsonFileName);
		String actualJson = transformer.transform(xml);
        assertThatJson(actualJson).isEqualTo(expectedJson);
	}

	private String readFile(String fileName) throws IOException {
		return IOUtils.toString(getClass().getClassLoader().getResource("xmlToJson/" + fileName), "UTF-8");
	}

}
