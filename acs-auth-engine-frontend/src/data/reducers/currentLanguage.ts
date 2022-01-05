import { CurrentLanguage, getDefaultLanguage } from 'utils/helpers';

import * as types from '../types';

export interface ICurrentLanguageState {
    currentLanguage: CurrentLanguage;
    didUserSelect: boolean;
}

const initialState = {
    currentLanguage: getDefaultLanguage(),
    didUserSelect: false,
};

const currentLanguageReducer = (state: ICurrentLanguageState = initialState, action: any) => {
    switch (action.type) {
        case types.SET_CURRENT_LANGUAGE: {
            if (state.didUserSelect) {
                return { ...state, currentLanguage: action.payload.currentLanguage };
            }

            return action.payload;
        }

        default: {
            return state;
        }
    }
};

export default currentLanguageReducer;
