"use strict";
var walletsPromise = new Promise(function(resolve, reject) {
  console.log('from promise')
  var result = getDataResult()
  console.log(result)
  if (result === 'mama') {
    console.log('defined')
    resolve(data)
  } else {
    console.log('undefined')
  }
});

document.addEventListener('readystatechange', event => {
    if (event.target.readyState === "complete") {
        getData()
    }
});

window.testParams = (...someData) => {
    console.log('test call 3')
    console.log(someData)
};

async function getDataResult(data) {
  console.log('getDataResult')
  return data
}

async function getData() {
  console.log('getData')
  window.androidJsBridge.getData()
  var result = await walletsPromise
  console.log('got result')
  console.log(result)
}
