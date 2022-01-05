import { AuthMethod } from 'data/api/authentication';

import * as types from '../types';

export interface IAuthenticationData {
    authorizationCode?: string;
    confirmationSuccessful?: boolean;
    authMethod?: AuthMethod;
    redirectUrl?: string;
}

export interface IAuthenticationState {
    data: IAuthenticationData;
    isFetching: boolean;
}

const initialState = {
    data: {},
    isFetching: false,
};

const authenticationReducer = (state: IAuthenticationState = initialState, action: any) => {
    switch (action.type) {
        case types.CHOOSE_AUTH_METHOD.REQUEST:
        case types.INIT_AUTH.REQUEST: {
            if (state.data.authMethod) {
                return { ...state, isFetching: true, error: null };
            }

            return { ...state, isFetching: true, data: { authMethod: action.authMethod }, error: null };
        }

        case types.GET_AUTH_STATUS.REQUEST: {
            return { ...state, isFetching: true, error: null };
        }

        case types.GET_AUTH_STATUS.SUCCESS:
        case types.INIT_AUTH.SUCCESS: {
            return { ...state, isFetching: false, data: { ...state.data, ...action.payload }, error: null };
        }

        case types.CHOOSE_AUTH_METHOD.SUCCESS: {
            return { ...state, isFetching: false, error: null };
        }

        case types.CHOOSE_AUTH_METHOD.FAILURE:
        case types.GET_AUTH_STATUS.FAILURE:
        case types.INIT_AUTH.FAILURE: {
            return { ...state, isFetching: false, error: action.error };
        }

        case types.CLEAR_AUTH_DATA: {
            return initialState;
        }

        default: {
            return state;
        }
    }
};

export default authenticationReducer;
