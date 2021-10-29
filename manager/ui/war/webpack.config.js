const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const CopyPlugin = require('copy-webpack-plugin');
const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;

module.exports = {
  mode: 'development',
  entry: {
    main: './plugins/api-manager/ts/apimanPlugin.ts'
  },
  devtool: 'inline-source-map',
  devServer: {
    port: 2772,
    static: './dist',
    historyApiFallback: true
  },
  plugins: [
    new HtmlWebpackPlugin({
      title: 'Apiman',
      template: './index.html'
    }),
    new CopyPlugin({
      patterns: [
        { from: "./apiman", to: "apiman" },
        { 
          from: "./plugins/api-manager/**/*.{html,include}",
          to({ context, absoluteFilename }) {
            return `${path.relative("./", absoluteFilename)}`;
          },
       }
      ],
    }),
    new BundleAnalyzerPlugin()
  ],
  module: {
    rules: [
      {
        test: /\.tsx?$/,
        use: 'ts-loader',
        exclude: /node_modules/
      },
      {
        test: /\.css$/i,
        use: ['style-loader', 'css-loader']
      },
      {
        test: /\.(png|svg|jpg|jpeg|gif)$/i,
        type: 'asset/resource',
      },
      {
        test: /\.(woff|woff2|eot|ttf|otf)$/i,
        type: 'asset/resource',
      },
      {
        test: /'\.(html|include)$/i,
        type: 'asset/resource'
      }
    ],
  },
  resolve: {
    extensions: ['.tsx', '.ts', '.js'],
  },
  output: {
    filename: 'apiman-manager-ui.js',
    path: path.resolve(__dirname, 'dist'),
    clean: true
  }
};