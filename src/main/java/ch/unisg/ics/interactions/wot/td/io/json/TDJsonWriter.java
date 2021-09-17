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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * A writer to serialize TDs in the JSON-LD 1.1 format.
 */
public class TDJsonWriter extends AbstractTDWriter {

  private final JsonObjectBuilder document;
  private final JsonArrayBuilder context;
  private Optional<JsonObjectBuilder> semanticContext;

  public TDJsonWriter(ThingDescription td) {
    super(td);
    document = Json.createObjectBuilder();
    context = Json.createArrayBuilder();
    semanticContext = Optional.empty();
    context.add(JWot.WOT_CONTEXT);
    document.add(JWot.CONTEXT, context);
  }

  @Override
  public String write() {
    semanticContext.ifPresent(context::add);
    this.addTitle()
      .addTypes()
      .addSecurity()
      .addBaseURI()
      .addProperties()
      .addActions()
      .addGraph();

    OutputStream out = new ByteArrayOutputStream();
    JsonWriter writer = Json.createWriter(out);
    writer.write(document.build());
    return out.toString();
  }

  @Override
  public TDJsonWriter setNamespace(String prefix, String namespace) {
    if(semanticContext.isPresent()){
      semanticContext.get().add(prefix, namespace);
    } else {
      JsonObjectBuilder semContextBuilder = Json.createObjectBuilder();
      semContextBuilder.add(prefix, namespace);
      semanticContext = Optional.of(semContextBuilder);
      document.add(JWot.CONTEXT, semanticContext.get());
    }

    return this;
  }

  @Override
  protected TDJsonWriter addTypes() {
    //TODO This is ugly why is the types sometimes a set and sometimes a list?
    document.add(JWot.SEMANTIC_TYPE, this.getSemanticTypes(new ArrayList<>(td.getSemanticTypes())));
    return this;
  }

  @Override
  protected TDJsonWriter addTitle() {
    document.add(JWot.TITLE, td.getTitle());
    return this;
  }

  @Override
  protected TDJsonWriter addSecurity() {
    //TODO implement: for the time being ignores security schemes and puts NoSecurityScheme
    //Add security def
    document.add(JWot.SECURITY_DEF,
      Json.createObjectBuilder().add("nosec_sc",
        Json.createObjectBuilder().add("scheme", "nosec" ))
    );
    //Add actual security field
    document.add(JWot.SECURITY, Json.createArrayBuilder().add("nosec"));

    return this;
  }

  @Override
  protected TDJsonWriter addBaseURI() {
    td.getBaseURI().ifPresent(uri -> document.add(JWot.BASE, uri));
    return this;
  }

  @Override
  protected TDJsonWriter addProperties() {
    document.add(JWot.PROPERTIES, this.getAffordancesObject(td.getProperties(), this::getProperty));
    return this;
  }

  @Override
  protected TDJsonWriter addActions() {
    document.add(JWot.ACTIONS, this.getAffordancesObject(td.getActions(), this::getAction));
    return this;
  }

  @Override
  protected TDJsonWriter addGraph() {
    td.getGraph().ifPresent(g -> {
      g.getStatements(null, null, null).forEach(statement -> {
        //TODO I'm not sure this is the right way to parse the statement
        document.add(statement.getPredicate().stringValue(), statement.getObject().stringValue());
      });
    });
    return this;
  }

  private<T extends InteractionAffordance> JsonObjectBuilder getAffordancesObject(List<T> affordances, Function<T, JsonObjectBuilder> mapper) {
    if (affordances.size() > 0) {
      JsonObjectBuilder rootObj = Json.createObjectBuilder();
      affordances.forEach(aff ->
        rootObj.add(aff.getTitle().get(), mapper.apply(aff))
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

  private JsonObjectBuilder getAction(ActionAffordance affordance) {
    JsonObjectBuilder actionObj = Json.createObjectBuilder();

    //TODO safe and idempotent are missing in the model

    affordance.getInputSchema().ifPresent(d ->
      actionObj.add(JWot.INPUT, SchemaJsonWriter.getDataSchema(d))
    );
    affordance.getOutputSchema().ifPresent(d ->
      actionObj.add(JWot.OUTPUT, SchemaJsonWriter.getDataSchema(d))
    );

    return actionObj;
  }


  private JsonArrayBuilder getSemanticTypes(List<String> semanticTypes) {
    JsonArrayBuilder types = Json.createArrayBuilder();
    semanticTypes.forEach(types::add);
    return types;
  }

  private JsonObjectBuilder getAffordance(InteractionAffordance affordance) {
    JsonObjectBuilder affordanceObj = Json.createObjectBuilder();

    //add semantic type(s)
    affordanceObj.add(JWot.SEMANTIC_TYPE, this.getSemanticTypes(affordance.getSemanticTypes()));

    //add readable name
    affordance.getName().ifPresent(n -> affordanceObj.add(JWot.TITLE, n));

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
