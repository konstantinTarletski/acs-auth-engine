import { stringify } from 'querystring';

import axios from 'axios';

import { CREQ, ACS_TRANSACTION_ID } from 'consts/common';
import apis from 'data/api/apis';
import UiActions from 'data/api/uiActions';
import { stringResponseToObject } from 'utils/helpers';

/*
const mockResponse =
    'state=RENDER_SELECT_AUTH_METHOD_PAGE&availableAuthMethods=SMART_ID,CODE_CALCULATOR,M_SIGNATURE' +
    '&defaultAuthMethod=CODE_CALCULATOR&acctNumber=XXXX XXXX XXXX 1234&purchaseAmount=15' +
    '&purchaseCurrency=EUR&merchantName=Merchant name AS&authorizationCode=1234&confirmationSuccessful=false';
*/

const postToSelfUrl = async <T>(uiAction: UiActions, payload = {}, config?): Promise<T> => {
    const requestBody = {
        whitelist: 'confirmed',
        creq: CREQ,
        threeDSSessionData: ACS_TRANSACTION_ID,
        uiAction,
        ...payload,
    };

    const { data } = await axios.post<string>(apis.OTP, stringify(requestBody), {
        ...config,
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
    });

    return stringResponseToObject(data);
};

export default postToSelfUrl;
