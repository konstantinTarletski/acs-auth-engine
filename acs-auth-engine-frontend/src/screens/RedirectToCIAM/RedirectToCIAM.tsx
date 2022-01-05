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
import { ACS_TRANSACTION_ID, CREQ } from 'consts/common';
import { ITranslationsState } from 'data/reducers/translations';
import { IStoreState } from 'data/rootReducer';

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

class RedirectToCIAM extends React.Component<Props & RouteComponentProps, IState> {
    componentDidUpdate() {
        const { authentication } = this.props;
        const { redirectUrl } = authentication.data;

        if (redirectUrl) {
            const cookies = new Cookies();

            cookies.set('creq', CREQ, { path: '/' });
            cookies.set('acs-transaction-id', ACS_TRANSACTION_ID, { path: '/' });
            window.location = redirectUrl;
        }
    }

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

const mapStateToProps = (state: IStoreState) => {
    return {
        initInfo: state.initInfo,
        authentication: state.authentication,
    };
};

export default compose<any>(
    (component) => withResponsiveWrapper(component, ['breakpointMobile']),
    connect(mapStateToProps, null),
)(RedirectToCIAM);
