import { API_URL } from 'consts/common';

const apiBaseUrl = API_URL;

const apis = {
    EMPTY: '',
    OTP: '/browser/otp',
    BROWSER: '/browser',
    GET_TRANSLATIONS: `${apiBaseUrl}/api/translations`,
};

export default apis;
