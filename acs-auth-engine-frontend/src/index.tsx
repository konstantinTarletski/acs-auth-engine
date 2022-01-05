import i18next from 'i18next';
import * as React from 'react';
import { render } from 'react-dom';
import { I18nextProvider } from 'react-i18next';
import { Provider } from 'react-redux';

import configureStore from 'data/reduxStore';

import App from './App';

import 'common/i18next';

// tslint:disable-next-line:no-var-requires
require('./assets/icons/favicon.ico');

const rootEl = document.getElementById('root');

document.documentElement.classList.remove('is-loading');

render(
    <Provider store={configureStore()}>
        <I18nextProvider i18n={i18next}>
            <App />
        </I18nextProvider>
    </Provider>,
    rootEl,
);
