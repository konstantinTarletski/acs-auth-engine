import { Dispatch } from 'redux';

import showPopUpError from 'common/errors';
import { ERROR, GENERAL_EXCEPTION_ERROR_CODE } from 'consts/common';
import * as types from 'data/types';

const pushPopUpError = (error: IError) => ({ type: types.PUSH_POP_UP_ERROR, payload: error });
const setCurrentError = (error: IError) => ({ type: types.SET_CURRENT_ERROR, payload: error });
const clearCurrentError = () => ({ type: types.CLEAR_CURRENT_ERROR });
const setPathAfterError = (payload) => ({ type: types.SET_PATH_TO_REDIRECT_AFTER_ERROR, payload });

export enum ErrorType {
    FATAL = 'FATAL',
    POP_UP = 'POP_UP',
    NON_FATAL = 'NON_FATAL',
}

export interface IError {
    errorCode?: string;
    errorType?: ErrorType;
    errorTranslation?: string;
}

const handleError = (error: IError, dispatch) => {
    switch (error.errorType) {
        case ErrorType.FATAL:
        case ErrorType.NON_FATAL:
            dispatch(setCurrentError(error));
            break;
        case ErrorType.POP_UP:
            dispatch(pushPopUpError(error));
            showPopUpError(error.errorCode);
            break;
        default:
        // unknown error
    }
};

export const handleErrorIfPresent = (error: IError, dispatch, popUpErrorCallback?) => {
    if (error.errorType) {
        // to prevent showing errors after initial error is shown
        if (ERROR && error.errorType !== ErrorType.FATAL) {
            return !!error.errorType;
        }

        handleError(error, dispatch);

        if (popUpErrorCallback && error.errorType === ErrorType.POP_UP) {
            dispatch(popUpErrorCallback(error));
        }
    }

    return !!error.errorType;
};

export const handleUnexpectedError = (dispatch) => {
    dispatch(
        setCurrentError({
            errorCode: GENERAL_EXCEPTION_ERROR_CODE,
            errorType: ErrorType.NON_FATAL,
        }),
    );
};

export const handleInitialErrorIfPresent = () => (dispatch: Dispatch) => {
    if (ERROR) {
        handleErrorIfPresent(JSON.parse(ERROR), dispatch);
    }
};

export const clearError = () => (dispatch: Dispatch) => {
    dispatch(clearCurrentError());
};

export const setPathToRedirectAfterError = (pathToRedirectAfterError: string) => (dispatch: Dispatch) => {
    dispatch(setPathAfterError(pathToRedirectAfterError));
};
