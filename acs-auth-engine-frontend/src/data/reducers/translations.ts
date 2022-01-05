import { ITranslations } from 'data/api/translations';

import * as types from '../types';

export interface ITranslationsState {
    data: ITranslations;
    isFetching: boolean;
    isFetched: boolean;
}

const initialState = {
    data: {},
    isFetching: false,
    isFetched: false,
};

const translationsReducer = (state: ITranslationsState = initialState, action: any) => {
    switch (action.type) {
        case types.GET_TRANSLATIONS.REQUEST: {
            return { ...state, isFetching: true, isFetched: false, error: null };
        }

        case types.GET_TRANSLATIONS.SUCCESS: {
            return { ...state, isFetching: false, isFetched: true, data: action.payload, error: null };
        }

        case types.GET_TRANSLATIONS.FAILURE: {
            return { ...state, isFetching: false, isFetched: true, error: action.error };
        }

        default: {
            return state;
        }
    }
};

export default translationsReducer;
