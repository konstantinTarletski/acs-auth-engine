import i18next from 'i18next';
import FocusviewContentCentered from 'luminor-components/lib/Applications/Web/Components/FocusviewContentCentered';
import AuthMethods, { IAuthMethod } from 'luminor-components/lib/Components/AuthMethods';
import Button from 'luminor-components/lib/Components/Button';
import Buttons from 'luminor-components/lib/Components/Buttons';
import {
    IWithResponsiveWrapperProps,
    withResponsiveWrapper,
} from 'luminor-components/lib/Hocs/ResponsiveWrapper/index';
import * as React from 'react';
import { isMobile } from 'react-device-detect';
import { withTranslation, WithTranslation } from 'react-i18next';
import { connect } from 'react-redux';
import { RouteComponentProps, Redirect } from 'react-router-dom';
import { compose } from 'redux';

import Page from 'components/Page';
import PaymentDetails from 'components/PaymentDetails';
import paths from 'consts/routes';
import { chooseAuthenticationMethod, initAuthentication } from 'data/actions/authentication';
import { changeLanguage } from 'data/actions/currentLanguage';
import { AuthMethod } from 'data/api/authentication';
import { IInitInfoState } from 'data/reducers/initInfo';
import { ITranslationsState } from 'data/reducers/translations';
import { IStoreState } from 'data/rootReducer';
import AuthenticationCancelPopUp from 'screens/AuthenticationCancelPopUp/AuthenticationCancelPopUp';
import { CurrentLanguage, inIframe } from 'utils/helpers';

export interface IOwnProps extends RouteComponentProps<any>, WithTranslation {}

export interface IStateProps {
    initInfo: IInitInfoState;
    translations: ITranslationsState;
}

export interface IDispatchProps {
    dispatchChangeLanguage: (language: CurrentLanguage, didUserSelect?: boolean) => void;
}

type Props = IDispatchProps & IStateProps & IOwnProps & IWithResponsiveWrapperProps;

export interface IState {
    authMethod: AuthMethodFE;
    isCancelPopupOpen: boolean;
}

export enum AuthMethodFE {
    SMART_ID = 'smartId',
    MOBILE_ID = 'mobileId',
    CODE_CALCULATOR = 'codeCalculator',
    ID_CARD = 'idCard',
}

const AUTH_METHOD_FE_BE_MAP = {
    [AuthMethodFE.SMART_ID]: AuthMethod.SMART_ID,
    [AuthMethodFE.MOBILE_ID]: AuthMethod.M_SIGNATURE,
    [AuthMethodFE.CODE_CALCULATOR]: AuthMethod.CODE_CALCULATOR,
    [AuthMethodFE.ID_CARD]: AuthMethod.ID_CARD,
};

const AUTH_METHOD_BE_FE_MAP = {
    [AuthMethod.SMART_ID]: AuthMethodFE.SMART_ID,
    [AuthMethod.M_SIGNATURE]: AuthMethodFE.MOBILE_ID,
    [AuthMethod.CODE_CALCULATOR]: AuthMethodFE.CODE_CALCULATOR,
    [AuthMethod.ID_CARD]: AuthMethodFE.ID_CARD,
};

const resolveIdCardTooltip = () => {
    if (inIframe()) {
        return 'screens.choose.disabledIdCardTooltip.iframe';
    }

    if (isMobile) {
        return 'screens.choose.disabledIdCardTooltip.mobile';
    }

    return undefined;
};

const allAuthMethodsNotTranslated: IAuthMethod[] = [
    { type: 'smartId', label: 'screens.choose.authMethods.smartId' },
    { type: 'mobileId', label: 'screens.choose.authMethods.mobileId' },
    {
        type: 'idCard',
        label: 'screens.choose.authMethods.idCard',
        disabled: isMobile || inIframe(),
        tooltip: resolveIdCardTooltip(),
    },
    { type: 'codeCalculator', label: 'screens.choose.authMethods.codeCalculator' },
];

class ChooseAuthMethod extends React.Component<Props & RouteComponentProps, IState> {
    constructor(props) {
        super(props);
        this.state = {
            authMethod: null,
            isCancelPopupOpen: false,
        };
    }

