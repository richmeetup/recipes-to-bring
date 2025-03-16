import Url from "../url";

describe("createBringApiUrl()", () => {
  it("should create a url with the correct syntax for bring's api", () => {
    expect(Url.createBringApiUrl("http://example.recipe-site.com/recipe.html"))
      .toEqual("https://api.getbring.com/rest/bringrecipes/deeplink?" +
        "url=http%3A%2F%2Fexample.recipe-site.com%2Frecipe.html&" +
        "source=web&" + 
        "baseQuantity=2&" +
        "requestedQuantity=2");
  });
});