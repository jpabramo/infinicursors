package jpabramo.infinicursors;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.http.fileupload.FileUtils;

public class CursorServer implements AutoCloseable {
  public final Path SERVER_DIR = Path.of("www/");
  public int port;
  public Tomcat tom;

  public Thread broadcastThread = new Thread() {
    @Override
    public void run() {
      while(!isInterrupted()) {
        Set<CursorUser> usersToUpdate;
        synchronized(CursorUser.USERS_TO_UPDATE) {
          try {
            CursorUser.USERS_TO_UPDATE.wait();
          } catch (InterruptedException e) {
            break;
          }

          if(CursorUser.USERS_TO_UPDATE.isEmpty()) {
            continue;
          }

          usersToUpdate = new HashSet<>(CursorUser.USERS_TO_UPDATE);
          CursorUser.USERS_TO_UPDATE.clear();
        }
        
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        
        try {
          for(CursorUser user : usersToUpdate) {
            if(user.command != null) {
              // System.out.println("Adding " + user.command + " to the buffer");
              buffer.write(user.command.toBuff().array());
            }
          }
        } catch (IOException e) {
          e.printStackTrace();
          continue;
        }
        
        ByteBuffer command = ByteBuffer.wrap(buffer.toByteArray());
        
        // System.out.printf("Assembled message: %s%n", Arrays.toString(command.array()));
        
        for(CursorUser user : CursorUser.INSTANCES.values()) {
          if(user.client != null) {
            // System.out.printf("Sending a message to id %d %n", user.id);
            user.client.sendBinary(command);
          }
        }
      }
    };
  };

  public CursorServer(int port) {
    this.port = port;

    tom = new Tomcat();
    tom.setPort(port);
    tom.setBaseDir(".");
  }

  public void start() throws IOException, LifecycleException {
    File webapps = new File("webapps");
    if(webapps.exists()) {
      FileUtils.cleanDirectory(webapps);
    } else {
      webapps.mkdirs();
    }
    
    tom.addWebapp("", SERVER_DIR.toAbsolutePath().toString());
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      try {
        tom.stop();
      } catch (LifecycleException e) {
        e.printStackTrace();
      }
    }));
    
    tom.getConnector().setURIEncoding("UTF-8");
    tom.start();
    broadcastThread.start();
  }

  public static void main(String[] args) throws IOException, LifecycleException {
    try (CursorServer server = new CursorServer(8080)) {
      server.start();
      server.tom.getServer().await();
    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void close() throws Exception {
    broadcastThread.interrupt();
    tom.stop();
    tom.destroy();
  }
}
