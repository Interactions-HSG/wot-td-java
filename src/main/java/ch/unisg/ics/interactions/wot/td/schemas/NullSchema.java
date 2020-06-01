package ch.unisg.ics.interactions.wot.td.schemas;

import java.util.Set;

public class NullSchema extends DataSchema {

  private NullSchema(Set<String> semanticTypes) {
    super(DataSchema.NULL, semanticTypes);
  }
  
  public static class Builder extends DataSchema.Builder<NullSchema, NullSchema.Builder> {

    @Override
    public NullSchema build() {
      return new NullSchema(semanticTypes);
    }
  }
}
