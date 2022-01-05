import i18n from 'i18next';
import { Toaster } from 'luminor-components/lib/Components/Toaster/index';

import { ERROR_POP_UP_TIMEOUT_IN_MS } from 'consts/common';

enum MessageType {
    Danger = 'danger',
}

const showPopUp = (intent: MessageType, messageCode: string) => {
    Toaster.addToast({
        intent: MessageType.Danger,
        text: i18n.t(messageCode),
        timeout: ERROR_POP_UP_TIMEOUT_IN_MS || 3000,
    });
};

const showPopUpError = (errorCode: string) => {
    showPopUp(MessageType.Danger, `errors.${errorCode}.description`);
};

export default showPopUpError;
