import { load } from 'cheerio';

export default class Parser {
  static containsRecipeSchemaFromHtml(html: string): boolean {
    const $ = load(html);

    // loop through the dom and look for <script type="application/ld+json"> with recipe schema
    var containsRecipeSchema = false;

    // XXX - ehh this is an ugly each loop
    $('script[type="application/ld+json"]').each((index, element) => {
      const scriptContent = $(element).html();
      if (scriptContent) {
        // parse the json and look for the "@type" key with value "Recipe"
        const json = JSON.parse(scriptContent);
        if (json["@type"] === "Recipe") {
          containsRecipeSchema = true;
        }
      }
    })

    return containsRecipeSchema;
  }
}