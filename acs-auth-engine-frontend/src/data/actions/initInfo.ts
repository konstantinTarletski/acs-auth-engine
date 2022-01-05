import { Dispatch } from 'redux';

import { handleErrorIfPresent, handleUnexpectedError } from 'data/actions/errors';
import { AuthMethod } from 'data/api/authentication';
import { getInitInfoData, getInitInfoDataForEnteredLogin, IInitInfo } from 'data/api/initInfo';
import * as types from 'data/types';

const getInitInfoRequest = () => ({ type: types.GET_INIT_INFO.REQUEST });
const getInitInfoSuccess = (data: IInitInfo) => ({ type: types.GET_INIT_INFO.SUCCESS, payload: data });
const getInitInfoFailure = (error: any) => ({ type: types.GET_INIT_INFO.FAILURE, error });

const getInitInfoForEnteredLoginRequest = () => ({ type: types.GET_INIT_INFO_FOR_ENTERED_LOGIN.REQUEST });
const getInitInfoForEnteredLoginSuccess = (data: IInitInfo) => ({
    type: types.GET_INIT_INFO_FOR_ENTERED_LOGIN.SUCCESS,
    payload: data,
});
const getInitInfoForEnteredLoginFailure = (error: any) => ({
    type: types.GET_INIT_INFO_FOR_ENTERED_LOGIN.FAILURE,
    error,
});

/*
const mockGetInitInfoResponse = {
    state: State.RENDER_ENTER_LOGIN_PAGE,
    acctNumber: 'XXXX XXXX XXXX 1234',
    purchaseAmount: '15',
    purchaseCurrency: 'EUR',
    merchantName: 'Merchant name AS',
    country: 'LT',
    userLanguage: null,
};

const mockGetInitInfoForEnteredLoginResponse = {
    availableAuthMethods: 'SMART_ID,CODE_CALCULATOR,M_SIGNATURE',
    defaultAuthMethod: AuthMethod.CODE_CALCULATOR,
    userLanguage: 'EN',
};
*/

export const fetchInitInfo = () => async (dispatch: Dispatch) => {
    dispatch(getInitInfoRequest());

    try {
        const data = await getInitInfoData();

        if (!handleErrorIfPresent(data, dispatch)) {
            const parsedData: IInitInfo = {
                ...data,
                availableAuthMethods: data.availableAuthMethods
                    ? (data.availableAuthMethods.split(',') as AuthMethod[])
                    : undefined,
            };

            dispatch(getInitInfoSuccess(parsedData));

            return parsedData;
        }

        return data;
    } catch (error) {
        dispatch(getInitInfoFailure(error));
        handleUnexpectedError(dispatch);

        return error;
    }
};

export const fetchInitInfoForEnteredLogin = (enteredLogin: string) => async (dispatch: Dispatch) => {
    dispatch(getInitInfoForEnteredLoginRequest());

    try {
        const data = await getInitInfoDataForEnteredLogin(enteredLogin);

        if (!handleErrorIfPresent(data, dispatch, getInitInfoForEnteredLoginFailure)) {
            const parsedData: IInitInfo = {
                ...data,
                availableAuthMethods: data.availableAuthMethods.split(',') as AuthMethod[],
            };

            dispatch(getInitInfoForEnteredLoginSuccess(parsedData));

            return parsedData;
        }

        return data;
    } catch (error) {
        dispatch(getInitInfoForEnteredLoginFailure(error));
        handleUnexpectedError(dispatch);

        return error;
    }
};
