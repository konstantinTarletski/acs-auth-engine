import * as React from 'react';
import { WithTranslation, withTranslation } from 'react-i18next';
import { connect } from 'react-redux';
import { RouteComponentProps, withRouter } from 'react-router-dom';
import { compose } from 'redux';

import { WRONG_APPLICATION_STATE_ERROR_CODE } from 'consts/common';
import paths from 'consts/routes';
import { clearError, ErrorType } from 'data/actions/errors';
import { IErrorsState } from 'data/reducers/errors';
import { IStoreState } from 'data/rootReducer';

export interface IOwnProps extends WithTranslation {}

export interface IStateProps {
    errors: IErrorsState;
}

export interface IDispatchProps {}

type Props = IDispatchProps & IStateProps & IOwnProps;

export interface IState {}

class ErrorHandler extends React.Component<Props & RouteComponentProps, IState> {
    componentDidUpdate = () => {
        const { errors, history, location, dispatchClearError } = this.props;
        const { errorType, errorCode } = errors.currentError;

        if (WRONG_APPLICATION_STATE_ERROR_CODE === errorCode) {
            if (location.pathname !== paths.MAIN) {
                dispatchClearError();
                history.push(paths.MAIN);
            }
        } else if (location.pathname !== paths.FAILURE && errorType === ErrorType.FATAL) {
            history.push(paths.FAILURE);
        } else if (location.pathname !== paths.AUTHENTICATION_FAILURE && errorType === ErrorType.NON_FATAL) {
            history.push(paths.AUTHENTICATION_FAILURE);
        }
    };

    render() {
        const { children } = this.props;

        return children;
    }
}

const mapStateToProps = (state: IStoreState) => {
    return {
        errors: state.errors,
    };
};

const mapDispatchToProps = (dispatch) => ({
    dispatchClearError: () => dispatch(clearError()),
});

export default compose<any>(connect(mapStateToProps, mapDispatchToProps), withRouter, withTranslation())(ErrorHandler);
