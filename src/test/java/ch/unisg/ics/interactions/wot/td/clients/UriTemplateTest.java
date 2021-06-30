package ch.unisg.ics.interactions.wot.td.clients;

import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.schemas.IntegerSchema;
import ch.unisg.ics.interactions.wot.td.schemas.StringSchema;
import org.eclipse.rdf4j.query.algebra.In;
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
    String expression = "{?p,q}";
    Map<String, Object> map = new HashMap<>();
    map.put("p", "abc");
    map.put("q", 32);
    Map<String, DataSchema> uriVariables = new HashMap<>();
    uriVariables.put("p", new StringSchema.Builder().build());
    uriVariables.put("q", new IntegerSchema.Builder().build());
    String actual = UriTemplate.replace(expression, uriVariables, map);
    String expected = "?p=abc&q=32";
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
}
