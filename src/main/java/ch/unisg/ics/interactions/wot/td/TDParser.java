package ch.unisg.ics.interactions.wot.td;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.RDFSyntax;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.commons.rdf.rdf4j.RDF4J;
import org.apache.commons.rdf.rdf4j.RDF4JGraph;
import org.apache.commons.rdf.rdf4j.RDF4JTriple;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;

import ch.unisg.ics.interactions.wot.td.interaction.Action;
import ch.unisg.ics.interactions.wot.td.interaction.HTTPForm;
import ch.unisg.ics.interactions.wot.td.schema.JSONSchema;
import ch.unisg.ics.interactions.wot.td.schema.Schema;
import ch.unisg.ics.interactions.wot.td.vocabularies.TDVocab;

public class TDParser {
  
  private static final Logger LOGGER = Logger.getLogger(TDParser.class.getName());
  
  private final static RDF4J RDF_IMPL = new RDF4J();
  private final static IRI IS_A = (new RDF4J()).createIRI(RDF.TYPE.stringValue());
  
  
  public static ThingDescription parseFromHttpIRI(IRI thingIRI) throws ParseException, IOException {
    HttpClient client = HttpClientBuilder.create().build();
    HttpResponse response = client.execute(new HttpGet(thingIRI.getIRIString()));
    int statusCode = response.getStatusLine().getStatusCode();
    
    if (statusCode == HttpStatus.SC_OK) {
      String payload = EntityUtils.toString(response.getEntity());
      
      if (payload != null && !payload.isEmpty()) {
        return parseThingDescription(thingIRI, payload, RDFSyntax.TURTLE);
      }
    } else {
      LOGGER.severe("Retrieving entity failed (status code " + statusCode + "): " + thingIRI.getIRIString());
    }
    
    return null;
  }
  
  public static ThingDescription parseFromString(IRI thingIRI, String data, RDFSyntax syntax) throws IllegalArgumentException, IOException {
    return parseThingDescription(thingIRI, data, syntax);
  }
  
  public static Graph stringToGraph(String graphString, String baseIRI, RDFSyntax syntax) throws IllegalArgumentException, IOException {
    StringReader stringReader = new StringReader(graphString);
    
    // TODO: don't hardcode the RDF format
    RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE);
    Model model = new LinkedHashModel();
    rdfParser.setRDFHandler(new StatementCollector(model));
    
    try {
      rdfParser.parse(stringReader, baseIRI);
    }
    catch (RDFParseException e) {
      throw new IllegalArgumentException("RDF parse error: " + e.getMessage());
    }
    catch (RDFHandlerException e) {
      throw new IOException("RDF handler exception: " + e.getMessage());
    }
    finally {
      stringReader.close();
    }
    
