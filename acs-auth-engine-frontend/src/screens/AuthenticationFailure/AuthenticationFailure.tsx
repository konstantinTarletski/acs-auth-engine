import i18next from 'i18next';
import FocusviewContentCentered from 'luminor-components/lib/Applications/Web/Components/FocusviewContentCentered';
import Button from 'luminor-components/lib/Components/Button';
import Buttons from 'luminor-components/lib/Components/Buttons';
import GeneralError from 'luminor-components/lib/Components/GeneralError';
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
import { GENERAL_EXCEPTION_ERROR_CODE, TRANSLATED_ERROR_ERROR_CODE } from 'consts/common';
import { changeLanguage } from 'data/actions/currentLanguage';
import { clearError } from 'data/actions/errors';
import { IInitInfoState } from 'data/reducers/initInfo';
import { ITranslationsState } from 'data/reducers/translations';
import { IStoreState } from 'data/rootReducer';
import AuthenticationCancelPopUp from 'screens/AuthenticationCancelPopUp';
import { CurrentLanguage } from 'utils/helpers';

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
    isCancelPopupOpen: boolean;
}

class AuthenticationFailure extends React.Component<Props & RouteComponentProps, IState> {
    constructor(props) {
        super(props);
        this.state = {
            isCancelPopupOpen: false,
        };
    }

    componentDidMount = () => {};

    handleUserChangedLanguage = (language) => {
        const { dispatchChangeLanguage } = this.props;

        i18next.changeLanguage(language.value);
        dispatchChangeLanguage(language.value, true);
    };

    onTryAgain = () => {
        const { history, dispatchClearError, errors } = this.props;

        dispatchClearError();
        history.push(errors.pathToRedirectAfterError);
    };

    closeCancelPopUp = () => {
        this.setState({ isCancelPopupOpen: false });
    };

    openCancelPopUp = () => {
        this.setState({ isCancelPopupOpen: true });
    };

    render() {
        const { isCancelPopupOpen } = this.state;
        const { t, responsiveData, errors, initInfo, translations } = this.props;
        const { merchantName } = initInfo.data;
        const { breakpointMobile } = responsiveData;

        const errorCode = errors.currentError.errorCode || GENERAL_EXCEPTION_ERROR_CODE;
        const errorTitle = t(`errors.${errorCode}.title`);
        const errorDescription =
            errorCode === TRANSLATED_ERROR_ERROR_CODE
                ? errors.currentError.errorTranslation
                : t(`errors.${errorCode}.description`);

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
                            <GeneralError title={errorTitle} description={errorDescription} />
                        </div>
                        <div className="focusview-content-centered-bottom">
                            <Buttons layout="vertical">
                                <Button title={t('screens.authFailure.buttons.tryAgain')} onClick={this.onTryAgain} />
                                <Button
                                    title={t('screens.authFailure.buttons.cancel')}
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
        errors: state.errors,
        initInfo: state.initInfo,
        translations: state.translations,
    };
};

const mapDispatchToProps = (dispatch) => ({
    dispatchClearError: () => dispatch(clearError()),
    dispatchChangeLanguage: (language: CurrentLanguage, didUserSelect?: boolean) =>
        dispatch(changeLanguage(language, didUserSelect)),
});

export default compose<any>(
    connect(mapStateToProps, mapDispatchToProps),
    (component) => withResponsiveWrapper(component, ['breakpointMobile']),
    withTranslation(),
)(AuthenticationFailure);
