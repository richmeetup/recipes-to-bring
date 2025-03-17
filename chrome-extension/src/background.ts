import Url from "./url";

// check when a tab is created
chrome.tabs.onCreated.addListener((tab) => {
    chrome.action.disable(tab.id)
});

// when clicked, load the url in a new tab
chrome.action.onClicked.addListener((tab: chrome.tabs.Tab) => {
  if (tab.url) {
    const url = Url.createBringApiUrl(tab.url + "");
    chrome.tabs.create({ url });
    // XXX - for some reason, might have to click on this twice to actually see the new recipe
  }
});

// when receiving message, update the badge
chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
  if (message.pageInfo === "recipe") {
    chrome.action.setBadgeText({ text: "🥗", tabId: sender.tab?.id });
    chrome.action.setBadgeBackgroundColor({ color: "#FF0000", tabId: sender.tab?.id });
    chrome.action.enable(sender.tab?.id);
  }
});