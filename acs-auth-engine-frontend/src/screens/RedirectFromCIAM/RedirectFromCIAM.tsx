import LoadingGateway from 'luminor-components/lib/Components/LoadingGateway';
import {
    IWithResponsiveWrapperProps,
    withResponsiveWrapper,
} from 'luminor-components/lib/Hocs/ResponsiveWrapper/index';
import * as React from 'react';
import { WithTranslation } from 'react-i18next';
import { connect } from 'react-redux';
import { RouteComponentProps } from 'react-router-dom';
import { compose } from 'redux';
import Cookies from 'universal-cookie';

import Page from 'components/Page';
import { AUTHORIZATION_CODE_FROM_CIAM, REQUEST_CANCELED_CODE } from 'consts/common';
import paths from 'consts/routes';
import { clearAuthenticationData, initAuthentication } from 'data/actions/authentication';
import { ITranslationsState } from 'data/reducers/translations';
import { IStoreState } from 'data/rootReducer';
import { isEmptyObject } from 'utils/helpers';

export interface IOwnProps extends WithTranslation {}

export interface IStateProps {
    translations: ITranslationsState;
}

export interface IDispatchProps {}

type Props = IDispatchProps & IStateProps & IOwnProps & IWithResponsiveWrapperProps;

export interface IState {
    confirmationCode: string;
    confirmationCodeError?: string;
}

class RedirectFromCIAM extends React.Component<Props & RouteComponentProps, IState> {
    componentDidMount() {
        const { dispatchInitAuthentication, history } = this.props;
        const cookies = new Cookies();

        cookies.remove('creq', { path: '/' });
        cookies.remove('acs-transaction-id', { path: '/' });

        if (AUTHORIZATION_CODE_FROM_CIAM) {
            dispatchInitAuthentication(AUTHORIZATION_CODE_FROM_CIAM).then((data) => {
                if (data.message === REQUEST_CANCELED_CODE) {
                    return;
                }

                if (isEmptyObject(data)) {
                    history.push(paths.AUTHENTICATION_SUCCESS);
                }
            });
        } else {
            history.push(paths.CHOOSE_AUTH_METHOD);
        }
    }

    componentWillUnmount = () => {
        this.clear();
    };

    clear = () => {
        const { dispatchClearAuthenticationData } = this.props;

        dispatchClearAuthenticationData();
    };

    render() {
        const { initInfo, responsiveData } = this.props;
        const { merchantName } = initInfo.data;
        const { breakpointMobile } = responsiveData;

        return (
            <Page merchantName={merchantName} breakpointMobile={breakpointMobile}>
                <LoadingGateway gateway="visa" />
            </Page>
        );
    }
}

const mapDispatchToProps = (dispatch) => ({
    dispatchInitAuthentication: (confirmationCode: string) => dispatch(initAuthentication({ confirmationCode })),
    dispatchClearAuthenticationData: () => dispatch(clearAuthenticationData()),
});

const mapStateToProps = (state: IStoreState) => {
    return {
        initInfo: state.initInfo,
        authentication: state.authentication,
    };
};

export default compose<any>(
    (component) => withResponsiveWrapper(component, ['breakpointMobile']),
    connect(mapStateToProps, mapDispatchToProps),
)(RedirectFromCIAM);
