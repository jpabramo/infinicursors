import { Command, PositionCommand, HideCommand } from "./modules/Command.js";
import { Cursor } from "./modules/Cursor.js";

const SOCK_URL = `ws://${location.hostname || "localhost"}:${location.port}/sock`;
var SOCKET;

function saveSkin() {
  document.cookie = `skin=${Cursor.myCursor.skin}`;
}

const SKIN_SELECT = document.getElementById("skin_select");

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
  
  // console.log(new Uint8Array(message));

  Command.execute(message);

  document.body.style.width = document.body.scrollWidth;
  document.body.style.height = document.body.scrollHeight;
}

SOCKET = createSocket();

function reconnect() {
  console.info("Reconnecting to server...");
  for(var cursorId in Cursor.CURSORS) {
    Cursor.removeCursor(cursorId);
  }

  if(SOCKET != undefined) {
    SOCKET.close();
  }

  SOCKET = createSocket();
}

function createSocket() {
  var retval = new WebSocket(SOCK_URL);
  retval.binaryType = "arraybuffer";
  retval.onmessage = handleMessage;
  retval.onerror = (event) => {
    console.warn("The socket has closed with an error");
  };
  retval.onclose = (event) => {
    console.warn("The socket has closed");
  };
  return retval;
}
document.body.ontouchmove = (event) => {
  event.preventDefault();

  Cursor.updateCursor(
    Cursor.myCursor.id, 
    event.touches.item(0).pageX, 
    event.touches.item(0).pageY, 
    Cursor.myCursor.skin
  );

  new PositionCommand(
    Cursor.myCursor.id,
    event.touches.item(0).pageX,
    event.touches.item(0).pageY,
    Cursor.myCursor.skin
  ).send(SOCKET);
}

document.body.onmousemove = (event) => {
  Cursor.updateCursor(
    Cursor.myCursor.id, 
    event.pageX, 
    event.pageY, 
    Cursor.myCursor.skin
  );

  new PositionCommand(
    Cursor.myCursor.id,
    event.pageX,
    event.pageY,
    Cursor.myCursor.skin
  ).send(SOCKET);
};

document.body.onmouseleave = (event) => {
  Cursor.myCursor.hide();

  new HideCommand(Cursor.myCursor.id).send(SOCKET);
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

function hideSelect() {
  SKIN_SELECT.style.display = "none";
}

function chooseSkin(id) {
  console.debug(`Setting skin to #${id}`);
  Cursor.myCursor.update(
    Cursor.myCursor.x,
    Cursor.myCursor.y,
    id
  );
  saveSkin();
  hideSelect();
}

for(var elem of document.getElementsByClassName("item")) {
  const skinId = parseInt(elem.id.split("_")[1]);
  elem.onclick = () => {
    chooseSkin(skinId);
  }
}

function arrToHex(arr) {
  var retval = "";
  for(var val of arr) {
    retval += val.toString(16).padStart(2, "0");
  }
  return retval;
}

document.body.onclick = (event) => {
  if(event.target == document.body) {
    hideSelect();
  }
}