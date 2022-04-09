package ch.unisg.ics.interactions.wot.td.clients;

import org.springframework.messaging.Message;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.Scanner;
import java.util.logging.Logger;

public class HTTPWebSocket {

  private final static Logger LOGGER = Logger.getLogger(TDHttpRequest.class.getCanonicalName());

  public HTTPWebSocket() {

  }

  public void connect(){
      WebSocketClient client = new StandardWebSocketClient();

      WebSocketStompClient stompClient = new WebSocketStompClient(client);
      stompClient.setMessageConverter(new MappingJackson2MessageConverter());

      StompSessionHandler sessionHandler = new MyStompSessionHandler();
      stompClient.connect("wss://demo.piesocket.com/v3/channel_1?" +
        "api_key=oCdCMcMPQpbvNjUIzqtvF1d2X2okWpDQj4AwARJuAgtjhzKxVEjQU6IdCjwm&notify_self", sessionHandler);

      new Scanner(System.in).nextLine(); // Don't close immediately.
  }


    private class MyStompSessionHandler implements StompSessionHandler {
    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
      session.subscribe("/test", this);
      session.send("/app/chat", "getSampleMessage()");
    }

      @Override
      public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {

      }

      @Override
      public void handleTransportError(StompSession session, Throwable exception) {

      }

      @Override
      public Type getPayloadType(StompHeaders headers) {
        return null;
      }

      @Override
    public void handleFrame(StompHeaders headers, Object payload) {
      Message msg = (Message) payload;
      LOGGER.info("Received : " + msg.toString());
    }
  }
}


