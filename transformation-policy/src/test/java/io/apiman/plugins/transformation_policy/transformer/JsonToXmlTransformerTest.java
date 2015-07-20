package io.apiman.plugins.transformation_policy.transformer;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;

@SuppressWarnings("nls")
public class JsonToXmlTransformerTest extends XMLTestCase {

    static {
        XMLUnit.setIgnoreWhitespace(true);
    }

	private JsonToXmlTransformer transformer = new JsonToXmlTransformer();

    public void test_jsonToXml_1() throws Exception {
        test("jsonToXml-input1.json", "jsonToXml-output1.xml");
    }

    public void test_jsonToXml_2() throws Exception {
        test("jsonToXml-input2.json", "jsonToXml-output2.xml");
    }

    public void test_jsonToXml_3() throws Exception {
        test("jsonToXml-input3.json", "jsonToXml-output3.xml");
    }

    public void test_jsonToXml_4() throws Exception {
        test("jsonToXml-input4.json", "jsonToXml-output4.xml");
    }

	private void test(String jsonFileName, String xmlFileName) throws Exception {
		String json = readFile(jsonFileName);
		String expectedXml = readFile(xmlFileName);

		String actualXml = transformer.transform(json);

		assertXMLEqual(expectedXml, actualXml);
	}

	private String readFile(String fileName) throws IOException {
		return IOUtils.toString(getClass().getClassLoader().getResource("jsonToXml/" + fileName), "UTF-8");
	}

}
