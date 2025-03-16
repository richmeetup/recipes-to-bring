const path = require('path');
const CopyPlugin = require('copy-webpack-plugin');
module.exports = {
  mode: "production",
  entry: {
    background: path.resolve(__dirname, "..", "src", "background.ts")
  },
  output: {
    path: path.join(__dirname, "../dist"),
    filename: "[name].js",
  },
  resolve: {
    extensions: [".ts", ".js"],
    fallback: {
      fs: false,
      vm: false,
      os: false,
      assert: false,
      string_decoder: false,
      tls: false,
      net: false,
      path: false,
      zlib: false,
      http: false,
      https: false,
      stream: false,
      crypto: false,
      url: false,
      util: false,
      buffer: false,
    },
  },
  module: {
    rules: [
      {
        test: /\.tsx?$/,
        loader: "ts-loader",
        exclude: /node_modules/,
      },
    ],
  },
};