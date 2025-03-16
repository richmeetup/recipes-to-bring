import Parser from "./parser";

console.log("Content script running");

function extractContent(): void {
    const isRecipePage = Parser.containsRecipeSchema(document);
    if (isRecipePage) {
        chrome.runtime.sendMessage({ action: "pageIsRecipe" });
    }
}

extractContent();