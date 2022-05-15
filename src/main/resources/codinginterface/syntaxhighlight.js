/*CodeMirror.defineSimpleMode("java", {
  start: [
    //capturing strings
    {regex: /\".*\"/, token: "string"},
    //capturing java specific keywords
    {regex: /\b(abstract|assert|boolean|break|byte|case|catch|char|class|const|continue|default|do|double|else|enum|extends|final|finally|float|for|goto|if|implements|import|instanceof|int|interface|long|native|new|package|private|protected|public|return|short|static|super|switch|synchronized|this|throw|throws|transient|try|void|volatile|while|;)\b/,
      token: "keyword"},
    //capturing atomic values
    {regex: /(true|false|null)/, token: "atomic"},
    //capturing line-comments
    //indenting/dedenting on opening/closing brackets
    {regex: /[\{\[\(]/, indent: true},
    {regex: /[\}\]\)]/, dedent: true},
  ],
  //handling multi-line comments
  comment: [

  ],
  //other misc. information
  meta: {
    //disabling indentation in comments
    dontIndentStates: ["comment"],
    //commenting an entire line, when typing "//"
    lineComment: "//"
  }
})*/


let codeArea = document.querySelector("#codeArea");
let codeMirror = CodeMirror.fromTextArea(codeArea, {
  mode: "text/x-java",
  theme: "barista",
  lineNumbers: true,
  autofocus: true,
  tabSize: 2
});

function loadContent() {
  codeMirror.setValue(window.persistenceService.getContentToOpen());
  return window.persistenceService.getContentToOpen();
}

function switchContent(){
  codeMirror.setValue(window.persistenceService.getContentToSwitch());
  return window.persistenceService.getContentToSwitch();
}

function getContent() {
  return codeMirror.getValue();
}