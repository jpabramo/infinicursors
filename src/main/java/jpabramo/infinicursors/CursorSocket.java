package jpabramo.infinicursors;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/sock")
public class CursorSocket {
  public CursorUser user;

  @OnOpen
  public void registerUser(Session session) {
    // System.out.println("Socket Open");
    user = new CursorUser();
    ByteBuffer buff = ByteBuffer.allocate(4);
    buff.order(ByteOrder.LITTLE_ENDIAN);
    buff.putInt(user.id);
    try {
      buff.position(0);
      session.getBasicRemote().sendBinary(buff);

      ByteArrayOutputStream stream = new ByteArrayOutputStream();

      for(CursorUser user : CursorUser.INSTANCES.values()) if(!user.isHidden()) {
        buff = ByteBuffer.allocate(14);
        buff.order(ByteOrder.LITTLE_ENDIAN);
        buff.put((byte) 0).putInt(user.id).putInt(user.x).putInt(user.y).put(user.skinId);
        stream.write(buff.array());
      }

      buff = ByteBuffer.wrap(stream.toByteArray());
      session.getBasicRemote().sendBinary(buff);

      user.client = session.getAsyncRemote();
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    // System.out.println("Registered new user: " + user.id);
  }

  @OnMessage
  public void updateUser(byte[] message) {
    // System.out.println("Received new message: " + Arrays.toString(message));
    ByteBuffer buff = ByteBuffer.wrap(message);
    buff.order(ByteOrder.LITTLE_ENDIAN);

    if(buff.array().length == 0) {
      // System.out.println("Hiding user");
      user.hide();
      return;
    }

    int x = buff.getInt();
    int y = buff.getInt();
    user.setPos(x, y);

    byte skin = buff.get();
    user.skinId = skin;
    // System.out.printf("%d x %d%n", x, y);
  }

  @OnError
  public void error(Throwable error) {
    error.printStackTrace();
  }

  @OnClose
  public void destroyUser() {
    // System.out.println("User disconnected");
    user.destroy();
    // System.out.println("User destroyed");
  }
}
