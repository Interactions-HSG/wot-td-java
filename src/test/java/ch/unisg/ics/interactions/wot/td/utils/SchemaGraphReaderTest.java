package ch.unisg.ics.interactions.wot.td.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.junit.Test;

import ch.unisg.ics.interactions.wot.td.schemas.ArraySchema;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.schemas.ObjectSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.JSONSchema;

public class SchemaGraphReaderTest {
  private final static String IO_BASE_IRI = "http://example.org/";

  @Test
  public void testReadSimpleSemanticObject() throws RDFParseException, RDFHandlerException, 
      IOException {
    
    String prefix = "http://example.org/#";
    String testSimpleSemObject =
        "@prefix td: <http://www.w3.org/ns/td#> .\n" +
        "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
        "[\n" + 
        "    a js:ObjectSchema, <http://example.org/#SemObject> ;\n" + 
        "    js:properties [\n" + 
        "        a js:BooleanSchema, <http://example.org/#SemBool> ;\n" +
        "        js:propertyName \"boolean_value\";\n" +
        "    ] ;\n" +
        "    js:properties [\n" + 
        "        a js:NumberSchema, <http://example.org/#SemNumber> ;\n" +
        "        js:propertyName \"number_value\";\n" +
        "        js:maximum 100.05 ;\n" + 
        "        js:minimum -100.05 ;\n" +
        "    ] ;\n" +
        "    js:properties [\n" + 
        "        a js:IntegerSchema, <http://example.org/#SemInt> ;\n" +
        "        js:propertyName \"integer_value\";\n" +
        "        js:maximum 100 ;\n" + 
        "        js:minimum -100 ;\n" +
        "    ] ;\n" +
        "    js:properties [\n" + 
        "        a js:StringSchema, <http://example.org/#SemString> ;\n" +
        "        js:propertyName \"string_value\";\n" +
        "    ] ;\n" +
        "    js:properties [\n" + 
        "        a js:NullSchema, <http://example.org/#SemNull> ;\n" +
        "        js:propertyName \"null_value\";\n" +
        "    ] ;\n" +
        "    js:required \"integer_value\", \"number_value\" ;\n" +
        "] .\n";
    
    Model model = readModelFromString(RDFFormat.TURTLE, testSimpleSemObject);
    Optional<Resource> nodeId = Models.subject(model.filter(null, RDF.TYPE, JSONSchema.ObjectSchema));
    
    Optional<DataSchema> schema = SchemaGraphReader.readDataSchema(nodeId.get(), model);
    
    assertTrue(schema.isPresent());
    assertEquals(DataSchema.OBJECT, schema.get().getDatatype());
    assertTrue(schema.get().getSemanticTypes().contains(prefix + "SemObject"));
    
    ObjectSchema object = (ObjectSchema) schema.get();
    assertEquals(5, object.getProperties().size());
    
    DataSchema booleanProperty = object.getProperties().get("boolean_value");
    assertEquals(DataSchema.BOOLEAN, booleanProperty.getDatatype());
    assertTrue(booleanProperty.getSemanticTypes().contains(prefix + "SemBool"));
    
    DataSchema integerProperty = object.getProperties().get("integer_value");
    assertEquals(DataSchema.INTEGER, integerProperty.getDatatype());
    assertTrue(integerProperty.getSemanticTypes().contains(prefix + "SemInt"));
    
    DataSchema numberProperty = object.getProperties().get("number_value");
    assertEquals(DataSchema.NUMBER, numberProperty.getDatatype());
    assertTrue(numberProperty.getSemanticTypes().contains(prefix + "SemNumber"));
    
    DataSchema stringProperty = object.getProperties().get("string_value");
    assertEquals(DataSchema.STRING, stringProperty.getDatatype());
    assertTrue(stringProperty.getSemanticTypes().contains(prefix + "SemString"));
    
    DataSchema nullProperty = object.getProperties().get("null_value");
    assertEquals(DataSchema.NULL, nullProperty.getDatatype());
    assertTrue(nullProperty.getSemanticTypes().contains(prefix + "SemNull"));
  }
  
