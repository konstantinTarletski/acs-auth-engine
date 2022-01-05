import { AxiosRequestConfig } from 'axios';

import { IError } from 'data/actions/errors';
import postToSelfUrl from 'data/api/postToSelfUrl';
import UiActions from 'data/api/uiActions';

export interface IChooseAuthMethodResponse extends IError {}

export interface IInitAuthResponse extends IError {
    authorizationCode?: string;
}

export interface IGetAuthStatusResponseNotParsed extends IError {
    confirmationSuccessful?: string;
}

export interface IGetAuthStatusResponse extends IError {
    confirmationSuccessful?: boolean;
}

export enum AuthMethod {
    SMART_ID = 'SMART_ID',
    M_SIGNATURE = 'M_SIGNATURE',
    CODE_CALCULATOR = 'CODE_CALCULATOR',
    ID_CARD = 'ID_CARD',
}

export interface IInitAuthPayload {
    authMethod?: AuthMethod;
    confirmationCode?: string;
}

export interface IChooseAuthMethodPayload {
    authMethod: AuthMethod;
}

export const initAuth = async (payload: IInitAuthPayload, config: AxiosRequestConfig) => {
    return postToSelfUrl<IInitAuthResponse>(UiActions.INIT_AUTH, payload, config);
};

export const getAuthStatus = async (config: AxiosRequestConfig) => {
    return postToSelfUrl<IGetAuthStatusResponseNotParsed>(UiActions.AUTH_STATUS, null, config);
};

export const chooseAuthMethod = async (payload: IChooseAuthMethodPayload) => {
    return postToSelfUrl<IInitAuthResponse>(UiActions.CHOOSE_AUTH_METHOD, payload);
};
