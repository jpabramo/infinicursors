const CURSORS = {};

const SOCK_URL = `ws://${location.hostname || "localhost"}/sock`;
var SOCKET;

var command;
var sending = false;
var myId;
var mySkin = getCookies()["mySkin"] || 0;
document.cookie = `mySkin=${mySkin}`;

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

const SKIN_SELECT = document.getElementById("skin_select");

function createCursor(id) {
  var cursor = CURSORS[id] = document.createElement("img");
  cursor.src = `image/cursor-0.svg`;
  cursor.className = "cursor";
  cursor.id = `C${id}`;
  document.body.appendChild(cursor);
  return cursor;
}

async function updateCursor(id, x, y, skin) {
  var cursor = CURSORS[id] || createCursor(id);
  cursor.style.left = x;
  cursor.style.top = y;
  cursor.src = `image/cursor-${skin}.svg`;
}

async function removeCursor(id) {
  var cursor = CURSORS[id];

  if(cursor != undefined) {
    cursor.remove();
    CURSORS[id] = null;
  }
}

async function handleMessage(event) {
  // console.log("Processing received message");
  
  var message;
  if(event.data instanceof Blob) {
    message = await event.data.arrayBuffer();
  } else if(event.data instanceof ArrayBuffer) {
    message = await event.data;
  } else {
    console.warn("Unexpected message type received");
    return;
  }

  var start = 0;
  while(start < message.byteLength) {
    var cmd = new Int8Array(message.slice(start, start+1))[0];
    start += 1;
    var id = new Int32Array(message.slice(start, start+4))[0];
    start += 4;
    // console.log(`Updating cursor #${id}`);
    switch(cmd) {
      case 0:
        var x = new Int32Array(message.slice(start, start+4))[0];
        start += 4;
        var y = new Int32Array(message.slice(start, start+4))[0];
        start += 4;
        var skin = new Int8Array(message.slice(start, start+1))[0];
        start += 1;
        // console.log(`Updating coords = ${x} x ${y}`);
        if(id != myId) {
          updateCursor(id, x, y, skin);
        }
        
        break;
      case 1:
        // console.log(`Removing`);
        if(id != myId) {
          removeCursor(id);
        }
    }
  }

  document.body.style.width = document.body.scrollWidth;
  document.body.style.height = document.body.scrollHeight;
}

async function handleFirstMessage(event) {
  var message;
  if(event.data instanceof Blob) {
    message = await event.data.arrayBuffer();
  } else if(event.data instanceof ArrayBuffer) {
    message = await event.data;
  } else {
    console.warn("Unexpected message type received");
    return;
  }

  var view = new Int32Array(message);
  myId = view[0];
  var cursor = createCursor(myId);
  cursor.src = `image/cursor-${mySkin}.svg`;
  document.styleSheets[0].insertRule(`#${cursor.id} {z-index: 2;}`);
  console.info(`Welcome! You are cursor #${myId}`);
  SOCKET.onmessage = handleMessage
}

SOCKET = createSocket();

function reconnect() {
  console.info("Reconnecting to server...");
  if(SOCKET != undefined) {
    SOCKET.close();
  }

  SOCKET = createSocket();
}

function createSocket() {
  var retval = new WebSocket(SOCK_URL);
  retval.binaryType = "arraybuffer";
  retval.onmessage = handleFirstMessage;
  retval.onerror = (event) => {
    console.warn("The socket has closed with an error");
  }
  retval.onclose = (event) => {
    console.warn("The socket has closed");
  }
  return retval;
}

async function sendCommand(cmd) {
  if(SOCKET.readyState != SOCKET.OPEN) {
    return;
  }

  SOCKET.send(cmd);
}

document.body.onmousemove = async (event) => {
  if(myId != undefined) {
    updateCursor(myId, event.pageX, event.pageY, mySkin);
  }

  updatePosition(event.pageX, event.pageY);
};

document.body.onmouseleave = (event) => {
  if(myId != undefined) {
    removeCursor(myId);
  }

  var cmd = new ArrayBuffer(0);
  sendCommand(cmd)
}

function updatePosition(x, y) {
  var buff = new ArrayBuffer(4);
  var view = new Int32Array(buff);
  var cmd = new Int8Array(9);

  view[0] = x;
  cmd.set(new Int8Array(buff));
  view[0] = y;
  cmd.set(new Int8Array(buff), 4);
  cmd.set(new Int8Array([mySkin]), 8);
  sendCommand(cmd);
}

setInterval(() => {
  if(SOCKET != undefined && SOCKET.readyState == SOCKET.CLOSED) {
    reconnect();
  }
}, 1000);

document.body.oncontextmenu = (event) => {
  SKIN_SELECT.style.display = "flex";
  SKIN_SELECT.style.top = event.pageY;
  SKIN_SELECT.style.left = event.pageX;

  return false;
}

function skinSelect(id) {
  mySkin = id;
  var cursor = CURSORS[myId];
  
  if(cursor != null) {
    cursor.src = `image/cursor-${mySkin}.svg`;
    document.cookie = `mySkin=${mySkin}`;
    updatePosition(
      parseInt(cursor.style.left), 
      parseInt(cursor.style.top)
    );
  }

  SKIN_SELECT.style.display = "none";
}