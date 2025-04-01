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
import jpabramo.infinicursors.commands.Command;
import jpabramo.infinicursors.commands.PositionCommand;
import jpabramo.infinicursors.commands.RegisterCommand;

@ServerEndpoint("/sock")
public class CursorSocket {
  public CursorUser user;

  @OnOpen
  public void registerUser(Session session) {
    System.out.println("Socket Open");
    user = new CursorUser();
    Command reg = new RegisterCommand(user);
    try {
      ByteArrayOutputStream stream = new ByteArrayOutputStream();

      stream.write(reg.toBuff().array());

      for (CursorUser peer : CursorUser.INSTANCES.values())
        if (!peer.isHidden() && peer != user) {
          Command cmd = new PositionCommand(peer);
          stream.write(cmd.toBuff().array());
        }

      ByteBuffer buff = ByteBuffer.wrap(stream.toByteArray());
      session.getBasicRemote().sendBinary(buff);

      user.client = session.getAsyncRemote();
    } catch (IOException e) {
      e.printStackTrace();
    }

    System.out.println("Registered new user: " + user.id);
  }

  @OnMessage
  public void runCommand(byte[] message) {
    // System.out.println("Received new message: " + Arrays.toString(message));
    ByteBuffer buff = ByteBuffer.wrap(message);
    buff.order(ByteOrder.LITTLE_ENDIAN);

    Command cmd = Command.fromBuffer(buff);
    cmd.accept(user);
    // System.out.printf("Received command %s%n", cmd.toString());
  }

  @OnError
  public void error(Throwable error) {
    // error.printStackTrace();
  }

  @OnClose
  public void destroyUser() {
    System.out.println("User disconnected");
    user.destroy();
    System.out.println("User destroyed");
  }
}
