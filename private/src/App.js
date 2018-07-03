import React, { Component } from 'react';
import 'App.css';
import MainTemplate from "containers/MainTemplate";
import { Provider } from 'react-redux'
import configureStore from "redux/store/configureStore";
import DevTools from 'containers/DevTools'
import T from 'i18n-react';

const initial_state = window.SERVER_DATA;
initial_state.is_prod = process.env.NODE_ENV === 'production';

const store = configureStore(initial_state);

let lang = initial_state.lang ? initial_state.lang : "en";
let langTranslations = require(`conf/translations/translations_${lang}.json`);
T.setTexts(langTranslations);

class App extends Component {

  render() {
    return (
        <Provider store= { store }>
          <div>
            <MainTemplate />
            {initial_state.is_prod ? "" : <DevTools />}
          </div>
        </Provider>
    );
  }
}

export default App;
