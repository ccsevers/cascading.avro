package com.bixolabs.cascading.avro;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.mapred.JobConf;
import org.junit.Before;
import org.junit.Test;

import cascading.flow.Flow;
import cascading.flow.FlowConnector;
import cascading.pipe.Pipe;
import cascading.scheme.SequenceFile;
import cascading.tap.Hfs;
import cascading.tap.Lfs;
import cascading.tap.SinkMode;
import cascading.tap.Tap;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;
import cascading.tuple.TupleEntryCollector;
import cascading.tuple.TupleEntryIterator;
import cascading.kryo.*;

public class AvroSchemeTest {

    private static final String OUTPUT_DIR = "build/test/AvroSchmeTest/"; 
    private static final Schema CHECK_SCHEMA = new Schema.Parser().parse(
    	      "{" +
    	      "  \"type\": \"record\"," +
    	      "  \"name\": \"WeatherRecord\"," +
    	      "  \"doc\": \"A weather reading.\"," +
    	      "  \"fields\": [" +
    	      "    {\"name\": \"year\", \"type\": \"int\"}," +
    	      "    {\"name\": \"temperature\", \"type\": \"int\"}," +
    	      "    {\"name\": \"stationId\", \"type\": \"string\"}" +
    	      "  ]" +
    	      "}"
    	  );
    private static final Schema ROUNDTRIP_SCHEMA = new Schema.Parser().parse(
  	      "{" +
  	      "  \"type\": \"record\"," +
  	      "  \"name\": \"RoundTripTestRecord\"," +
  	      "  \"doc\": \"For a round trip test\"," +
  	      "  \"fields\": [" +
  	      "    {\"name\": \"integerField\", \"type\": \"int\"}," +
  	      "    {\"name\": \"longField\", \"type\": \"long\"}," +
  	      "    {\"name\": \"booleanField\", \"type\": \"boolean\"}," +
  	      "    {\"name\": \"doubleField\", \"type\": \"double\"}," +
  	      "    {\"name\": \"floatField\", \"type\": \"float\"}," +
  	      "    {\"name\": \"stringField\", \"type\": \"string\"}," +
  	      "    {\"name\": \"bytesField\", \"type\": \"bytes\"}," +
  	      "    {\"name\": \"arrayOfLongsField\", \"type\": { \"type\":\"array\", \"items\":\"long\"}}," +
  	      "    {\"name\": \"mapOfStringsField\", \"type\": {\"type\":\"map\", \"values\":\"string\"}}" +
  	      "  ]" +
  	      "}"
  	  );
    private static final Schema NULLSTRING_SCHEMA = new Schema.Parser().parse(
    	      "{" +
    	      "  \"type\": \"record\"," +
    	      "  \"name\": \"NullStringRecord\"," +
    	      "  \"doc\": \"For a null string test\"," +
    	      "  \"fields\": [" +
    	      "    {\"name\": \"nullString\", \"type\":[\"null\",\"string\"]}" +
    	      "  ]" +
    	      "}"
    	  );
//	private static final Schema RECORDNAME_SCHEMA = new Schema.Parser().parse(
//  	      "{" +
//  	      "  \"type\": \"record\"," +
//  	      "  \"name\": \"SchemaNameTest\"," +
//  	      "  \"doc\": \"For a null string test\"," +
//  	      "  \"fields\": [" +
//  	      "    {\"name\": \"nullString\", \"type\": [\"null\",\"string\"]}" +
//  	      "  ]" +
//  	      "}"
//  	  );
//    private static enum TestE {
//        ONE,
//        TWO
//    }

