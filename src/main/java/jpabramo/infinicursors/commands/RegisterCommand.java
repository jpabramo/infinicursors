package jpabramo.infinicursors.commands;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import jpabramo.infinicursors.CursorUser;

public class RegisterCommand extends Command {
  public static final byte CMD_CODE = (byte) 2;
  public static final byte BUFF_SIZE = 5;

  public RegisterCommand(ByteBuffer buff) {
    super(buff);
  }

  public RegisterCommand(CursorUser user) {
    super(user);
  }

  @Override
  public ByteBuffer toBuff() {
    ByteBuffer retval = ByteBuffer.allocate(BUFF_SIZE).order(ByteOrder.LITTLE_ENDIAN);
    retval.put(CMD_CODE).putInt(user.id);

    return retval;
  }

  @Override
  public String toString() {
    return "RegisterCommand " + user.id;
  }
}
