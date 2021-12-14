package ch.unisg.ics.interactions.wot.td.clients;

import ch.unisg.ics.interactions.wot.td.schemas.*;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class UriTemplateTest {

  @Test
  public void testExtraction(){
    String path = "http://example.com/{?p,q}";
    List<String> extracted = UriTemplate.extract(path);
    List expected = new ArrayList();
    expected.add("http://example.com/");
    expected.add("{?p,q}");
    assertEquals(expected, extracted);
  }

  @Test
  public void testGetVariables(){
    String expression = "{?p,q}";
    Set<String> variables = UriTemplate.getVariables(expression);
    Set expected = new HashSet();
    expected.add("p");
    expected.add("q");
    assertEquals(expected, variables);
  }

  @Test
  public void testGetValue(){
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
  public void testGetValueObject(){
    Object object = new Object();
    String datatype = DataSchema.OBJECT;
    UriTemplate.getValue(object, datatype);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetValueArray(){
    ArrayList<String> array = new ArrayList<>();
    array.add("abc");
    array.add("de");
    String datatype = DataSchema.ARRAY;
    UriTemplate.getValue(array, datatype);
  }

  @Test(expected = IllegalArgumentException.class)
    public void testGetValueEmpty(){
    Object object = new Object();
    String datatype = DataSchema.EMPTY;
    UriTemplate.getValue(object, datatype);
  }

  @Test
  public void testReplaceVariables(){
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
    System.out.println("actual: "+actual);
    String expected = "?p=abc&q=32&r=true&s=23.3";
    assertEquals(expected, actual);
  }

  @Test
  public void testUriVariables(){
    String path = "http://example.com/{?p,q}";
    Map<String, DataSchema> uriVariables = new HashMap<>();
    uriVariables.put("p", new StringSchema.Builder().build());
    uriVariables.put("q", new IntegerSchema.Builder().build());
    Map<String, Object> map2 = new HashMap<>();
    map2.put("p", "abc");
    map2.put("q", 32);
    String uri = UriTemplate.createUri(path, uriVariables, map2);
    assertEquals("http://example.com/?p=abc&q=32",uri);
  }

  @Test
  public void testUriVariables2(){
    String path = "http://example.com/{p}";
    Map<String, DataSchema> uriVariables = new HashMap<>();
    uriVariables.put("p", new StringSchema.Builder().build());
    Map<String, Object> map2 = new HashMap<>();
    map2.put("p", "abc");
    String uri = UriTemplate.createUri(path, uriVariables, map2);
    assertEquals("http://example.com/abc",uri);
  }
}
