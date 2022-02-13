package ch.unisg.ics.interactions.wot.td.io;

import ch.unisg.ics.interactions.wot.td.schemas.ArraySchema;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.schemas.ObjectSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.JSONSchema;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class SchemaGraphReaderTest {
  private static final String IO_BASE_IRI = "http://example.org/";
  private static final String PREFIX = "http://example.org/#";
  private static final ValueFactory rdf = SimpleValueFactory.getInstance();

  private static final String SEMANTIC_USER_ACCOUNT = "[\n" +
       "        a js:ObjectSchema, <http://example.org/#UserAccount> ;\n" +
       "        js:contentMediaType \"application/json\";\n" +
       "        js:properties [\n" +
       "            a js:StringSchema, <http://example.org/#FullName> ;\n" +
       "            js:propertyName \"full_name\";\n" +
       "        ] ;\n" +
       "        js:required \"full_name\" ;\n" +
       "    ]";

  @Test
  public void testReadSimpleSemanticObject() throws RDFParseException, RDFHandlerException,
      IOException {

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
        "		 js:enum \"label1\", <http://example.org/label2>, \"label3\" ;\n" +
        "    ] ;\n" +
        "    js:properties [\n" +
        "        a js:NullSchema, <http://example.org/#SemNull> ;\n" +
        "        js:propertyName \"null_value\";\n" +
        "    ] ;\n" +
        "    js:required \"integer_value\", \"number_value\" ;\n" +
        "] .\n";

    ObjectSchema object = assertObjectMetadata(testSimpleSemObject, PREFIX + "SemObject", 5, 2);

    DataSchema booleanProperty = object.getProperties().get("boolean_value");
    assertEquals(DataSchema.BOOLEAN, booleanProperty.getDatatype());
    assertTrue(booleanProperty.getSemanticTypes().contains(PREFIX + "SemBool"));

    DataSchema integerProperty = object.getProperties().get("integer_value");
    assertEquals(DataSchema.INTEGER, integerProperty.getDatatype());
    assertTrue(integerProperty.getSemanticTypes().contains(PREFIX + "SemInt"));

    DataSchema numberProperty = object.getProperties().get("number_value");
    assertEquals(DataSchema.NUMBER, numberProperty.getDatatype());
    assertTrue(numberProperty.getSemanticTypes().contains(PREFIX + "SemNumber"));

    DataSchema stringProperty = object.getProperties().get("string_value");
    assertEquals(DataSchema.STRING, stringProperty.getDatatype());
    assertTrue(stringProperty.getSemanticTypes().contains(PREFIX + "SemString"));
    assertEquals(3,stringProperty.getEnumeration().size());
    assertTrue(stringProperty.getEnumeration().contains("label1"));
    assertTrue(stringProperty.getEnumeration().contains("http://example.org/label2"));

    DataSchema nullProperty = object.getProperties().get("null_value");
    assertEquals(DataSchema.NULL, nullProperty.getDatatype());
    assertTrue(nullProperty.getSemanticTypes().contains(PREFIX + "SemNull"));
  }

  @Test
  public void testReadSimpleSemanticObjectWithArray() throws RDFParseException,
      RDFHandlerException, IOException {

    String testObject =
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
        "        js:items " + SEMANTIC_USER_ACCOUNT + ";\n" +
        "    ] ;\n" +
        "    js:required \"count\" ;\n" +
        "] .\n";

    ObjectSchema object = assertObjectMetadata(testObject, PREFIX + "UserDB", 2, 1);

    DataSchema count = object.getProperties().get("count");
    assertEquals(DataSchema.INTEGER, count.getDatatype());
    assertTrue(count.getSemanticTypes().contains(PREFIX + "UserCount"));

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
        .contains(PREFIX + "FullName"));
  }

  @Test
  public void testReadNestedSemanticObject() throws RDFParseException, RDFHandlerException,
      IOException {

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

    Model model = ReadWriteUtils.readModelFromString(RDFFormat.TURTLE, testNestedSemanticObject,
        IO_BASE_IRI);
    Optional<Resource> nodeId = Models.subject(model.filter(null, RDF.TYPE,
        SimpleValueFactory.getInstance().createIRI(PREFIX + "SemObject")));

    Optional<DataSchema> schema = SchemaGraphReader.readDataSchema(nodeId.get(), model);
    assertTrue(schema.isPresent());
    assertEquals(DataSchema.OBJECT, schema.get().getDatatype());
    assertTrue(schema.get().getSemanticTypes().contains(PREFIX + "SemObject"));

    ObjectSchema object = (ObjectSchema) schema.get();
    assertEquals(2, object.getProperties().size());
    assertTrue(object.getRequiredProperties().contains("string_value"));

    DataSchema stringProperty = object.getProperties().get("string_value");
    assertEquals(DataSchema.STRING, stringProperty.getDatatype());
    assertTrue(stringProperty.getSemanticTypes().contains(PREFIX + "SemString"));

    ObjectSchema innerObject = (ObjectSchema) object.getProperties().get("inner_object");
    assertTrue(innerObject.getSemanticTypes().contains(PREFIX + "AnotherSemObject"));
    assertEquals(4, innerObject.getProperties().size());
    assertTrue(innerObject.getRequiredProperties().contains("integer_value"));

    DataSchema booleanProperty = innerObject.getProperties().get("boolean_value");
    assertEquals(DataSchema.BOOLEAN, booleanProperty.getDatatype());
    assertTrue(booleanProperty.getSemanticTypes().contains(PREFIX + "SemBool"));

    DataSchema integerProperty = innerObject.getProperties().get("integer_value");
    assertEquals(DataSchema.INTEGER, integerProperty.getDatatype());
    assertTrue(integerProperty.getSemanticTypes().contains(PREFIX + "SemInt"));

    DataSchema numberProperty = innerObject.getProperties().get("number_value");
    assertEquals(DataSchema.NUMBER, numberProperty.getDatatype());
    assertTrue(numberProperty.getSemanticTypes().contains(PREFIX + "SemNumber"));

    DataSchema nullProperty = innerObject.getProperties().get("null_value");
    assertEquals(DataSchema.NULL, nullProperty.getDatatype());
    assertTrue(nullProperty.getSemanticTypes().contains(PREFIX + "SemNull"));
  }

  @Test
  public void testReadArrayOneSemanticObject() throws RDFParseException, RDFHandlerException,
      IOException {

    String testArray =
        "@prefix td: <http://www.w3.org/ns/td#> .\n" +
        "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
        "[\n" +
        "    a js:ArraySchema, <http://example.org/#UserAccountList> ;\n" +
        "    js:minItems 0 ;\n" +
        "    js:maxItems 100 ;\n" +
        "    js:items " + SEMANTIC_USER_ACCOUNT + ";\n" +
        "] .\n";

    ArraySchema array = assertUserAccountArrayMetadata(testArray);
    assertEquals(1, array.getItems().size());
    assertEquals(DataSchema.OBJECT, array.getItems().get(0).getDatatype());

    ObjectSchema user = (ObjectSchema) array.getItems().get(0);
    assertTrue(user.getSemanticTypes().contains(PREFIX + "UserAccount"));

    assertTrue(user.getContentMediaType().isPresent());
    assertEquals("application/json", user.getContentMediaType().get());

    assertEquals(1, user.getProperties().size());
    assertTrue(user.getProperties().containsKey("full_name"));
    assertTrue(user.getProperties().get("full_name").getSemanticTypes()
      .contains(PREFIX + "FullName"));
    assertEquals(1, user.getRequiredProperties().size());
    assertTrue(user.getRequiredProperties().contains("full_name"));
  }

  @Test
  public void testReadSchemaOneOf() throws RDFParseException, RDFHandlerException,
    IOException {
    String testObject =
      "@prefix td: <http://www.w3.org/ns/td#> .\n" +
        "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
        "@prefix ex: <http://example.org/#> .\n" +
        "[\n" +
        "    a js:ObjectSchema ;\n" +
        "    js:oneOf [ a js:ObjectSchema, ex:Schema0 ] ;\n" +
        "    js:oneOf [ a js:ObjectSchema, ex:Schema1 ] \n" +
        "] .";

    Model model = ReadWriteUtils.readModelFromString(RDFFormat.TURTLE, testObject,
      IO_BASE_IRI);
    Optional<Resource> nodeId = Models.subject(model.filter(null, RDF.TYPE,
      rdf.createIRI(JSONSchema.ObjectSchema)));

    Optional<DataSchema> schema = SchemaGraphReader.readDataSchema(nodeId.get(), model);

    assertTrue(schema.isPresent());
    assertEquals(DataSchema.OBJECT, schema.get().getDatatype());
    assertEquals(schema.get().getValidSchemas().size(), 2);

    ObjectSchema schema0 = (ObjectSchema) schema.get().getValidSchemas().get(0);
    ObjectSchema schema1 = (ObjectSchema) schema.get().getValidSchemas().get(1);

    assertEquals(DataSchema.OBJECT, schema0.getDatatype());
    assertEquals(DataSchema.OBJECT, schema1.getDatatype());
  }

  @Test
  public void testReadSuperSchema() throws RDFParseException, RDFHandlerException,
    IOException {
    String testObject =
      "@prefix td: <http://www.w3.org/ns/td#> .\n" +
        "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
        "@prefix ex: <http://example.org/#> .\n" +
        "[\n" +
        "    a js:DataSchema ;\n" +
        "    js:oneOf [ a js:StringSchema, ex:Schema0 ] ;\n" +
        "    js:oneOf [ a js:ObjectSchema, ex:Schema1 ] \n" +
        "] .";

    Model model = ReadWriteUtils.readModelFromString(RDFFormat.TURTLE, testObject,
      IO_BASE_IRI);
    Optional<Resource> nodeId = Models.subject(model.filter(null, RDF.TYPE,
      rdf.createIRI(JSONSchema.DataSchema)));

    Optional<DataSchema> schema = SchemaGraphReader.readDataSchema(nodeId.get(), model);

    assertTrue(schema.isPresent());
    assertEquals(DataSchema.DATA, schema.get().getDatatype());
    assertEquals(schema.get().getValidSchemas().size(), 2);

    List<DataSchema> schemas0 = schema.get().getValidSchemasBySemanticType("http://example.org/#Schema0");
    List<DataSchema> schemas1 = schema.get().getValidSchemasBySemanticType("http://example.org/#Schema1");

    assertEquals(1, schemas0.size());
    assertEquals(1, schemas1.size());
    assertEquals(DataSchema.STRING, schemas0.get(0).getDatatype());
    assertEquals(DataSchema.OBJECT, schemas1.get(0).getDatatype());
  }

  @Test
  public void testReadArrayMultipleSemanticObjects() throws RDFParseException,
    RDFHandlerException, IOException {

    String testArray =
      "@prefix td: <http://www.w3.org/ns/td#> .\n" +
        "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
        "[\n" +
        "    a js:ArraySchema, <http://example.org/#UserAccountList> ;\n" +
        "    js:contentMediaType \"application/json\";\n" +
        "    js:minItems 0 ;\n" +
        "    js:maxItems 100 ;\n" +
        "    js:items " + SEMANTIC_USER_ACCOUNT + ";\n" +
        "    js:items " + SEMANTIC_USER_ACCOUNT + ";\n" +
        "] .\n";

    ArraySchema array = assertUserAccountArrayMetadata(testArray);

    assertEquals(2, array.getItems().size());
    assertEquals(DataSchema.OBJECT, array.getItems().get(0).getDatatype());
    assertEquals(DataSchema.OBJECT, array.getItems().get(1).getDatatype());
  }


  private ObjectSchema assertObjectMetadata(String testSemObject, String semType, int props, int req)
      throws RDFParseException, RDFHandlerException, IOException {
    Model model = ReadWriteUtils.readModelFromString(RDFFormat.TURTLE, testSemObject,
        IO_BASE_IRI);
    Optional<Resource> nodeId = Models.subject(model.filter(null, RDF.TYPE,
        rdf.createIRI(JSONSchema.ObjectSchema)));

    Optional<DataSchema> schema = SchemaGraphReader.readDataSchema(nodeId.get(), model);

    assertTrue(schema.isPresent());
    assertEquals(DataSchema.OBJECT, schema.get().getDatatype());
    assertTrue(schema.get().getSemanticTypes().contains(semType));

    ObjectSchema object = (ObjectSchema) schema.get();
    assertFalse(object.getContentMediaType().isPresent());
    assertEquals(props, object.getProperties().size());
    assertEquals(req, object.getRequiredProperties().size());

    return (ObjectSchema) schema.get();
  }

  private ArraySchema assertUserAccountArrayMetadata(String testArray) throws RDFParseException,
      RDFHandlerException, IOException {
    Model model = ReadWriteUtils.readModelFromString(RDFFormat.TURTLE, testArray, IO_BASE_IRI);
    Optional<Resource> nodeId = Models.subject(model.filter(null, RDF.TYPE,
        rdf.createIRI(JSONSchema.ArraySchema)));

    DataSchema schema = SchemaGraphReader.readDataSchema(nodeId.get(), model).get();
    assertEquals(DataSchema.ARRAY, schema.getDatatype());

    ArraySchema array = (ArraySchema) schema;
    assertTrue(array.getSemanticTypes().contains(PREFIX + "UserAccountList"));
    assertEquals(0, array.getMinItems().get().intValue());
    assertEquals(100, array.getMaxItems().get().intValue());

    return array;
  }
}