    @Before
    public void setup() throws IOException {
        File outputDir = new File(OUTPUT_DIR);
        if (outputDir.exists()) {
            FileUtils.deleteDirectory(outputDir);
        }
    }
    
//    @Test
//    public void testSchemeChecks() {
//    		
//        try {
//            new AvroScheme(new Fields("a", "b"), CHECK_SCHEMA );
//        } catch (Exception e) {
//            fail("Exception should not be thrown - this is the valid case");
//        }
//
//        try {
//            new AvroScheme(new Fields(), CHECK_SCHEMA);
//            fail("Exception should be thrown when scheme field is empty");
//        } catch (Exception e) {
//        }

//        try {
//            new AvroScheme(new Fields("a", "b", "c"), new Class[] { Integer.class });
//            fail("Exception should be thrown as there are more fields defined than types");
//        } catch (Exception e) {
//        }
//
//        try {
//            new AvroScheme(new Fields("a"), new Class[] { Integer.class, String.class });
//            fail("Exception should be thrown as there are more types defined than fields");
//        } catch (Exception e) {
//        }
//
//        try {
//            new AvroScheme(new Fields("array"), new Class[] { List.class, Long.class });
//        } catch (Exception e) {
//            fail("Exception shouldn't be thrown as array type is valid");
//        }
//
//        try {
//            new AvroScheme(new Fields("array"), new Class[] { List.class, List.class });
//            fail("Exception should be thrown as array type isn't a primitive");
//        } catch (Exception e) {
//        }

//    }

    
//    @SuppressWarnings("serial")
//    @Test
//    public void testRoundTrip() throws Exception {
//        
//        // Create a scheme that tests each of the supported types
//
//        final Fields testFields = new Fields("integerField", "longField", "booleanField", "doubleField", "floatField", 
//                        "stringField", "bytesField", "arrayOfLongsField", "mapOfStringsField"); //, "enumField");
////        final Class<?>[] schemeTypes = {Integer.class, Long.class, Boolean.class, Double.class, Float.class, 
////                        String.class, BytesWritable.class, List.class, Long.class, Map.class, String.class }; //TestE.class};
//        final String in = OUTPUT_DIR+ "testRoundTrip/in";
//        final String out = OUTPUT_DIR + "testRoundTrip/out";
//        final String verifyout = OUTPUT_DIR + "testRoundTrip/verifyout";
//        
//        final int numRecords = 2;
//        
//        // Create a sequence file with the appropriate tuples
//        Lfs lfsSource = new Lfs(new SequenceFile(testFields), in, SinkMode.REPLACE);
//        TupleEntryCollector write = lfsSource.openForWrite(new JobConf());
//        Tuple t = new Tuple();
//        t.add(0);
//        t.add(0L);
//        t.add(false);
//        t.add(0.0d);
//        t.add(0.0f);
//        t.add("0");
//        AvroScheme.addToTuple(t, new byte[] {0});
//        
//        List<Long> arrayOfLongs = new ArrayList<Long>() {{
//            add(0L);
//        }};
//        t.add(arrayOfLongs);
//
//        Map<String, String> mapOfStrings = new HashMap<String, String>() {{
//            put("key-0", "value-0");
//        }};
//        t.add(mapOfStrings);
//        
//        //AvroScheme.addToTuple(t, TestE.ONE);
//        write.add(t);
//
//        t = new Tuple();
//        t.add(1);
//        t.add(1L);
//        t.add(true);
//        t.add(1.0d);
//        t.add(1.0f);
//        t.add("1");
//        AvroScheme.addToTuple(t, new byte[]{0, 1});
//        List<Long> secondListofLongs = new LinkedList<Long>() {{
//        		add(0L);
//        		add(1L);
//        }};
//        t.add(secondListofLongs);
//        Map<String, String> secondMapOfStrings = new HashMap<String, String>() {{
//            put("key-0", "value-0");
//            put("key-1", "value-1");
//        }};
//        t.add(secondMapOfStrings);
//        //AvroScheme.addToTuple(t, TestE.TWO);
//        write.add(t);
//
//        write.close();
//
//        // Now read from the results, and write to an Avro file.
//        Pipe writePipe = new Pipe("tuples to avro");
//
//        Tap avroSink = new Lfs(new AvroScheme(testFields, ROUNDTRIP_SCHEMA), out);
//        Flow flow = new FlowConnector().connect(lfsSource, avroSink, writePipe);
//        flow.complete();
//        
//        // Now read it back in, and verify that the data/types match up.
//        Tap avroSource = new Lfs(new AvroScheme(testFields, ROUNDTRIP_SCHEMA), out);
//        Pipe readPipe = new Pipe("avro to tuples");
//        Tap verifySink = new Hfs(new SequenceFile(testFields), verifyout, SinkMode.REPLACE);
//
//        Flow readFlow = new FlowConnector().connect(avroSource, verifySink, readPipe);
//        JobConf jobConf = readFlow.getJobConf();
//        String iosers = jobConf.get("io.serializations");
//        jobConf.set("io.serializations", iosers+ ",cascading.kryo.KryoSerialization");
//        readFlow.complete();
//
//        TupleEntryIterator sinkTuples = verifySink.openForRead(new JobConf());
//        assertTrue(sinkTuples.hasNext());
//        
//        int i = 0;
//        while (sinkTuples.hasNext()) {
//            TupleEntry te = sinkTuples.next();
//            
//            assertTrue(te.get("integerField") instanceof Integer);
//            assertTrue(te.get("longField") instanceof Long);
//            assertTrue(te.get("booleanField") instanceof Boolean);
//            assertTrue(te.get("doubleField") instanceof Double);
//            assertTrue(te.get("floatField") instanceof Float);
//            assertTrue(te.get("stringField") instanceof String);
//            assertTrue(te.get("bytesField") instanceof BytesWritable);
//            assertTrue(te.get("arrayOfLongsField") instanceof List);
//            assertTrue(te.get("mapOfStringsField") instanceof Map);
//            //assertTrue(te.get("enumField") instanceof String);
//
//            assertEquals(i, te.getInteger("integerField"));
//            assertEquals(i, te.getLong("longField"));
//            assertEquals(i > 0, te.getBoolean("booleanField"));
//            assertEquals((double)i, te.getDouble("doubleField"), 0.0001);
//            assertEquals((float)i, te.getFloat("floatField"), 0.0001);
//            assertEquals("" + i, te.getString("stringField"));
//            //assertEquals(i == 0 ? TestE.ONE : TestE.TWO, TestE.valueOf(te.getString("enumField")));
//            
//            int bytesLength = ((BytesWritable)te.get("bytesField")).getLength();
//            byte[] bytes = ((BytesWritable)te.get("bytesField")).getBytes();
//            assertEquals(i + 1, bytesLength);
//            for (int j = 0; j < bytesLength; j++) {
//                assertEquals(j, bytes[j]);
//            }
//            
//            List<Long> longArray = (List<Long>) te.get("arrayOfLongsField");
//            assertEquals(i + 1, longArray.size());
//            for (int j = 0; j < longArray.size(); j++) {
//                assertTrue(longArray.get(j) instanceof Long);
//                assertEquals(new Long(j), longArray.get(j));
//            }
//            
//            Tuple stringMap = (Tuple)te.get("mapOfStringsField");
//            int numMapEntries = i + 1;
//            assertEquals(2 * numMapEntries, stringMap.size());
//            
//            // Build a map from the data
//            Map<String, String> testMap = new HashMap<String, String>();
//            for (int j = 0; j < numMapEntries; j++) {
//                assertTrue(stringMap.get(j * 2) instanceof String);
//                String key = stringMap.getString(j * 2);
//                assertTrue(stringMap.get((j * 2) + 1) instanceof String);
//                String value = stringMap.getString((j * 2) + 1);
//                testMap.put(key, value);
//            }
//            
//            // Now make sure it has everything we're expecting.
//            for (int j = 0; j < numMapEntries; j++) {
//                assertEquals("value-" + j, testMap.get("key-" + j));
//            }
//
//            i++;
//        }
//        
//        assertEquals(numRecords, i);
//        
//        // Ensure that the Avro file we write out is readable via the standard Avro API
//        File avroFile = new File(out + "/part-00000.avro");
//        DataFileReader<Object> reader =
//            new DataFileReader<Object>(avroFile, new GenericDatumReader<Object>());     
//        i = 0;
//        while (reader.hasNext()) {
//            reader.next();
//            i++;
//        }
//        assertEquals(numRecords, i);
//
//    }

//    @Test
//    public void testInvalidArrayData() {
//        final Fields testFields = new Fields("arrayOfLongsField");
//        final Class<?>[] schemeTypes = {List.class, Long.class};
//
//        final String in = OUTPUT_DIR+ "testInvalidArrayData/in";
//        final String out = OUTPUT_DIR + "testInvalidArrayData/out";
//        
//        
//        // Create a sequence file with the appropriate tuples
//        Lfs lfsSource = new Lfs(new SequenceFile(testFields), in, SinkMode.REPLACE);
//        TupleEntryCollector write;
//        try {
//            write = lfsSource.openForWrite(new JobConf());
//            Tuple t = new Tuple();
//            t.add(new Tuple(0L, "invalid data type"));
//            write.add(t);
//            write.close();
//            // Now read from the results, and write to an Avro file.
//            Pipe writePipe = new Pipe("tuples to avro");
//
//            Tap avroSink = new Lfs(new AvroScheme(testFields, schemeTypes), out);
//            Flow flow = new FlowConnector().connect(lfsSource, avroSink, writePipe);
//            flow.complete();
//            fail("Exception should be thrown as there is an invalid array element");
//
//        } catch (Exception e) {
//            // Ignore.
//        }
//    }
    
//    @Test
//    public void testInvalidMap() {
//        final Fields testFields = new Fields("mapOfStringsField");
//        final Class<?>[] schemeTypes = {Map.class, String.class};
//
//        final String in = OUTPUT_DIR+ "testInvalidMap/in";
//        final String out = OUTPUT_DIR + "testInvalidMap/out";
//
//        // Create a sequence file with the appropriate tuples
//        Lfs lfsSource = new Lfs(new SequenceFile(testFields), in, SinkMode.REPLACE);
//        TupleEntryCollector write;
//        try {
//            write = lfsSource.openForWrite(new JobConf());
//            Tuple t = new Tuple();
//            // add invalid map data - where only key is present and no value
//            t.add(new Tuple("key-0", "value-0", "key-1"));
//            write.add(t);
//            write.close();
//            // Now read from the results, and write to an Avro file.
//            Pipe writePipe = new Pipe("tuples to avro");
//
//            Tap avroSink = new Lfs(new AvroScheme(testFields, schemeTypes), out);
//            Flow flow = new FlowConnector().connect(lfsSource, avroSink, writePipe);
//            flow.complete();
//            fail("Exception should be thrown as there is an invalid map");
//
//        } catch (Exception e) {
//            // Ignore.
//        }
//    }

//    @Test
//    public void testInvalidMapData() {
//        final Fields testFields = new Fields("mapOfStringsField");
//        final Class<?>[] schemeTypes = {Map.class, String.class};
//
//        final String in = OUTPUT_DIR+ "testInvalidMapData/in";
//        final String out = OUTPUT_DIR + "testInvalidMapData/out";
//
//        // Create a sequence file with the appropriate tuples
//        Lfs lfsSource = new Lfs(new SequenceFile(testFields), in, SinkMode.REPLACE);
//        TupleEntryCollector write;
//        try {
//            write = lfsSource.openForWrite(new JobConf());
//            Tuple t = new Tuple();
//            // add invalid map data - key isn't a String
//            t.add(new Tuple("key-0", "value-0", 1L, "value-2"));
//            write.add(t);
//            write.close();
//            // Now read from the results, and write to an Avro file.
//            Pipe writePipe = new Pipe("tuples to avro");
//
//            Tap avroSink = new Lfs(new AvroScheme(testFields, schemeTypes), out);
//            Flow flow = new FlowConnector().connect(lfsSource, avroSink, writePipe);
//            flow.complete();
//            fail("Exception should be thrown as the key isn't a String");
//
//        } catch (Exception e) {
//            // Ignore.
//        }
//    }

