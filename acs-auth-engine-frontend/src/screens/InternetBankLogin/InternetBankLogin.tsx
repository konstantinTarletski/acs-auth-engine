import i18next from 'i18next';
import FocusviewContentCentered from 'luminor-components/lib/Applications/Web/Components/FocusviewContentCentered';
import Button from 'luminor-components/lib/Components/Button';
import Buttons from 'luminor-components/lib/Components/Buttons';
import ControlInput from 'luminor-components/lib/Components/ControlInput/index';
import FormRow from 'luminor-components/lib/Components/FormRow/index';
import {
    IWithResponsiveWrapperProps,
    withResponsiveWrapper,
} from 'luminor-components/lib/Hocs/ResponsiveWrapper/index';
import * as React from 'react';
import { withTranslation, WithTranslation } from 'react-i18next';
import { connect } from 'react-redux';
import { RouteComponentProps } from 'react-router-dom';
import { compose } from 'redux';

import Page from 'components/Page';
import PaymentDetails from 'components/PaymentDetails';
import paths from 'consts/routes';
import { changeLanguage } from 'data/actions/currentLanguage';
import { setPathToRedirectAfterError } from 'data/actions/errors';
import { fetchInitInfoForEnteredLogin } from 'data/actions/initInfo';
import { IInitInfoState } from 'data/reducers/initInfo';
import { IStoreState } from 'data/rootReducer';
import AuthenticationCancelPopUp from 'screens/AuthenticationCancelPopUp/AuthenticationCancelPopUp';
import { containsNotDigits, CurrentLanguage, getDefaultLanguage } from 'utils/helpers';

export interface IOwnProps extends RouteComponentProps<any>, WithTranslation {}

export interface IStateProps {
    initInfo: IInitInfoState;
}

export interface IDispatchProps {
    dispatchChangeLanguage: (language: CurrentLanguage, didUserSelect?: boolean) => void;
}

type Props = IDispatchProps & IStateProps & IOwnProps & IWithResponsiveWrapperProps;

export interface IState {
    internetBankLogin: string;
    internetBankLoginError?: string;
    isCancelPopupOpen: boolean;
}

class InternetBankLogin extends React.Component<Props & RouteComponentProps, IState> {
    constructor(props) {
        super(props);
        this.state = {
            internetBankLogin: '',
            internetBankLoginError: null,
            isCancelPopupOpen: false,
        };
    }

    componentDidMount = () => {
        const { dispatchSetPathToRedirectAfterError } = this.props;

        dispatchSetPathToRedirectAfterError(paths.INTERNET_BANK_LOGIN);
    };

    onContinue = () => {
        const {
            history,
            dispatchFetchInitInfoForEnteredLogin,
            dispatchChangeLanguage,
            currentLanguageState,
            dispatchSetPathToRedirectAfterError,
        } = this.props;
        const { internetBankLogin, internetBankLoginError } = this.state;

        if (internetBankLoginError) {
            return;
        }

        dispatchFetchInitInfoForEnteredLogin(internetBankLogin).then((data) => {
            if (data.availableAuthMethods) {
                if (!currentLanguageState.didUserSelect) {
                    const userLanguage = data.userLanguage ? data.userLanguage.toLowerCase() : getDefaultLanguage();

                    i18next.changeLanguage(userLanguage);
                    dispatchChangeLanguage(userLanguage);
                }

                dispatchSetPathToRedirectAfterError(paths.CHOOSE_AUTH_METHOD);
                history.push(paths.CHOOSE_AUTH_METHOD);
            }
        });
    };

    handleKeypress = (e: React.KeyboardEvent) => {
        if (e.key.toLowerCase() === 'enter') {
            this.onContinue();
        }
    };

    onInternetBankLoginChange = (event) => {
        this.setState({
            internetBankLogin: event.target.value,
            internetBankLoginError: this.checkForUnexpectedCharacter(event.target.value),
        });
    };

    checkForUnexpectedCharacter = (value): string | null => {
        const { t } = this.props;

        return containsNotDigits(value) ? t('screens.internetBankLogin.unexpectedCharacterError') : null;
    };

