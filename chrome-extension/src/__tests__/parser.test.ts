import Parser from "../parser";
import * as fs from "fs";

describe("containsRecipeSchemaFromHtml()", () => {
  it("should return true when test html contains recipe schema", () => {
    const recipeHtml: string = fs.readFileSync(
      "../testdata/nytimes_recipe.html",
      "utf8",
    );

    expect(Parser.containsRecipeSchemaFromHtml(recipeHtml)).toBeTruthy();
  });

  it("should return false when test document doesn't contain recipe schema", () => {
    const recipeHtml: string = fs.readFileSync(
      "../testdata/tiny_urban_dumpling_recipe.html",
      "utf8",
    );

    expect(Parser.containsRecipeSchemaFromHtml(recipeHtml)).toBeFalsy();
  });
});