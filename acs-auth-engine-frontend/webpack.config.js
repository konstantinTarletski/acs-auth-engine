/* eslint-disable @typescript-eslint/no-var-requires */
const path = require('path');

const ForkTsCheckerWebpackPlugin = require('fork-ts-checker-webpack-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const LodashModuleReplacementPlugin = require('lodash-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
// const TerserPlugin = require('terser-webpack-plugin');
const webpack = require('webpack');

const environment = require('./scripts/environment');
const revision = require('./scripts/revision.js');

const rootPath = path.resolve(__dirname);
const srcPath = `${rootPath}/src`;

const config = {
    devServer: {
        contentBase: [`${rootPath}/www`, path.join('.')],
        filename: '/build/js/application.js',
        historyApiFallback: true,
        host: 'localhost',
        hot: true,
        inline: true,
        port: 3000,
        quiet: false,
        stats: {
            assets: false,
            chunkModules: false,
            chunks: false,
            colors: true,
            hash: false,
            timings: true,
            version: false,
        },
    },
    devtool: 'cheap-source-map',
    resolve: {
        alias: {
            'react-dom':
                process.env.NODE_ENV === 'development'
                    ? `${rootPath}/node_modules/@hot-loader/react-dom`
                    : `${rootPath}/node_modules/react-dom`,
            assets: `${srcPath}/assets`,
            common: `${srcPath}/common`,
            consts: `${srcPath}/consts`,
            components: `${srcPath}/components`,
            screens: `${srcPath}/screens`,
            data: `${srcPath}/data`,
            utils: `${srcPath}/utils`,
        },
        extensions: ['.ts', '.tsx', '.js', '.jsx', '.json'],
    },
    entry: {
        _polyfill: `${rootPath}/config/polyfills.js`,
        application: `${srcPath}/index.tsx`,
    },
    mode: process.env.NODE_ENV === 'production' ? 'production' : 'development',
    module: {
        rules: [
            {
                include: [path.resolve(srcPath), path.resolve(`${rootPath}/node_modules/luminor-components`)],
                test: /\.(ts|js)x?$/,
                loader: 'babel-loader',
            },
            {
                loader: 'url-loader',
                options: {
                    limit: 8000,
                    outputPath: 'build/',
                },
                test: /\.(png|jpg|jpeg|gif|svg)$/,
            },
            {
                test: /\.(sa|sc|c)ss$/,
                use: [
                    'css-hot-loader',
                    MiniCssExtractPlugin.loader,
                    {
                        loader: 'css-loader',
                        options: {
                            sourceMap: true,
                        },
                    },
                    {
                        loader: 'postcss-loader',
                        options: {
                            sourceMap: true,
                        },
                    },
                    {
                        loader: 'sass-loader',
                        options: {
                            sassOptions: {
                                includePaths: [`${srcPath}/assets/scss/`],
                            },
                            sourceMap: true,
                        },
                    },
                ],
            },
            {
                test: /\.(woff|woff2|eot|ttf|otf)$/,
                use: ['url-loader'],
            },
            {
                test: /\.ico$/,
                loader: 'file-loader?name=[name].[ext]', // <-- retain original file name
            },
        ],
    },
    output: {
        filename: '[name].bundle.js',
        path: `${rootPath}/www`,
        pathinfo: true,
    },
    plugins: [
        new LodashModuleReplacementPlugin({
            collections: true,
            paths: true,
            shorthands: true,
        }),
        new webpack.HotModuleReplacementPlugin({
            disable: process.env.NODE_ENV !== 'development',
        }),
        new HtmlWebpackPlugin({
            REVISION: revision,
            hash: true,
            inject: true,
            template: `${srcPath}/index.ejs`,
            title: 'Luminor',
            favicon: `${srcPath}/assets/icons/favicon.ico`,
        }),
        new MiniCssExtractPlugin({
            disable: process.env.NODE_ENV === 'development',
            filename: '[name].css',
        }),
        new webpack.LoaderOptionsPlugin({
            minimize: true,
        }),
        new ForkTsCheckerWebpackPlugin({ eslint: false }),
        new webpack.DefinePlugin({
            Env: JSON.stringify(environment),
        }),
    ],
    stats: {
        assets: false,
        children: false,
        chunks: false,
        errorDetails: true,
        errors: true,
        hash: false,
        modules: false,
        publicPath: false,
        reasons: false,
        source: false,
        timings: false,
        version: false,
        warnings: true,
    },
};

module.exports = config;
