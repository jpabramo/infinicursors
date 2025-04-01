package jpabramo.infinicursors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.websocket.RemoteEndpoint.Async;
import jpabramo.infinicursors.commands.Command;
import jpabramo.infinicursors.commands.HideCommand;
import jpabramo.infinicursors.commands.PositionCommand;

public class CursorUser {
  public static final Map<Integer, CursorUser> INSTANCES = new HashMap<>();
  public static final Set<CursorUser> USERS_TO_UPDATE = new HashSet<>();

  public Integer id;
  public Integer x = 0, y = 0;
  public Async client;
  private boolean hidden = false;
  public int timestamp;
  public byte skin;
  public Command command;

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
    command = new PositionCommand(this);

    synchronized (USERS_TO_UPDATE) {
      USERS_TO_UPDATE.add(this);
      USERS_TO_UPDATE.notify();
    }
  }

  public void setPos(Integer x, Integer y) {
    hidden = false;
    this.x = x;
    this.y = y;

    broadcastPos();
  }

  private void broadcastHide() {
    command = new HideCommand(this);

    synchronized (USERS_TO_UPDATE) {
      USERS_TO_UPDATE.add(this);
      USERS_TO_UPDATE.notify();
    }
  }

  public void destroy() {
    INSTANCES.remove(id);

    broadcastHide();
  }

  public void hide() {
    hidden = true;
    broadcastHide();
  }

}