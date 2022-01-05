import axios from 'axios';
import { Dispatch } from 'redux';

import { REQUEST_CANCELED_CODE } from 'consts/common';
import { handleErrorIfPresent, handleUnexpectedError } from 'data/actions/errors';
import {
    AuthMethod,
    chooseAuthMethod,
    getAuthStatus,
    IChooseAuthMethodResponse,
    IGetAuthStatusResponse,
    IGetAuthStatusResponseNotParsed,
    IInitAuthPayload,
    IInitAuthResponse,
    initAuth,
} from 'data/api/authentication';
import * as types from 'data/types';
import { booleanStringToBoolean } from 'utils/helpers';

const initAuthRequest = (authMethod?: AuthMethod) => ({ type: types.INIT_AUTH.REQUEST, authMethod });
const initAuthSuccess = (data: IInitAuthResponse) => ({ type: types.INIT_AUTH.SUCCESS, payload: data });
const initAuthFailure = (error: any) => ({ type: types.INIT_AUTH.FAILURE, error });

const getAuthStatusRequest = () => ({ type: types.GET_AUTH_STATUS.REQUEST });
const getAuthStatusSuccess = (data: IGetAuthStatusResponse) => ({ type: types.GET_AUTH_STATUS.SUCCESS, payload: data });
const getAuthStatusFailure = (error: any) => ({ type: types.GET_AUTH_STATUS.FAILURE, error });

const chooseAuthMethodRequest = (authMethod: AuthMethod) => ({ type: types.CHOOSE_AUTH_METHOD.REQUEST, authMethod });
const chooseAuthMethodSuccess = (data: IChooseAuthMethodResponse) => ({
    type: types.CHOOSE_AUTH_METHOD.SUCCESS,
    payload: data,
});
const chooseAuthMethodFailure = (error: any) => ({ type: types.CHOOSE_AUTH_METHOD.FAILURE, error });

const clearAuthData = () => ({ type: types.CLEAR_AUTH_DATA });

const { CancelToken } = axios;
let source = CancelToken.source();

/*
const mockInitAuthResponse = { authorizationCode: '1234' };
const mockGetAuthStatusResponse = { confirmationSuccessful: 'true' };
const mockChooseAuthMethodResponse = {};
 */

export const initAuthentication = (payload: IInitAuthPayload) => async (dispatch: Dispatch) => {
    dispatch(initAuthRequest(payload.authMethod));

    try {
        const data: IInitAuthResponse = await initAuth(payload, { cancelToken: source.token });

        if (!handleErrorIfPresent(data, dispatch, initAuthFailure)) {
            dispatch(initAuthSuccess(data));
        }

        return data;
    } catch (error) {
        if (error.message !== REQUEST_CANCELED_CODE) {
            dispatch(initAuthFailure(error));
            handleUnexpectedError(dispatch);
        }

        return error;
    }
};

export const fetchAuthStatus = () => async (dispatch: Dispatch) => {
    dispatch(getAuthStatusRequest());

    try {
        const data: IGetAuthStatusResponseNotParsed = await getAuthStatus({ cancelToken: source.token });

        if (!handleErrorIfPresent(data, dispatch)) {
            const parsedData: IGetAuthStatusResponse = {
                confirmationSuccessful: booleanStringToBoolean(data.confirmationSuccessful),
            };

            dispatch(getAuthStatusSuccess(parsedData));
        }
    } catch (error) {
        if (error.message !== REQUEST_CANCELED_CODE) {
            dispatch(getAuthStatusFailure(error));
            handleUnexpectedError(dispatch);
        }
    }
};

export const clearAuthenticationData = () => (dispatch: Dispatch) => {
    source.cancel(REQUEST_CANCELED_CODE);
    source = CancelToken.source();
    dispatch(clearAuthData());
};

export const chooseAuthenticationMethod = (authMethod: AuthMethod) => async (dispatch: Dispatch) => {
    dispatch(chooseAuthMethodRequest(authMethod));

    try {
        const data: IInitAuthResponse = await chooseAuthMethod({ authMethod });

        if (!handleErrorIfPresent(data, dispatch, chooseAuthMethodFailure)) {
            dispatch(chooseAuthMethodSuccess(data));
        }

        return data;
    } catch (error) {
        dispatch(chooseAuthMethodFailure(error));
        handleUnexpectedError(dispatch);

        return error;
    }
};
