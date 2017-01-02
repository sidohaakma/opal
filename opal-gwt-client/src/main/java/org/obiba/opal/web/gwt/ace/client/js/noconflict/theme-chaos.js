/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

ace.define('ace/theme/chaos', ['require', 'exports', 'module' , 'ace/lib/dom'], function (require, exports, module) {

    exports.isDark = true;
    exports.cssClass = "ace-chaos";
    exports.cssText = ".ace-chaos .ace_gutter {\
background: #141414;\
color: #595959;\
border-right: 1px solid #282828;\
}\
.ace-chaos .ace_gutter-cell.ace_warning {\
background-image: none;\
background: #FC0;\
border-left: none;\
padding-left: 0;\
color: #000;\
}\
.ace-chaos .ace_gutter-cell.ace_error {\
background-position: -6px center;\
background-image: none;\
background: #F10;\
border-left: none;\
padding-left: 0;\
color: #000;\
}\
.ace-chaos .ace_print-margin {\
border-left: 1px solid #555;\
right: 0;\
background: #1D1D1D;\
}\
.ace-chaos .ace_scroller {\
background-color: #161616;\
}\
.ace-chaos .ace_text-layer {\
cursor: text;\
color: #E6E1DC;\
}\
.ace-chaos .ace_cursor {\
border-left: 2px solid #FFFFFF;\
}\
.ace-chaos .ace_cursor.ace_overwrite {\
border-left: 0px;\
border-bottom: 1px solid #FFFFFF;\
}\
.ace-chaos .ace_marker-layer .ace_selection {\
background: #494836;\
}\
.ace-chaos .ace_marker-layer .ace_step {\
background: rgb(198, 219, 174);\
}\
.ace-chaos .ace_marker-layer .ace_bracket {\
margin: -1px 0 0 -1px;\
border: 1px solid #FCE94F;\
}\
.ace-chaos .ace_marker-layer .ace_active-line {\
background: #333;\
}\
.ace-chaos .ace_gutter-active-line {\
background-color: #222;\
}\
.ace-chaos .ace_invisible {\
color: #404040;\
}\
.ace-chaos .ace_keyword {\
color:#00698F;\
}\
.ace-chaos .ace_keyword.ace_operator {\
color:#FF308F;\
}\
.ace-chaos .ace_constant {\
color:#1EDAFB;\
}\
.ace-chaos .ace_constant.ace_language {\
color:#FDC251;\
}\
.ace-chaos .ace_constant.ace_library {\
color:#8DFF0A;\
}\
.ace-chaos .ace_constant.ace_numeric {\
color:#58C554;\
}\
.ace-chaos .ace_invalid {\
color:#FFFFFF;\
background-color:#990000;\
}\
.ace-chaos .ace_invalid.ace_deprecated {\
color:#FFFFFF;\
background-color:#990000;\
}\
.ace-chaos .ace_support {\
color: #999;\
}\
.ace-chaos .ace_support.ace_function {\
color:#00AEEF;\
}\
.ace-chaos .ace_function {\
color:#00AEEF;\
}\
.ace-chaos .ace_string {\
color:#58C554;\
}\
.ace-chaos .ace_comment {\
color:#555;\
font-style:italic;\
padding-bottom: 0px;\
}\
.ace-chaos .ace_variable {\
color:#997744;\
}\
.ace-chaos .ace_meta.ace_tag {\
color:#BE53E6;\
}\
.ace-chaos .ace_entity.ace_other.ace_attribute-name {\
color:#FFFF89;\
}\
.ace-chaos .ace_markup.ace_underline {\
text-decoration: underline;\
}\
.ace-chaos .ace_fold-widget {\
text-align: center;\
}\
.ace-chaos .ace_fold-widget:hover {\
color: #777;\
}\
.ace-chaos .ace_fold-widget.ace_start,\
.ace-chaos .ace_fold-widget.ace_end,\
.ace-chaos .ace_fold-widget.ace_closed{\
background: none;\
border: none;\
box-shadow: none;\
}\
.ace-chaos .ace_fold-widget.ace_start:after {\
content: '▾'\
}\
.ace-chaos .ace_fold-widget.ace_end:after {\
content: '▴'\
}\
.ace-chaos .ace_fold-widget.ace_closed:after {\
content: '‣'\
}";

    var dom = require("../lib/dom");
    dom.importCssString(exports.cssText, exports.cssClass);

});