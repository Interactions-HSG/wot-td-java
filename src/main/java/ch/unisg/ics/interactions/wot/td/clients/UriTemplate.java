package ch.unisg.ics.interactions.wot.td.clients;

import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;

import java.util.*;

public class UriTemplate {


  private final List<String> extracted;

  public UriTemplate(String expression) {
    this.extracted = extract(expression);
  }

  static List<String> extract(String path) {
    path = path.replaceAll("%7B", "{");
    path = path.replaceAll("%7D", "}");
    List<String> extracted = new ArrayList<>();
    int n = path.length();
    String s = "";
    for (int i = 0; i < n; i++) {
      if (path.charAt(i) == '{') {
        extracted.add(s);
        s = "{";
      } else if (path.charAt(i) == '}') {
        s = s + "}";
        extracted.add(s);
        s = "";
      } else if (i == n - 1) {
        s = s + path.charAt(i);
        extracted.add(s);
      } else {
        s = s + path.charAt(i);
      }
    }
    return extracted;
  }

  static List<String> getListVariables(String expression) {
    List<String> variables = new ArrayList<>();
    String s = "";
    int n = expression.length();
    for (int i = 0; i < n; i++) {
      char c = expression.charAt(i);
      if (c == ',') {
        variables.add(s);
        s = "";
      } else if (i == n - 1) {
        variables.add(s);
      } else if ("{}?".indexOf(c) == -1) {
        s = s + c;
      }
    }

    return variables;
  }

  static Set<String> getVariables(String expression) {
    return new HashSet<>(getListVariables(expression));
  }

  static String replace(String expression, Map<String, DataSchema> uriVariables, Map<String, Object> values) {
    String s = "";
    if (expression.charAt(1) == '?') {
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
    } else {
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

  static String getValue(Object object, String datatype) {
    String value;
    try {
      switch (datatype) {
        case DataSchema.STRING:
          value = object.toString();
          break;
        case DataSchema.INTEGER:
          value = String.valueOf((int) object);
          break;
        case DataSchema.NUMBER:
          value = String.valueOf((double) object);
          break;
        case DataSchema.BOOLEAN:
          value = String.valueOf((boolean) object);
          break;
        case DataSchema.NULL:
          if (object == null) {
            value = "null";
            break;
          }
        default:
          throw new IllegalArgumentException("Unknown data schema of URI variable.");
      }
    } catch (ClassCastException e) {
      throw new IllegalArgumentException("Invalid value for URI variable. Expected value of type "
        + datatype, e);
    }
    return value;
  }

  static String getType(Object object) {
    if (object instanceof String) {
      return DataSchema.STRING;
    } else if (object instanceof Integer) {
      return DataSchema.INTEGER;
    } else if (object instanceof Long) {
      return DataSchema.INTEGER;
    } else if (object instanceof Double) {
      return DataSchema.NUMBER;
    } else if (object instanceof Boolean) {
      return DataSchema.BOOLEAN;
    } else if (object instanceof List) {
      return DataSchema.ARRAY;
    } else if (object == null) {
      return DataSchema.NULL;
    } else {
      return DataSchema.OBJECT;
    }
  }

  static boolean check(Map<String, DataSchema> uriVariables, Map<String, Object> values) {
    boolean b = true;
    for (String key : uriVariables.keySet()) {
      DataSchema schema = uriVariables.get(key);
      Object value = values.get(key);
      String datatype = schema.getDatatype();
      String valueType = getType(value);
      b = ((valueType.equals(DataSchema.INTEGER) && datatype.equals(DataSchema.NUMBER))
        || datatype.equals(valueType));
    }
    return b;
  }

  public String createUri(Map<String, DataSchema> uriVariables, Map<String, Object> values) {
    boolean b = check(uriVariables, values);
    if (b) {
      String s = "";
      int n = extracted.size();
      for (int i = 0; i < n; i++) {
        String e = extracted.get(i);
        if (e.charAt(0) == '{') {
          e = replace(e, uriVariables, values);
        }
        s = s + e;
      }
      return s;
    } else {
      throw new IllegalArgumentException("Failed to fill URI path template");
    }
  }

}
