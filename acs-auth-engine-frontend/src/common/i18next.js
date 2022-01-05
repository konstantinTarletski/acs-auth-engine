import i18next from 'i18next';
import { initReactI18next } from 'react-i18next';

import { getDefaultLanguage } from 'utils/helpers';

const enCommon = {
    screens: {
        failure: {
            error: {
                title: 'Your card is Blocked',
                description:
                    'Your payment card is blocked or has expired, so the purchase cannot be ' +
                    'completed. Check your card status in your Internet Bank.',
            },
            backToMerchantButton: 'BACK TO MERCHANT',
        },
        auth: {
            code: {
                title: 'Your control code is',
                description: 'Notification is sent to your mobile! Please check the code and enter PIN1 on your phone!',
            },
            backButton: 'BACK',
        },
        authFailure: {
            buttons: {
                tryAgain: 'TRY AGAIN',
                cancel: 'CANCEL PAYMENT',
            },
        },
        authSuccess: {
            justPaid: 'You just paid <bold>{{sum}}</bold> to',
            backToMerchantButton: 'BACK TO MERCHANT WEBSITE',
        },
        choose: {
            title: 'Keep Your Account Safe',
            description: 'Please authenticate the purchase',
            buttons: {
                continue: 'LOG IN',
                cancel: 'CANCEL PAYMENT',
            },
        },
    },
    paymentDetails: {
        amount: 'AMOUNT',
        card: 'PAYMENT CARD',
    },
    general: {
        youCameFromWebsite: 'You came here from website:',
    },
};

const resources = {
    en: {
        common: enCommon,
    },
};

const i18nextConfig = i18next.use(initReactI18next).init({
    lng: getDefaultLanguage(),
    resources,
    defaultNS: 'common',
    interpolation: {
        escapeValue: false,
    },
});

export default i18nextConfig;
