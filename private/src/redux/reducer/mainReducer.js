import { combineReducers } from 'redux';
import { default as userReducer } from 'redux/reducer/userReducer';
import { default as errorReducer } from 'redux/reducer/errorReducer';
import { default as commonReducer } from 'redux/reducer/commonReducer';

const appReducer = combineReducers({
  errors: errorReducer,
  user: userReducer,
  lang: (state = {}) => state,
  is_prod: (state = {}) => state
});

const rootReducer = (state, action) => {
  const intermediateState = appReducer(state, action);
  return commonReducer(intermediateState, action);
};

export default rootReducer;