    handleUserChangedLanguage = (language) => {
        const { dispatchChangeLanguage } = this.props;

        i18next.changeLanguage(language.value);
        dispatchChangeLanguage(language.value, true);
    };

    onContinue = () => {
        const { history, dispatchInitAuthentication, dispatchChooseAuthenticationMethod } = this.props;
        const { authMethod } = this.state;

        if (authMethod === AuthMethodFE.CODE_CALCULATOR) {
            dispatchChooseAuthenticationMethod(AUTH_METHOD_FE_BE_MAP[authMethod]);
            history.push(paths.AUTHENTICATION_ENTER_CODE);
        } else if (authMethod === AuthMethodFE.ID_CARD) {
            dispatchInitAuthentication(AUTH_METHOD_FE_BE_MAP[authMethod]);
            history.push(paths.REDIRECT_TO_CIAM);
        } else {
            dispatchInitAuthentication(AUTH_METHOD_FE_BE_MAP[authMethod]);
            history.push(paths.AUTHENTICATION);
        }
    };

    onSetAuthMethod = (authMethod) => {
        this.setState({
            authMethod,
        });
    };

    closeCancelPopUp = () => {
        this.setState({ isCancelPopupOpen: false });
    };

    openCancelPopUp = () => {
        this.setState({ isCancelPopupOpen: true });
    };

    render() {
        const { isCancelPopupOpen } = this.state;
        const { t, initInfo, responsiveData, translations } = this.props;
        const { breakpointMobile } = responsiveData;
        const {
            availableAuthMethods,
            defaultAuthMethod,
            purchaseAmount,
            acctNumber,
            purchaseCurrency,
            merchantName,
        } = initInfo.data;

        if (!availableAuthMethods || availableAuthMethods.length === 0) {
            return <Redirect to={paths.FAILURE} />;
        }

        const allAuthMethods = allAuthMethodsNotTranslated.map((notTranslatedAuthMethod) => ({
            type: notTranslatedAuthMethod.type,
            label: t(notTranslatedAuthMethod.label),
            disabled: notTranslatedAuthMethod.disabled,
            tooltip: t(notTranslatedAuthMethod.tooltip),
        }));

        const supportedMethods = allAuthMethods.filter(
            (authMethod) =>
                availableAuthMethods && availableAuthMethods.includes(AUTH_METHOD_FE_BE_MAP[authMethod.type]),
        );

        const shouldDisableIdCard = allAuthMethods.find(
            (authMethod) => AUTH_METHOD_FE_BE_MAP[authMethod.type] === AuthMethod.ID_CARD,
        ).disabled;

        const initialAuthMethod =
            defaultAuthMethod === AuthMethod.ID_CARD && shouldDisableIdCard
                ? null
                : AUTH_METHOD_BE_FE_MAP[defaultAuthMethod];

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
                            <AuthMethods
                                supportedMethods={supportedMethods}
                                setAuthMethod={this.onSetAuthMethod}
                                initialAuthMethod={initialAuthMethod}
                            />
                        </div>
                        <div className="focusview-content-centered-bottom">
                            <Buttons layout="vertical">
                                <Button title={t('screens.choose.buttons.continue')} onClick={this.onContinue} />
                                <Button
                                    title={t('screens.choose.buttons.cancel')}
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
        translations: state.translations,
    };
};

const mapDispatchToProps = (dispatch) => ({
    dispatchInitAuthentication: (authMethod: AuthMethod) => dispatch(initAuthentication({ authMethod })),
    dispatchChooseAuthenticationMethod: (authMethod: AuthMethod) => dispatch(chooseAuthenticationMethod(authMethod)),
    dispatchChangeLanguage: (language: CurrentLanguage, didUserSelect?: boolean) =>
        dispatch(changeLanguage(language, didUserSelect)),
});

export default compose<any>(
    connect(mapStateToProps, mapDispatchToProps),
    (component) => withResponsiveWrapper(component, ['breakpointMobile']),
    withTranslation(),
)(ChooseAuthMethod);
