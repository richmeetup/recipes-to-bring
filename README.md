# Recipes to Bring!

## Overview

*Have you ever looked at a recipe on a webpage and wanted to bring it directly into your favorite shopping list app?*

This project utilizes the [Bring! shopping app](https://www.getbring.com/en/home) to store recipes and load a list of ingredients for shopping.

There are two components to this project:

1) **Chrome Extension** -- Take the currently viewed page with a recipe and utilize the Bring! API to store this recipe in your account.

2) **Parser Backend** -- For recipe pages without Recipe meta-information based off the schema.org spec, a Parser backend endpoint will be available that will take a webpage as a parameter and attempt to create a importable recipe page for Bring!. 

## Chrome Extension

The Chrome extension will detect whether the page is parsable, based on the presence of a [schema.org Recipe spec](https://schema.org/Recipe). 

### Installation

```
cd chrome-extension && npm run build
```

Then, follow the instructions to [Load an unpacked extension](https://developer.chrome.com/docs/extensions/get-started/tutorial/hello-world#load-unpacked) from Google.

### Handling Parsable Recipe Pages

If there is a Recipe spec item available, then the Extension icon will change to indicate the presence of a Recipe spec on the page. Clicking the Extension icon will import the recipe into the Bring! web app using the link:

```
https://api.getbring.com/rest/bringrecipes/deeplink?url=<url>&source=web&baseQuantity=<quantity>&requestedQuantity=<quantity>
```

Details of this API can be found on the [Bring! Import Developer Guide](https://sites.google.com/getbring.com/bring-import-dev-guide/web-to-app-integration#h.p_MCSdfKdC6YjI).

While testing what can be fed into this API, it was found that not every recipe webpage can be handled. Instead, an error toast of `Import failed. The site does not contain a valid recipe.` is displayed instead.

### Handling Non-Parsable Recipe Pages

If there is *no* such Recipe spec item available, then the Chrome Extension will make a call to the Parser backend and will attempt to create a new Recipe page that contains the needed information.

## Parser Backend

In addition to the extension, this project also provides the ability to spin up an AWS Lambda endpoint that can process a webpage and produce an exact copy of the website with a Recipe spec which will be stored in a publicly available S3 bucket. 

Using Open AI, a Recipe spec will be generated from the webpage and added as JSON-LD in a `<script>` tag.

## TODOs

* Add screenshots of Extension
* Add steps to build Parser Backend to AWS

## Notes

This project was done as a fun exercise to play around with AWS and Chrome extensions and an attempt to utilize best practices. :-)

### Languages, tools & libraries used

* For Chrome Extension -- [Typescript](https://www.typescriptlang.org/), [Jest](https://jestjs.io/), Webpack, Prettier

### Known issues:

* When trying to import a recipe for the first time by clicking the extension icon, a "dynamic link" error message might appear. Wait 30 seconds or so and retry the icon click -- the Bring! web app should open with recipe import page.