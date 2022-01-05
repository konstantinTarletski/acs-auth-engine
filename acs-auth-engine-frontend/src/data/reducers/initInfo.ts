import { IInitInfo } from 'data/api/initInfo';

import * as types from '../types';

export interface IInitInfoState {
    data: IInitInfo;
    isFetching: boolean;
    isFetched: boolean;
}

const initialState = {
    data: {},
    isFetching: false,
    isFetched: false,
};

const initInfoReducer = (state: IInitInfoState = initialState, action: any) => {
    switch (action.type) {
        case types.GET_INIT_INFO_FOR_ENTERED_LOGIN.REQUEST:
        case types.GET_INIT_INFO.REQUEST: {
            return { ...state, isFetching: true, isFetched: false, error: null };
        }

        case types.GET_INIT_INFO_FOR_ENTERED_LOGIN.SUCCESS:
        case types.GET_INIT_INFO.SUCCESS: {
            return {
                ...state,
                isFetching: false,
                isFetched: true,
                data: { ...state.data, ...action.payload },
                error: null,
            };
        }

        case types.GET_INIT_INFO_FOR_ENTERED_LOGIN.FAILURE:
        case types.GET_INIT_INFO.FAILURE: {
            return { ...state, isFetching: false, isFetched: true, error: action.error };
        }

        default: {
            return state;
        }
    }
};

export default initInfoReducer;
