import Url from "./url";
import Parser from "./parser";

// check when new tab is selected
/*
chrome.tabs.onActivated.addListener((activeInfo) => {
  chrome.action.setBadgeText({ text: "" });

  chrome.tabs.get(activeInfo.tabId, (tab) => {
    if (tab.status === "complete") {
      checkIfRecipe(tab);
    }
  });
});
*/

// check when page has completed loading
chrome.tabs.onUpdated.addListener((tabId, changeInfo, tab) => {
  if (changeInfo.status === "complete") {
    checkIfRecipe(tab);
  }
});

function checkIfRecipe(tab: chrome.tabs.Tab) {
  if (tab && tab.id !== undefined) {
    const tabContent = getTabContent(tab);
    if (tabContent !== undefined) {
      const isRecipePage = Parser.containsRecipeSchema(tabContent);

      // update the icon based on whether the current page is a recipe page
      if (isRecipePage) {
        chrome.action.setBadgeText({ text: "Recipe!" });
      } else {
        chrome.action.setBadgeText({ text: "" });
      }
    }
  }
}

// load the url in a new tab
chrome.action.onClicked.addListener((tab: chrome.tabs.Tab) => {
  if (tab.url) {
    const url = Url.createBringApiUrl(tab.url + "");
    chrome.tabs.create({ url });
  }
});

function getTabContent(tab: chrome.tabs.Tab): void {
  if (chrome.runtime.lastError) {
    console.error("Error accessing tab:", chrome.runtime.lastError);
    return;
  }

  // Ensure the tab has a valid URL and is not a Chrome internal page
  if (
    !tab.url ||
    tab.url.startsWith("chrome://") ||
    tab.url.startsWith("chrome-extension://")
  ) {
    console.warn("Skipping script injection for restricted tab:", tab.url);
    return;
  }
  
  chrome.scripting.executeScript(
    {
      target: { tabId: tab.id || -1 },
      func: () => document, // Extracts the visible text from the page
    },
    (results?: chrome.scripting.InjectionResult<Document>[]) => {
      if (chrome.runtime.lastError) {
        console.error("Script injection failed:", chrome.runtime.lastError);
        return;
      }

      if (results && results.length > 0 && results[0].result) {
        console.log("Extracted content:", results[0].result);
      } else {
        console.log("No content extracted.");
      }
    },
  );
}