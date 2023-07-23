package ch.unisg.ics.interactions.wot.td.bindings;

import ch.unisg.ics.interactions.wot.td.affordances.Link;
import org.junit.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

import static junit.framework.TestCase.assertEquals;

public class BaseResponseTest {

  private class DummyResponse extends BaseResponse {

    private final boolean withSingleLinePayload;

    public DummyResponse(Boolean withSingleLinePayload) {
      super(null);

      this.withSingleLinePayload = withSingleLinePayload;
    }

    @Override
    public ResponseStatus getStatus() {
      return ResponseStatus.OK;
    }

    @Override
    public Optional<Object> getPayload() {
      if (withSingleLinePayload) {
        return Optional.of("payload over a single line");
      } else {
        return Optional.of("payload\nover several lines");
      }
    }

    @Override
    public Collection<Link> getLinks() {
      return new HashSet<>();
    }

  }

  @Test
  public void testToString() throws NoResponseException {
    Response res = new DummyResponse(true);

    assertEquals("[DummyResponse] OK, Links: <none>, Payload: payload over a single line", res.toString());

    res = new DummyResponse(false);

    assertEquals("[DummyResponse] OK, Links: <none>, Payload: payload...", res.toString());
  }

}
