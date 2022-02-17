const codeQuery = $("#code-area");
const code = document.querySelector("#code-area")
const body = document.querySelector("body");

const stringReg = /(".*?")|('.*?')/g;
const languageReg = /\b(abstract|assert|boolean|break|byte|case|catch|char|class|continue|default|do|double|else|enum|extends|final|finally|float|for|if|implements|import|instanceof|int|interface|long|native|new|null|package|private|public|return|short|strictfp|static|super|switch|synchronized|this|throw|throws|transient|try|void|volatile|while|const|goto|true|false|,)\b/g
const methodReg = /([a-zA-Z0-9_]*)(?=\((.*)\){)/g;

//(?<!(if|for|while|catch|do|else|new|throw))

function handleKeyPress(event) {
  switch (event.keyCode) {
    case 13: //enter
      break;
    default:
      highlight();
      break;
  }
}

function highlight() {
  let parsed = "";
  codeQuery.each(function () {
    let string = this.textContent;
    parsed = string.replaceAll(languageReg, "<span class=language>$1</span>");
    parsed = parsed.replaceAll(stringReg, "<span class=string>$1</span>");
    parsed = parsed.replaceAll(methodReg, "<span class=method>$1</span>");
  });

  let caretPos = getCaretIndex();
  code.innerHTML = parsed;
  setCaretPosition(caretPos);
}

function getCaretIndex() {
  let position = 0;
  const isSupported = typeof window.getSelection() !== "undefined";
  if (isSupported) {
    const selection = window.getSelection();
    if (selection.rangeCount !== 0) {
      const range = window.getSelection().getRangeAt(0);
      const preCaretRange = range.cloneRange();
      preCaretRange.selectNodeContents(code);
      preCaretRange.setEnd(range.endContainer, range.endOffset);
      position = preCaretRange.toString().length;
    }
  }
  return position;
}

function setCaretPosition(index) {
  let range = document.createRange();
  let selection = document.getSelection();

  let substringLength = 0;
  let prevSubstringLength = 0;
  let indexToSet;
  let childIndex;

  let i = -1;
  while (substringLength < index) {
    prevSubstringLength = substringLength;
    i++;

    if (code.childNodes[i].nodeType != Node.TEXT_NODE) {
      substringLength += code.childNodes[i].firstChild === null
          ? code.childNodes[i].textContent.length
          : code.childNodes[i].firstChild.textContent.length;
    } else {
      substringLength += code.childNodes[i].textContent.length;
    }

  }

  childIndex = i;

  if (childIndex === 0) {
    indexToSet = index;
  } else {
    indexToSet = index - prevSubstringLength;
  }

  if(index === 0){
    range.setStart(code, 0)
  } else {
    if (code.childNodes[childIndex].nodeType !== Node.TEXT_NODE) {
      range.setStart(
          code.childNodes[childIndex].firstChild ?? code.childNodes[childIndex],
          indexToSet);
    } else {
      range.setStart(code.childNodes[childIndex], indexToSet);
    }
  }

  range.collapse(true);

  selection.removeAllRanges();
  selection.addRange(range);
}

code.addEventListener("keyup", handleKeyPress);