import { createStore, applyMiddleware } from 'redux'
import thunk from 'redux-thunk'
import api from 'redux/middleware/api'
import rootReducer from 'redux/reducer/mainReducer';

const configureStore = preloadedState => createStore(
  rootReducer,
  preloadedState,
  applyMiddleware(thunk, api)
)

export default configureStore