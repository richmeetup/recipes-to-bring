import { load } from 'cheerio';

export default class Parser {
  static containsRecipeSchema(document: Document): boolean {

    // loop through the dom and look for <script type="application/ld+json"> with recipe schema
    const scripts = document.querySelectorAll('script[type="application/ld+json"]');

    for (const script of scripts) {
      if (script.innerHTML) {
        // parse the json and look for the "@type" key with value "Recipe"
        const json = JSON.parse(script.innerHTML);
        if (json["@type"] === "Recipe") {
          return true;
        }
      }
    }

    return false;
  }
}