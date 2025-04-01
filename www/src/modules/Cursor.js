export class Cursor {
  static CURSORS = {};

  static SVG_MAP = {
    0: "cursor-arrow-light",
    1: "cursor-plane-light",
    2: "cursor-arrow-dark",
    3: "cursor-plane-dark"
  }

  static myCursor;

  constructor(id) {
    this.id = id;
    Cursor.CURSORS[id] = this;
    this.create();
  }

  static async updateCursor(id, x, y, skin) {
    var cursor = Cursor.CURSORS[id] || new Cursor(id);
    cursor.create();
    cursor.update(x, y, skin);
  }

  static async removeCursor(id) {
    var cursor = Cursor.CURSORS[id];
    if(cursor != null) {
      cursor.remove();
    }
  }

  update(x, y, skinId = this.skin) {
    this.x = x;
    this.y = y;
    if(skinId != this.skin || this.element == null) {
      this.skin = skinId;
      this.create();
    }

    this.element.style.left = x;
    this.element.style.top = y;
  }

  create() {
    if((this.element = document.getElementById(`C${this.id}`)) != null) {
      this.element.remove();
    }
    this.element = document.getElementById(Cursor.SVG_MAP[this.skin]).cloneNode(true);
    this.element.classList.add("cursor");
    this.element.id = `C${this.id}`;

    document.body.appendChild(this.element);
    this.element.style.left = this.x;
    this.element.style.top = this.y;
  }

  hide() {
    if(this.element == null) {
      return;
    }

    this.element.remove();
    this.element = null;
  }

  remove() {
    this.hide();
    Cursor.CURSORS[this.id] = null;
  }

  id;
  x = 0;
  y = 0;
  skin = 0;
  element = null;
}