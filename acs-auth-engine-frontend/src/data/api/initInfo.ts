import { IError } from 'data/actions/errors';
import { AuthMethod } from 'data/api/authentication';
import postToSelfUrl from 'data/api/postToSelfUrl';
import UiActions from 'data/api/uiActions';

export enum State {
    RENDER_ENTER_LOGIN_PAGE = 'RENDER_ENTER_LOGIN_PAGE',
    RENDER_SELECT_AUTH_METHOD_PAGE = 'RENDER_SELECT_AUTH_METHOD_PAGE',
    RENDER_SUCCESSFUL_PAGE = 'RENDER_SUCCESSFUL_PAGE',
    RENDER_ERROR_PAGE = 'RENDER_ERROR_PAGE',
    REDIRECTED_TO_EXTERNAL_SYSTEM = 'REDIRECTED_TO_EXTERNAL_SYSTEM',
}

export type Country = 'LT' | 'LV' | 'EE';

export interface IInitInfo extends IError {
    state?: State;
    acctNumber?: string;
    purchaseDate?: string;
    purchaseCurrency?: string;
    merchantName?: string;
    purchaseAmount?: string;
    defaultAuthMethod?: AuthMethod;
    availableAuthMethods?: AuthMethod[];
    country?: Country;
}

export interface IInitInfoNotParsed extends IError {
    state?: State;
    acctNumber?: string;
    purchaseDate?: string;
    purchaseCurrency?: string;
    merchantName?: string;
    purchaseAmount?: string;
    defaultAuthMethod?: AuthMethod;
    availableAuthMethods?: string;
}

export const getInitInfoData = async () => {
    return postToSelfUrl<IInitInfoNotParsed>(UiActions.GET_INITIAL_INFORMATION);
};

export const getInitInfoDataForEnteredLogin = async (enteredLogin: string) => {
    return postToSelfUrl<IInitInfoNotParsed>(UiActions.CONFIRM_USER_LOGIN, {
        enteredLogin,
    });
};
