const chalk = require('chalk');
const async = require('async');
const im = require('imagemagick');

const faviconSettings = {
    android: {
        name: 'favicon',
        type: 'icon',
        sizes: [
            {width: 36, height: 36},
            {width: 48, height: 48},
            {width: 72, height: 72},
            {width: 96, height: 96},
            {width: 144, height: 144},
            {width: 192, height: 192}
        ]
    },
    appleStartup: {
        name: 'apple-touch-startup-image',
        type: 'splash',
        sizes: [
            {width: 1536, height: 2008, media: {deviceWidth: 768, deviceHeight: 1024, orientation: 'portrait', pixelratio: 2}},
            {width: 2048, height: 1496, media: {deviceWidth: 768, deviceHeight: 1024, orientation: 'landscape', pixelratio: 2}},
            {width: 768, height: 1004, media: {deviceWidth: 768, deviceHeight: 1024, orientation: 'portrait', pixelratio: 1}},
            {width: 1024, height: 748, media: {deviceWidth: 768, deviceHeight: 1024, orientation: 'landscape', pixelratio: 1}},
            {width: 640, height: 1096, media: {deviceWidth: 320, deviceHeight: 568, pixelratio: 2}},
            {width: 640, height: 920, media: {deviceWidth: 320, deviceHeight: 480, pixelratio: 2}},
            {width: 320, height: 460, media: {deviceWidth: 320, deviceHeight: 480, pixelratio: 1}}
        ]
    },
    appleTouch: {
        name: 'apple-touch-icon',
        type: 'icon',
        sizes: [
            {width: 57, height: 57}, // Non-Retina iPhone, ? iOS 6
            {width: 60, height: 60}, // Non-Retina iPhone, ? iOS 7
            {width: 72, height: 72}, // Non-Retina iPad, ? iOS 6
            {width: 76, height: 76}, // Non-Retina iPad, ? iOS 7
            {width: 114, height: 114}, // Retina iPhone, ? iOS 6
            {width: 120, height: 120}, // Retina iPhone, ? iOS 7
            {width: 144, height: 144}, // Retina iPad, ? iOS 6
            {width: 152, height: 152}, // Retina iPad, ? iOS 7
            {width: 180, height: 180}  // iPhone 6 Plus, ? iOS 8
        ]
    },
    coast: {
        name: 'coast',
        type: 'icon',
        sizes: [
            {width: 228, height: 228}
        ]
    },
    favico: {
        name: 'favicon',
        type: 'icon',
        sizes: [
            {width: 48, height: 48},
            {width: 32, height: 32},
            {width: 16, height: 16}
        ]
    },
    favicon: {
        name: 'favicon',
        type: 'icon',
        sizes: [
            {width: 160, height: 160},
            {width: 96, height: 96},
            {width: 32, height: 32},
            {width: 16, height: 16}
        ]
    },
    firefox: {
        name: 'firefox_app',
        type: 'icon',
        sizes: [
            {width: 512, height: 512},
            {width: 128, height: 128},
            {width: 60, height: 60}
        ]
    },
    opengraph: {
        name: 'open-graph',
        type: 'splash',
        sizes: [
            {width: 1500, height: 1500}
        ]
    },
    windows: {
        name: 'mstile',
        type: 'icon',
        sizes: [
            {width: 70, height: 70},
            {width: 144, height: 144},
            {width: 150, height: 150},
            {width: 310, height: 150},
            {width: 310, height: 310}
        ]
    },
    yandex: {
        name: 'yandex',
        type: 'icon',
        sizes: [
            {width: 50, height: 50}
        ]
    }
};

function faviconsGenerator(params) {
    console.time(chalk.green('Favicons build done'));

    function generateIcon(favicon) {
        im.convert([
                favicon.src,
                '-filter', 'Lanczos',
                '-background', (favicon.background ? favicon.background : 'transparent'),
                '-layers', 'flatten',
                '-alpha', (favicon.background ? 'remove' : 'set'),
                '-gravity', 'center',
                '-resize', (favicon.dimensions) + '^',
                '-extent', favicon.dimensions,
                favicon.dest], function (err) {
                if (err) {
                    console.log(chalk.red(err));
                }
            }
        );
    }

    function makeFavicon() {
        im.convert([
                params.srcFavicon,
                '-filter', 'Lanczos',
                '-background', 'transparent',
                '-layers', 'flatten',
                '-alpha', 'set',
                '-colors', '256',
                '-define', 'icon:auto-resize=128,64,48,32,16',
                params.dest + 'favicon.ico'
            ],
            function (err) {
                if (err) {
                    console.log(chalk.red(err));
                }
            }
        );
    }

    function calculateDimensions(favicon) {
        var x = favicon.width,
            y = favicon.height,
            dimensions = x + 'x' + y;
        return [dimensions];
    }

    function main() {
        async.each(Object.keys(faviconSettings), function (favicons) {
            async.each(faviconSettings[favicons].sizes, function (favicon) {
                var dimensions = calculateDimensions(favicon),
                    filename = faviconSettings[favicons].name + '-' + dimensions[0] + '.png',
                    faviconParams = {
                        src: faviconSettings[favicons].type === 'splash' ? params.srcSplash : params.srcIcon,
                        dest: params.dest + filename,
                        dimensions: dimensions[0],
                        background: params.bgcolor
                    };
                generateIcon(faviconParams);
            }, function (err) {
                if (err) {
                    console.log(chalk.red(getStamp() + ' ' + err));
                } else {
                    console.timeEnd(chalk.green('Favicons build done'));
                }
            });
        }, function (err) {
            console.log(chalk.red(getStamp() + ' ' + err));
        });
    }

    main();
    makeFavicon();
}

faviconsGenerator({
    srcIcon: './assets/icons/icon.psd',
    srcFavicon: './assets/icons/favicon.psd',
    srcSplash: './assets/icons/splash.psd',
    bgcolor: '#ffffff',
    dest: './www/assets/favicons/'
});