import FocusviewContentCentered from 'luminor-components/lib/Applications/Web/Components/FocusviewContentCentered';
import Button from 'luminor-components/lib/Components/Button';
import Buttons from 'luminor-components/lib/Components/Buttons';
import ButtonWithIcon from 'luminor-components/lib/Components/ButtonWithIcon';
import Overlay from 'luminor-components/lib/Components/Overlay';
import LayoutWide from 'luminor-components/lib/Layouts/Wide';
import * as React from 'react';
import { WithTranslation, withTranslation } from 'react-i18next';
import { connect } from 'react-redux';
import { compose } from 'redux';

import BackToMerchantForm from 'components/BackToMerchantForm';
import PaymentDetails from 'components/PaymentDetails';
import UiActions from 'data/api/uiActions';
import { IInitInfoState } from 'data/reducers/initInfo';
import { IStoreState } from 'data/rootReducer';

export interface IOwnProps extends WithTranslation {
    isPopupOpen: boolean;
    closeCancelPopUp: () => void;
}

export interface IStateProps {
    initInfo: IInitInfoState;
}

export interface IDispatchProps {}

type Props = IDispatchProps & IStateProps & IOwnProps;

export interface IState {}

class AuthenticationCancelPopUp extends React.Component<Props, IState> {
    private submitBackToMerchantButtonId = 'cancel-back-to-merchant';

    constructor(props) {
        super(props);
        this.state = {};
    }

    componentDidMount = () => {};

    cancelThePayment = () => {
        document.getElementById(this.submitBackToMerchantButtonId).click();
    };

    render() {
        const { t, isPopupOpen, closeCancelPopUp, initInfo } = this.props;
        const { purchaseAmount, acctNumber, purchaseCurrency, merchantName } = initInfo.data;

        return (
            <Overlay isOpen={isPopupOpen} doClose={closeCancelPopUp} layout="focusview" className="">
                <LayoutWide
                    addOn={<ButtonWithIcon onClick={closeCancelPopUp} icon="close" size="large" intent="secondary" />}
                    layout="custom"
                    className="main"
                >
                    <FocusviewContentCentered>
                        <div className="focusview-content-centered-middle">
                            <PaymentDetails
                                amount={purchaseAmount}
                                currency={purchaseCurrency}
                                paymentCard={acctNumber}
                                title={t('screens.authCancel.title')}
                                description={t('screens.authCancel.description')}
                                merchantName={merchantName}
                            />
                        </div>
                        <div className="focusview-content-centered-bottom">
                            <Buttons layout="vertical">
                                <Button
                                    onClick={this.cancelThePayment}
                                    title={t('screens.authCancel.buttons.continue')}
                                />
                                <Button
                                    layout="link"
                                    onClick={closeCancelPopUp}
                                    title={t('screens.authCancel.buttons.back')}
                                />
                            </Buttons>
                        </div>
                        <BackToMerchantForm
                            submitButtonId={this.submitBackToMerchantButtonId}
                            uiAction={UiActions.BACK_TO_MERCHANT_CANCEL}
                        />
                    </FocusviewContentCentered>
                </LayoutWide>
            </Overlay>
        );
    }
}

const mapStateToProps = (state: IStoreState) => {
    return {
        initInfo: state.initInfo,
    };
};

export default compose<any>(connect(mapStateToProps, null), withTranslation())(AuthenticationCancelPopUp);
