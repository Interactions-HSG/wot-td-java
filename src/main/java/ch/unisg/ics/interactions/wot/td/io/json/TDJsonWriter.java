package ch.unisg.ics.interactions.wot.td.io.json;

import ch.unisg.ics.interactions.wot.td.ThingDescription;
import ch.unisg.ics.interactions.wot.td.affordances.ActionAffordance;
import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.affordances.InteractionAffordance;
import ch.unisg.ics.interactions.wot.td.affordances.PropertyAffordance;
import ch.unisg.ics.interactions.wot.td.io.AbstractTDWriter;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import javax.json.*;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

/**
 * A writer to serialize TDs in the JSON-LD 1.1 format.
 */
public class TDJsonWriter extends AbstractTDWriter {

  private final JsonObjectBuilder document;
  private final Map<String, String> prefixMap;
  private Optional<JsonObjectBuilder> semanticContext;

  public TDJsonWriter(ThingDescription td) {
    super(td);
    document = Json.createObjectBuilder();
    semanticContext = Optional.empty();
    prefixMap = new HashMap<>();
  }

  public JsonObject getJson() {

    if (td.getGraph().isPresent()) {
      td.getGraph().get().getNamespaces().stream()
        .filter(ns -> !prefixMap.containsKey(ns.getName()))
        .forEach(ns -> setNamespace(ns.getPrefix(), ns.getName()));
    }
    if (semanticContext.isPresent()) {
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
    if (semanticContext.isPresent()) {
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

    Set<String> semanticTypes = td.getSemanticTypes();

    if (td.getThingURI().isPresent()) {
      Resource thingURI = SimpleValueFactory.getInstance().createIRI(td.getThingURI().get());
      td.getGraph().ifPresent(g -> g.getStatements(thingURI, RDF.TYPE, null)
        .forEach(statement -> {
          semanticTypes.add(statement.getObject().stringValue());
        }));
    }

    if (semanticTypes.size() > 1) {
      document.add(JWot.SEMANTIC_TYPE, this.getSemanticTypes(new ArrayList<>(semanticTypes)));
    } else if (!semanticTypes.isEmpty()) {
      document.add(JWot.SEMANTIC_TYPE,
        this.getPrefixedAnnotation(semanticTypes.stream().findFirst().orElse("")));
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
        Json.createObjectBuilder().add("scheme", "nosec"))
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
    if (!td.getProperties().isEmpty()) {
      document.add(JWot.PROPERTIES, this.getAffordancesObject(td.getProperties(), this::getProperty));
    }
    return this;
  }

  @Override
  protected TDJsonWriter addActions() {
    if (!td.getActions().isEmpty()) {
      document.add(JWot.ACTIONS, this.getAffordancesObject(td.getActions(), this::getAction));
    }
    return this;
  }

  @Override
  protected TDJsonWriter addGraph() {
    // TODO the getStatementObject can be exapnded so that addGraph() calls directly :
    // document.addAll(getStatementObject(thingURI);
    if (td.getThingURI().isPresent()) {
      Resource thingURI = SimpleValueFactory.getInstance().createIRI(td.getThingURI().get());
      td.getGraph().ifPresent(g -> g.getStatements(thingURI, null, null)
        .forEach(statement -> {

          if (!statement.getPredicate().equals(RDF.TYPE)) {
            IRI predicate = statement.getPredicate();
            Value object = statement.getObject();

            if (!object.isBNode()) {
              document.add(getPrefixedAnnotation(predicate.stringValue()), getPrefixedAnnotation(object.stringValue()));
            }
            else {
              JsonObjectBuilder objectObjBuilder = getStatementObject((Resource) object);
              document.add(getPrefixedAnnotation(predicate.stringValue()), objectObjBuilder);
            }
          }
        }));
    }

    return this;
  }

  protected JsonObjectBuilder getStatementObject(Resource subject) {
    //TODO Convert to JsonArry when sub and pred are the same.
    JsonObjectBuilder subjectObjBuilder = Json.createObjectBuilder();

    td.getGraph().ifPresent(g -> g.getStatements(subject, null, null)
      .forEach(statement -> {
      IRI predicate = statement.getPredicate();
      Value object = statement.getObject();
      String key;
      JsonValue currentValue;
      if (!object.isBNode()) {
        if (predicate.equals(RDF.TYPE)) {
          key = JWot.SEMANTIC_TYPE;
        } else {
          key = getPrefixedAnnotation(predicate.stringValue());
        }
        currentValue = Json.createValue(getPrefixedAnnotation(object.stringValue()));
      }
      else {
        key = getPrefixedAnnotation(predicate.stringValue());
        currentValue = getStatementObject((Resource) object).build();
      }
      subjectObjBuilder.add(key,currentValue);
    }));

    return subjectObjBuilder;
  }

  private JsonObjectBuilder getPredicateBuilder(String key, String currentValue, JsonObject currentObj) {
    JsonObjectBuilder objBuilder = Json.createObjectBuilder();
    if(currentObj.containsKey(key)){
      JsonValue previousValue = currentObj.get(key);
      objBuilder.add(key, Json.createArrayBuilder().add(previousValue).add(currentValue));
    } else {
      objBuilder.add(key, currentValue);
    }
    return objBuilder;
  }

  private String getPrefixedAnnotation(String annotation) {

    if (annotation.startsWith(TD.PREFIX)) {
      return annotation.replace(TD.PREFIX,"");
    }

    Map<String, String> matchedPref = prefixMap.entrySet()
      .stream()
      .filter(map -> annotation.startsWith(map.getKey()))
      .collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue()));

    if (matchedPref.isEmpty()) {
      return annotation;
    }
    if (matchedPref.size() == 1) {
      String namespace = (String) matchedPref.keySet().toArray()[0];
      return annotation.replace(namespace, matchedPref.get(namespace) + ":");
    } else {
      Map.Entry<String, String> bestMatch = Collections.max(matchedPref.entrySet(),
        comparing(Map.Entry::getKey));
      return annotation.replace(bestMatch.getKey(), bestMatch.getValue() + ":");
    }
  }

  private <T extends InteractionAffordance> JsonObjectBuilder getAffordancesObject(List<T> affordances, Function<T, JsonObjectBuilder> mapper) {
    if (!affordances.isEmpty()) {
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
    if (affordance.getSemanticTypes().size() > 1) {
      affordanceObj.add(JWot.SEMANTIC_TYPE,
        this.getSemanticTypes(new ArrayList<>(affordance.getSemanticTypes())));
    } else if (!affordance.getSemanticTypes().isEmpty()) {
      affordanceObj.add(JWot.SEMANTIC_TYPE,
        this.getPrefixedAnnotation(affordance.getSemanticTypes().stream().findFirst().orElse("")));
    }

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
      form.getOperationTypes().forEach(op -> {
        if (JWot.JSON_OPERATION_TYPES.containsKey(op)) {
          opArray.add((String) JWot.JSON_OPERATION_TYPES.get(op));
        } else {
          opArray.add(op);
        }
      });
      formObj.add(JWot.OPERATIONS, opArray);

      //Add methodName only if there is one operation type to avoid ambiguity
      form.getMethodName().ifPresent(m -> {
        if (form.getOperationTypes().size() == 1)
          formObj.add(JWot.METHOD, m);
      });
      formArray.add(formObj);
    });
    return formArray;
  }
}
