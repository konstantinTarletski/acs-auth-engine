const { defaults } = require('jest-config');

module.exports = {
    collectCoverage: true,
    modulePathIgnorePatterns: ['<rootDir>/dist/', '<rootDir>/target/'],
    transformIgnorePatterns: [
        // '/node_modules/(?!luminor-components/.*)'
    ],
    testEnvironment: 'jest-environment-jsdom-global',
    setupFiles: [
        '<rootDir>/node_modules/jest-canvas-mock/lib/index.js', // Get rid off HTMLCanvasElement.prototype.getContext, and canvas errors. Now no need to mock in the test file explicitly.
    ],
    setupFilesAfterEnv: [
        '<rootDir>/scripts/setupTests.js', // Enzyme setup
    ],
    moduleNameMapper: {
        '^.+\\.(css|less|scss)$': 'identity-obj-proxy',
    },
    moduleFileExtensions: ['.d.ts', 'ts', 'tsx', 'js', 'jsx', 'json', 'node'],
    collectCoverageFrom: ['!**/dist/**', 'src/**/*.{ts,tsx}', '!**/node_modules/**', '!**/vendor/**'],
    coverageThreshold: {
        global: {
            branches: 0.1,
            functions: 0.1,
            lines: 0.1,
            statements: 0.1,
        },
    },
    coverageReporters: ['text', 'text-summary'],
    snapshotSerializers: ['enzyme-to-json/serializer'],
};
