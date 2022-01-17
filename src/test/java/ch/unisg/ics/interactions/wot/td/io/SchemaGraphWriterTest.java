package ch.unisg.ics.interactions.wot.td.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.junit.Before;
import org.junit.Test;

import ch.unisg.ics.interactions.wot.td.schemas.ArraySchema;
import ch.unisg.ics.interactions.wot.td.schemas.BooleanSchema;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.schemas.IntegerSchema;
import ch.unisg.ics.interactions.wot.td.schemas.NullSchema;
import ch.unisg.ics.interactions.wot.td.schemas.NumberSchema;
import ch.unisg.ics.interactions.wot.td.schemas.ObjectSchema;
import ch.unisg.ics.interactions.wot.td.schemas.StringSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.JSONSchema;

public class SchemaGraphWriterTest {
  private final static String IO_BASE_IRI = "http://example.org/";
  private final static String PREFIX = "https://example.org/#";

  private final static String TEST_SCHEMA_PREFIXES =
      "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
      "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" +
      "@prefix ex: <https://example.org/#> .\n";

  private final static String SEMANTIC_OBJECT = "[\n" +
      "    a js:ObjectSchema, ex:SemObject ;\n" +
      "    js:properties [\n" +
      "        a js:BooleanSchema, ex:SemBoolean ;\n" +
      "        js:propertyName \"boolean_value\";\n" +
      "    ] ;\n" +
      "    js:properties [\n" +
      "        a js:IntegerSchema, ex:SemInteger ;\n" +
      "        js:propertyName \"integer_value\" ;\n" +
      "        js:minimum \"-1000\"^^xsd:int ;\n" +
      "        js:maximum \"1000\"^^xsd:int ;\n" +
      "    ] ;\n" +
      "    js:properties [\n" +
      "        a js:NumberSchema, ex:SemNumber ;\n" +
      "        js:propertyName \"number_value\";\n" +
      "    ] ;\n" +
      "    js:properties [\n" +
      "        a js:StringSchema, ex:SemString ;\n" +
      "        js:propertyName \"string_value\";\n" +
      "    ] ;\n" +
      "    js:properties [\n" +
      "        a js:NullSchema, ex:SemNull ;\n" +
      "        js:propertyName \"null_value\";\n" +
      "    ] ;\n" +
      "    js:required \"string_value\" ;\n" +
      "]";

  private final static String USER_ACCOUNT_OBJECT = "[\n" +
      "            a js:ObjectSchema, ex:UserAccount ;\n" +
      "            js:properties [\n" +
      "                a js:StringSchema, ex:FullName ;\n" +
      "                js:propertyName \"full_name\";\n" +
      "            ] ;\n" +
      "            js:required \"full_name\" ;\n" +
      "        ]";

  private static DataSchema semanticObjectSchema;
  private static final ValueFactory rdf = SimpleValueFactory.getInstance();

  @Before
  public void init() {
    semanticObjectSchema = new ObjectSchema.Builder()
        .addSemanticType(PREFIX + "SemObject")
        .addProperty("boolean_value", new BooleanSchema.Builder()
            .addSemanticType(PREFIX + "SemBoolean")
            .build())
        .addProperty("integer_value", new IntegerSchema.Builder()
            .addSemanticType(PREFIX + "SemInteger")
            .addMinimum(-1000)
            .addMaximum(1000)
            .build())
        .addProperty("number_value", new NumberSchema.Builder()
            .addSemanticType(PREFIX + "SemNumber")
            .build())
        .addProperty("string_value", new StringSchema.Builder()
            .addSemanticType(PREFIX + "SemString")
            .build())
        .addProperty("null_value", new NullSchema.Builder()
            .addSemanticType(PREFIX + "SemNull")
            .build())
        .addRequiredProperties("string_value")
        .build();
  }

  @Test
  public void testWriteEnumeration() throws RDFParseException, RDFHandlerException, IOException {
    String expectedSchema = TEST_SCHEMA_PREFIXES + "[ a js:StringSchema ;\n" +
        "        js:enum \"label1\", <http://example.org/label2>, \"label3\";\n" +
        "    ] .\n";

    Set<String> enumeration = new HashSet<String>();
    enumeration.add("label1");
    enumeration.add("http://example.org/label2");
    enumeration.add("label3");

    DataSchema schema = new StringSchema.Builder()
        .addEnum(enumeration)
        .build();

    assertModel(expectedSchema, schema);
  }

