package ch.unisg.ics.interactions.wot.td.bindings;

import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.affordances.Link;
import ch.unisg.ics.interactions.wot.td.schemas.IntegerSchema;
import ch.unisg.ics.interactions.wot.td.schemas.NumberSchema;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BaseOperationTest {

  private class DummyOperation extends BaseOperation {

    private final static String DUMMY_OP = "executeDummyOp";

    private Object payload;

    private long delay = 1000;

    public DummyOperation() {
      super(new Form.Builder("http://example.org/dummy").addOperationType(DUMMY_OP).build(), DUMMY_OP);
    }

    public Object getPayload() {
      return payload;
    }

    public void setDelay(long d) {
      delay = d;
    }

    public long getDelay() {
      return delay;
    }

    @Override
    public void sendRequest() throws IOException {
      new Thread(() -> {
        Response r = new DummyResponse();

        try {
          Thread.sleep(delay);
          onResponse(r);
        } catch (InterruptedException e) {
          // hoping enough time has elapsed to properly test blocking...
          onResponse(r);
        }
      }).start();
    }

    @Override
    public Response getResponse() throws NoResponseException {
      return super.getResponse();
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
  public void testGetResponse() throws IOException {
    DummyOperation op = new DummyOperation();
    long delay = op.getDelay();
    op.setTimeout(2 * delay / 1000);

    long t1 = System.currentTimeMillis();
    op.sendRequest();
    Response r = op.getResponse();
    long t2 = System.currentTimeMillis();

    assertEquals(Response.ResponseStatus.OK, r.getStatus());
    assertTrue(t2 - t1 > 0.8 * delay);
    assertTrue(t2 - t1 < 1.2 * 2 * delay);
  }

  @Test(expected = NoResponseException.class)
  public void testGetNoResponse() throws IOException {
    DummyOperation op = new DummyOperation();
    op.setTimeout(1);
    op.setDelay(2000);

    op.sendRequest();
    op.getResponse(); // should time out
  }

  @Test
  public void testNoTimeout() throws IOException {
    DummyOperation op = new DummyOperation();
    op.setTimeout(0);
    long delay = op.getDelay();

    long t1 = System.currentTimeMillis();
    op.sendRequest();
    Response r = op.getResponse();
    long t2 = System.currentTimeMillis();

    assertEquals(Response.ResponseStatus.OK, r.getStatus());
    assertTrue(t2 - t1 > 0.8 * delay);
    assertTrue(t2 - t1 < 1.2 * delay);
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

  @Test
  public void testNumberCasting() throws IOException {
    BaseOperation intOp = new DummyOperation();
    intOp.setPayload(new IntegerSchema.Builder().build(), (short) 2);
    intOp.sendRequest();

    assertEquals(Response.ResponseStatus.OK, intOp.getResponse().getStatus());

    BaseOperation nbOp = new DummyOperation();
    nbOp.setPayload(new NumberSchema.Builder().build(), BigDecimal.valueOf(225657456, 4367));
    nbOp.sendRequest();

    assertEquals(Response.ResponseStatus.OK, nbOp.getResponse().getStatus());
  }

  @Test
  public void testToString() {
    BaseOperation op = new DummyOperation();
    String str = op.toString();

    assertTrue(str.contains(DummyOperation.DUMMY_OP));
  }

}
