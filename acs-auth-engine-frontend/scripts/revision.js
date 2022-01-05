const spawnSync = require('child_process').spawnSync;

module.exports = spawnSync('git', ['rev-parse', '--short', 'HEAD']).stdout.toString().trim();
