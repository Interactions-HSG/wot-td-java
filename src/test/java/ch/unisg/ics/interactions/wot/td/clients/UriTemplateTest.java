package ch.unisg.ics.interactions.wot.td.clients;

import ch.unisg.ics.interactions.wot.td.ThingDescription;
import ch.unisg.ics.interactions.wot.td.affordances.ActionAffordance;
import ch.unisg.ics.interactions.wot.td.io.TDGraphReader;
import ch.unisg.ics.interactions.wot.td.schemas.*;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class UriTemplateTest {

  @Test
  public void testExtraction() {
    String path = "http://example.com/{?p,q}";
    List<String> extracted = UriTemplate.extract(path);
    List<String> expected = new ArrayList<>();
    expected.add("http://example.com/");
    expected.add("{?p,q}");
    assertEquals(expected, extracted);
  }

  @Test
  public void testGetVariables() {
    String expression = "{?p,q}";
    Set<String> variables = UriTemplate.getVariables(expression);
    Set<String> expected = new HashSet<>();
    expected.add("p");
    expected.add("q");
    assertEquals(expected, variables);
  }

  @Test
  public void testGetValue() {
    Object object1 = "abc";
    String datatype1 = DataSchema.STRING;
    String value1 = UriTemplate.getValue(object1, datatype1);
    Object object2 = Integer.valueOf(2);
    String datatype2 = DataSchema.INTEGER;
    String value2 = UriTemplate.getValue(object2, datatype2);
    Object object3 = Double.valueOf(22.3);
    String datatype3 = DataSchema.NUMBER;
    String value3 = UriTemplate.getValue(object3, datatype3);
    String datatype4 = DataSchema.BOOLEAN;
    String value4 = UriTemplate.getValue(Boolean.TRUE, datatype4);
    Object object5 = null;
    String datatype5 = DataSchema.NULL;
    String value5 = UriTemplate.getValue(object5, datatype5);
    assertEquals("abc", value1);
    assertEquals("2", value2);
    assertEquals("22.3", value3);
    assertEquals("true", value4);
    assertEquals("null", value5);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetValueObject() {
    Object object = new Object();
    String datatype = DataSchema.OBJECT;
    UriTemplate.getValue(object, datatype);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetValueArray() {
    ArrayList<String> array = new ArrayList<>();
    array.add("abc");
    array.add("de");
    String datatype = DataSchema.ARRAY;
    UriTemplate.getValue(array, datatype);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetValueEmpty() {
    Object object = new Object();
    String datatype = DataSchema.DATA;
    UriTemplate.getValue(object, datatype);
  }

  @Test(expected = IllegalArgumentException.class)
  public void getInvalidSchemaUriVariable() {
    UriTemplate.getValue(0.5, DataSchema.INTEGER);
  }

  @Test
  public void testReplaceVariables() {
    String expression = "{?p,q,r,s}";
    Map<String, Object> map = new HashMap<>();
    map.put("p", "abc");
    map.put("q", 32);
    map.put("r", true);
    map.put("s", 23.3);
    Map<String, DataSchema> uriVariables = new HashMap<>();
    uriVariables.put("p", new StringSchema.Builder().build());
    uriVariables.put("q", new IntegerSchema.Builder().build());
    uriVariables.put("r", new BooleanSchema.Builder().build());
    uriVariables.put("s", new NumberSchema.Builder().build());
    String actual = UriTemplate.replace(expression, uriVariables, map);
    System.out.println("actual: " + actual);
    String expected = "?p=abc&q=32&r=true&s=23.3";
    assertEquals(expected, actual);
  }

  @Test
  public void testCheck() {
    Map<String, DataSchema> uriVariables = new Hashtable<>();
    uriVariables.put("p", new StringSchema.Builder().build());
    uriVariables.put("q", new IntegerSchema.Builder().build());
    uriVariables.put("r1", new NumberSchema.Builder().build());
    uriVariables.put("r2", new NumberSchema.Builder().build());
    uriVariables.put("s", new BooleanSchema.Builder().build());
    Map<String, Object> values = new Hashtable<>();
    values.put("p", "abc");
    values.put("q", Integer.valueOf(2));
    values.put("r1", Double.valueOf(22.3));
    values.put("r2", Integer.valueOf(3));
    values.put("s", Boolean.TRUE);
    boolean b = UriTemplate.check(uriVariables, values);
    Map<String, DataSchema> uriVariables2 = new Hashtable<>();
    uriVariables2.put("p", new ObjectSchema.Builder().build());
    Map<String, Object> values2 = new Hashtable<>();
    values2.put("p", "abc");
    boolean b2 = UriTemplate.check(uriVariables2, values2);
    assertTrue(b);
    assertFalse(b2);
  }

  @Test
  public void testUriVariables() {
    String path = "http://example.com/{?p,q}";
    Map<String, DataSchema> uriVariables = new HashMap<>();
    uriVariables.put("p", new StringSchema.Builder().build());
    uriVariables.put("q", new IntegerSchema.Builder().build());
    Map<String, Object> map2 = new HashMap<>();
    map2.put("p", "abc");
    map2.put("q", 32);
    String uri = new UriTemplate(path).createUri(uriVariables, map2); //UriTemplate.createUri(path, uriVariables, map2);
    assertEquals("http://example.com/?p=abc&q=32", uri);
  }

  @Test
  public void testUriVariables2() {
    String path = "http://example.com/{p}";
    Map<String, DataSchema> uriVariables = new HashMap<>();
    uriVariables.put("p", new StringSchema.Builder().build());
    Map<String, Object> map2 = new HashMap<>();
    map2.put("p", "abc");
    String uri = new UriTemplate(path).createUri(uriVariables, map2);
    assertEquals("http://example.com/abc", uri);
  }

  @Test
  public void testFromTD(){
    String TDDescription = "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
      "@prefix htv: <http://www.w3.org/2011/http#> .\n" +
      "@prefix hctl: <https://www.w3.org/2019/wot/hypermedia#> .\n" +
      "@prefix dct: <http://purl.org/dc/terms/> .\n" +
      "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
      "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
      "<http://example.org/lamp123> a td:Thing, <https://saref.etsi.org/core/LightSwitch>;\n" +
      "  td:title \"My Lamp Thing\";\n" +
      "  td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme\n" +
      "    ];\n" +
      "  td:hasActionAffordance [ a td:ActionAffordance,\n" +
      "        <https://saref.etsi.org/core/ToggleCommand>;\n" +
      "  td:name   \"toggleAffordance\"; "+
      "      td:hasUriTemplateSchema [ a js:StringSchema;\n"+
      "      td:name \"p\"    ];"+
      "      td:hasUriTemplateSchema [ a js:StringSchema;\n"+
      "      td:name \"q\"    ];"+
      "      td:title \"Toggle\";\n" +
      "      td:hasForm [\n" +
      "          htv:methodName \"PUT\";\n" +
      "          hctl:hasTarget <http://mylamp.example.org/toggle{?p,q}>;\n" +
      "          hctl:forContentType \"application/json\";\n" +
      "          hctl:hasOperationType td:invokeAction\n" +
      "        ];\n" +
      "      td:hasInputSchema [ a js:ObjectSchema,\n" +
      "            <https://saref.etsi.org/core/OnOffState>;\n" +
      "          js:properties [ a js:BooleanSchema;\n" +
      "              js:propertyName \"status\"\n" +
      "            ];\n" +
      "          js:required \"status\"\n" +
      "        ]\n" +
      "    ] .\n";
    ThingDescription td = TDGraphReader.readFromString(ThingDescription.TDFormat.RDF_TURTLE, TDDescription);
    ActionAffordance actionAffordance = td.getActionByName("toggleAffordance").get();
    String path = actionAffordance.getFirstForm().get().getTarget();
    Map<String, DataSchema> uriVariables = new HashMap<>();
    uriVariables.put("p", new StringSchema.Builder().build());
    uriVariables.put("q", new IntegerSchema.Builder().build());
    Map<String, Object> map2 = new HashMap<>();
    map2.put("p", "abc");
    map2.put("q", 32);
    String uri = new UriTemplate(path).createUri(uriVariables, map2);  
    assertEquals("http://mylamp.example.org/toggle?p=abc&q=32", uri);
  }
}
