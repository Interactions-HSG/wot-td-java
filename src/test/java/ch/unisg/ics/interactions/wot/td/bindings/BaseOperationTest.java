package ch.unisg.ics.interactions.wot.td.bindings;

import ch.unisg.ics.interactions.wot.td.affordances.Link;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class BaseOperationTest {

  private class DummyOperation extends BaseOperation {

    private Object payload;

    public Object getPayload() {
      return payload;
    }

    @Override
    public void sendRequest() throws IOException {
      // do nothing
    }

    @Override
    protected void setObjectPayload(Map<String, Object> payload) {
      this.payload = payload;
    }

    @Override
    protected void setArrayPayload(List<Object> payload) {
      this.payload = payload;
    }

    @Override
    protected void setStringPayload(String payload) {
      this.payload = payload;
    }

    @Override
    protected void setBooleanPayload(Boolean payload) {
      this.payload = payload;
    }

    @Override
    protected void setIntegerPayload(Long payload) {
      this.payload = payload;
    }

    @Override
    protected void setNumberPayload(Double payload) {
      this.payload = payload;
    }

  }

  private class DummyResponseCallback implements ResponseCallback {

    private String state = "init";

    @Override
    public void onResponse(Response response) {
      if (response.getStatus().equals(Response.ResponseStatus.OK)) {
        state = (String) response.getPayload().get();
      }
    }

    @Override
    public void onError() {
      state = "error";
    }

    public String getState() {
      return state;
    }

  }

  private class DummyResponse implements Response {

    @Override
    public ResponseStatus getStatus() {
      return ResponseStatus.OK;
    }

    @Override
    public Optional<Object> getPayload() {
      return Optional.of("ok");
    }

    @Override
    public Collection<Link> getLinks() {
      return new HashSet<>();
    }

  }

  @Test
  public void testOnResponse() throws NoResponseException {
    BaseOperation op = new DummyOperation();

    op.onResponse(new DummyResponse());
    Response r = op.getResponse();
    assertEquals("ok", r.getPayload().get());
  }

  @Test(expected = NoResponseException.class)
  public void testOnError() throws NoResponseException {
    BaseOperation op = new DummyOperation();
    op.onError();
    op.getResponse();
  }

  @Test
  public void testCallback() {
    BaseOperation op = new DummyOperation();
    DummyResponseCallback cb = new DummyResponseCallback();

    op.registerResponseCallback(cb);

    op.onResponse(new DummyResponse());
    assertEquals("ok", cb.getState());

    op.onError();
    assertEquals("error", cb.getState());
  }

}
