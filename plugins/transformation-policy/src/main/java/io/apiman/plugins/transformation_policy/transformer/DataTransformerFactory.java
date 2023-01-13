package io.apiman.plugins.transformation_policy.transformer;

import io.apiman.plugins.transformation_policy.beans.DataFormat;

import java.util.HashMap;
import java.util.Map;

public class DataTransformerFactory {

    private static final Map<DataFormat, Map<DataFormat, DataTransformer>> dataTransformers = new HashMap<>();
    
    static {
        dataTransformers.put(DataFormat.JSON, new HashMap<DataFormat, DataTransformer>());
        dataTransformers.get(DataFormat.JSON).put(DataFormat.XML, new JsonToXmlTransformer());
        
        dataTransformers.put(DataFormat.XML, new HashMap<DataFormat, DataTransformer>());
        dataTransformers.get(DataFormat.XML).put(DataFormat.JSON, new XmlToJsonTransformer());
    }
    
    public static DataTransformer getDataTransformer(DataFormat inputFormat, DataFormat outputFormat) {
        return dataTransformers.get(inputFormat).get(outputFormat);
    }
    
}
