import i18next from 'i18next';
import * as AnimLockSnap from 'luminor-components/assets/animations/lock_snap.json';
import FocusviewContentCentered from 'luminor-components/lib/Applications/Web/Components/FocusviewContentCentered';
import Button from 'luminor-components/lib/Components/Button';
import Buttons from 'luminor-components/lib/Components/Buttons';
import LottieAnimation from 'luminor-components/lib/Components/LottieAnimation';
import NonIdealStateLarge from 'luminor-components/lib/Components/NonIdealStateLarge';
import {
    IWithResponsiveWrapperProps,
    withResponsiveWrapper,
} from 'luminor-components/lib/Hocs/ResponsiveWrapper/index';
import * as React from 'react';
import { WithTranslation, withTranslation } from 'react-i18next';
import { connect } from 'react-redux';
import { compose } from 'redux';

import BackToMerchantForm from 'components/BackToMerchantForm';
import Page from 'components/Page';
import { GENERAL_EXCEPTION_ERROR_CODE } from 'consts/common';
import { changeLanguage } from 'data/actions/currentLanguage';
import UiActions from 'data/api/uiActions';
import { IErrorsState } from 'data/reducers/errors';
import { IInitInfoState } from 'data/reducers/initInfo';
import { IStoreState } from 'data/rootReducer';
import { CurrentLanguage } from 'utils/helpers';

export interface IOwnProps extends WithTranslation {}

export interface IStateProps {
    initInfo: IInitInfoState;
    errors: IErrorsState;
    translations: any;
}

export interface IDispatchProps {
    dispatchChangeLanguage: (language: CurrentLanguage, didUserSelect?: boolean) => void;
}

type Props = IDispatchProps & IStateProps & IOwnProps & IWithResponsiveWrapperProps;

export interface IState {}

class Failure extends React.Component<Props, IState> {
    private submitBackToMerchantButtonId = 'failure-back-to-merchant';

    constructor(props) {
        super(props);
        this.state = {};
    }

    componentDidMount = () => {};

    handleUserChangedLanguage = (language) => {
        const { dispatchChangeLanguage } = this.props;

        i18next.changeLanguage(language.value);
        dispatchChangeLanguage(language.value, true);
    };

    handleBackToMerchant = () => {
        document.getElementById(this.submitBackToMerchantButtonId).click();
    };

    render() {
        const { t, responsiveData, initInfo, errors, translations } = this.props;
        const { merchantName } = initInfo.data;
        const { breakpointMobile } = responsiveData;
        const lottieAnimation = (
            <LottieAnimation customSize={200} source={AnimLockSnap} loop={false} hasConfetti={false} delay={50} />
        );
        const errorCode = errors.currentError.errorCode || GENERAL_EXCEPTION_ERROR_CODE;
        const errorTitle = t(`errors.${errorCode}.title`);
        const errorDescription = t(`errors.${errorCode}.description`);

        return (
            <Page
                merchantName={merchantName}
                onChangeLanguage={this.handleUserChangedLanguage}
                breakpointMobile={breakpointMobile}
                languagesToShow={Object.keys(translations.data) as CurrentLanguage[]}
            >
                <FocusviewContentCentered>
                    <div className="focusview-content-centered-middle">
                        <NonIdealStateLarge
                            title={errorTitle}
                            description={errorDescription}
                            hideConfetti
                            hideShadows
                            iconElement={lottieAnimation}
                        />
                    </div>
                    <div className="focusview-content-centered-bottom">
                        <Buttons layout="vertical">
                            <Button
                                title={t('screens.failure.backToMerchantButton')}
                                onClick={this.handleBackToMerchant}
                            />
                        </Buttons>
                    </div>
                    <BackToMerchantForm
                        submitButtonId={this.submitBackToMerchantButtonId}
                        uiAction={UiActions.BACK_TO_MERCHANT_CANCEL}
                    />
                </FocusviewContentCentered>
            </Page>
        );
    }
}

const mapStateToProps = (state: IStoreState) => {
    return {
        initInfo: state.initInfo,
        errors: state.errors,
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
)(Failure);
