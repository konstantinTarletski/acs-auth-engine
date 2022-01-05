const autoprefixer = require('autoprefixer');

module.exports = {
    plugins: [
        autoprefixer({
            add: true,
            cascade: false,
            grid: true,
        }),
    ],
};
