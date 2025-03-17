import Parser from "./parser";

function checkIfRecipe(): void {
  if (Parser.containsRecipeSchema(document)) {
    chrome.runtime.sendMessage({ pageInfo: "recipe" });
  }
}

checkIfRecipe();