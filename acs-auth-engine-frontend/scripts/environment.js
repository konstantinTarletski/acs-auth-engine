/**
 * This script loads variables from the .env file into process.env
 * The default export is a filtered list of environmental variables (only the ones that are present in .env.example)
 */

const fs = require('fs');

const dotenv = require('dotenv-safe');
const lodash = require('lodash');

const envKeys = Object.keys(dotenv.parse(fs.readFileSync('.env.example')));

dotenv.config();

module.exports = lodash.pickBy(process.env, (value, key) => lodash.includes(envKeys, key));
