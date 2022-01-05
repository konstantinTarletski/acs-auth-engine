import i18next from 'i18next';
import LoadingApp from 'luminor-components/lib/Components/LoadingApp/index';
import * as React from 'react';
import { hot } from 'react-hot-loader';
import { connect } from 'react-redux';
import { HashRouter as Router, Route, Switch } from 'react-router-dom';
import { compose } from 'redux';

import ErrorHandler from 'components/ErrorHandler';
import paths from 'consts/routes';
import { handleInitialErrorIfPresent } from 'data/actions/errors';
import fetchTranslations from 'data/actions/translations';
import { ITranslations } from 'data/api/translations';
import { ITranslationsState } from 'data/reducers/translations';
import { IStoreState } from 'data/rootReducer';
import Authentication from 'screens/Authentication';
import AuthenticationEnterCode from 'screens/AuthenticationEnterCode';
import AuthenticationFailure from 'screens/AuthenticationFailure';
import AuthenticationSuccess from 'screens/AuthenticationSuccess';
import ChooseAuthMethod from 'screens/ChooseAuthMethod/ChooseAuthMethod';
import Failure from 'screens/Failure';
import InternetBankLogin from 'screens/InternetBankLogin';
import Main from 'screens/Main';
import RedirectFromCIAM from 'screens/RedirectFromCIAM';
import RedirectToCIAM from 'screens/RedirectToCIAM';

import './assets/scss/index.scss';

export interface IOwnProps {}

export interface IStateProps {
    translations: ITranslationsState;
}

export interface IDispatchProps {
    dispatchFetchTranslations: () => Promise<any>;
    dispatchHandleInitialErrorIfPresent: () => void;
}

type Props = IDispatchProps & IStateProps & IOwnProps;

export interface IAppState {
    translationsLoaded: boolean;
}

class App extends React.Component<Props, IAppState> {
    constructor(props) {
        super(props);
        this.state = {
            translationsLoaded: false,
        };
    }

    componentDidMount = () => {
        const { dispatchFetchTranslations, dispatchHandleInitialErrorIfPresent } = this.props;

        dispatchFetchTranslations().then((translations) => {
            if (translations.en) {
                this.loadTranslations(translations);
            }

            dispatchHandleInitialErrorIfPresent();
        });
    };

    loadTranslations = (translationsData: ITranslations) => {
        const languages = Object.keys(translationsData);

        languages.forEach((language) => {
            i18next.addResourceBundle(language, 'common', translationsData[language]);
        });
        this.setState({
            translationsLoaded: true,
        });
    };

    render() {
        const { translationsLoaded } = this.state;

        if (!translationsLoaded) {
            return <LoadingApp />;
        }

        return (
            <Router>
                <ErrorHandler>
                    <Switch>
                        <Route exact path={paths.MAIN} component={Main} />
                        <Route exact path={paths.INTERNET_BANK_LOGIN} component={InternetBankLogin} />
                        <Route exact path={paths.CHOOSE_AUTH_METHOD} component={ChooseAuthMethod} />
                        <Route exact path={paths.AUTHENTICATION} component={Authentication} />
                        <Route exact path={paths.AUTHENTICATION_SUCCESS} component={AuthenticationSuccess} />
                        <Route exact path={paths.AUTHENTICATION_FAILURE} component={AuthenticationFailure} />
                        <Route exact path={paths.AUTHENTICATION_ENTER_CODE} component={AuthenticationEnterCode} />
                        <Route exact path={paths.FAILURE} component={Failure} />
                        <Route exact path={paths.REDIRECT_TO_CIAM} component={RedirectToCIAM} />
                        <Route exact path={paths.REDIRECT_FROM_CIAM} component={RedirectFromCIAM} />
                    </Switch>
                </ErrorHandler>
            </Router>
        );
    }
}

const mapStateToProps = (state: IStoreState) => {
    return {
        translations: state.translations,
    };
};

const mapDispatchToProps = (dispatch) => ({
    dispatchFetchTranslations: () => dispatch(fetchTranslations()),
    dispatchHandleInitialErrorIfPresent: () => dispatch(handleInitialErrorIfPresent()),
});

export default compose<any>(connect(mapStateToProps, mapDispatchToProps), hot(module))(App);
