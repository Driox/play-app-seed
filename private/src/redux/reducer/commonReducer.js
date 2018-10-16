import T from 'i18n-react';

export default function commonReducer(state = {}, action) {
  switch (action.type) {
    case "UPDATE_LNG_SUCCESS": 
      let new_lng = action.response.lng;
      let langTranslations = require(`conf/translations/translations_${new_lng}.json`);
      T.setTexts(langTranslations);
      
      return Object.assign({}, state, {
          lang : new_lng
        });
    default:
      return state;
  }
}
