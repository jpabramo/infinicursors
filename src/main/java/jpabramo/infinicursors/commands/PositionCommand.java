package jpabramo.infinicursors.commands;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import jpabramo.infinicursors.CursorUser;

public class PositionCommand extends Command {
  public static final byte CMD_CODE = (byte) 0;
  private static final int BUFF_SIZE = 14;

  private int x;
  private int y;
  private byte skin;

  @Override
  public void accept(CursorUser user) {
    user.x = x;
    user.y = y;
    user.timestamp = skin;
    super.accept(user);
  }

  public PositionCommand(ByteBuffer buff) {
    super(buff);

    this.x = buff.getInt();
    this.y = buff.getInt();
    this.skin = buff.get();
  }


  public PositionCommand(CursorUser user) {
    super(user);
  }

  @Override
  public ByteBuffer toBuff() {
    ByteBuffer retval = ByteBuffer.allocate(BUFF_SIZE);
    retval.order(ByteOrder.LITTLE_ENDIAN)
      .put(CMD_CODE).putInt(user.id)
      .putInt(user.x).putInt(user.y)
      .put(skin);
    
      retval.position(0);
    return retval;
  }

  @Override
  public String toString() {
    return String.format("PositionCommand #%d %dx%d %d", user.id, x, y, skin);
  }
}
