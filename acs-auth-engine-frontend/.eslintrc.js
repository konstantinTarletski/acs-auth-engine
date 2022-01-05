module.exports = {
    parser: '@typescript-eslint/parser',
    parserOptions: {
        ecmaVersion: 2020,
        sourceType: 'module',
        project: './tsconfig.json',
        ecmaFeatures: {
            jsx: true,
        },
    },
    extends: [
        // airbnb
        'airbnb-typescript',

        // intermediate, before migrating to airbnb
        // 'plugin:@typescript-eslint/recommended',
        // 'plugin:import/errors',
        // 'plugin:import/warnings',
        // 'plugin:react/recommended',

        // prettier compat
        'prettier/@typescript-eslint',
        'plugin:prettier/recommended',
    ],
    plugins: ['react', 'prettier'],
    env: {
        es2020: true,
        browser: true,
        node: true,
        jest: true,
    },
    rules: {
        'max-len': [
            'error',
            {
                code: 120,
            },
        ],
        'padding-line-between-statements': [
            'error',
            {
                blankLine: 'always',
                prev: '*',
                next: 'return',
            },
            {
                blankLine: 'always',
                prev: ['const', 'let', 'var'],
                next: '*',
            },
            {
                blankLine: 'any',
                prev: ['const', 'let', 'var'],
                next: ['const', 'let', 'var'],
            },
            {
                blankLine: 'always',
                prev: '*',
                next: 'block-like',
            },
            {
                blankLine: 'always',
                prev: 'block-like',
                next: '*',
            },
            {
                blankLine: 'any',
                prev: 'case',
                next: 'case',
            },
        ],
        'no-multiple-empty-lines': [
            'error',
            {
                max: 2,
                maxEOF: 0,
            },
        ],
        'eol-last': 'error',
        'react/jsx-indent-props': ['error', 4],
        'react/jsx-indent': ['error', 4],
        'no-use-before-define': [
            'error',
            {
                functions: false,
                classes: true,
            },
        ],
        'import/order': [
            'error',
            {
                groups: ['builtin', 'external', 'internal', 'parent', 'sibling', 'index'],
                'newlines-between': 'always',
                alphabetize: {
                    order: 'asc',
                    caseInsensitive: true,
                },
            },
        ],
        'import/no-extraneous-dependencies': [
            'error',
            {
                devDependencies: true,
            },
        ],
        // lint customizations over airbnb
        'import/named': 0, // disabled, because do no resolve TS interface imports, but TS compiler takes care of that
        'react/jsx-one-expression-per-line': 0, // still conflicts with Prettier
        'react/prop-types': 0, // do not need for TS
    },
    settings: {
        react: {
            version: 'detect',
        },
        'import/resolver': {
            node: {
                extensions: ['.js', '.jsx', '.ts', '.tsx', '.json'],
            },
            webpack: {
                config: 'webpack.config.js',
            },
        },
    },
};