  @Test
  public void testReadSimpleSemanticObjectWithArray() throws RDFParseException, 
      RDFHandlerException, IOException {
    
    String prefix = "http://example.org/#";
    String testSemObjectWithArray =
        "@prefix td: <http://www.w3.org/ns/td#> .\n" +
        "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
        "[\n" + 
        "    a js:ObjectSchema, <http://example.org/#UserDB> ;\n" + 
        "    js:properties [\n" + 
        "        a js:IntegerSchema, <http://example.org/#UserCount> ;\n" +
        "        js:propertyName \"count\";\n" +
        "    ] ,\n" +
        "    [\n" + 
        "        a js:ArraySchema, <http://example.org/#UserAccountList> ;\n" +
        "        js:propertyName \"user_list\";\n" +
        "        js:minItems 0 ;\n" +
        "        js:maxItems 100 ;\n" +
        "        js:items [\n" +
        "            a js:ObjectSchema, <http://example.org/#UserAccount> ;\n" + 
        "            js:properties [\n" + 
        "                a js:StringSchema, <http://example.org/#FullName> ;\n" +
        "                js:propertyName \"full_name\";\n" +
        "            ] ;\n" +
        "            js:required \"full_name\" ;\n" +
        "        ] ;\n" +
        "    ] ;\n" +
        "    js:required \"count\" ;\n" +
        "] .\n";
    
    Model model = readModelFromString(RDFFormat.TURTLE, testSemObjectWithArray);
    Optional<Resource> nodeId = Models.subject(model.filter(null, RDF.TYPE, JSONSchema.ObjectSchema));
    
    Optional<DataSchema> schema = SchemaGraphReader.readDataSchema(nodeId.get(), model);
    assertTrue(schema.isPresent());
    assertEquals(DataSchema.OBJECT, schema.get().getDatatype());
    assertTrue(schema.get().getSemanticTypes().contains(prefix + "UserDB"));
    
    ObjectSchema object = (ObjectSchema) schema.get();
    assertEquals(2, object.getProperties().size());
    assertEquals(1, object.getRequiredProperties().size());
    assertTrue(object.getRequiredProperties().contains("count"));
    
    DataSchema count = object.getProperties().get("count");
    assertEquals(DataSchema.INTEGER, count.getDatatype());
    assertTrue(count.getSemanticTypes().contains(prefix + "UserCount"));
    
    ArraySchema array = (ArraySchema) object.getProperties().get("user_list");
    assertEquals(DataSchema.ARRAY, array.getDatatype());
    assertEquals(100, array.getMaxItems().get().intValue());
    assertEquals(0, array.getMinItems().get().intValue());
    assertEquals(1, array.getItems().size());
    
    ObjectSchema user = (ObjectSchema) array.getItems().get(0);
    assertEquals(DataSchema.OBJECT, user.getDatatype());
    assertEquals(1, user.getProperties().size());
    assertEquals(1, user.getRequiredProperties().size());
    assertTrue(user.getRequiredProperties().contains("full_name"));
    assertEquals(DataSchema.STRING, user.getProperties().get("full_name").getDatatype());
    assertTrue(user.getProperties().get("full_name").getSemanticTypes()
        .contains(prefix + "FullName"));
  }
  
