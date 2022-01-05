import { applyMiddleware, createStore } from 'redux';
import { composeWithDevTools } from 'redux-devtools-extension';
import thunk from 'redux-thunk';

import rootReducer from 'data/rootReducer';

const composerEnhancer = composeWithDevTools({
    name: `Redux`,
    trace: true,
    traceLimit: 25,
});

export default function configureStore() {
    return createStore(rootReducer, composerEnhancer(applyMiddleware(thunk)));
}
