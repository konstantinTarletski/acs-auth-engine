import i18next from 'i18next';
import FocusviewContentCentered from 'luminor-components/lib/Applications/Web/Components/FocusviewContentCentered';
import Button from 'luminor-components/lib/Components/Button';
import Buttons from 'luminor-components/lib/Components/Buttons';
import ControlInput from 'luminor-components/lib/Components/ControlInput';
import FormRow from 'luminor-components/lib/Components/FormRow';
import Icon from 'luminor-components/lib/Components/Icon/index';
import LoadingGateway from 'luminor-components/lib/Components/LoadingGateway';
import {
    IWithResponsiveWrapperProps,
    withResponsiveWrapper,
} from 'luminor-components/lib/Hocs/ResponsiveWrapper/index';
import * as React from 'react';
import { WithTranslation, withTranslation } from 'react-i18next';
import { connect } from 'react-redux';
import { RouteComponentProps } from 'react-router-dom';
import { compose } from 'redux';

import Page from 'components/Page';
import { REQUEST_CANCELED_CODE } from 'consts/common';
import paths from 'consts/routes';
import { clearAuthenticationData, initAuthentication } from 'data/actions/authentication';
import { changeLanguage } from 'data/actions/currentLanguage';
import { ITranslationsState } from 'data/reducers/translations';
import { IStoreState } from 'data/rootReducer';
import { containsNotDigits, CurrentLanguage, isEmptyObject } from 'utils/helpers';

export interface IOwnProps extends WithTranslation {}

export interface IStateProps {
    translations: ITranslationsState;
}

export interface IDispatchProps {
    dispatchChangeLanguage: (language: CurrentLanguage, didUserSelect?: boolean) => void;
}

type Props = IDispatchProps & IStateProps & IOwnProps & IWithResponsiveWrapperProps;

export interface IState {
    confirmationCode: string;
    confirmationCodeError?: string;
}

class AuthenticationEnterCode extends React.Component<Props & RouteComponentProps, IState> {
    constructor(props) {
        super(props);
        this.state = {
            confirmationCode: '',
            confirmationCodeError: null,
        };
    }

    componentWillUnmount = () => {
        this.clear();
    };

    handleUserChangedLanguage = (language) => {
        const { dispatchChangeLanguage } = this.props;

        i18next.changeLanguage(language.value);
        dispatchChangeLanguage(language.value, true);
        this.setState(prevState => ({
            confirmationCodeError: this.checkForUnexpectedCharacter(prevState.confirmationCodeError)
        }))
    };

    clear = () => {
        const { dispatchClearAuthenticationData } = this.props;

        dispatchClearAuthenticationData();
    };

    handleKeypress = (e: React.KeyboardEvent) => {
        if (e.key.toLowerCase() === 'enter') {
            this.onContinue();
        }
    };

    onResponseCodeChange = (event) => {
        this.setState({
            confirmationCode: event.target.value,
            confirmationCodeError: this.checkForUnexpectedCharacter(event.target.value),
        });
    };

    checkForUnexpectedCharacter = (value): string | null => {
        const { t } = this.props;

        return containsNotDigits(value) ? t('screens.authEnterCode.unexpectedCharacterError') : null;
    };

    onContinue = () => {
        const { dispatchInitAuthentication, history } = this.props;
        const { confirmationCode, confirmationCodeError } = this.state;

        if (confirmationCodeError) {
            return;
        }

        dispatchInitAuthentication(confirmationCode).then((data) => {
            if (data.message === REQUEST_CANCELED_CODE) {
                return;
            }

            if (isEmptyObject(data)) {
                history.push(paths.AUTHENTICATION_SUCCESS);
            }
        });
    };

    handleBack = () => {
        const { history } = this.props;

        history.push(paths.CHOOSE_AUTH_METHOD);
    };

    render() {
        const { t, initInfo, authentication, responsiveData, translations } = this.props;
        const { merchantName } = initInfo.data;
        const { confirmationCode, confirmationCodeError } = this.state;
        const { breakpointMobile } = responsiveData;

        const errorMessage =
            authentication.error && authentication.error.errorCode
                ? t(`errors.${authentication.error.errorCode}.title`)
                : null;

        return (
            <Page
                merchantName={merchantName}
                breakpointMobile={breakpointMobile}
                onChangeLanguage={this.handleUserChangedLanguage}
                languagesToShow={Object.keys(translations.data) as CurrentLanguage[]}
            >
                {authentication.isFetching && <LoadingGateway gateway="visa" />}
                <FocusviewContentCentered>
                    <div className="focusview-content-centered-middle">
                        <div className="code-display">
                            <Icon kind="lock" width={90} height={90} wrapperClassName="icon" />
                            <p className="main-description">{t('screens.authEnterCode.enterResponseCode')}</p>
                            <FormRow
                                label={t('screens.authEnterCode.confirmationCodeLabel')}
                                size="medium"
                                error={confirmationCodeError || errorMessage}
                            >
                                <ControlInput
                                    errorAnimated
                                    hasError={!!confirmationCodeError || !!errorMessage}
                                    value={confirmationCode}
                                    autoFocus
                                    onChange={this.onResponseCodeChange}
                                    onKeyDown={this.handleKeypress}
                                    maxLength={15}
                                    type="tel"
                                    secureTextEntry
                                />
                            </FormRow>
                        </div>
                    </div>
                    <div className="focusview-content-centered-bottom">
                        <Buttons layout="vertical">
                            <Button
                                title={t('screens.authEnterCode.buttons.continue')}
                                onClick={this.onContinue}
                                disabled={!!confirmationCodeError || !confirmationCode}
                            />
                            <Button
                                title={t('screens.authEnterCode.buttons.back')}
                                onClick={this.handleBack}
                                layout="link"
                                intent="discrete"
                                size="small"
                            />
                        </Buttons>
                    </div>
                </FocusviewContentCentered>
            </Page>
        );
    }
}

const mapStateToProps = (state: IStoreState) => {
    return {
        initInfo: state.initInfo,
        authentication: state.authentication,
        translations: state.translations,
    };
};

const mapDispatchToProps = (dispatch) => ({
    dispatchInitAuthentication: (confirmationCode: string) => dispatch(initAuthentication({ confirmationCode })),
    dispatchClearAuthenticationData: () => dispatch(clearAuthenticationData()),
    dispatchChangeLanguage: (language: CurrentLanguage, didUserSelect?: boolean) =>
        dispatch(changeLanguage(language, didUserSelect)),
});

export default compose<any>(
    connect(mapStateToProps, mapDispatchToProps),
    (component) => withResponsiveWrapper(component, ['breakpointMobile']),
    withTranslation(),
)(AuthenticationEnterCode);
