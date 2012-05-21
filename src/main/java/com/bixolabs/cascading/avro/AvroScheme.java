/**
 * Copyright 2010 TransPac Software, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bixolabs.cascading.avro;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericEnumSymbol;
import org.apache.avro.mapred.AvroInputFormat;
import org.apache.avro.mapred.AvroJob;
import org.apache.avro.mapred.AvroKey;
import org.apache.avro.mapred.AvroKeyComparator;
import org.apache.avro.mapred.AvroOutputFormat;
import org.apache.avro.mapred.AvroSerialization;
import org.apache.avro.mapred.AvroValue;
import org.apache.avro.mapred.AvroWrapper;
import org.apache.avro.util.Utf8;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.log4j.Logger;

import cascading.scheme.Scheme;
import cascading.scheme.SequenceFile;
import cascading.tap.Tap;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;

/**
 * The AvroScheme class is a {@link Scheme} subclass. It supports reading and
 * writing of data that has been serialized using Apache Avro.
 */
@SuppressWarnings("serial")
public class AvroScheme extends SequenceFile {
    public static final Class<?> ARRAY_CLASS = List.class;
    public static final Class<?> MAP_CLASS = Map.class;

    /**
     * Helper class used to save an Enum name in a type that Avro requires for
     * serialization.
     * 
     */
//    private static class CascadingEnumSymbol implements GenericEnumSymbol {
//
//        private String _name;
//        private Schema _schema;
//
//        public CascadingEnumSymbol(String name, Schema schema) {
//            _name = name;
//            _schema = schema;
//        }
//
//        @Override
//        public String toString() {
//            return _name;
//        }
//
//		public Schema getSchema() {
//			// TODO Auto-generated method stub
//			return null;
//		}
//    }

    private static final Logger LOGGER = Logger.getLogger(AvroScheme.class);
    private static final String RECORD_NAME = "CascadingAvroSchema";
    private String _recordName = RECORD_NAME;
    private Fields _schemeFields;
    
    private String _schemaString; 
    
//    private Class<?>[] _schemeTypes;
    private HashMap<Class<?>, Schema.Type> _typeMap = createTypeMap();

    private  transient Schema _schema;

    public AvroScheme(Fields schemeFields, Schema schema) {
        super(schemeFields);

//        validateFields(schemeFields, schema);

        _schemeFields = schemeFields;
        _schema = schema;
        _schemaString = schema.toString();
    }

    @SuppressWarnings( { "deprecation" })
    @Override
    public void sourceInit(Tap tap, JobConf conf) {
        conf.set(AvroJob.INPUT_SCHEMA, getSchema().toString());
        conf.setInputFormat(AvroInputFormat.class);

        // add AvroSerialization to io.serializations
//        	conf.set("io.serializations", conf.get("io.serializations")+","+AvroSerialization.class.getName() );
//        Collection<String> serializations = conf.getStringCollection("io.serializations");
//        if (!serializations.contains(AvroSerialization.class.getName())) {
//            serializations.add(AvroSerialization.class.getName()); 
////        if (!serializations.contains("cascading.kryo.KryoSerialization")) {
////            serializations.add("cascading.kryo.KryoSerialization"); }
//            conf.setStrings("io.serializations", serializations.toArray(new String[0]));
//        };
        

        LOGGER.info(String.format("Initializing Avro scheme for source tap - scheme fields: %s", _schemeFields));
    }