  @Test
  public void testReadNestedSemanticObject() throws RDFParseException, RDFHandlerException, 
      IOException {
    
    String prefix = "http://example.org/#";
    String testNestedSemanticObject =
        "@prefix td: <http://www.w3.org/ns/td#> .\n" +
        "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
        "[\n" + 
        "    a js:ObjectSchema, <http://example.org/#SemObject> ;\n" +
        "    js:properties [\n" + 
        "        a js:StringSchema, <http://example.org/#SemString> ;\n" +
        "        js:propertyName \"string_value\";\n" +
        "    ] ;\n" +
        "    js:properties [\n" + 
        "        a js:ObjectSchema, <http://example.org/#AnotherSemObject> ;\n" +
        "        js:propertyName \"inner_object\";\n" +
        "        js:properties [\n" + 
        "            a js:BooleanSchema, <http://example.org/#SemBool> ;\n" +
        "            js:propertyName \"boolean_value\";\n" +
        "        ] ;\n" +
        "        js:properties [\n" + 
        "            a js:NumberSchema, <http://example.org/#SemNumber> ;\n" +
        "            js:propertyName \"number_value\";\n" +
        "            js:maximum 100.05 ;\n" + 
        "            js:minimum -100.05 ;\n" +
        "        ] ;\n" +
        "        js:properties [\n" + 
        "            a js:IntegerSchema, <http://example.org/#SemInt> ;\n" +
        "            js:propertyName \"integer_value\";\n" +
        "            js:maximum 100 ;\n" + 
        "            js:minimum -100 ;\n" +
        "        ] ;\n" +
        "        js:properties [\n" + 
        "            a js:NullSchema, <http://example.org/#SemNull> ;\n" +
        "            js:propertyName \"null_value\";\n" +
        "        ] ;\n" +
        "        js:required \"integer_value\" ;\n" +
        "    ] ;\n" +
        "    js:required \"string_value\" ;\n" +
        "] .\n";
    
    Model model = readModelFromString(RDFFormat.TURTLE, testNestedSemanticObject);
    Optional<Resource> nodeId = Models.subject(model.filter(null, RDF.TYPE, 
        SimpleValueFactory.getInstance().createIRI(prefix + "SemObject")));
    
    Optional<DataSchema> schema = SchemaGraphReader.readDataSchema(nodeId.get(), model);
    assertTrue(schema.isPresent());
    assertEquals(DataSchema.OBJECT, schema.get().getDatatype());
    assertTrue(schema.get().getSemanticTypes().contains(prefix + "SemObject"));
    
    ObjectSchema object = (ObjectSchema) schema.get();
    assertEquals(2, object.getProperties().size());
    assertTrue(object.getRequiredProperties().contains("string_value"));
    
    DataSchema stringProperty = object.getProperties().get("string_value");
    assertEquals(DataSchema.STRING, stringProperty.getDatatype());
    assertTrue(stringProperty.getSemanticTypes().contains(prefix + "SemString"));
    
    ObjectSchema innerObject = (ObjectSchema) object.getProperties().get("inner_object");
    assertTrue(innerObject.getSemanticTypes().contains(prefix + "AnotherSemObject"));
    assertEquals(4, innerObject.getProperties().size());
    assertTrue(innerObject.getRequiredProperties().contains("integer_value"));
    
    DataSchema booleanProperty = innerObject.getProperties().get("boolean_value");
    assertEquals(DataSchema.BOOLEAN, booleanProperty.getDatatype());
    assertTrue(booleanProperty.getSemanticTypes().contains(prefix + "SemBool"));
    
    DataSchema integerProperty = innerObject.getProperties().get("integer_value");
    assertEquals(DataSchema.INTEGER, integerProperty.getDatatype());
    assertTrue(integerProperty.getSemanticTypes().contains(prefix + "SemInt"));
    
    DataSchema numberProperty = innerObject.getProperties().get("number_value");
    assertEquals(DataSchema.NUMBER, numberProperty.getDatatype());
    assertTrue(numberProperty.getSemanticTypes().contains(prefix + "SemNumber"));
    
    DataSchema nullProperty = innerObject.getProperties().get("null_value");
    assertEquals(DataSchema.NULL, nullProperty.getDatatype());
    assertTrue(nullProperty.getSemanticTypes().contains(prefix + "SemNull"));
  }
  
