/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

ace.define("ace/mode/python", ["require", "exports", "module", "ace/lib/oop", "ace/mode/text", "ace/tokenizer", "ace/mode/python_highlight_rules", "ace/mode/folding/pythonic", "ace/range"], function (e, t, n) {
    var r = e("../lib/oop"), i = e("./text").Mode, s = e("../tokenizer").Tokenizer, o = e("./python_highlight_rules").PythonHighlightRules, u = e("./folding/pythonic").FoldMode, a = e("../range").Range, f = function () {
        this.$tokenizer = new s((new o).getRules()), this.foldingRules = new u("\\:")
    };
    r.inherits(f, i), function () {
        this.toggleCommentLines = function (e, t, n, r) {
            var i = !0, s = /^(\s*)#/;
            for (var o = n; o <= r; o++)if (!s.test(t.getLine(o))) {
                i = !1;
                break
            }
            if (i) {
                var u = new a(0, 0, 0, 0);
                for (var o = n; o <= r; o++) {
                    var f = t.getLine(o), l = f.match(s);
                    u.start.row = o, u.end.row = o, u.end.column = l[0].length, t.replace(u, l[1])
                }
            } else t.indentRows(n, r, "#")
        }, this.getNextLineIndent = function (e, t, n) {
            var r = this.$getIndent(t), i = this.$tokenizer.getLineTokens(t, e), s = i.tokens;
            if (s.length && s[s.length - 1].type == "comment")return r;
            if (e == "start") {
                var o = t.match(/^.*[\{\(\[\:]\s*$/);
                o && (r += n)
            }
            return r
        };
        var e = {pass: 1, "return": 1, raise: 1, "break": 1, "continue": 1};
        this.checkOutdent = function (t, n, r) {
            if (r !== "\r\n" && r !== "\r" && r !== "\n")return!1;
            var i = this.$tokenizer.getLineTokens(n.trim(), t).tokens;
            if (!i)return!1;
            do var s = i.pop(); while (s && (s.type == "comment" || s.type == "text" && s.value.match(/^\s+$/)));
            return s ? s.type == "keyword" && e[s.value] : !1
        }, this.autoOutdent = function (e, t, n) {
            n += 1;
            var r = this.$getIndent(t.getLine(n)), i = t.getTabString();
            r.slice(-i.length) == i && t.remove(new a(n, r.length - i.length, n, r.length))
        }
    }.call(f.prototype), t.Mode = f
}), ace.define("ace/mode/python_highlight_rules", ["require", "exports", "module", "ace/lib/oop", "ace/mode/text_highlight_rules"], function (e, t, n) {
    var r = e("../lib/oop"), i = e("./text_highlight_rules").TextHighlightRules, s = function () {
        var e = "and|as|assert|break|class|continue|def|del|elif|else|except|exec|finally|for|from|global|if|import|in|is|lambda|not|or|pass|print|raise|return|try|while|with|yield", t = "True|False|None|NotImplemented|Ellipsis|__debug__", n = "abs|divmod|input|open|staticmethod|all|enumerate|int|ord|str|any|eval|isinstance|pow|sum|basestring|execfile|issubclass|print|super|binfile|iter|property|tuple|bool|filter|len|range|type|bytearray|float|list|raw_input|unichr|callable|format|locals|reduce|unicode|chr|frozenset|long|reload|vars|classmethod|getattr|map|repr|xrange|cmp|globals|max|reversed|zip|compile|hasattr|memoryview|round|__import__|complex|hash|min|set|apply|delattr|help|next|setattr|buffer|dict|hex|object|slice|coerce|dir|id|oct|sorted|intern", r = this.createKeywordMapper({"invalid.deprecated": "debugger", "support.function": n, "constant.language": t, keyword: e}, "identifier"), i = "(?:r|u|ur|R|U|UR|Ur|uR)?", s = "(?:(?:[1-9]\\d*)|(?:0))", o = "(?:0[oO]?[0-7]+)", u = "(?:0[xX][\\dA-Fa-f]+)", a = "(?:0[bB][01]+)", f = "(?:" + s + "|" + o + "|" + u + "|" + a + ")", l = "(?:[eE][+-]?\\d+)", c = "(?:\\.\\d+)", h = "(?:\\d+)", p = "(?:(?:" + h + "?" + c + ")|(?:" + h + "\\.))", d = "(?:(?:" + p + "|" + h + ")" + l + ")", v = "(?:" + d + "|" + p + ")";
        this.$rules = {start: [
            {token: "comment", regex: "#.*$"},
            {token: "string", regex: i + '"{3}(?:[^\\\\]|\\\\.)*?"{3}'},
            {token: "string", regex: i + '"{3}.*$', next: "qqstring"},
            {token: "string", regex: i + '"(?:[^\\\\]|\\\\.)*?"'},
            {token: "string", regex: i + "'{3}(?:[^\\\\]|\\\\.)*?'{3}"},
            {token: "string", regex: i + "'{3}.*$", next: "qstring"},
            {token: "string", regex: i + "'(?:[^\\\\]|\\\\.)*?'"},
            {token: "constant.numeric", regex: "(?:" + v + "|\\d+)[jJ]\\b"},
            {token: "constant.numeric", regex: v},
            {token: "constant.numeric", regex: f + "[lL]\\b"},
            {token: "constant.numeric", regex: f + "\\b"},
            {token: r, regex: "[a-zA-Z_$][a-zA-Z0-9_$]*\\b"},
            {token: "keyword.operator", regex: "\\+|\\-|\\*|\\*\\*|\\/|\\/\\/|%|<<|>>|&|\\||\\^|~|<|>|<=|=>|==|!=|<>|="},
            {token: "paren.lparen", regex: "[\\[\\(\\{]"},
            {token: "paren.rparen", regex: "[\\]\\)\\}]"},
            {token: "text", regex: "\\s+"}
        ], qqstring: [
            {token: "string", regex: '(?:[^\\\\]|\\\\.)*?"{3}', next: "start"},
            {token: "string", regex: ".+"}
        ], qstring: [
            {token: "string", regex: "(?:[^\\\\]|\\\\.)*?'{3}", next: "start"},
            {token: "string", regex: ".+"}
        ]}
    };
    r.inherits(s, i), t.PythonHighlightRules = s
}), ace.define("ace/mode/folding/pythonic", ["require", "exports", "module", "ace/lib/oop", "ace/mode/folding/fold_mode"], function (e, t, n) {
    var r = e("../../lib/oop"), i = e("./fold_mode").FoldMode, s = t.FoldMode = function (e) {
        this.foldingStartMarker = new RegExp("([\\[{])(?:\\s*)$|(" + e + ")(?:\\s*)(?:#.*)?$")
    };
    r.inherits(s, i), function () {
        this.getFoldWidgetRange = function (e, t, n) {
            var r = e.getLine(n), i = r.match(this.foldingStartMarker);
            if (i)return i[1] ? this.openingBracketBlock(e, i[1], n, i.index) : i[2] ? this.indentationBlock(e, n, i.index + i[2].length) : this.indentationBlock(e, n)
        }
    }.call(s.prototype)
})