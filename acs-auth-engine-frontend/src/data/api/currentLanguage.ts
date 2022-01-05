import postToSelfUrl from 'data/api/postToSelfUrl';
import UiActions from 'data/api/uiActions';
import { CurrentLanguage } from 'utils/helpers';

const changeCurrentLanguage = async (language: CurrentLanguage) => {
    return postToSelfUrl(UiActions.CHANGE_CURRENT_LANGUAGE, { language });
};

export default changeCurrentLanguage;
