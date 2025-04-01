import { Cursor } from "./Cursor.js"

export class Command {
  static buffSize = 1;

  static allFromBuffer(buff) {
    var retval = [];

    while(buff.byteLength > 0) {
      var view = new Uint8Array(buff);
      var cmd = view[0];
      
      if(cmd >= Command.CMD_MAP.length) {
        break;
      }
  
      var cmdClass = this.CMD_MAP[cmd];
      retval.push(cmdClass.fromBuffer(buff));
      buff = buff.slice(cmdClass.buffSize);
    }

    return retval;
  }

  static execute(message) {
    for(var cmd of this.allFromBuffer(message)) {
      cmd.receive();
    }
  }

  receive() {
  }

  toBuffer() {
    
  }

  async send(socket) {
    if(socket.readyState == WebSocket.OPEN) {
      socket.send(this.toBuffer());
    }
  }
}

export class PositionCommand extends Command {
  static CODE = 0;
  static buffSize = 14;

  x = 0;
  y = 0;
    
  static fromBuffer(buff) {
    if(buff.byteLength < this.buffSize) {
      throw new Error("Buffer is too small");
    }
    
    var view = new Int32Array(buff.slice(1,13));
    var byteView = new Int8Array(buff.slice(13));

    var id = view[0];
    var x = view[1];
    var y = view[2];
    var skin = byteView[0];

    return new PositionCommand(id, x, y, skin);
  }

  constructor(id, x, y, skin) {
    super();

    this.id = id;
    this.x = x;
    this.y = y;
    this.skin = skin;
  }

  receive() {
    if(this.id != Cursor.myCursor.id) {
      Cursor.updateCursor(this.id, this.x, this.y, this.skin);
    }
  }

  toBuffer() {
    var buffer = new ArrayBuffer(PositionCommand.buffSize - 4);

    var view = new DataView(buffer);

    view.setUint8(0, PositionCommand.CODE);
    view.setInt32(1, this.x, true);
    view.setInt32(5, this.y, true);
    view.setUint8(9, this.skin);

    return buffer;
  }
}

export class HideCommand extends Command { 
  static buffSize = 5;
  static CODE = 1;
  
  static fromBuffer(buff) {
    if(buff.byteLength < this.buffSize) {
      throw new Error("Buffer is too small");
    }

    var retval = new HideCommand();
    var view = new Int32Array(buff.slice(1, HideCommand.buffSize));
    retval.id = view[0];
    return retval;
  }

  constructor(id) {
    super();
    this.id = id;
  }

  receive() {
    if(this.id != Cursor.myCursor.id) {
      Cursor.removeCursor(this.id);
    }
  }

  toBuffer() {
    var buffer = new ArrayBuffer(HideCommand.buffSize - 4);
    
    var view = new DataView(buffer);
    view.setUint8(0, HideCommand.CODE);

    return buffer;
  }
}

function getCookies() {
  var retval = {};
  for(var line of document.cookie.split(";")) {
    if(line.indexOf("=") == -1) {
      continue;
    }
    var lineSplit = line.trim().split("=");
    var key = lineSplit[0].trim();
    var val = lineSplit[1].trim();
    retval[key] = val;
  } 
  return retval;
}

export class RegisterCommand extends Command {
  static buffSize = 5;
  static CODE = 2;

  static fromBuffer(buff) {
    if(buff.byteLength < this.buffSize) {
      throw new Error("Buffer is too small");
    }
    
    var retval = new RegisterCommand();
    var viewInt = new Int32Array(buff.slice(1, RegisterCommand.buffSize));
    retval.id = viewInt[0];
    return retval;
  }

  receive() {
    Cursor.myCursor = new Cursor(this.id);
    var skin = getCookies()["skin"] || 0;
    Cursor.myCursor.update(0, 0, skin);
    document.styleSheets[0].insertRule(`#${Cursor.myCursor.element.id} {z-index: 2;}`);
    Cursor.myCursor.hide();

    console.info(`Welcome! You are cursor #${this.id}`);
  }
}

Command.CMD_MAP = [
  PositionCommand,
  HideCommand,
  RegisterCommand
];