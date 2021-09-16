package ch.unisg.ics.interactions.wot.td.io.json;

public interface JWot {

  String WOT_CONTEXT = "https://www.w3.org/2019/wot/td/v1";


  String BASE = "base";
  String CONTEXT = "@context";

  String TITLE = "title";
  String DESCRIPTION = "description";

  String PROPERTIES = "properties";
  String OBSERVABLE = "observable";

  String ACTIONS = "actions";
  String INPUT = "input";
  String OUTPUT = "output";
  String SAFE = "safe";
  String IDEMPOTENT = "idempotent";

  String EVENTS = "events";

  String SEMANTIC_TYPE = "@type";
  String TYPE = "type";

  String FORMS = "forms";
  String TARGET = "href";
  String METHOD = "htv:methodName";
  String CONTENT_TYPE = "contentType";
  String SUBPROTOCOL = "subprotocol";
  String OPERATIONS = "op";

  String SECURITY = "security";
  String SECURITY_DEF = "securityDefinitions";

}
