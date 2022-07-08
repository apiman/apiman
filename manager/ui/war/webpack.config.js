const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const CopyPlugin = require('copy-webpack-plugin');
const webpack = require('webpack')

module.exports = (env, argv) => {
  return {
    mode: 'development',
    entry: {
      main: './plugins/api-manager/ts/apimanPlugin.ts'
    },
    devtool: argv.mode === 'development' ? 'source-map' : false,
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
      // https://github.com/webpack/changelog-v5/issues/10
      new webpack.ProvidePlugin({
        Buffer: ['buffer', 'Buffer'],
        process: 'process/browser',

      })
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
      fallback: {
        "stream": false
      }
    },
    optimization: {
      splitChunks: {
        chunks: 'all'
      },
    },
    output: {
      filename: 'apiman-manager-ui-[name]-[chunkhash].js',
      path: path.resolve(__dirname, 'dist'),
      clean: true
    }
  }
};
