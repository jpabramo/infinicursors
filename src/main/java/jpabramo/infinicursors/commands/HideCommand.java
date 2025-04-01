package jpabramo.infinicursors.commands;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import jpabramo.infinicursors.CursorUser;

public class HideCommand extends Command {
  public static final byte CMD_CODE = (byte) 1;
  private static final int BUFF_SIZE = 5;

  @Override
  public void accept(CursorUser user) {
    user.hide();
    super.accept(user);
  }
  
  public HideCommand(ByteBuffer buff) {
    super(buff);
  }

  public HideCommand(CursorUser user) {
    super(user);
  }

  @Override
  public ByteBuffer toBuff() {
    ByteBuffer retval = ByteBuffer.allocate(BUFF_SIZE);
    retval.order(ByteOrder.LITTLE_ENDIAN).put(CMD_CODE).putInt(user.id);
    
    retval.position(0);
    return retval;
  }

  @Override
  public String toString() {
    return "HideCommand #" + user.id;
  }
}
