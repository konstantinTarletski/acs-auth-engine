import i18next from 'i18next';
import FocusviewContentCentered from 'luminor-components/lib/Applications/Web/Components/FocusviewContentCentered';
import Button from 'luminor-components/lib/Components/Button';
import Buttons from 'luminor-components/lib/Components/Buttons';
import CodeDisplay from 'luminor-components/lib/Components/CodeDisplay';
import {
    IWithResponsiveWrapperProps,
    withResponsiveWrapper,
} from 'luminor-components/lib/Hocs/ResponsiveWrapper/index';
import * as React from 'react';
import { WithTranslation, withTranslation } from 'react-i18next';
import { connect } from 'react-redux';
import { RouteComponentProps, Redirect } from 'react-router-dom';
import { compose } from 'redux';

import Page from 'components/Page';
import paths from 'consts/routes';
import { clearAuthenticationData, fetchAuthStatus } from 'data/actions/authentication';
import { changeLanguage } from 'data/actions/currentLanguage';
import { ITranslationsState } from 'data/reducers/translations';
import { IStoreState } from 'data/rootReducer';
import { CurrentLanguage } from 'utils/helpers';

export interface IOwnProps extends WithTranslation {}

export interface IStateProps {
    translations: ITranslationsState;
}

export interface IDispatchProps {
    dispatchChangeLanguage: (language: CurrentLanguage, didUserSelect?: boolean) => void;
}

type Props = IDispatchProps & IStateProps & IOwnProps & IWithResponsiveWrapperProps;

export interface IState {}

class Authentication extends React.Component<Props & RouteComponentProps, IState> {
    private statusPollingInterval;

    constructor(props) {
        super(props);
        this.state = {};
    }

    componentDidMount = () => {
        const { authentication } = this.props;
        const { authorizationCode } = authentication.data;

        if (!this.statusPollingInterval && !!authorizationCode) {
            this.startPolling();
        }
    };

    componentDidUpdate() {
        const { authentication, history } = this.props;
        const { isFetching, data } = authentication;
        const { authorizationCode, confirmationSuccessful } = data;

        if (!this.statusPollingInterval && confirmationSuccessful === undefined && !!authorizationCode && !isFetching) {
            this.startPolling();
        }

        if (confirmationSuccessful) {
            history.push(paths.AUTHENTICATION_SUCCESS);
        }
    }

    componentWillUnmount = () => {
        this.clear();
    };

    handleUserChangedLanguage = (language) => {
        const { dispatchChangeLanguage } = this.props;

        i18next.changeLanguage(language.value);
        dispatchChangeLanguage(language.value, true);
    };

    clear = () => {
        const { dispatchClearAuthenticationData } = this.props;

        clearInterval(this.statusPollingInterval);
        this.statusPollingInterval = null;
        dispatchClearAuthenticationData();
    };

    startPolling = () => {
        this.fetchAuthStatusIfNeeded();
        this.statusPollingInterval = setInterval(() => {
            this.fetchAuthStatusIfNeeded();
        }, 3000);
    };

    fetchAuthStatusIfNeeded = () => {
        const {
            authentication: { isFetching },
            dispatchFetchAuthStatus,
        } = this.props;

        if (!isFetching) {
            dispatchFetchAuthStatus();
        }
    };

    handleBack = () => {
        const { history } = this.props;

        history.push(paths.CHOOSE_AUTH_METHOD);
    };

    render() {
        const { t, initInfo, authentication, responsiveData, translations } = this.props;
        const { merchantName } = initInfo.data;
        const { authorizationCode, authMethod } = authentication.data;
        const { breakpointMobile } = responsiveData;

        if (!authorizationCode && !authentication.isFetching) {
            return <Redirect to={paths.CHOOSE_AUTH_METHOD} />;
        }

        return (
            <Page
                merchantName={merchantName}
                breakpointMobile={breakpointMobile}
                onChangeLanguage={this.handleUserChangedLanguage}
                languagesToShow={Object.keys(translations.data) as CurrentLanguage[]}
            >
                <FocusviewContentCentered>
                    <div className="focusview-content-centered-middle">
                        <CodeDisplay
                            code={authorizationCode}
                            icon="lock"
                            title={t('screens.auth.code.title')}
                            description={t(`screens.auth.code.description.${authMethod}`)}
                        />
                    </div>
                    <div className="focusview-content-centered-bottom">
                        <Buttons layout="vertical">
                            <Button
                                title={t('screens.auth.backButton')}
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
    dispatchFetchAuthStatus: () => dispatch(fetchAuthStatus()),
    dispatchClearAuthenticationData: () => dispatch(clearAuthenticationData()),
    dispatchChangeLanguage: (language: CurrentLanguage, didUserSelect?: boolean) =>
        dispatch(changeLanguage(language, didUserSelect)),
});

export default compose<any>(
    connect(mapStateToProps, mapDispatchToProps),
    (component) => withResponsiveWrapper(component, ['breakpointMobile']),
    withTranslation(),
)(Authentication);