    @SuppressWarnings( { "deprecation" })
    @Override
    public void sinkInit(Tap tap, JobConf conf) {
        conf.set(AvroJob.OUTPUT_SCHEMA, getSchema().toString());
        conf.setOutputFormat(AvroOutputFormat.class);

        // Since we're outputting to Avro, we need to set up output values.
        // TODO KKr - why don't we need to set the OutputValueClass?
        // TODO KKr - why do we need to set the OutputKeyComparatorClass?
        conf.setOutputKeyClass(NullWritable.class);
        conf.setOutputValueClass(AvroWrapper.class);
        conf.setOutputKeyComparatorClass(AvroKeyComparator.class);
//        conf.setMapOutputKeyClass(AvroKey.class);
//        conf.setMapOutputValueClass(AvroValue.class);

        // add AvroSerialization to io.serializations
//        Collection<String> serializations = conf.getStringCollection("io.serializations");
//        if (!serializations.contains(AvroSerialization.class.getName())) {
//            serializations.add(AvroSerialization.class.getName());
//            conf.setStrings("io.serializations", serializations.toArray(new String[0]));
//        }

        // Class<? extends Mapper> mapClass = conf.getMapperClass();
        // Class<? extends Reducer> reduceClass = conf.getReducerClass();
        // AvroJob.setOutputSchema(conf, getSchema());
        // conf.setMapperClass(mapClass);
        // conf.setReducerClass(reduceClass);

        LOGGER.info(String.format("Initializing Avro scheme for sink tap - scheme fields: %s", _schemeFields));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Tuple source(Object key, Object ignore) {
        // Convert the AvroWrapper<T> key value to a tuple.
        Tuple result = new Tuple();

        Fields sourceFields = getSourceFields();

        // Unpack this datum into source tuple fields
        AvroWrapper<GenericData.Record> wrapper = (AvroWrapper<GenericData.Record>) key;
        GenericData.Record datum = wrapper.datum();

        for ( int i = 0; i < sourceFields.size(); i++) {
            String fieldName = sourceFields.get(i).toString();
            Object inObj = datum.get(fieldName);
            
//            if (curType == ARRAY_CLASS) {
//                typeIndex++;
//                result.add(convertFromAvroArray(inObj, _schemeTypes[typeIndex]));
            if (inObj instanceof Map) {
                result.add(convertFromAvroMap(inObj));
            } else {
                result.add(convertFromAvroPrimitive(inObj));
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void sink(TupleEntry tupleEntry, OutputCollector outputCollector) throws IOException {
        // Create the appropriate AvroWrapper<T> from the result, and pass that
        // as the key for the collect
        Fields sinkFields = getSinkFields();
        Tuple result = sinkFields != null ? tupleEntry.selectTuple(sinkFields) : tupleEntry.getTuple();
        Schema schema = getSchema();
        // Create a Generic data using the sink field names
        GenericData.Record datum = new GenericData.Record(schema);
       
        
        for ( int i = 0; i < sinkFields.size(); i++) {
            String fieldName = sinkFields.get(i).toString();
            Object inObj = result.get(i);
            Schema objSchema = schema.getField(fieldName).schema();
                datum.put(fieldName, convertToAvro(inObj, objSchema));
        }

        AvroWrapper<GenericData.Record> wrapper = new AvroWrapper<GenericData.Record>(datum);
        outputCollector.collect( NullWritable.get(), wrapper);
    }

    /**
     * Set the record name to be used when generating the Avro Schema. If there
     * are nested records, then the depth is added to the name (e.g. FooBar,
     * FooBar1...)
     * 
     * @param recordName
     */
    public void setRecordName(String recordName) {
        _recordName = recordName;
    }

    /**
     * 
     * @return the JSON representation of the Avro schema that will be generated
     */
    public String getJsonSchema() {
        // Since _schema is set up as transient generate the most current state
//        Schema schema = generateSchema(_schemeFields, _schemeTypes, 0);
        return _schema.toString();
    }

    /*
     * Cascading serializes the object and stashes it in the conf. Since Schema
     * isn't Serializable we work around it by making it a transient field and
     * getting to it at execution time.
     */
    private Schema getSchema() {
        if (_schema == null) {
            _schema = new Schema.Parser().parse(_schemaString);
        }

        return _schema;
    }

//    private Schema generateSchema(Fields schemeFields, Class<?>[] schemeTypes, int depth) {
//        // Create a 'record' that is made up of fields.
//        // Since we support arrays and maps that means we can have nested
//        // records
//
//        List<Schema.Field> fields = new ArrayList<Schema.Field>();
//        for (int typeIndex = 0, fieldIndex = 0; typeIndex < schemeTypes.length; typeIndex++, fieldIndex++) {
//            String fieldName = schemeFields.get(fieldIndex).toString();
//            Class<?>[] subSchemeTypes = new Class[2]; // at most 2, since we
//                                                      // only allow primitive
//                                                      // types for arrays and
//                                                      // maps
//            subSchemeTypes[0] = schemeTypes[typeIndex];
//            if ((schemeTypes[typeIndex] == ARRAY_CLASS) || (schemeTypes[typeIndex] == MAP_CLASS)) {
//                typeIndex++;
//                subSchemeTypes[1] = schemeTypes[typeIndex];
//            }
//
//            final Schema schema = createAvroSchema(schemeFields, subSchemeTypes, depth + 1);
//            final Schema nullSchema = Schema.create(Schema.Type.NULL);
//            List<Schema> schemas = new LinkedList<Schema>() {
//                {
//                    add(nullSchema);
//                    add(schema);
//                }
//            };
//
//            fields.add(new Schema.Field(fieldName, Schema.createUnion(schemas), "", null));
//        }
//
//        // Avro doesn't like anonymous records - so create a named one.
//        String recordName = _recordName;
//        if (depth > 0) {
//            recordName = recordName + depth;
//        }
//        Schema schema = Schema.createRecord(recordName, "auto generated", "", false);
//        schema.setFields(fields);
//        return schema;
//    }

//    @SuppressWarnings({ "static-access", "unchecked" })
//    private Schema createAvroSchema(Fields schemeFields, Class<?>[] fieldTypes, int depth) {
//        Schema.Type avroType = toAvroSchemaType(fieldTypes[0]);
//
//        if (avroType == Schema.Type.ARRAY) {
//            Class<?> arrayTypes[] = { fieldTypes[1] };
//            Schema schema = Schema.createArray(createAvroSchema(schemeFields.offsetSelector(schemeFields.size() - 1, 1), arrayTypes, depth + 1));
//            return schema;
//        } else if (avroType == Schema.Type.MAP) {
//            Class<?> mapTypes[] = { fieldTypes[1] };
//            return Schema.createMap(createAvroSchema(schemeFields.offsetSelector(schemeFields.size() - 1, 1), mapTypes, depth + 1));
//        } else if (avroType == Schema.Type.RECORD) {
//            return generateSchema(schemeFields.offsetSelector(schemeFields.size() - 1, 1), fieldTypes, depth + 1);
//        } else if (avroType == Schema.Type.ENUM) {
//            Class clazz = fieldTypes[0];
//            Object[] names = clazz.getEnumConstants();
//            List<String> enumNames = new ArrayList<String>(names.length);
//            for (Object name : names) {
//                enumNames.add(name.toString());
//            }
//
//            return Schema.createEnum(fieldTypes[0].getName(), null, null, enumNames);
//        } else {
//            return Schema.create(avroType);
//        }
//    }

    private Object convertFromAvroPrimitive(Object inObj) {
        if (inObj == null) {
            return null;
        } else if (inObj instanceof Utf8) {
            String convertedObj = ((Utf8) inObj).toString();
            return convertedObj;
        } else if (inObj instanceof BytesWritable) {
            // TODO KKr - this is very inefficient, since we make a copy of
            // the ByteBuffer array in the call to BytesWritable.set(buffer, pos, length).
            // A potentially small win is to check if buffer.position() == 0, and if
            // so then do result = new BytesWritable(buffer.array()), followed by
            // result.setSize(buffer.limit())
            ByteBuffer buffer = (ByteBuffer)inObj;
            BytesWritable result = new BytesWritable();
            result.set(buffer.array(), buffer.position(), buffer.limit());
            return result;
        } else if (inObj instanceof Enum) {
            return inObj.toString();
        } else {
            return inObj;
        }
    }

    private Object convertFromAvroArray(Object inObj) {
        
        if (inObj == null) {
            return null;
        }

        GenericData.Array<?> arr = (GenericData.Array<?>) inObj;
        Schema entrySchema = arr.getSchema().getElementType();
        
        List<Object> outputList = new ArrayList<Object>();
        for (Object obj : arr) {
            outputList.add(convertFromAvro(obj));
        }
        return outputList;
    }

    @SuppressWarnings("unchecked")
    private Object convertFromAvroMap(Object inObj) {
        if (inObj == null) {
            return null;
        }

        Map<Utf8, Object> inMap = (Map<Utf8, Object>) inObj;

        Map<String, Object> convertedMap = new HashMap<String, Object>();
        for (Map.Entry<Utf8, Object> e : inMap.entrySet()) {
            convertedMap.put(e.getKey().toString(), convertFromAvroPrimitive(e.getValue()));
        }
        return convertedMap;
    }

    private Object convertToAvroPrimitive(Object inObj) {
        if (inObj == null) {
            return null;
        } else if (inObj instanceof String) {
        		return new Utf8((String) inObj);
        } else if (inObj instanceof  BytesWritable) {
            BytesWritable bw = (BytesWritable) inObj;
            ByteBuffer convertedObj = ByteBuffer.wrap(bw.getBytes(), 0, bw.getLength());
            return convertedObj;
//        } else if (curType.isEnum()) {
//            Object result = new CascadingEnumSymbol((String) inObj);
//            return result;
        } else if (inObj instanceof Short) {
        		Short val = (Short) inObj;
        		return val.intValue();
        }
        	else {
            return inObj;
        }
    }    

    @SuppressWarnings("unchecked")
    private Object convertToAvroArray(Object inObj, Schema schema) {
        if (inObj == null) {
            return null;
        }
        else if (inObj instanceof List) {
        		List<Object> inList = (List<Object>) inObj;
        		if (inList.isEmpty()) {
        			return null;
        		}
        		
        		Schema entrySchema = schema.getElementType();
        		GenericData.Array arr = new GenericData.Array(inList.size(), schema);
        		for (Object listObj : inList) {
        			arr.add(convertToAvro(listObj, entrySchema));
        		}
        		return arr;
        }
        else return null;
    }

    private Object convertToAvro( Object inObj, Schema schema) {
    		Object retObj;
    		if (inObj instanceof List) {
    			retObj = convertToAvroArray(inObj, schema);
    		} else if (inObj instanceof Map ) {
    			retObj = convertToAvroMap(inObj, schema);
    		} else {
    			retObj = convertToAvroPrimitive(inObj);
    		}
    		return retObj;
    }
    
    private Object convertFromAvro( Object inObj) {
		Object retObj;
		if (inObj instanceof List) {
			retObj = convertFromAvroArray(inObj);
		} else if (inObj instanceof Map ) {
			retObj = convertFromAvroMap(inObj);
		} else {
			retObj = convertFromAvroPrimitive(inObj);
		}
		return retObj;
} 
    private Object convertToAvroMap(Object inObj, Schema schema) {
        if (inObj == null) {
            return null;
        }

        Map<String, Object> inMap = (Map<String, Object>) inObj;
        Schema valSchema = schema.getValueType();
        
        Map<Utf8, Object> convertedObj = new HashMap<Utf8, Object>();
        for (Map.Entry<String, Object> entry : inMap.entrySet()) {
        		Utf8 key = new Utf8(entry.getKey());
        		Object value = convertToAvro(entry.getValue(), valSchema);
        		convertedObj.put(key,  value);
        		
        		
        }
        return convertedObj;
    }

    private static HashMap<Class<?>, Schema.Type> createTypeMap() {
        HashMap<Class<?>, Schema.Type> typeMap = new HashMap<Class<?>, Schema.Type>();

        	typeMap.put(Short.class, Schema.Type.INT);
        typeMap.put(Integer.class, Schema.Type.INT);
        typeMap.put(Long.class, Schema.Type.LONG);
        typeMap.put(Boolean.class, Schema.Type.BOOLEAN);
        typeMap.put(Double.class, Schema.Type.DOUBLE);
        typeMap.put(Float.class, Schema.Type.FLOAT);
        typeMap.put(String.class, Schema.Type.STRING);
        typeMap.put(BytesWritable.class, Schema.Type.BYTES);

        // Note : Cascading field type for Array and Map is really a Tuple
        typeMap.put(ARRAY_CLASS, Schema.Type.ARRAY);
        typeMap.put(MAP_CLASS, Schema.Type.MAP);

        // TODO - the following Avro Schema.Types are not handled as yet
        // FIXED
        // RECORD
        // UNION

        return typeMap;
    }


    private Schema.Type toAvroSchemaType(Class<?> clazz) {
        if (_typeMap.containsKey(clazz)) {
            return _typeMap.get(clazz);
        } else if (clazz.isEnum()) {
            return Schema.Type.ENUM;
        } else {
            throw new UnsupportedOperationException("The class type " + clazz + " is currently unsupported");
        }
    }



    private boolean isValidArrayType(Class<?> arrayType) {
        // only primitive types are allowed for arrays

        if (arrayType == Boolean.class || arrayType == Integer.class || arrayType == Long.class || arrayType == Float.class || arrayType == Double.class || arrayType == String.class
                        || arrayType == BytesWritable.class)
            return true;

        return false;
    }

    private int getSchemeTypesSize(Class<?>[] schemeTypes) {
        int len = 0;

        for (int i = 0; i < schemeTypes.length; i++, len++) {
            if (schemeTypes[i] == ARRAY_CLASS || schemeTypes[i] == MAP_CLASS) {
                i++;
            }
        }
        return len;
    }

    // TODO - add hashcode and equals, once we have the fields settled.

    public static void addToTuple(Tuple t, byte[] bytes) {
        t.add(new BytesWritable(bytes));
    }

    @SuppressWarnings("unchecked")
    public static void addToTuple(Tuple t, Enum e) {
        t.add(e.toString());
    }

    public static void addToTuple(Tuple t, List<?> list) {
        Tuple listTuple = new Tuple();
        for (Object item : list) {
            listTuple.add(item);
        }

        t.add(listTuple);
    }

    public static void addToTuple(Tuple t, Map<String, ?> map) {
        Tuple mapTuple = new Tuple();
        for (String key : map.keySet()) {
            mapTuple.add(key);
            mapTuple.add(map.get(key));
        }

        t.add(mapTuple);
    }
}
