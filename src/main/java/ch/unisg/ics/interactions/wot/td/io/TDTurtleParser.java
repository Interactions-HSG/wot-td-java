package ch.unisg.ics.interactions.wot.td.io;

import ch.unisg.ics.interactions.wot.td.ThingDescription;
import ch.unisg.ics.interactions.wot.td.vocabularies.HCTL;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.helpers.TurtleParserSettings;
import org.eclipse.rdf4j.rio.turtle.TurtleParser;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A class extending {@link TurtleParser} for parsing TDs represented in {@link ThingDescription.TDFormat#RDF_TURTLE}.
 * <p>
 * The parser is an extension of {@link TurtleParser} that is lenient in syntax validation of URI values
 * for the object property <a href="https://www.w3.org/2019/wot/hypermedia#hasTarget">hctl:hasTarget</a>.
 */
public class TDTurtleParser extends TurtleParser {

  private final List<IRI> predicates;
  private final ValueFactory rdf = SimpleValueFactory.getInstance();

  protected TDTurtleParser() {
    super();
    List<IRI> predicatesAsync = new ArrayList<IRI>();
    predicates = Collections.synchronizedList(predicatesAsync);
  }

  @Override
  public synchronized void parse(Reader reader, String baseURI)
    throws IOException, RDFParseException, RDFHandlerException {
    predicates.clear();
    super.parse(reader, baseURI);
  }

  @Override
  protected void parsePredicateObjectList() throws IOException, RDFParseException, RDFHandlerException {
    predicate = parsePredicate();
    predicates.add(predicate);

    skipWSC();

    parseObjectList();

    while (skipWSC() == ';') {
      readCodePoint();

      int c = skipWSC();

      if (c == '.' || // end of triple
        c == ']' || c == '}') { // end of predicateObjectList inside
        break;
      } else if (c == ';') {
        // empty predicateObjectList, skip to next
        continue;
      }

      predicate = parsePredicate();
      predicates.add(predicate);
      skipWSC();

      parseObjectList();
    }
  }

  @Override
  protected Value parseValue() throws IOException, RDFParseException, RDFHandlerException {
    if (getParserConfig().get(TurtleParserSettings.ACCEPT_TURTLESTAR) && peekIsTripleValue()) {
      return parseTripleValue();
    }

    int c = peekCodePoint();

    Value value;
    if (c == '<') {
      value = parseTargetOrOtherURI();
    } else {
      value = super.parseValue();
    }
    return value;
  }

  protected Value parseTargetOrOtherURI() throws IOException {
    int predicateNum = predicates.size();

    Value value;
    //href values will be later resolved against the base URI of the Thing Description
    if ((predicateNum > 0) && rdf.createIRI(HCTL.hasTarget).equals(predicates.get(predicateNum - 1))) {
      value = parseFormTargetURI();
    } else {
      value = super.parseURI();
    }

    return value;
  }

  private Value parseFormTargetURI() throws IOException {
    StringBuilder strBuff = new StringBuilder();
    int c = readCodePoint();
    verifyCharacterOrFail(c, "<");

    while (true) {
      c = readCodePoint();

      if (c == '>') {
        break;
      } else if (c == -1) {
        throwEOFException();
      }
      strBuff.append(Character.toChars(c));
    }

    String targetURI = strBuff.toString();
    try {
      return rdf.createIRI(targetURI);
    } catch (IllegalArgumentException e) {
      return rdf.createLiteral(targetURI);
    }
  }

}
