import Url from "./url";
import Parser from "./parser";

// check when new tab is selected
chrome.tabs.onActivated.addListener((activeInfo) => {
  chrome.action.setBadgeText({ text: "" });

  console.log("Tab activated:", activeInfo);

  chrome.tabs.get(activeInfo.tabId, (tab) => {
    if (tab.status === "complete") {
      //checkTabContent(tab);
    }
  });
});

// load the url in a new tab
chrome.action.onClicked.addListener((tab: chrome.tabs.Tab) => {
  if (tab.url) {
    const url = Url.createBringApiUrl(tab.url + "");
    chrome.tabs.create({ url });
  }
});

chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
  if (request.action === "pageIsRecipe") {
      chrome.action.setBadgeText({ text: "!" });
  }
});

/*
function showBadgeCallback(): void {
  console.log("showBadgeCallback");
  if (document) {
    console.log("document exists");
    const isRecipePage = Parser.containsRecipeSchema(document);
    console.log("isRecipePage", isRecipePage);

    // update the icon based on whether the current page is a recipe page
    if (isRecipePage) {
      chrome.action.setBadgeText({ text: "Recipe!" });
    } else {
      chrome.action.setBadgeText({ text: "" });
    }
  }
}
*/

/*
function checkTabContent(tab: chrome.tabs.Tab): void {
  chrome.scripting
    .executeScript({
      target: { tabId: tab.id || -1 },
      func: showBadgeCallback, 
    })
    .then(injectionResults => {
      for (const {frameId, result} of injectionResults) { 
        console.log(`Frame ${frameId} returned ${result}`);
      }
    });
}
*/

