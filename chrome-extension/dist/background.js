(()=>{"use strict";var e={785:(e,t)=>{Object.defineProperty(t,"__esModule",{value:!0});class r{static createBringApiUrl(e){return r.bringApiUrl+"?url="+encodeURIComponent(e)+"&source=web&baseQuantity=2&requestedQuantity=2"}}r.bringApiUrl="https://api.getbring.com/rest/bringrecipes/deeplink",t.default=r},994:function(e,t,r){var i=this&&this.__importDefault||function(e){return e&&e.__esModule?e:{default:e}};Object.defineProperty(t,"__esModule",{value:!0});const o=i(r(785));chrome.tabs.onCreated.addListener((e=>{chrome.action.disable(e.id)})),chrome.action.onClicked.addListener((e=>{if(e.url){const t=o.default.createBringApiUrl(e.url+"");chrome.tabs.create({url:t})}})),chrome.runtime.onMessage.addListener(((e,t,r)=>{var i,o,a;"recipe"===e.pageInfo&&(chrome.action.setBadgeText({text:"🥗",tabId:null===(i=t.tab)||void 0===i?void 0:i.id}),chrome.action.setBadgeBackgroundColor({color:"#FF0000",tabId:null===(o=t.tab)||void 0===o?void 0:o.id}),chrome.action.enable(null===(a=t.tab)||void 0===a?void 0:a.id))}))}},t={};!function r(i){var o=t[i];if(void 0!==o)return o.exports;var a=t[i]={exports:{}};return e[i].call(a.exports,a,a.exports,r),a.exports}(994)})();