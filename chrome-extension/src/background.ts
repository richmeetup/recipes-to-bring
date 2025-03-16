import Url from "./url";
import Parser from "./parser";

// check when new tab is selected
chrome.tabs.onActivated.addListener((activeInfo) => {
  chrome.action.setBadgeText({ text: "" });
  chrome.tabs.get(activeInfo.tabId, (tab) => {
    if (tab.status === "complete") {
      checkTabContent(tab);
    }
  });
});

// check when the page is updated
chrome.tabs.onUpdated.addListener((tabId, changeInfo, tab) => {
  if (changeInfo.status === "loading") {
    resetBadge(tab);
  }
  else if (changeInfo.status === "complete") {
    checkTabContent(tab);
  }
});

// when clicked, load the url in a new tab
chrome.action.onClicked.addListener((tab: chrome.tabs.Tab) => {
  if (tab.url) {
    const url = Url.createBringApiUrl(tab.url + "");
    chrome.tabs.create({ url });
    // for some reason, might have to click on this twice to actually see the new recipe
  }
});

function resetBadge(tab: chrome.tabs.Tab): void {
  chrome.action.setBadgeText({ text: "" });
  chrome.action.setBadgeBackgroundColor({ color: "#333333" });
}

function displayCorrectBadge(html?: string): void {
  if (!html) return;

  const isRecipePage = Parser.containsRecipeSchemaFromHtml(html);

  // update the icon based on whether the current page is a recipe page
  chrome.action.setBadgeText({ text: isRecipePage ? "ON" : ""});
  chrome.action.setBadgeBackgroundColor({ color: isRecipePage ? "#4688F1" : "#333333" });
}

function checkTabContent(tab: chrome.tabs.Tab): void {
  chrome.scripting
    .executeScript({
      target: { tabId: tab.id || -1 },
      func: () => document.documentElement.outerHTML, 
    })
    .then(injectionResults => {
      for (const {frameId, result} of injectionResults) { 
        displayCorrectBadge(result);
      }
    });
}
