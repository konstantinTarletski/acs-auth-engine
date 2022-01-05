import i18next from 'i18next';
import FocusviewContentCentered from 'luminor-components/lib/Applications/Web/Components/FocusviewContentCentered';
import TransactionInfo from 'luminor-components/lib/Applications/Web/Components/TransactionInfo';
import Button from 'luminor-components/lib/Components/Button';
import Buttons from 'luminor-components/lib/Components/Buttons';
import {
    IWithResponsiveWrapperProps,
    withResponsiveWrapper,
} from 'luminor-components/lib/Hocs/ResponsiveWrapper/index';
import * as React from 'react';
import { Trans, withTranslation, WithTranslation } from 'react-i18next';
import { connect } from 'react-redux';
import { compose } from 'redux';

import BackToMerchantForm from 'components/BackToMerchantForm';
import Page from 'components/Page';
import { changeLanguage } from 'data/actions/currentLanguage';
import UiActions from 'data/api/uiActions';
import { IInitInfoState } from 'data/reducers/initInfo';
import { ITranslationsState } from 'data/reducers/translations';
import { IStoreState } from 'data/rootReducer';
import { CurrentLanguage } from 'utils/helpers';

import './index.scss';

export interface IOwnProps extends WithTranslation {}

export interface IStateProps {
    initInfo: IInitInfoState;
    translations: ITranslationsState;
}

export interface IDispatchProps {
    dispatchChangeLanguage: (language: CurrentLanguage, didUserSelect?: boolean) => void;
}

type Props = IDispatchProps & IStateProps & IOwnProps & IWithResponsiveWrapperProps;

export interface IState {
    secondsTillAutoRedirect: number;
    hasBackToMerchantStarted: boolean;
}

class AuthenticationSuccess extends React.Component<Props, IState> {
    private submitBackToMerchantButtonId = 'success-back-to-merchant';

    constructor(props) {
        super(props);
        this.state = {
            secondsTillAutoRedirect: 3,
            hasBackToMerchantStarted: false,
        };
    }

    componentDidMount = () => {
        setInterval(this.decreaseCounter, 1000);
    };

    componentDidUpdate() {
        const { secondsTillAutoRedirect } = this.state;

        if (secondsTillAutoRedirect === 0) {
            this.handleBackToMerchant();
        }
    }

    handleUserChangedLanguage = (language) => {
        const { dispatchChangeLanguage } = this.props;

        i18next.changeLanguage(language.value);
        dispatchChangeLanguage(language.value, true);
    };

    decreaseCounter = () => {
        const { secondsTillAutoRedirect } = this.state;

        if (secondsTillAutoRedirect > 0) {
            this.setState((prevState) => ({ secondsTillAutoRedirect: prevState.secondsTillAutoRedirect - 1 }));
        }
    };

    renderYouJustPaidMessage = (sum, merchantName) => (
        <Trans
            i18nKey="screens.authSuccess.justPaid"
            values={{ sum, merchantName }}
            components={{ bold: <strong /> }}
        />
    );

    handleBackToMerchant = () => {
        const { hasBackToMerchantStarted } = this.state;

        if (!hasBackToMerchantStarted) {
            this.setState({ hasBackToMerchantStarted: true });
            document.getElementById(this.submitBackToMerchantButtonId).click();
        }
    };

    render() {
        const { secondsTillAutoRedirect } = this.state;
        const { t, responsiveData, initInfo, translations } = this.props;
        const { purchaseAmount, purchaseCurrency, merchantName } = initInfo.data;
        const { breakpointMobile } = responsiveData;

        return (
            <Page
                merchantName={merchantName}
                breakpointMobile={breakpointMobile}
                onChangeLanguage={this.handleUserChangedLanguage}
                languagesToShow={Object.keys(translations.data) as CurrentLanguage[]}
            >
                <FocusviewContentCentered>
                    <div className="focusview-content-centered-middle">
                        <TransactionInfo
                            success
                            message={this.renderYouJustPaidMessage(
                                `${purchaseAmount} ${purchaseCurrency}`,
                                merchantName,
                            )}
                        />
                    </div>
                    <div className="focusview-content-centered-bottom">
                        <div className="success-redirect-text">
                            <Trans
                                i18nKey="screens.authSuccess.youWillBeRedirected"
                                values={{ sec: secondsTillAutoRedirect }}
                            />
                        </div>
                        <Buttons layout="vertical">
                            <Button
                                title={t('screens.authSuccess.backToMerchantButton')}
                                onClick={this.handleBackToMerchant}
                            />
                        </Buttons>
                    </div>
                    <BackToMerchantForm
                        submitButtonId={this.submitBackToMerchantButtonId}
                        uiAction={UiActions.BACK_TO_MERCHANT_SUCCESS}
                    />
                </FocusviewContentCentered>
            </Page>
        );
    }
}

const mapStateToProps = (state: IStoreState) => {
    return {
        initInfo: state.initInfo,
        translations: state.translations,
    };
};

const mapDispatchToProps = (dispatch) => ({
    dispatchChangeLanguage: (language: CurrentLanguage, didUserSelect?: boolean) =>
        dispatch(changeLanguage(language, didUserSelect)),
});

export default compose<any>(
    connect(mapStateToProps, mapDispatchToProps),
    (component) => withResponsiveWrapper(component, ['breakpointMobile']),
    withTranslation(),
)(AuthenticationSuccess);
