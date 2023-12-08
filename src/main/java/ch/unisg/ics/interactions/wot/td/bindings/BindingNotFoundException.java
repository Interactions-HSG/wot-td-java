package ch.unisg.ics.interactions.wot.td.bindings;

import ch.unisg.ics.interactions.wot.td.affordances.Form;

/**
 * Exception thrown whenever {@link ProtocolBindings#getBinding(Form)} is called
 * but no suitable binding has been registered for the URI scheme provided in the form.
 */
public class BindingNotFoundException extends RuntimeException {}
