import { Dispatch } from 'redux';

import changeCurrentLanguage from 'data/api/currentLanguage';
import * as types from 'data/types';
import { CurrentLanguage } from 'utils/helpers';

const setCurrentLanguage = (language: CurrentLanguage, didUserSelect: boolean = false) => ({
    type: types.SET_CURRENT_LANGUAGE,
    payload: {
        currentLanguage: language,
        didUserSelect,
    },
});

export const changeLanguage = (language: CurrentLanguage, didUserSelect: boolean = false) => async (
    dispatch: Dispatch,
) => {
    dispatch(setCurrentLanguage(language, didUserSelect));

    if (didUserSelect) {
        changeCurrentLanguage(language);
    }
};

export default setCurrentLanguage;
