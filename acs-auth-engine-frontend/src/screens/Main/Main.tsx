import i18next from 'i18next';
import LoadingApp from 'luminor-components/lib/Components/LoadingApp';
import * as React from 'react';
import { connect } from 'react-redux';
import { Redirect, RouteComponentProps } from 'react-router-dom';
import { compose } from 'redux';

import paths from 'consts/routes';
import { changeLanguage } from 'data/actions/currentLanguage';
import { fetchInitInfo } from 'data/actions/initInfo';
import { State } from 'data/api/initInfo';
import { IInitInfoState } from 'data/reducers/initInfo';
import { IStoreState } from 'data/rootReducer';
import { getDefaultLanguage, CurrentLanguage } from 'utils/helpers';

export interface IOwnProps {}

export interface IStateProps {
    initInfo: IInitInfoState;
}

export interface IDispatchProps {
    dispatchFetchInitInfo: () => void;
    dispatchChangeLanguage: (language: CurrentLanguage) => void;
}

type Props = IDispatchProps & IStateProps & IOwnProps;

export interface IMainState {}

// TODO: map all states from back end with Konstantin
const stateToPath = {
    [State.RENDER_SELECT_AUTH_METHOD_PAGE]: paths.CHOOSE_AUTH_METHOD,
    [State.RENDER_ERROR_PAGE]: paths.FAILURE,
    [State.RENDER_ENTER_LOGIN_PAGE]: paths.INTERNET_BANK_LOGIN,
    [State.REDIRECTED_TO_EXTERNAL_SYSTEM]: paths.REDIRECT_FROM_CIAM,
};

class Main extends React.Component<Props & RouteComponentProps, IMainState> {
    componentDidMount = () => {
        const { dispatchFetchInitInfo, dispatchChangeLanguage } = this.props;

        dispatchFetchInitInfo().then((data) => {
            if (data.country) {
                const userLanguage = data.userLanguage ? data.userLanguage.toLowerCase() : getDefaultLanguage();

                i18next.changeLanguage(userLanguage);
                dispatchChangeLanguage(userLanguage);
            }
        });
    };

    render() {
        const { initInfo, currentLanguageState } = this.props;

        if (initInfo.isFetching || !initInfo.isFetched || !currentLanguageState.currentLanguage) {
            return <LoadingApp />;
        }

        if (initInfo.data.state) {
            return <Redirect to={stateToPath[initInfo.data.state]} />;
        }

        return <Redirect to={paths.FAILURE} />;
    }
}

const mapStateToProps = (state: IStoreState) => {
    return {
        initInfo: state.initInfo,
        currentLanguageState: state.currentLanguageState,
    };
};

const mapDispatchToProps = (dispatch) => ({
    dispatchFetchInitInfo: () => dispatch(fetchInitInfo()),
    dispatchChangeLanguage: (language: CurrentLanguage) => dispatch(changeLanguage(language)),
});

export default compose<any>(connect(mapStateToProps, mapDispatchToProps))(Main);