    closeCancelPopUp = () => {
        this.setState({ isCancelPopupOpen: false });
    };

    openCancelPopUp = () => {
        this.setState({ isCancelPopupOpen: true });
    };

    handleUserChangedLanguage = (language) => {
        const { dispatchChangeLanguage } = this.props;

        i18next.changeLanguage(language.value);
        dispatchChangeLanguage(language.value, true);
        this.setState(prevState => ({
            internetBankLoginError: this.checkForUnexpectedCharacter(prevState.internetBankLoginError)
        }))
    };

    render() {
        const { internetBankLogin, internetBankLoginError, isCancelPopupOpen } = this.state;
        const { t, initInfo, responsiveData, translations } = this.props;
        const { breakpointMobile } = responsiveData;
        const { purchaseAmount, acctNumber, purchaseCurrency, merchantName } = initInfo.data;

        const errorMessage =
            initInfo.error && initInfo.error.errorCode ? t(`errors.${initInfo.error.errorCode}.title`) : null;

        return (
            <>
                <Page
                    merchantName={merchantName}
                    breakpointMobile={breakpointMobile}
                    onChangeLanguage={this.handleUserChangedLanguage}
                    languagesToShow={Object.keys(translations.data) as CurrentLanguage[]}
                >
                    <FocusviewContentCentered>
                        <div className="focusview-content-centered-middle">
                            <PaymentDetails
                                amount={purchaseAmount}
                                currency={purchaseCurrency}
                                paymentCard={acctNumber}
                                title={t('screens.choose.title')}
                                description={t('screens.choose.description')}
                            />
                            <p className="main-description spaced">
                                {t('screens.internetBankLogin.enterInternetBankLogin')}
                            </p>
                            <FormRow
                                label={t('screens.internetBankLogin.loginCodeInputLabel')}
                                size="medium"
                                error={internetBankLoginError || errorMessage}
                            >
                                <ControlInput
                                    errorAnimated
                                    hasError={!!internetBankLoginError || !!errorMessage}
                                    value={internetBankLogin}
                                    autoFocus
                                    onChange={this.onInternetBankLoginChange}
                                    onKeyDown={this.handleKeypress}
                                    maxLength={50}
                                    type="tel"
                                    secureTextEntry
                                />
                            </FormRow>
                        </div>
                        <div className="focusview-content-centered-bottom">
                            <Buttons layout="vertical">
                                <Button
                                    title={t('screens.internetBankLogin.buttons.continue')}
                                    onClick={this.onContinue}
                                    disabled={!!internetBankLoginError || !internetBankLogin}
                                />
                                <Button
                                    title={t('screens.internetBankLogin.buttons.cancel')}
                                    onClick={this.openCancelPopUp}
                                    layout="link"
                                    intent="discrete"
                                    size="small"
                                />
                            </Buttons>
                        </div>
                    </FocusviewContentCentered>
                </Page>
                <AuthenticationCancelPopUp isPopupOpen={isCancelPopupOpen} closeCancelPopUp={this.closeCancelPopUp} />
            </>
        );
    }
}

const mapStateToProps = (state: IStoreState) => {
    return {
        initInfo: state.initInfo,
        currentLanguageState: state.currentLanguageState,
        translations: state.translations,
    };
};

const mapDispatchToProps = (dispatch) => ({
    dispatchFetchInitInfoForEnteredLogin: (login: string) => dispatch(fetchInitInfoForEnteredLogin(login)),
    dispatchChangeLanguage: (language: CurrentLanguage, didUserSelect?: boolean) =>
        dispatch(changeLanguage(language, didUserSelect)),
    dispatchSetPathToRedirectAfterError: (payload: string) => dispatch(setPathToRedirectAfterError(payload)),
});

export default compose<any>(
    connect(mapStateToProps, mapDispatchToProps),
    (component) => withResponsiveWrapper(component, ['breakpointMobile']),
    withTranslation(),
)(InternetBankLogin);