    @Test
    public void testNullable() throws Exception {
        final Fields testFields = new Fields("nullString");
//        final Class<?>[] schemeTypes = {String.class};

        final String in = OUTPUT_DIR+ "testNullable/in";
        final String out = OUTPUT_DIR + "testNullable/out";

        // Create a sequence file with the appropriate tuples
        Lfs lfsSource = new Lfs(new SequenceFile(testFields), in, SinkMode.REPLACE);
        TupleEntryCollector write;

        write = lfsSource.openForWrite(new JobConf());
        Tuple t = new Tuple();
        String nullString = null;
        t.add(nullString);
        write.add(t);
        write.close();
        // Now read from the results, and write to an Avro file.
        Pipe writePipe = new Pipe("tuples to avro");

        Tap avroSink = new Lfs(new AvroScheme(testFields, NULLSTRING_SCHEMA), out);
        Flow flow = new FlowConnector().connect(lfsSource, avroSink, writePipe);
//        JobConf jobConf = flow.getJobConf();
//        String iosers = jobConf.get("io.serializations");
//        jobConf.set("io.serializations", "cascading.kryo.KryoSerialization,"+iosers);
        flow.complete();
    }

    
   
//    @Test
//    public void testSetRecordName() {
//        AvroScheme avroScheme = new AvroScheme(new Fields("a"), RECORDNAME_SCHEMA);
//        String expected = "{\"type\":\"record\",\"name\":\"SchemaNameTest\",\"namespace\":\"\",\"doc\":\"auto generated\",\"fields\":[{\"name\":\"a\",\"type\":[\"null\",\"long\"],\"doc\":\"\"}]}";
//        String jsonSchema = avroScheme.getJsonSchema();
//        assertEquals(expected, jsonSchema);
//        avroScheme.setRecordName("FooBar");
//        String jsonSchemaWithRecordName = avroScheme.getJsonSchema();
//        String expectedWithName = "{\"type\":\"record\",\"name\":\"FooBar\",\"namespace\":\"\",\"doc\":\"auto generated\",\"fields\":[{\"name\":\"a\",\"type\":[\"null\",\"long\"],\"doc\":\"\"}]}";
//        assertEquals(expectedWithName, jsonSchemaWithRecordName);
//    }
    
//    @Test
//    public void testEnumInSchema() throws Exception {
//        AvroScheme avroScheme = new AvroScheme(new Fields("a"), new Class[] { TestE.class });
//        String jsonSchema = avroScheme.getJsonSchema();
//        String enumField = String.format("{\"type\":\"enum\",\"name\":\"%s\",\"namespace\":\"%s\",\"symbols\":[\"ONE\",\"TWO\"]}",
//                        "AvroSchemeTest$TestE", TestE.class.getPackage().getName());
//        String expected = String.format("{\"type\":\"record\",\"name\":\"CascadingAvroSchema\",\"namespace\":\"\",\"fields\":[{\"name\":\"a\",\"type\":[\"null\",%s],\"doc\":\"\"}]}",
//                        enumField);
//        String expected = "bloop";
//        String jsonSchema = "blah";
//        assertEquals(expected, jsonSchema);
//    }

 }
