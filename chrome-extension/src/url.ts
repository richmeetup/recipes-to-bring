export default class Url {
  static readonly bringApiUrl = "https://api.getbring.com/rest/bringrecipes/deeplink";

  static createBringApiUrl(url: string): string {
    return Url.bringApiUrl + "?" +
      "url=" + encodeURIComponent(url) + "&" +
      "source=web&" + 
      "baseQuantity=2&" +
      "requestedQuantity=2";
  }
}