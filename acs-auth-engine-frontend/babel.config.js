module.exports = {
    presets: [
        '@babel/preset-react',
        '@babel/preset-typescript',
        [
            '@babel/preset-env',
            {
                modules: false,
                targets: {
                    browsers: ['last 3 versions', 'ie >= 11'],
                },
            },
        ],
    ],
    plugins: [
        // '@babel/plugin-transform-modules-commonjs', commented out because of luminor-components issues
        // '@babel/plugin-proposal-export-default-from', commented out because of luminor-components issues
        '@babel/plugin-proposal-class-properties',
        'lodash',
    ],
    env: {
        development: {
            plugins: ['react-hot-loader/babel'],
        },
    },
};
