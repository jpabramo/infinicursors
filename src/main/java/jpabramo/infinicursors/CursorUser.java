package jpabramo.infinicursors;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.websocket.RemoteEndpoint.Async;

public class CursorUser {
  public static final Map<Integer, CursorUser> INSTANCES = new HashMap<>();
  public static final Set<CursorUser> USERS_TO_UPDATE = new HashSet<>();

  public Integer id;
  public Integer x = 0, y = 0;
  public Async client;
  public byte[] command;
  private boolean hidden = false;
  public byte skinId = 0;

  public boolean isHidden() {
    return hidden;
  }

  public CursorUser() {
    id = 0;
    synchronized(INSTANCES) {
      while (INSTANCES.keySet().contains(id)) {
        id++;
      }
      INSTANCES.put(id, this);
    }
  }

  private void broadcastPos() {
    ByteBuffer buff = ByteBuffer.allocate(14);
    buff.order(ByteOrder.LITTLE_ENDIAN)
        .put((byte) 0).putInt(id)
        .putInt(x).putInt(y).put(skinId);
    command = buff.array();

    synchronized (USERS_TO_UPDATE) {
      USERS_TO_UPDATE.add(this);
      USERS_TO_UPDATE.notifyAll();
    }
  }

  public void setPos(Integer x, Integer y) {
    hidden = false;
    this.x = x;
    this.y = y;

    broadcastPos();
  }

  private void broadcastDestroy() {
    ByteBuffer buff = ByteBuffer.allocate(5);
    buff.order(ByteOrder.LITTLE_ENDIAN).put((byte) 1).putInt(id);
    command = buff.array();

    synchronized (USERS_TO_UPDATE) {
      USERS_TO_UPDATE.add(this);
      USERS_TO_UPDATE.notifyAll();
    }
  }

  public void destroy() {
    INSTANCES.remove(id);

    broadcastDestroy();
  }

  public void hide() {
    hidden = true;
    broadcastDestroy();
  }

}