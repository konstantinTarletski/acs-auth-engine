import { combineReducers } from 'redux';

import authenticationReducer, { IAuthenticationState } from 'data/reducers/authentication';
import currentLanguageReducer, { ICurrentLanguageState } from 'data/reducers/currentLanguage';
import errorsReducer, { IErrorsState } from 'data/reducers/errors';
import initInfoReducer, { IInitInfoState } from 'data/reducers/initInfo';
import translationsReducer, { ITranslationsState } from 'data/reducers/translations';

export interface IStoreState {
    initInfo: IInitInfoState;
    translations: ITranslationsState;
    authentication: IAuthenticationState;
    currentLanguageState: ICurrentLanguageState;
    errors: IErrorsState;
}

const rootReducer = combineReducers<IStoreState>({
    initInfo: initInfoReducer,
    translations: translationsReducer,
    authentication: authenticationReducer,
    currentLanguageState: currentLanguageReducer,
    errors: errorsReducer,
});

export type RootState = ReturnType<typeof rootReducer>;

export default rootReducer;
