/* global clipboard */
/* eslint-disable no-console */

/* This function is used when I am adding a copy button to bash
 * codeblocks. I like to put the command after a `$ ` and to include an
 * output produced. So I want the copy button to ignore all of that and
 * just copy the command */
const bash_reducer = (acc, ele) => acc + ele.replace(/\$ /g, '')

function addCopyButtons(clipboard) {
  document.querySelectorAll('pre > code').forEach(function (codeBlock) {
    var button = document.createElement('button');
    button.className = 'copy-code-button';
    button.type = 'button';
    button.innerText = 'Copy';
    button.addEventListener('click', function () {
      var command = codeBlock.innerText.trim();
      if (codeBlock.className === 'language-bash'){
        command = command.match(/\$ (.*)\n?/g).reduce(bash_reducer, '')
      }
      clipboard.writeText(command).then(function () {
        /* Chrome doesn't seem to blur automatically,
           leaving the button in a focused state. */
        button.blur();
        button.innerText = 'Copied!';
        setTimeout(function () {
          button.innerText = 'Copy';
        }, 2000);
      }, function (error) {
        button.innerText = 'Error';
      });
    });
    var pre = codeBlock.parentNode;
    if (pre.parentNode.classList.contains('highlight')) {
      var highlight = pre.parentNode;
      highlight.parentNode.insertBefore(button, highlight);
    } else {
      pre.parentNode.insertBefore(button, pre);
    }
  });
}
if (navigator && navigator.clipboard) {
  addCopyButtons(navigator.clipboard);
} else {
  var script = document.createElement('script');
  script.src =
    'https://cdnjs.cloudflare.com/ajax/libs/clipboard-polyfill/2.7.0/clipboard-polyfill.promise.js';
  script.integrity = 'sha256-waClS2re9NUbXRsryKoof+F9qc1gjjIhc2eT7ZbIv94=';
  script.crossOrigin = 'anonymous';
  script.onload = function () {
    addCopyButtons(clipboard);
  };
  document.body.appendChild(script);
}
