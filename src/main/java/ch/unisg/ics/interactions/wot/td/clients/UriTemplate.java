package ch.unisg.ics.interactions.wot.td.clients;

import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;

import java.util.*;

public final class UriTemplate {

  private UriTemplate(){
    throw new UnsupportedOperationException();
  }

  public static List<String> extract(String path){
    List<String> extracted = new ArrayList<>();
    int n = path.length();
    String s = "";
    for (int i = 0; i < n; i++){
      if (path.charAt(i) == '{'){
        extracted.add(s);
        s = "{";
      }
      else if (path.charAt(i)=='}'){
        s = s +"}";
        extracted.add(s);
        s = "";
      }
      else if (i==n-1){
        s = s + path.charAt(i);
        extracted.add(s);
      }
      else {
        s = s + path.charAt(i);
      }
    }
    return extracted;
  }

  public static List<String> getListVariables(String expression){
    List<String> variables = new ArrayList<>();
    String s = "";
    int n = expression.length();
    for (int i = 0; i < n;i++){
      char c = expression.charAt(i);
      if (!(c == '{' || c == '}' || c == '?' || c == ',')){
        s = s + c;

      }
      else if (c == ','){
        variables.add(s);
        s = "";
      }
      else if (i == n-1){
        variables.add(s);
      }
    }


    return variables;
  }

  public static Set<String> getVariables(String expression){
    return new HashSet<>(getListVariables(expression));
  }



  public static String replace(String expression, Map<String, DataSchema> uriVariables, Map<String, Object> values){
    String s = "";
    if (expression.charAt(1)=='?') {
      s = s + '?';
      List<String> variables = getListVariables(expression);
      int n = variables.size();
      for (int i = 0; i < n; i++) {
        String variable = variables.get(i);
        Object object = values.get(variable);
        if (uriVariables.containsKey(variable)) {
          String datatype = uriVariables.get(variable).getDatatype();
          String value = getValue(object, datatype);
          s = s + variable + "=" + value;
          if (i != n - 1) {
            s = s + "&";
          }
        }
      }
    }
    else {
      List<String> variables = getListVariables(expression);
      int n = variables.size();
      for (int i = 0; i < n; i++) {
        String variable = variables.get(i);
        Object object = values.get(variable);
        if (uriVariables.containsKey(variable)) {
          String datatype = uriVariables.get(variable).getDatatype();
          String value = getValue(object, datatype);
          s = s + value;
          if (i != n - 1) {
            s = s + ",";
          }
        }
      }

    }

    return s;
  }

  public static String getValue(Object object, String datatype){
    if (datatype.equals(DataSchema.STRING)) {
      return (String) object;
    } else if (datatype.equals(DataSchema.INTEGER)) {
      Integer value = (Integer) object;
      return value.toString();

    } else if (datatype.equals(DataSchema.NUMBER)) {
      Double value = (Double) object;
      return value.toString();
    } else if (datatype.equals(DataSchema.BOOLEAN)) {
      Boolean value = (Boolean) object;
      return value.toString();
    }
    else if (datatype.equals(DataSchema.NULL)){
      return "null";
    }
    else {
      throw new IllegalArgumentException();
    }

  }

  public static String createUri(String path, Map<String, DataSchema> uriVariables, Map<String, Object> values){
    List<String> extracted = extract(path);
    String s = "";
    int n = extracted.size();
    for (int i = 0; i<n;i++){
      String e = extracted.get(i);
      if (e.charAt(0)=='{'){
        e = replace(e,uriVariables, values);
      }
      s = s + e;
    }
    return s;
  }
}
