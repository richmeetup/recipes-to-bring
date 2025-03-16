
import { JSDOM } from "jsdom";

export default class Parser {
  static containsRecipeSchema(html: string): boolean {
    const document: JSDOM = new JSDOM(html);

    // loop through the dom and look for <script type="application/ld+json"> with recipe schema
    const scripts = document.window.document.querySelectorAll("script[type='application/ld+json']");

    // parse the json and look for the "@type" key with value "Recipe"
    for (let i = 0; i < scripts.length; i++) {
      const script = scripts[i];
      const json = JSON.parse(script.innerHTML);

      if (json["@type"] === "Recipe") {
        return true;
      }
    }

    return false;
  }
}