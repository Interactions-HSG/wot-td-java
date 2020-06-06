package ch.unisg.ics.interactions.wot.td.vocabularies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public final class TD {
  public static final String PREFIX = "https://www.w3.org/2019/wot/td#";
  
  /* Classes */
  public static final IRI Thing = createIRI("Thing");
  public static final IRI ActionAffordance = createIRI("ActionAffordance");
  
  /* Object properties */
  public static final IRI hasBase = createIRI("hasBase");
  public static final IRI name = createIRI("name");
  
  public static final IRI hasInteractionAffordance = createIRI("hasInteractionAffordance");
  public static final IRI hasActionAffordance = createIRI("hasActionAffordance");
  
  public static final IRI hasSecurityConfiguration = createIRI("hasSecurityConfiguration");
  
  public static final IRI hasInputSchema = createIRI("hasInputSchema");
  public static final IRI hasOutputSchema = createIRI("hasOutputSchema");
  
  public static final IRI hasForm = createIRI("hasForm");
  
  /* Named individuals */
  public static final IRI invokeAction = createIRI("invokeAction");
  
  public static IRI createIRI(String fragment) {
    return SimpleValueFactory.getInstance().createIRI(PREFIX + fragment);
  }
  
  private TD() { }
}
