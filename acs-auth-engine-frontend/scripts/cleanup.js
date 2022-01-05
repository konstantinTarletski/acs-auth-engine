const del = require('del');
const path = require('path');

del.sync([path.resolve(__dirname, '..', 'dist')], { force: true });
del.sync([path.resolve(__dirname, '..', '__generated__')], { force: true });
del.sync([path.resolve(__dirname, '..', 'www/dist')], { force: true });
del.sync([path.resolve(__dirname, '..', 'www/build')], { force: true });
del.sync([path.resolve(__dirname, '..', 'www/custom_html')], { force: true });
del.sync([path.resolve(__dirname, '..', 'www/*.css')], { force: true });
del.sync([path.resolve(__dirname, '..', 'www/*.map')], { force: true });
del.sync([path.resolve(__dirname, '..', 'www/*.js')], { force: true });
del.sync([path.resolve(__dirname, '..', 'www/*.html')], { force: true });
del.sync([path.resolve(__dirname, '..', 'www/*.ttf')], { force: true });
del.sync([path.resolve(__dirname, '..', 'www/email')], { force: true });
