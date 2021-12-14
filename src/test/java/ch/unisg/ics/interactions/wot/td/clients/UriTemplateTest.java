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
