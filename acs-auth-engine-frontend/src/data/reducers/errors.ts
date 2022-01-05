import paths from 'consts/routes';
import { IError } from 'data/actions/errors';

import * as types from '../types';

export interface IErrorsState {
    currentError: IError;
    popUpErrors: IError[];
    pathToRedirectAfterError: string;
}

const initialState = {
    currentError: {},
    popUpErrors: [],
    pathToRedirectAfterError: paths.CHOOSE_AUTH_METHOD,
};

const errorsReducer = (state: IErrorsState = initialState, action: any) => {
    switch (action.type) {
        case types.PUSH_POP_UP_ERROR: {
            return { ...state, popUpErrors: [...state.popUpErrors, action.payload] };
        }

        case types.SET_CURRENT_ERROR: {
            return { ...state, currentError: action.payload };
        }

        case types.CLEAR_CURRENT_ERROR: {
            return { ...state, currentError: {} };
        }

        case types.SET_PATH_TO_REDIRECT_AFTER_ERROR: {
            return { ...state, pathToRedirectAfterError: action.payload };
        }

        default: {
            return state;
        }
    }
};

export default errorsReducer;
