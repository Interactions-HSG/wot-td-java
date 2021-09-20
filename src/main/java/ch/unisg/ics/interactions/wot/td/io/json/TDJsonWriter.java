package ch.unisg.ics.interactions.wot.td.io.json;

import ch.unisg.ics.interactions.wot.td.ThingDescription;
import ch.unisg.ics.interactions.wot.td.affordances.ActionAffordance;
import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.affordances.InteractionAffordance;
import ch.unisg.ics.interactions.wot.td.affordances.PropertyAffordance;
import ch.unisg.ics.interactions.wot.td.io.AbstractTDWriter;

import javax.json.*;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * A writer to serialize TDs in the JSON-LD 1.1 format.
 */
public class TDJsonWriter extends AbstractTDWriter {

  private final JsonObjectBuilder document;
  private Optional<JsonObjectBuilder> semanticContext;
  private final Map<String, String> prefixMap;

  public TDJsonWriter(ThingDescription td) {
    super(td);
    document = Json.createObjectBuilder();
    semanticContext = Optional.empty();
    prefixMap = new HashMap<>();
  }

  public JsonObject getJson(){
    if(semanticContext.isPresent()){
      document.add(JWot.CONTEXT, Json.createArrayBuilder()
        .add(JWot.WOT_CONTEXT)
        .add(semanticContext.get()));
    } else {
      document.add(JWot.CONTEXT, JWot.WOT_CONTEXT);
    }

    this.addTitle()
      .addTypes()
      .addSecurity()
      .addBaseURI()
      .addProperties()
      .addActions()
      .addGraph();
    return document.build();
  }

  @Override
  public String write() {
    OutputStream out = new ByteArrayOutputStream();
    JsonWriter writer = Json.createWriter(out);
    writer.write(this.getJson());
    return out.toString();
  }

  @Override
  public TDJsonWriter setNamespace(String prefix, String namespace) {
    this.prefixMap.put(namespace, prefix);
    if(semanticContext.isPresent()){
      semanticContext.get().add(prefix, namespace);
    } else {
      JsonObjectBuilder semContextObj = Json.createObjectBuilder()
      .add(prefix, namespace);
      semanticContext = Optional.of(semContextObj);
    }

    return this;
  }

  @Override
  protected TDJsonWriter addTypes() {
    //TODO This is ugly why is the types sometimes a set and sometimes a list?
    if(td.getSemanticTypes().size() > 1) {
      document.add(JWot.SEMANTIC_TYPE, this.getSemanticTypes(new ArrayList<>(td.getSemanticTypes())));
    } else if(!td.getSemanticTypes().isEmpty()){
      document.add(JWot.SEMANTIC_TYPE, td.getSemanticTypes().stream().findFirst().orElse(""));
    }
    return this;
  }

  @Override
  protected TDJsonWriter addTitle() {
    document.add(JWot.TITLE, td.getTitle());
    td.getThingURI().ifPresent(iri -> document.add("id", iri));
    return this;
  }

  @Override
  protected TDJsonWriter addSecurity() {
    //TODO implement: for the time being ignores security schemes and puts NoSecurityScheme
    // because I don't know ho to serialize them from the model
    //Add security def
    document.add(JWot.SECURITY_DEF,
      Json.createObjectBuilder().add("nosec_sc",
        Json.createObjectBuilder().add("scheme", "nosec" ))
    );
    //Add actual security field
    document.add(JWot.SECURITY, Json.createArrayBuilder().add("nosec_sc"));
    return this;
  }

  @Override
  protected TDJsonWriter addBaseURI() {
    td.getBaseURI().ifPresent(uri -> document.add(JWot.BASE, uri));
    return this;
  }

  @Override
  protected TDJsonWriter addProperties() {
    if(!td.getProperties().isEmpty()){
      document.add(JWot.PROPERTIES, this.getAffordancesObject(td.getProperties(), this::getProperty));
    }
    return this;
  }

  @Override
  protected TDJsonWriter addActions() {
    if(!td.getActions().isEmpty()) {
      document.add(JWot.ACTIONS, this.getAffordancesObject(td.getActions(), this::getAction));
    }
    return this;
  }

