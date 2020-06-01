package ch.unisg.ics.interactions.wot.td.schemas;

import java.util.Set;

public class BooleanSchema extends DataSchema {

  private BooleanSchema(Set<String> semanticTypes) {
    super(DataSchema.BOOLEAN, semanticTypes);
  }
  
  public static class Builder extends DataSchema.Builder<BooleanSchema, BooleanSchema.Builder> {

    @Override
    public BooleanSchema build() {
      return new BooleanSchema(semanticTypes);
    }
  }
}
