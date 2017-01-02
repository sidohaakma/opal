/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

define("ace/keyboard/vim", ["require", "exports", "module", "ace/keyboard/vim/commands", "ace/keyboard/vim/maps/util", "ace/lib/useragent"], function (e, t, n) {
    var r = e("./vim/commands"), i = r.coreCommands, s = e("./vim/maps/util"), o = e("../lib/useragent"), u = {i: {command: i.start}, I: {command: i.startBeginning}, a: {command: i.append}, A: {command: i.appendEnd}, "ctrl-f": {command: "gotopagedown"}, "ctrl-b": {command: "gotopageup"}};
    t.handler = {handleMacRepeat: function (e, t, n) {
        if (t == -1)e.inputChar = n, e.lastEvent = "input"; else if (e.inputChar && e.$lastHash == t && e.$lastKey == n) {
            if (e.lastEvent == "input")e.lastEvent = "input1"; else if (e.lastEvent == "input1")return!0
        } else e.$lastHash = t, e.$lastKey = n, e.lastEvent = "keypress"
    }, handleKeyboard: function (e, t, n, s, a) {
        if (t != 0 && (n == "" || n == "\0"))return null;
        t == 1 && (n = "ctrl-" + n);
        if (n == "esc" && t == 0 || n == "ctrl-[")return{command: i.stop};
        if (e.state == "start") {
            o.isMac && this.handleMacRepeat(e, t, n) && (t = -1, n = e.inputChar);
            if (t == -1 || t == 1 || t == 0 && n.length > 1)return r.inputBuffer.idle && u[n] ? u[n] : {command: {exec: function (e) {
                return r.inputBuffer.push(e, n)
            }}};
            if (n.length == 1 && (t == 0 || t == 4))return{command: "null", passEvent: !0};
            if (n == "esc" && t == 0)return{command: i.stop}
        } else if (n == "ctrl-w")return{command: "removewordleft"}
    }, attach: function (e) {
        e.on("click", t.onCursorMove), s.currentMode !== "insert" && r.coreCommands.stop.exec(e), e.$vimModeHandler = this
    }, detach: function (e) {
        e.removeListener("click", t.onCursorMove), s.noMode(e), s.currentMode = "normal"
    }, actions: r.actions, getStatusText: function () {
        return s.currentMode == "insert" ? "INSERT" : s.onVisualMode ? (s.onVisualLineMode ? "VISUAL LINE " : "VISUAL ") + r.inputBuffer.status : r.inputBuffer.status
    }}, t.onCursorMove = function (e) {
        r.onCursorMove(e.editor, e), t.onCursorMove.scheduled = !1
    }
}), define("ace/keyboard/vim/commands", ["require", "exports", "module", "ace/keyboard/vim/maps/util", "ace/keyboard/vim/maps/motions", "ace/keyboard/vim/maps/operators", "ace/keyboard/vim/maps/aliases", "ace/keyboard/vim/registers"], function (e, t, n) {
    "never use strict";
    function g(e) {
        m.previous = {action: {action: {fn: e}}}
    }

    var r = e("./maps/util"), i = e("./maps/motions"), s = e("./maps/operators"), o = e("./maps/aliases"), u = e("./registers"), a = 1, f = 2, l = 3, c = 4, h = 8, p = function (t, n, r) {
        while (0 < n--)t.apply(this, r)
    }, d = function (e) {
        var t = e.renderer, n = t.$cursorLayer.getPixelPosition(), r = n.top, i = h * t.layerConfig.lineHeight;
        2 * i > t.$size.scrollerHeight && (i = t.$size.scrollerHeight / 2), t.scrollTop > r - i && t.session.setScrollTop(r - i), t.scrollTop + t.$size.scrollerHeight < r + i + t.lineHeight && t.session.setScrollTop(r + i + t.lineHeight - t.$size.scrollerHeight)
    }, v = t.actions = {z: {param: !0, fn: function (e, t, n, r) {
        switch (r) {
            case"z":
                e.renderer.alignCursor(null, .5);
                break;
            case"t":
                e.renderer.alignCursor(null, 0);
                break;
            case"b":
                e.renderer.alignCursor(null, 1)
        }
    }}, r: {param: !0, fn: function (e, t, n, r) {
        r && r.length && (p(function () {
            e.insert(r)
        }, n || 1), e.navigateLeft())
    }}, R: {fn: function (e, t, n, i) {
        r.insertMode(e), e.setOverwrite(!0)
    }}, "~": {fn: function (e, t, n) {
        p(function () {
            var t = e.selection.getRange();
            t.isEmpty() && t.end.column++;
            var n = e.session.getTextRange(t), r = n.toUpperCase();
            r == n ? e.navigateRight() : e.session.replace(t, r)
        }, n || 1)
    }}, "*": {fn: function (e, t, n, r) {
        e.selection.selectWord(), e.findNext(), d(e);
        var i = e.selection.getRange();
        e.selection.setSelectionRange(i, !0)
    }}, "#": {fn: function (e, t, n, r) {
        e.selection.selectWord(), e.findPrevious(), d(e);
        var i = e.selection.getRange();
        e.selection.setSelectionRange(i, !0)
    }}, m: {param: !0, fn: function (e, t, n, r) {
        var i = e.session, s = i.vimMarkers || (i.vimMarkers = {}), o = e.getCursorPosition();
        s[r] || (s[r] = e.session.doc.createAnchor(o)), s[r].setPosition(o.row, o.column, !0)
    }}, n: {fn: function (e, t, n, r) {
        var i = e.getLastSearchOptions();
        i.backwards = !1, e.selection.moveCursorRight(), e.selection.clearSelection(), e.findNext(i), d(e);
        var s = e.selection.getRange();
        s.end.row = s.start.row, s.end.column = s.start.column, e.selection.setSelectionRange(s, !0)
    }}, N: {fn: function (e, t, n, r) {
        var i = e.getLastSearchOptions();
        i.backwards = !0, e.findPrevious(i), d(e);
        var s = e.selection.getRange();
        s.end.row = s.start.row, s.end.column = s.start.column, e.selection.setSelectionRange(s, !0)
    }}, v: {fn: function (e, t, n, i) {
        e.selection.selectRight(), r.visualMode(e, !1)
    }, acceptsMotion: !0}, V: {fn: function (e, t, n, i) {
        var s = e.getCursorPosition().row;
        e.selection.clearSelection(), e.selection.moveCursorTo(s, 0), e.selection.selectLineEnd(), e.selection.visualLineStart = s, r.visualMode(e, !0)
    }, acceptsMotion: !0}, Y: {fn: function (e, t, n, i) {
        r.copyLine(e)
    }}, p: {fn: function (e, t, n, r) {
        var i = u._default;
        e.setOverwrite(!1);
        if (i.isLine) {
            var s = e.getCursorPosition(), o = i.text.split("\n");
            e.session.getDocument().insertLines(s.row + 1, o), e.moveCursorTo(s.row + 1, 0)
        } else e.navigateRight(), e.insert(i.text), e.navigateLeft();
        e.setOverwrite(!0), e.selection.clearSelection()
    }}, P: {fn: function (e, t, n, r) {
        var i = u._default;
        e.setOverwrite(!1);
        if (i.isLine) {
            var s = e.getCursorPosition(), o = i.text.split("\n");
            e.session.getDocument().insertLines(s.row, o), e.moveCursorTo(s.row, 0)
        } else e.insert(i.text);
        e.setOverwrite(!0), e.selection.clearSelection()
    }}, J: {fn: function (e, t, n, r) {
        var i = e.session;
        t = e.getSelectionRange();
        var s = {row: t.start.row, column: t.start.column};
        n = n || t.end.row - t.start.row;
        var o = Math.min(s.row + (n || 1), i.getLength() - 1);
        t.start.column = i.getLine(s.row).length, t.end.column = i.getLine(o).length, t.end.row = o;
        var u = "";
        for (var a = s.row; a < o; a++) {
            var f = i.getLine(a + 1);
            u += " " + /^\s*(.*)$/.exec(f)[1] || ""
        }
        i.replace(t, u), e.moveCursorTo(s.row, s.column)
    }}, u: {fn: function (e, t, n, r) {
        n = parseInt(n || 1, 10);
        for (var i = 0; i < n; i++)e.undo();
        e.selection.clearSelection()
    }}, "ctrl-r": {fn: function (e, t, n, r) {
        n = parseInt(n || 1, 10);
        for (var i = 0; i < n; i++)e.redo();
        e.selection.clearSelection()
    }}, ":": {fn: function (e, t, n, r) {
    }}, "/": {fn: function (e, t, n, r) {
    }}, "?": {fn: function (e, t, n, r) {
    }}, ".": {fn: function (e, t, n, i) {
        r.onInsertReplaySequence = m.lastInsertCommands;
        var s = m.previous;
        s && m.exec(e, s.action, s.param)
    }}, "ctrl-x": {fn: function (e, t, n, r) {
        e.modifyNumber(-(n || 1))
    }}, "ctrl-a": {fn: function (e, t, n, r) {
        e.modifyNumber(n || 1)
    }}}, m = t.inputBuffer = {accepting: [a, f, l, c], currentCmd: null, currentCount: "", status: "", operator: null, motion: null, lastInsertCommands: [], push: function (e, t, n) {
        var r = !0;
        this.idle = !1;
        var u = this.waitingForParam;
        if (u)this.exec(e, u, t); else if (t === "0" && !this.currentCount.length || !t.match(/^\d+$/) || !this.isAccepting(a))if (!this.operator && this.isAccepting(f) && s[t])this.operator = {ch: t, count: this.getCount()}, this.currentCmd = f, this.accepting = [a, l, c], this.exec(e, {operator: this.operator}); else if (i[t] && this.isAccepting(l)) {
            this.currentCmd = l;
            var h = {operator: this.operator, motion: {ch: t, count: this.getCount()}};
            i[t].param ? this.waitForParam(h) : this.exec(e, h)
        } else if (o[t] && this.isAccepting(l))o[t].operator.count = this.getCount(), this.exec(e, o[t]); else if (v[t] && this.isAccepting(c)) {
            var p = {action: {fn: v[t].fn, count: this.getCount()}};
            v[t].param ? this.waitForParam(p) : this.exec(e, p), v[t].acceptsMotion && (this.idle = !1)
        } else this.operator ? this.exec(e, {operator: this.operator}, t) : (r = t.length == 1, this.reset()); else this.currentCount += t, this.currentCmd = a, this.accepting = [a, f, l, c];
        if (this.waitingForParam || this.motion || this.operator)this.status += t; else if (this.currentCount)this.status = this.currentCount; else {
            if (!this.status)return r;
            this.status = ""
        }
        return e._emit("changeStatus"), r
    }, waitForParam: function (e) {
        this.waitingForParam = e
    }, getCount: function () {
        var e = this.currentCount;
        return this.currentCount = "", e && parseInt(e, 10)
    }, exec: function (e, t, n) {
        var o = t.motion, u = t.operator, a = t.action;
        n || (n = t.param), u && (this.previous = {action: t, param: n});
        if (u && !e.selection.isEmpty()) {
            s[u.ch].selFn && (s[u.ch].selFn(e, e.getSelectionRange(), u.count, n), this.reset());
            return
        }
        if (!o && !a && u && n)s[u.ch].fn(e, null, u.count, n), this.reset(); else if (o) {
            var f = function (t) {
                t && typeof t == "function" && (o.count && !l.handlesCount ? p(t, o.count, [e, null, o.count, n]) : t(e, null, o.count, n))
            }, l = i[o.ch], c = l.sel;
            u ? c && p(function () {
                f(l.sel), s[u.ch].fn(e, e.getSelectionRange(), u.count, n)
            }, u.count || 1) : (r.onVisualMode || r.onVisualLineMode) && c ? f(l.sel) : f(l.nav), this.reset()
        } else a && (a.fn(e, e.getSelectionRange(), a.count, n), this.reset());
        y(e)
    }, isAccepting: function (e) {
        return this.accepting.indexOf(e) !== -1
    }, reset: function () {
        this.operator = null, this.motion = null, this.currentCount = "", this.status = "", this.accepting = [a, f, l, c], this.idle = !0, this.waitingForParam = null
    }};
    t.coreCommands = {start: {exec: function b(e) {
        r.insertMode(e), g(b)
    }}, startBeginning: {exec: function w(e) {
        e.navigateLineStart(), r.insertMode(e), g(w)
    }}, stop: {exec: function (t) {
        m.reset(), r.onVisualMode = !1, r.onVisualLineMode = !1, m.lastInsertCommands = r.normalMode(t)
    }}, append: {exec: function E(e) {
        var t = e.getCursorPosition(), n = e.session.getLine(t.row).length;
        n && e.navigateRight(), r.insertMode(e), g(E)
    }}, appendEnd: {exec: function S(e) {
        e.navigateLineEnd(), r.insertMode(e), g(S)
    }}};
    var y = t.onCursorMove = function (e, t) {
        if (r.currentMode === "insert" || y.running)return;
        if (!e.selection.isEmpty()) {
            y.running = !0;
            if (r.onVisualLineMode) {
                var n = e.selection.visualLineStart, i = e.getCursorPosition().row;
                if (n <= i) {
                    var s = e.session.getLine(i);
                    e.selection.clearSelection(), e.selection.moveCursorTo(n, 0), e.selection.selectTo(i, s.length)
                } else {
                    var s = e.session.getLine(n);
                    e.selection.clearSelection(), e.selection.moveCursorTo(n, s.length), e.selection.selectTo(i, 0)
                }
            }
            y.running = !1;
            return
        }
        t && (r.onVisualLineMode || r.onVisualMode) && (e.selection.clearSelection(), r.normalMode(e)), y.running = !0;
        var o = e.getCursorPosition(), u = e.session.getLine(o.row).length;
        u && o.column === u && e.navigateLeft(), y.running = !1
    }
}), define("ace/keyboard/vim/maps/util", ["require", "exports", "module", "ace/keyboard/vim/registers", "ace/lib/dom"], function (e, t, n) {
    var r = e("../registers"), i = e("../../../lib/dom");
    i.importCssString(".insert-mode .ace_cursor{    border-left: 2px solid #333333;}.ace_dark.insert-mode .ace_cursor{    border-left: 2px solid #eeeeee;}.normal-mode .ace_cursor{    border: 0!important;    background-color: red;    opacity: 0.5;}", "vimMode"), n.exports = {onVisualMode: !1, onVisualLineMode: !1, currentMode: "normal", noMode: function (e) {
        e.unsetStyle("insert-mode"), e.unsetStyle("normal-mode"), e.commands.recording && e.commands.toggleRecording(e), e.setOverwrite(!1)
    }, insertMode: function (e) {
        this.currentMode = "insert", e.setStyle("insert-mode"), e.unsetStyle("normal-mode"), e.setOverwrite(!1), e.keyBinding.$data.buffer = "", e.keyBinding.$data.state = "insertMode", this.onVisualMode = !1, this.onVisualLineMode = !1, this.onInsertReplaySequence ? (e.commands.macro = this.onInsertReplaySequence, e.commands.replay(e), this.onInsertReplaySequence = null, this.normalMode(e)) : (e._emit("changeStatus"), e.commands.recording || e.commands.toggleRecording(e))
    }, normalMode: function (e) {
        this.currentMode = "normal", e.unsetStyle("insert-mode"), e.setStyle("normal-mode"), e.clearSelection();
        var t;
        return e.getOverwrite() || (t = e.getCursorPosition(), t.column > 0 && e.navigateLeft()), e.setOverwrite(!0), e.keyBinding.$data.buffer = "", e.keyBinding.$data.state = "start", this.onVisualMode = !1, this.onVisualLineMode = !1, e._emit("changeStatus"), e.commands.recording ? (e.commands.toggleRecording(e), e.commands.macro) : []
    }, visualMode: function (e, t) {
        if (this.onVisualLineMode && t || this.onVisualMode && !t) {
            this.normalMode(e);
            return
        }
        e.setStyle("insert-mode"), e.unsetStyle("normal-mode"), e._emit("changeStatus"), t ? this.onVisualLineMode = !0 : (this.onVisualMode = !0, this.onVisualLineMode = !1)
    }, getRightNthChar: function (e, t, n, r) {
        var i = e.getSession().getLine(t.row), s = i.substr(t.column + 1).split(n);
        return r < s.length ? s.slice(0, r).join(n).length : null
    }, getLeftNthChar: function (e, t, n, r) {
        var i = e.getSession().getLine(t.row), s = i.substr(0, t.column).split(n);
        return r < s.length ? s.slice(-1 * r).join(n).length : null
    }, toRealChar: function (e) {
        return e.length === 1 ? e : /^shift-./.test(e) ? e[e.length - 1].toUpperCase() : ""
    }, copyLine: function (e) {
        var t = e.getCursorPosition();
        e.selection.clearSelection(), e.moveCursorTo(t.row, t.column), e.selection.selectLine(), r._default.isLine = !0, r._default.text = e.getCopyText().replace(/\n$/, ""), e.selection.clearSelection(), e.moveCursorTo(t.row, t.column)
    }}
}), define("ace/keyboard/vim/registers", ["require", "exports", "module"], function (e, t, n) {
    "never use strict";
    n.exports = {_default: {text: "", isLine: !1}}
}), define("ace/keyboard/vim/maps/motions", ["require", "exports", "module", "ace/keyboard/vim/maps/util", "ace/search", "ace/range"], function (e, t, n) {
    function s(e) {
        if (typeof e == "function") {
            var t = e;
            e = this
        } else var t = e.getPos;
        return e.nav = function (e, n, r, i) {
            var s = t(e, n, r, i, !1);
            if (!s)return;
            e.clearSelection(), e.moveCursorTo(s.row, s.column)
        }, e.sel = function (e, n, r, i) {
            var s = t(e, n, r, i, !0);
            if (!s)return;
            e.selection.selectTo(s.row, s.column)
        }, e
    }

    function h(e, t, n) {
        return c.$options.needle = t, c.$options.backwards = n == -1, c.find(e.session)
    }

    var r = e("./util"), i = function (e, t) {
        var n = e.renderer.getScrollTopRow(), r = e.getCursorPosition().row, i = r - n;
        t && t.call(e), e.renderer.scrollToRow(e.getCursorPosition().row - i)
    }, o = /[\s.\/\\()\"'-:,.;<>~!@#$%^&*|+=\[\]{}`~?]/, u = /[.\/\\()\"'-:,.;<>~!@#$%^&*|+=\[\]{}`~?]/, a = /\s/, f = function (e, t) {
        var n = e.selection;
        this.range = n.getRange(), t = t || n.selectionLead, this.row = t.row, this.col = t.column;
        var r = e.session.getLine(this.row), i = e.session.getLength();
        this.ch = r[this.col] || "\n", this.skippedLines = 0, this.next = function () {
            return this.ch = r[++this.col] || this.handleNewLine(1), this.ch
        }, this.prev = function () {
            return this.ch = r[--this.col] || this.handleNewLine(-1), this.ch
        }, this.peek = function (t) {
            var n = r[this.col + t];
            return n ? n : t == -1 ? "\n" : this.col == r.length - 1 ? "\n" : e.session.getLine(this.row + 1)[0] || "\n"
        }, this.handleNewLine = function (t) {
            if (t == 1)return this.col == r.length ? "\n" : this.row == i - 1 ? "" : (this.col = 0, this.row++, r = e.session.getLine(this.row), this.skippedLines++, r[0] || "\n");
            if (t == -1)return this.row === 0 ? "" : (this.row--, r = e.session.getLine(this.row), this.col = r.length, this.skippedLines--, "\n")
        }, this.debug = function () {
            console.log(r.substring(0, this.col) + "|" + this.ch + "'" + this.col + "'" + r.substr(this.col + 1))
        }
    }, l = e("../../../search").Search, c = new l, p = e("../../../range").Range;
    n.exports = {w: new s(function (e) {
        var t = new f(e);
        if (t.ch && u.test(t.ch))while (t.ch && u.test(t.ch))t.next(); else while (t.ch && !o.test(t.ch))t.next();
        while (t.ch && a.test(t.ch) && t.skippedLines < 2)t.next();
        return t.skippedLines == 2 && t.prev(), {column: t.col, row: t.row}
    }), W: new s(function (e) {
        var t = new f(e);
        while (t.ch && (!a.test(t.ch) || !!a.test(t.peek(1))) && t.skippedLines < 2)t.next();
        return t.skippedLines == 2 ? t.prev() : t.next(), {column: t.col, row: t.row}
    }), b: new s(function (e) {
        var t = new f(e);
        t.prev();
        while (t.ch && a.test(t.ch) && t.skippedLines > -2)t.prev();
        if (t.ch && u.test(t.ch))while (t.ch && u.test(t.ch))t.prev(); else while (t.ch && !o.test(t.ch))t.prev();
        return t.ch && t.next(), {column: t.col, row: t.row}
    }), B: new s(function (e) {
        var t = new f(e);
        t.prev();
        while (t.ch && (!!a.test(t.ch) || !a.test(t.peek(-1))) && t.skippedLines > -2)t.prev();
        return t.skippedLines == -2 && t.next(), {column: t.col, row: t.row}
    }), e: new s(function (e) {
        var t = new f(e);
        t.next();
        while (t.ch && a.test(t.ch))t.next();
        if (t.ch && u.test(t.ch))while (t.ch && u.test(t.ch))t.next(); else while (t.ch && !o.test(t.ch))t.next();
        return t.ch && t.prev(), {column: t.col, row: t.row}
    }), E: new s(function (e) {
        var t = new f(e);
        t.next();
        while (t.ch && (!!a.test(t.ch) || !a.test(t.peek(1))))t.next();
        return{column: t.col, row: t.row}
    }), l: {nav: function (e) {
        var t = e.getCursorPosition(), n = t.column, r = e.session.getLine(t.row).length;
        r && n !== r && e.navigateRight()
    }, sel: function (e) {
        var t = e.getCursorPosition(), n = t.column, r = e.session.getLine(t.row).length;
        r && n !== r && e.selection.selectRight()
    }}, h: {nav: function (e) {
        var t = e.getCursorPosition();
        t.column > 0 && e.navigateLeft()
    }, sel: function (e) {
        var t = e.getCursorPosition();
        t.column > 0 && e.selection.selectLeft()
    }}, H: {nav: function (e) {
        var t = e.renderer.getScrollTopRow();
        e.moveCursorTo(t)
    }, sel: function (e) {
        var t = e.renderer.getScrollTopRow();
        e.selection.selectTo(t)
    }}, M: {nav: function (e) {
        var t = e.renderer.getScrollTopRow(), n = e.renderer.getScrollBottomRow(), r = t + (n - t) / 2;
        e.moveCursorTo(r)
    }, sel: function (e) {
        var t = e.renderer.getScrollTopRow(), n = e.renderer.getScrollBottomRow(), r = t + (n - t) / 2;
        e.selection.selectTo(r)
    }}, L: {nav: function (e) {
        var t = e.renderer.getScrollBottomRow();
        e.moveCursorTo(t)
    }, sel: function (e) {
        var t = e.renderer.getScrollBottomRow();
        e.selection.selectTo(t)
    }}, k: {nav: function (e) {
        e.navigateUp()
    }, sel: function (e) {
        e.selection.selectUp()
    }}, j: {nav: function (e) {
        e.navigateDown()
    }, sel: function (e) {
        e.selection.selectDown()
    }}, i: {param: !0, sel: function (e, t, n, r) {
        switch (r) {
            case"w":
                e.selection.selectWord();
                break;
            case"W":
                e.selection.selectAWord();
                break;
            case"(":
            case"{":
            case"[":
                var i = e.getCursorPosition(), s = e.session.$findClosingBracket(r, i, /paren/);
                if (!s)return;
                var o = e.session.$findOpeningBracket(e.session.$brackets[r], i, /paren/);
                if (!o)return;
                o.column++, e.selection.setSelectionRange(p.fromPoints(o, s));
                break;
            case"'":
            case'"':
            case"/":
                var s = h(e, r, 1);
                if (!s)return;
                var o = h(e, r, -1);
                if (!o)return;
                e.selection.setSelectionRange(p.fromPoints(o.end, s.start))
        }
    }}, a: {param: !0, sel: function (e, t, n, r) {
        switch (r) {
            case"w":
                e.selection.selectAWord();
                break;
            case"W":
                e.selection.selectAWord();
                break;
            case"(":
            case"{":
            case"[":
                var i = e.getCursorPosition(), s = e.session.$findClosingBracket(r, i, /paren/);
                if (!s)return;
                var o = e.session.$findOpeningBracket(e.session.$brackets[r], i, /paren/);
                if (!o)return;
                s.column++, e.selection.setSelectionRange(p.fromPoints(o, s));
                break;
            case"'":
            case'"':
            case"/":
                var s = h(e, r, 1);
                if (!s)return;
                var o = h(e, r, -1);
                if (!o)return;
                s.column++, e.selection.setSelectionRange(p.fromPoints(o.start, s.end))
        }
    }}, f: new s({param: !0, handlesCount: !0, getPos: function (e, t, n, i, s) {
        var o = e.getCursorPosition(), u = r.getRightNthChar(e, o, i, n || 1);
        if (typeof u == "number")return o.column += u + (s ? 2 : 1), o
    }}), F: new s({param: !0, handlesCount: !0, getPos: function (e, t, n, i, s) {
        var o = e.getCursorPosition(), u = r.getLeftNthChar(e, o, i, n || 1);
        if (typeof u == "number")return o.column -= u + 1, o
    }}), t: new s({param: !0, handlesCount: !0, getPos: function (e, t, n, i, s) {
        var o = e.getCursorPosition(), u = r.getRightNthChar(e, o, i, n || 1);
        if (typeof u == "number")return o.column += u + (s ? 1 : 0), o
    }}), T: new s({param: !0, handlesCount: !0, getPos: function (e, t, n, i, s) {
        var o = e.getCursorPosition(), u = r.getLeftNthChar(e, o, i, n || 1);
        if (typeof u == "number")return o.column -= u, o
    }}), "^": {nav: function (e) {
        e.navigateLineStart()
    }, sel: function (e) {
        e.selection.selectLineStart()
    }}, $: {nav: function (e) {
        e.navigateLineEnd()
    }, sel: function (e) {
        e.selection.selectLineEnd()
    }}, 0: new s(function (e) {
        return{row: e.selection.lead.row, column: 0}
    }), G: {nav: function (e, t, n, r) {
        !n && n !== 0 && (n = e.session.getLength()), e.gotoLine(n)
    }, sel: function (e, t, n, r) {
        !n && n !== 0 && (n = e.session.getLength()), e.selection.selectTo(n, 0)
    }}, g: {param: !0, nav: function (e, t, n, r) {
        switch (r) {
            case"m":
                console.log("Middle line");
                break;
            case"e":
                console.log("End of prev word");
                break;
            case"g":
                e.gotoLine(n || 0);
            case"u":
                e.gotoLine(n || 0);
            case"U":
                e.gotoLine(n || 0)
        }
    }, sel: function (e, t, n, r) {
        switch (r) {
            case"m":
                console.log("Middle line");
                break;
            case"e":
                console.log("End of prev word");
                break;
            case"g":
                e.selection.selectTo(n || 0, 0)
        }
    }}, o: {nav: function (e, t, n, i) {
        n = n || 1;
        var s = "";
        while (0 < n--)s += "\n";
        s.length && (e.navigateLineEnd(), e.insert(s), r.insertMode(e))
    }}, O: {nav: function (e, t, n, i) {
        var s = e.getCursorPosition().row;
        n = n || 1;
        var o = "";
        while (0 < n--)o += "\n";
        o.length && (s > 0 ? (e.navigateUp(), e.navigateLineEnd(), e.insert(o)) : (e.session.insert({row: 0, column: 0}, o), e.navigateUp()), r.insertMode(e))
    }}, "%": new s(function (e) {
        var t = /[\[\]{}()]/g, n = e.getCursorPosition(), r = e.session.getLine(n.row)[n.column];
        if (!t.test(r)) {
            var i = h(e, t);
            if (!i)return;
            n = i.start
        }
        var s = e.session.findMatchingBracket({row: n.row, column: n.column + 1});
        return s
    }), "{": new s(function (e) {
        var t = e.session, n = t.selection.lead.row;
        while (n > 0 && !/\S/.test(t.getLine(n)))n--;
        while (/\S/.test(t.getLine(n)))n--;
        return{column: 0, row: n}
    }), "}": new s(function (e) {
        var t = e.session, n = t.getLength(), r = t.selection.lead.row;
        while (r < n && !/\S/.test(t.getLine(r)))r++;
        while (/\S/.test(t.getLine(r)))r++;
        return{column: 0, row: r}
    }), "ctrl-d": {nav: function (e, t, n, r) {
        e.selection.clearSelection(), i(e, e.gotoPageDown)
    }, sel: function (e, t, n, r) {
        i(e, e.selectPageDown)
    }}, "ctrl-u": {nav: function (e, t, n, r) {
        e.selection.clearSelection(), i(e, e.gotoPageUp)
    }, sel: function (e, t, n, r) {
        i(e, e.selectPageUp)
    }}, "`": new s({param: !0, handlesCount: !0, getPos: function (e, t, n, r, i) {
        var s = e.session, o = s.vimMarkers && s.vimMarkers[r];
        if (o)return o.getPosition()
    }}), "'": new s({param: !0, handlesCount: !0, getPos: function (e, t, n, r, i) {
        var s = e.session, o = s.vimMarkers && s.vimMarkers[r];
        if (o) {
            var u = o.getPosition(), a = e.session.getLine(u.row);
            return u.column = a.search(/\S/), u.column == -1 && (u.column = a.length), u
        }
    }})}, n.exports.backspace = n.exports.left = n.exports.h, n.exports.right = n.exports.l, n.exports.up = n.exports.k, n.exports.down = n.exports.j, n.exports.pagedown = n.exports["ctrl-d"], n.exports.pageup = n.exports["ctrl-u"]
}), define("ace/keyboard/vim/maps/operators", ["require", "exports", "module", "ace/keyboard/vim/maps/util", "ace/keyboard/vim/registers"], function (e, t, n) {
    "never use strict";
    var r = e("./util"), i = e("../registers");
    n.exports = {d: {selFn: function (e, t, n, s) {
        i._default.text = e.getCopyText(), i._default.isLine = r.onVisualLineMode, r.onVisualLineMode ? e.removeLines() : e.session.remove(t), r.normalMode(e)
    }, fn: function (e, t, n, r) {
        n = n || 1;
        switch (r) {
            case"d":
                i._default.text = "", i._default.isLine = !0;
                for (var s = 0; s < n; s++) {
                    e.selection.selectLine(), i._default.text += e.getCopyText();
                    var o = e.getSelectionRange();
                    if (!o.isMultiLine()) {
                        lastLineReached = !0;
                        var u = o.start.row - 1, a = e.session.getLine(u).length;
                        o.setStart(u, a), e.session.remove(o), e.selection.clearSelection();
                        break
                    }
                    e.session.remove(o), e.selection.clearSelection()
                }
                i._default.text = i._default.text.replace(/\n$/, "");
                break;
            default:
                t && (e.selection.setSelectionRange(t), i._default.text = e.getCopyText(), i._default.isLine = !1, e.session.remove(t), e.selection.clearSelection())
        }
    }}, c: {selFn: function (e, t, n, i) {
        e.session.remove(t), r.insertMode(e)
    }, fn: function (e, t, n, i) {
        n = n || 1;
        switch (i) {
            case"c":
                for (var s = 0; s < n; s++)e.removeLines(), r.insertMode(e);
                break;
            default:
                t && (e.session.remove(t), r.insertMode(e))
        }
    }}, y: {selFn: function (e, t, n, s) {
        i._default.text = e.getCopyText(), i._default.isLine = r.onVisualLineMode, e.selection.clearSelection(), r.normalMode(e)
    }, fn: function (e, t, n, r) {
        n = n || 1;
        switch (r) {
            case"y":
                var s = e.getCursorPosition();
                e.selection.selectLine();
                for (var o = 0; o < n - 1; o++)e.selection.moveCursorDown();
                i._default.text = e.getCopyText().replace(/\n$/, ""), e.selection.clearSelection(), i._default.isLine = !0, e.moveCursorToPosition(s);
                break;
            default:
                if (t) {
                    var s = e.getCursorPosition();
                    e.selection.setSelectionRange(t), i._default.text = e.getCopyText(), i._default.isLine = !1, e.selection.clearSelection(), e.moveCursorTo(s.row, s.column)
                }
        }
    }}, ">": {selFn: function (e, t, n, i) {
        n = n || 1;
        for (var s = 0; s < n; s++)e.indent();
        r.normalMode(e)
    }, fn: function (e, t, n, r) {
        n = parseInt(n || 1, 10);
        switch (r) {
            case">":
                var i = e.getCursorPosition();
                e.selection.selectLine();
                for (var s = 0; s < n - 1; s++)e.selection.moveCursorDown();
                e.indent(), e.selection.clearSelection(), e.moveCursorToPosition(i), e.navigateLineEnd(), e.navigateLineStart()
        }
    }}, "<": {selFn: function (e, t, n, i) {
        n = n || 1;
        for (var s = 0; s < n; s++)e.blockOutdent();
        r.normalMode(e)
    }, fn: function (e, t, n, r) {
        n = n || 1;
        switch (r) {
            case"<":
                var i = e.getCursorPosition();
                e.selection.selectLine();
                for (var s = 0; s < n - 1; s++)e.selection.moveCursorDown();
                e.blockOutdent(), e.selection.clearSelection(), e.moveCursorToPosition(i), e.navigateLineEnd(), e.navigateLineStart()
        }
    }}}
}), "use strict", define("ace/keyboard/vim/maps/aliases", ["require", "exports", "module"], function (e, t, n) {
    n.exports = {x: {operator: {ch: "d", count: 1}, motion: {ch: "l", count: 1}}, X: {operator: {ch: "d", count: 1}, motion: {ch: "h", count: 1}}, D: {operator: {ch: "d", count: 1}, motion: {ch: "$", count: 1}}, C: {operator: {ch: "c", count: 1}, motion: {ch: "$", count: 1}}, s: {operator: {ch: "c", count: 1}, motion: {ch: "l", count: 1}}, S: {operator: {ch: "c", count: 1}, param: "c"}}
})