import * as React from 'react';

import { ACS_TRANSACTION_ID, CREQ } from 'consts/common';
import apis from 'data/api/apis';
import UiActions from 'data/api/uiActions';

interface IProps {
    submitButtonId: string;
    uiAction: UiActions;
}

const BackToMerchantForm: React.FC<IProps> = ({ submitButtonId, uiAction }) => {
    return (
        <form method="post" action={apis.OTP}>
            <input type="text" hidden readOnly name="whitelist" value="confirmed" />
            <input type="text" hidden readOnly name="creq" value={CREQ} />
            <input type="text" hidden readOnly name="threeDSSessionData" value={ACS_TRANSACTION_ID} />
            <input type="text" hidden readOnly name="uiAction" value={uiAction} />
            <input id={submitButtonId} type="submit" hidden />
        </form>
    );
};

export default BackToMerchantForm;