  @Test
  public void testWriteContentMediaType() throws IOException {
    String expectedSchema = TEST_SCHEMA_PREFIXES + "[ a js:StringSchema ;\n" +
      "        js:contentMediaType \"application/json\";\n" +
      "    ] .\n";

    DataSchema schema = new StringSchema.Builder()
      .setContentMediaType("application/json")
      .build();

    assertModel(expectedSchema, schema);
  }

  // Serialization of decimal values requires specific testing (not considered in this test)
  @Test
  public void testWriteSemanticObjectNoDecimals() throws RDFParseException,
      RDFHandlerException, IOException {
    String expectedSchema = TEST_SCHEMA_PREFIXES + SEMANTIC_OBJECT + " .\n";
    assertModel(expectedSchema, semanticObjectSchema);
  }

  @Test
  public void testWriteSemanticObjectWithDecimals() throws RDFParseException,
      RDFHandlerException, IOException {
    DataSchema schema = new ObjectSchema.Builder()
        .addSemanticType(PREFIX + "SemObject")
        .addProperty("number_value", new NumberSchema.Builder()
            .addSemanticType(PREFIX + "SemNumber")
            .addMinimum(-1000.005)
            .addMaximum(1000.005)
            .build())
        .build();

    String description = getTestModelDescription(schema);
    Model schemaModel = ReadWriteUtils.readModelFromString(RDFFormat.TURTLE, description,
        IO_BASE_IRI);

    Optional<Literal> minimum = Models.objectLiteral(schemaModel.filter(null,
        rdf.createIRI(JSONSchema.minimum), null));
    assertTrue(minimum.isPresent());
    assertEquals(-1000.005, minimum.get().doubleValue(), 0.001);

    Optional<Literal> maximum = Models.objectLiteral(schemaModel.filter(null,
        rdf.createIRI(JSONSchema.minimum), null));
    assertTrue(maximum.isPresent());
    assertEquals(-1000.005, maximum.get().doubleValue(), 0.001);
  }

  @Test
  public void testWriteNestedSemanticObject() throws RDFParseException, RDFHandlerException,
      IOException {
    String expectedSchema = TEST_SCHEMA_PREFIXES +
        "[\n" +
        "    a js:ObjectSchema, ex:SemObject ;\n" +
        "    js:properties [\n" +
        "        a js:StringSchema, ex:SemString ;\n" +
        "        js:propertyName \"string_value\";\n" +
        "    ] ;\n" +
        "    js:properties [\n" +
        "        a js:ObjectSchema, ex:AnotherSemObject ;\n" +
        "        js:propertyName \"inner_object\";\n" +
        "        js:properties [\n" +
        "            a js:IntegerSchema, ex:SemInteger ;\n" +
        "            js:propertyName \"integer_value\" ;\n" +
        "        ] ;\n" +
        "        js:required \"integer_value\" ;\n" +
        "    ] ;\n" +
        "    js:required \"string_value\" ;\n" +
        "] ." ;

    DataSchema schema = new ObjectSchema.Builder()
        .addSemanticType(PREFIX + "SemObject")
        .addProperty("string_value", new StringSchema.Builder()
            .addSemanticType(PREFIX + "SemString")
            .build())
        .addProperty("inner_object", new ObjectSchema.Builder()
            .addSemanticType(PREFIX + "AnotherSemObject")
            .addProperty("integer_value", new IntegerSchema.Builder()
                .addSemanticType(PREFIX + "SemInteger")
                .build())
            .addRequiredProperties("integer_value")
            .build())
        .addRequiredProperties("string_value")
        .build();

    assertModel(expectedSchema, schema);
  }

