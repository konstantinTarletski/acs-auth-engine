import { mapValues, keyBy } from 'lodash';

import { DEFAULT_LANGUAGE, FATAL_EXCEPTION_ERROR_CODE } from 'consts/common';
import { ErrorType } from 'data/actions/errors';

// eslint-disable-next-line import/prefer-default-export
export const stringResponseToObject = (stringResponse: string) => {
    if (!stringResponse) {
        return {};
    }

    if (stringResponse.includes('</html>')) {
        return {
            errorCode: FATAL_EXCEPTION_ERROR_CODE,
            errorType: ErrorType.FATAL,
        };
    }

    const jsonWithStringValues = JSON.parse(
        `{"${stringResponse.replace(/&/g, '","').replace(/=/g, '":"').replace('\r', '\\r').replace('\n', '\\n')}"}`,
        (key, value) => {
            const valueToDecode = key && value && value.replace(/\+/g, '%20');

            return key === '' ? value : decodeURIComponent(valueToDecode);
        },
    );
    const paramsList = Object.keys(jsonWithStringValues).map((key) => ({
        key,
        value: ['null', ''].includes(jsonWithStringValues[key]) ? undefined : jsonWithStringValues[key],
    }));

    return mapValues(keyBy(paramsList, 'key'), 'value');
};

export const booleanStringToBoolean = (booleanString: string): boolean => {
    if (!booleanString) {
        return undefined;
    }

    return booleanString === 'true';
};

export const isEmptyObject = (obj): boolean => {
    return Object.keys(obj).length === 0 && obj.constructor === Object;
};

export type CurrentLanguage = 'en' | 'ru' | 'lt' | 'lv' | 'et';

export const getDefaultLanguage = (): CurrentLanguage => {
    return DEFAULT_LANGUAGE.toLowerCase() as CurrentLanguage;
};

export const containsNotDigits = (value: string): boolean => {
    return !/^\d*$/.test(value);
};

export const inIframe = (): boolean => {
    try {
        return window.self !== window.top;
    } catch (e) {
        return true;
    }
};