  @Override
  protected TDJsonWriter addGraph() {
    td.getGraph().ifPresent(g -> g.getStatements(null, null, null).forEach(statement -> {
      //TODO I'm not sure this is the right way to parse the statement
      document.add(getPrefixedAnnotation(statement.getPredicate().stringValue()),statement.getObject().stringValue());
    }));
    return this;
  }

  private String getPrefixedAnnotation(String annotation){
    String[] splitAnnotation = annotation.split("#");
    if(splitAnnotation.length <= 1){
      return annotation;
    }
    String root = splitAnnotation[0]+'#';
    String fragment = splitAnnotation[1];
    String result;
    if(this.prefixMap.containsKey(root)) {
      result = this.prefixMap.get(root)+":"+fragment;
    } else {
      result = annotation;
    }
    return result;
  }

  private<T extends InteractionAffordance> JsonObjectBuilder getAffordancesObject(List<T> affordances, Function<T, JsonObjectBuilder> mapper) {
    if (!affordances.isEmpty()) {
      JsonObjectBuilder rootObj = Json.createObjectBuilder();
      affordances.forEach(aff ->{
          rootObj.add(aff.getName().get(), mapper.apply(aff));
        }
      );
      return rootObj;
    }
    return Json.createObjectBuilder(); //empty
  }

  private JsonObjectBuilder getProperty(PropertyAffordance prop) {
    JsonObjectBuilder propertyObj = getAffordance(prop)
      .add(JWot.OBSERVABLE, prop.isObservable());
    JsonObjectBuilder dataSchema = SchemaJsonWriter.getDataSchema(prop.getDataSchema());
    propertyObj.addAll(dataSchema);
    return propertyObj;
  }

  private JsonObjectBuilder getAction(ActionAffordance action) {
    JsonObjectBuilder actionObj = getAffordance(action);

    //TODO safe and idempotent are missing in the model

    action.getInputSchema().ifPresent(d ->
      actionObj.add(JWot.INPUT, SchemaJsonWriter.getDataSchema(d))
    );
    action.getOutputSchema().ifPresent(d ->
      actionObj.add(JWot.OUTPUT, SchemaJsonWriter.getDataSchema(d))
    );

    return actionObj;
  }


  private JsonArrayBuilder getSemanticTypes(List<String> semanticTypes) {
    JsonArrayBuilder types = Json.createArrayBuilder();
    semanticTypes.forEach(t -> types.add(getPrefixedAnnotation(t)));
    return types;
  }

  private JsonObjectBuilder getAffordance(InteractionAffordance affordance) {
    JsonObjectBuilder affordanceObj = Json.createObjectBuilder();

    //add semantic type(s)
    if(!affordance.getSemanticTypes().isEmpty()) {
      affordanceObj.add(JWot.SEMANTIC_TYPE, this.getSemanticTypes(affordance.getSemanticTypes()));
    }

    affordance.getTitle().ifPresent(n -> affordanceObj.add(JWot.TITLE, n));

    //TODO description is missing in the model

    //add forms
    affordanceObj.add(JWot.FORMS, this.getFormsArray(affordance.getForms()));

    return affordanceObj;
  }

  private JsonArrayBuilder getFormsArray(List<Form> forms) {
    JsonArrayBuilder formArray = Json.createArrayBuilder();
    forms.forEach(form -> {
      JsonObjectBuilder formObj = Json.createObjectBuilder()
        .add(JWot.TARGET, form.getTarget())
        .add(JWot.CONTENT_TYPE, form.getContentType());

      form.getSubProtocol().ifPresent(sub -> formObj.add(JWot.SUBPROTOCOL, sub));

      //Add operations
      JsonArrayBuilder opArray = Json.createArrayBuilder();
      form.getOperationTypes().forEach(opArray::add);
      formObj.add(JWot.OPERATIONS, opArray);

      //Add methodName only if there is one operation type to avoid ambiguity
      form.getMethodName().ifPresent(m -> {
        if(form.getOperationTypes().size() == 1)
          formObj.add(JWot.METHOD, m);
      });
      formArray.add(formObj);
    });
    return formArray;
  }
}