  @Test
  public void testWriteObjectWithArray() throws RDFParseException, RDFHandlerException,
      IOException {
    String expectedSchema = TEST_SCHEMA_PREFIXES +
        "[\n" +
        "    a js:ObjectSchema, ex:UserDB ;\n" +
        "    js:properties [\n" +
        "        a js:IntegerSchema, ex:UserCount ;\n" +
        "        js:propertyName \"count\";\n" +
        "    ] ;\n" +
        "    js:properties [\n" +
        "        a js:ArraySchema, ex:UserAccountList ;\n" +
        "        js:propertyName \"user_list\";\n" +
        "        js:minItems \"0\"^^xsd:int ;\n" +
        "        js:maxItems \"100\"^^xsd:int ;\n" +
        "        js:items " + USER_ACCOUNT_OBJECT + ";\n" +
        "    ] ;\n" +
        "    js:required \"count\" ;\n" +
        "] .\n";

    ObjectSchema schema = new ObjectSchema.Builder()
        .addSemanticType(PREFIX + "UserDB")
        .addProperty("count", new IntegerSchema.Builder()
            .addSemanticType(PREFIX + "UserCount")
            .build())
        .addProperty("user_list", new ArraySchema.Builder()
            .addSemanticType(PREFIX + "UserAccountList")
            .addMaxItems(100)
            .addMinItems(0)
            .addItem(new ObjectSchema.Builder()
                .addSemanticType(PREFIX + "UserAccount")
                .addProperty("full_name", new StringSchema.Builder()
                    .addSemanticType(PREFIX + "FullName")
                    .build())
                .addRequiredProperties("full_name")
                .build())
            .build())
        .addRequiredProperties("count")
        .build();

    assertModel(expectedSchema, schema);
  }

  @Test
  public void testWriteArrayOneObject() throws RDFParseException, RDFHandlerException,
      IOException {
    String expectedSchema = TEST_SCHEMA_PREFIXES +
        "[\n" +
        "    a js:ArraySchema, ex:UserAccountList ;\n" +
        "    js:minItems \"0\"^^xsd:int ;\n" +
        "    js:maxItems \"100\"^^xsd:int ;\n" +
        "    js:items " + USER_ACCOUNT_OBJECT + ";\n" +
        "] ." ;

    ObjectSchema userAccount = getUserAccountSchema();
    ArraySchema schema = new ArraySchema.Builder()
        .addSemanticType(PREFIX + "UserAccountList")
        .addMaxItems(100)
        .addMinItems(0)
        .addItem(userAccount)
        .build();

    assertModel(expectedSchema, schema);
  }

  @Test
  public void testWriteArrayMultipleObjects() throws RDFParseException, RDFHandlerException,
      IOException {
    String expectedSchema = TEST_SCHEMA_PREFIXES +
        "[\n" +
        "    a js:ArraySchema, ex:UserAccountList ;\n" +
        "    js:items " + USER_ACCOUNT_OBJECT + ";\n" +
        "    js:items " + USER_ACCOUNT_OBJECT + ";\n" +
        "] ." ;

    ObjectSchema userAccount = getUserAccountSchema();

    ArraySchema schema = new ArraySchema.Builder()
        .addSemanticType(PREFIX + "UserAccountList")
        .addItem(userAccount)
        .addItem(userAccount)
        .build();

    assertModel(expectedSchema, schema);
  }

  private ObjectSchema getUserAccountSchema() {
    return new ObjectSchema.Builder()
    .addSemanticType(PREFIX + "UserAccount")
    .addProperty("full_name", new StringSchema.Builder()
        .addSemanticType(PREFIX + "FullName")
        .build())
    .addRequiredProperties("full_name")
    .build();
  }

  private String getTestModelDescription(DataSchema testSchema) {
    ModelBuilder builder = new ModelBuilder();
    BNode nodeId = SimpleValueFactory.getInstance().createBNode();
    SchemaGraphWriter.write(builder, nodeId, testSchema);

    return ReadWriteUtils.writeToString(RDFFormat.TURTLE, builder.build());
  }

  private void assertModel(String expectedSchema, DataSchema schema) throws RDFParseException,
      RDFHandlerException, IOException {
    Model expectedModel = ReadWriteUtils.readModelFromString(RDFFormat.TURTLE, expectedSchema,
        IO_BASE_IRI);

    String description = getTestModelDescription(schema);
    Model schemaModel = ReadWriteUtils.readModelFromString(RDFFormat.TURTLE, description,
        IO_BASE_IRI);

    assertTrue(Models.isomorphic(expectedModel, schemaModel));
  }
}