  @Test
  public void testReadArrayOneSemanticObject() throws RDFParseException, RDFHandlerException,
      IOException {
    
    String prefix = "http://example.org/#";
    String testArray =
        "@prefix td: <http://www.w3.org/ns/td#> .\n" +
        "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
        "[\n" + 
        "    a js:ArraySchema, <http://example.org/#UserAccountList> ;\n" +
        "    js:minItems 0 ;\n" +
        "    js:maxItems 100 ;\n" +
        "    js:items [\n" +
        "        a js:ObjectSchema, <http://example.org/#UserAccount> ;\n" + 
        "        js:properties [\n" + 
        "            a js:StringSchema, <http://example.org/#FullName> ;\n" +
        "            js:propertyName \"full_name\";\n" +
        "        ] ;\n" +
        "        js:required \"full_name\" ;\n" +
        "    ] ;\n" +
        "] .\n";
    
    Model model = readModelFromString(RDFFormat.TURTLE, testArray);
    Optional<Resource> nodeId = Models.subject(model.filter(null, RDF.TYPE, JSONSchema.ArraySchema));
    
    DataSchema schema = SchemaGraphReader.readDataSchema(nodeId.get(), model).get();
    assertEquals(DataSchema.ARRAY, schema.getDatatype());
    
    ArraySchema array = (ArraySchema) schema;
    assertTrue(array.getSemanticTypes().contains(prefix + "UserAccountList"));
    assertEquals(0, array.getMinItems().get().intValue());
    assertEquals(100, array.getMaxItems().get().intValue());
    assertEquals(1, array.getItems().size());
    assertEquals(DataSchema.OBJECT, array.getItems().get(0).getDatatype());
    
    ObjectSchema user = (ObjectSchema) array.getItems().get(0);
    assertTrue(user.getSemanticTypes().contains(prefix + "UserAccount"));
    assertEquals(1, user.getProperties().size());
    assertTrue(user.getProperties().containsKey("full_name"));
    assertTrue(user.getProperties().get("full_name").getSemanticTypes()
        .contains(prefix + "FullName"));
    assertEquals(1, user.getRequiredProperties().size());
    assertTrue(user.getRequiredProperties().contains("full_name"));
  }
  
  @Test
  public void testReadArrayMultipleSemanticObjects() throws RDFParseException,
      RDFHandlerException, IOException {
    
    String prefix = "http://example.org/#";
    String testArray =
        "@prefix td: <http://www.w3.org/ns/td#> .\n" +
        "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
        "[\n" + 
        "    a js:ArraySchema, <http://example.org/#UserAccountList> ;\n" +
        "    js:minItems 0 ;\n" +
        "    js:maxItems 100 ;\n" +
        "    js:items [\n" +
        "        a js:ObjectSchema, <http://example.org/#UserAccount> ;\n" + 
        "        js:properties [\n" + 
        "            a js:StringSchema, <http://example.org/#FullName> ;\n" +
        "            js:propertyName \"full_name\";\n" +
        "        ] ;\n" +
        "        js:required \"full_name\" ;\n" +
        "    ] ;\n" +
        "    js:items [\n" +
        "        a js:ObjectSchema, <http://example.org/#UserAccount> ;\n" + 
        "        js:properties [\n" + 
        "            a js:StringSchema, <http://example.org/#FullName> ;\n" +
        "            js:propertyName \"full_name\";\n" +
        "        ] ;\n" +
        "        js:required \"full_name\" ;\n" +
        "    ] ;\n" +
        "] .\n";
    
    Model model = readModelFromString(RDFFormat.TURTLE, testArray);
    Optional<Resource> nodeId = Models.subject(model.filter(null, RDF.TYPE, JSONSchema.ArraySchema));
    
    DataSchema schema = SchemaGraphReader.readDataSchema(nodeId.get(), model).get();
    assertEquals(DataSchema.ARRAY, schema.getDatatype());
    
    ArraySchema array = (ArraySchema) schema;
    assertTrue(array.getSemanticTypes().contains(prefix + "UserAccountList"));
    assertEquals(0, array.getMinItems().get().intValue());
    assertEquals(100, array.getMaxItems().get().intValue());
    assertEquals(2, array.getItems().size());
    assertEquals(DataSchema.OBJECT, array.getItems().get(0).getDatatype());
    assertEquals(DataSchema.OBJECT, array.getItems().get(1).getDatatype());
  }
  
  private Model readModelFromString(RDFFormat format, String description) throws RDFParseException, 
      RDFHandlerException, IOException {
    StringReader stringReader = new StringReader(description);
    
    RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE);
    Model model = new LinkedHashModel();
    rdfParser.setRDFHandler(new StatementCollector(model));
    
    rdfParser.parse(stringReader, IO_BASE_IRI);
    
    return model;
  }
}
