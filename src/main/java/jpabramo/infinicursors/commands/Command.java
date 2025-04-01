package jpabramo.infinicursors.commands;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

import jpabramo.infinicursors.CursorUser;

public abstract class Command implements Consumer<CursorUser> {
  public static final byte CMD_CODE = (byte) -1;

  protected CursorUser user;

  @Override
  public void accept(CursorUser user) {
    user.command = this;
    this.user = user;
    synchronized(CursorUser.USERS_TO_UPDATE) {
      CursorUser.USERS_TO_UPDATE.add(user);
      CursorUser.USERS_TO_UPDATE.notify();
    }
  }

  private static final Class<?>[] COMMAND_CLASS = {
    PositionCommand.class,
    HideCommand.class,
    RegisterCommand.class
  };

  public static Command fromBuffer(ByteBuffer buff) {
    int command = buff.get();
    Class<?> cmdClass = COMMAND_CLASS[command];
    try {
      return (Command) cmdClass.cast(cmdClass.getConstructor(ByteBuffer.class).newInstance(buff));
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
        | NoSuchMethodException | SecurityException e) {
      e.printStackTrace();
      return null;
    }
  }

  public Command(ByteBuffer buff) {

  }

  public Command(CursorUser user) {
    this.user = user;
  }

  public abstract ByteBuffer toBuff();
}
