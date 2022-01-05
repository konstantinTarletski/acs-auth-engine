import { Dispatch } from 'redux';

import { handleUnexpectedError } from 'data/actions/errors';
import getTranslations, { ITranslations } from 'data/api/translations';
import * as types from 'data/types';

const getTranslationsRequest = () => ({ type: types.GET_TRANSLATIONS.REQUEST });
const getTranslationsSuccess = (data: ITranslations) => ({ type: types.GET_TRANSLATIONS.SUCCESS, payload: data });
const getTranslationsFailure = (error: any) => ({ type: types.GET_TRANSLATIONS.FAILURE, error });

const fetchTranslations = () => async (dispatch: Dispatch) => {
    dispatch(getTranslationsRequest());

    try {
        const data: ITranslations = await getTranslations();

        dispatch(getTranslationsSuccess(data));

        return data;
    } catch (error) {
        dispatch(getTranslationsFailure(error));
        handleUnexpectedError(dispatch);

        return error;
    }
};

export default fetchTranslations;