    return (new RDF4J()).asGraph(model);
  }
  
  public static String graphToString(Graph graph, RDFSyntax syntax) throws IllegalArgumentException, IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    // TODO: don't hardcode the RDF format
    RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, out);
    
    if (graph instanceof RDF4JGraph) {
      try {
        writer.startRDF();
        try (Stream<RDF4JTriple> stream = ((RDF4JGraph) graph).stream()) {
          stream.forEach(triple -> {
            writer.handleStatement(triple.asStatement());
          });
        }
        writer.endRDF();
      }
      catch (RDFHandlerException e) {
        throw new IOException("RDF handler exception: " + e.getMessage());
      }
      catch (UnsupportedRDFormatException e) {
        throw new IllegalArgumentException("Unsupported RDF syntax: " + e.getMessage()); 
      }
      finally {
        out.close();
      }
    } else {
      throw new IllegalArgumentException("Unsupported RDF graph implementation");
    }
    
    return out.toString();
  }
  
  
  private static ThingDescription parseThingDescription(IRI thingIRI, String rdfDataStr, RDFSyntax syntax) throws IllegalArgumentException, IOException {
    Graph graph = stringToGraph(rdfDataStr, thingIRI.getIRIString(), RDFSyntax.TURTLE);
    
    Optional<Literal> name = getFirstObjectAsLiteral(graph, thingIRI, TDVocab.name);
    Optional<String> thingTitle = (name.isPresent()) ? Optional.of(name.get().getLexicalForm()) : Optional.empty();
    
    Optional<Literal> baseIRIStr = getFirstObjectAsLiteral(graph, thingIRI, TDVocab.base);
    Optional<IRI> baseIRI = (baseIRIStr.isPresent()) ? Optional.of(RDF_IMPL.createIRI(baseIRIStr.get().getLexicalForm())) : Optional.empty();
    
    List<Action> actions = new ArrayList<Action>(parseActions(graph, thingIRI).values());
    
    return new ThingDescription.Builder(thingTitle.get())
        .addURI(thingIRI.getIRIString())
        .addBaseURI(baseIRI.get().getIRIString())
        .addActions(actions)
        .build();
  }
  
  private static Map<BlankNodeOrIRI, Action> parseActions(Graph graph, IRI thingIRI) {
    Map<BlankNodeOrIRI, Action> actions = new HashMap<BlankNodeOrIRI, Action>();
    
    List<BlankNodeOrIRI> actionNodes = graph.stream(thingIRI, TDVocab.interaction, null)
                                              .filter(t -> t.getObject() instanceof BlankNodeOrIRI)
                                              .map(t -> (BlankNodeOrIRI) t.getObject())
                                              .filter(interaction -> graph.contains(interaction, IS_A, TDVocab.Action))
                                              .collect(Collectors.toList());
    
    actionNodes.forEach(node -> {
      Optional<Action> action = parseAction(graph, node);
      if (action.isPresent()) {
        actions.put(node, action.get());
      }
    });
    
    return actions;
  }
  
  private static Optional<Action> parseAction(Graph tdGraph, BlankNodeOrIRI actionNode) {
    Optional<Literal> name = getFirstObjectAsLiteral(tdGraph, actionNode, TDVocab.name);
    Optional<String> actionName = (name.isPresent()) ? Optional.of(name.get().getLexicalForm()) : Optional.empty();
    
    List<String> actionTypes = getActionTypes(tdGraph, actionNode).stream()
        .map(a -> a.getIRIString()).collect(Collectors.toList());
    
    List<HTTPForm> forms = getFormsForAction(tdGraph, actionNode);
    
    Optional<Schema> inputSchema = parseSchemaForAction(tdGraph, actionNode, TDVocab.inputSchema);
    
    
    Action action = (new Action.Builder(forms))
        .addTitle(actionName.get())
        .addTypes(actionTypes)
        .addInputSchema(inputSchema.get())
        .build();
    
    return Optional.of(action);
  }
  
  private static List<IRI> getActionTypes(Graph tdGraph, BlankNodeOrIRI actionIRI) {
    return tdGraph.stream(actionIRI, IS_A, null)
        .filter(triple -> (triple.getObject() instanceof IRI && !triple.getObject().equals(TDVocab.Action)))
        .map(triple -> (IRI) triple.getObject())
        .collect(Collectors.toList());
  }
  
  private static List<HTTPForm> getFormsForAction(Graph tdGraph, BlankNodeOrIRI actionIRI) {
    List<HTTPForm> forms = new ArrayList<HTTPForm>();
    
    List<BlankNodeOrIRI> formIRIs = tdGraph.stream(actionIRI, TDVocab.form, null)
                                            .filter(triple -> triple.getObject() instanceof BlankNodeOrIRI)
                                            .map(triple -> (BlankNodeOrIRI) triple.getObject())
                                            .collect(Collectors.toList());
    
    formIRIs.forEach(form -> {
      HTTPForm httpForm = parseHTTPForm(tdGraph, form);
      if (httpForm != null) {
        forms.add(httpForm);
      }
    });
    
    return forms;
  }
  
  private static HTTPForm parseHTTPForm(Graph tdGraph, BlankNodeOrIRI form) {
    Optional<Literal> name = getFirstObjectAsLiteral(tdGraph, form, (new RDF4J()).createIRI("http://iotschema.org/protocol/httpmethodName"));
    Optional<String> methodName = (name.isPresent()) ? Optional.of(name.get().getLexicalForm()) : Optional.empty();
    
    Optional<Literal> hrefStr = getFirstObjectAsLiteral(tdGraph, form, TDVocab.href);
    Optional<String> href = (hrefStr.isPresent()) ? Optional.of(hrefStr.get().getLexicalForm()) : Optional.empty();
    
    Optional<Literal> mt = getFirstObjectAsLiteral(tdGraph, form, TDVocab.mediaType);
    Optional<String> mediaType = (mt.isPresent()) ? Optional.of(mt.get().getLexicalForm()) : Optional.empty();
    
    List<String> rels = tdGraph.stream(form, TDVocab.rel, null)
                                  .filter(triple -> triple.getObject() instanceof Literal)
                                  .map(triple -> ((Literal) triple.getObject()).getLexicalForm())
                                  .collect(Collectors.toList());
    
    if (!methodName.isPresent() || !href.isPresent() || !mediaType.isPresent()) {
      return null;
    }
    
    return new HTTPForm(methodName.get(), href.get(), mediaType.get(), rels);
  }
  
  private static Optional<Schema> parseSchemaForAction(Graph tdGraph, BlankNodeOrIRI actionIRI, IRI hasSchemaIRI) {
    Optional<BlankNodeOrIRI> schemaIRI = tdGraph.stream(actionIRI, hasSchemaIRI, null)
                                                  .filter(triple -> triple.getObject() instanceof BlankNodeOrIRI)
                                                  .map(triple -> (BlankNodeOrIRI) triple.getObject())
                                                  .findFirst();
    
    if (schemaIRI.isPresent()) {
      // TODO: support other types of schemas
      return Optional.of(new JSONSchema(schemaIRI.get(), tdGraph));
    }
    
    return Optional.empty();
  }
  
  private static Optional<Literal> getFirstObjectAsLiteral(Graph tdGraph, BlankNodeOrIRI subject, IRI propertyIRI) {
    Optional<RDFTerm> term = getFirstObject(tdGraph, subject, propertyIRI);
    
    if (term.isPresent() && term.get() instanceof Literal) {
      return Optional.of((Literal) term.get());
    }
    
    return Optional.empty();
  }
  
  private static Optional<RDFTerm> getFirstObject(Graph tdGraph, BlankNodeOrIRI subject, IRI propertyIRI) {
    if (!tdGraph.contains(subject, propertyIRI, null)) {
      return Optional.empty();
    }
    
    RDFTerm object = tdGraph.stream(subject, propertyIRI, null).findFirst().get().getObject();
    
    return Optional.of(object);
  }
}
