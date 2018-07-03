import 'isomorphic-fetch'

const API_ROOT = (process.env.NODE_ENV === 'production') ? window.location.origin : "http://localhost:9000";

// Fetches an API response and normalizes the result JSON according to schema.
// This makes every API response have the same shape, regardless of how nested it was.
function callApi(endpoint, method, data) {
  const fullUrl = (endpoint.indexOf(API_ROOT) === -1) ? API_ROOT + endpoint : endpoint

  let fetchConfig = {
    method: method,
    credentials: 'include',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json',
      'Csrf-Token': 'nocheck'
    }
  };

  if (typeof data !== 'undefined') {
    Object.assign(fetchConfig, 
    {
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json',
        'Csrf-Token': 'nocheck'
      },
      body: JSON.stringify(data)
    })
  }

  return fetch(fullUrl, fetchConfig)
    .then(response => {
          console.log("response : ");
          console.log(response);
          if (!response.ok) {
            return Promise.reject(response)
          } else {
            return response.json();
          }
      }
    )
}

// Action key that carries API call info interpreted by this Redux middleware.
export const CALL_API = Symbol('Call API')

// A Redux middleware that interprets actions with CALL_API info specified.
// Performs the call and promises when such actions are dispatched.
export default store => next => action => {
  const callAPI = action[CALL_API]
  if (typeof callAPI === 'undefined') {
    return next(action)
  }

  let { endpoint } = callAPI
  const { method, types, data } = callAPI

  if (typeof endpoint === 'function') {
    endpoint = endpoint(store.getState())
  }

  if (typeof endpoint !== 'string') {
    throw new Error('Specify a string endpoint URL.')
  }

  if (!Array.isArray(types) || types.length !== 3) {
    throw new Error('Expected an array of three action types.')
  }
  if (!types.every(type => typeof type === 'string')) {
    throw new Error('Expected action types to be strings.')
  }

  function actionWith(data) {
    const finalAction = Object.assign({}, action, data)
    delete finalAction[CALL_API]
    return finalAction
  }

  const [ requestType, successType, failureType ] = types
  next(actionWith({ type: requestType }))

  return callApi(endpoint, method, data).then(
    response => next(actionWith({
      response,
      type: successType
    })),
    error => next(actionWith({
      type: failureType,
      error: computeErrorMsg(error)
    }))
  )
}

function computeErrorMsg(response){
  return response.message || `[${response.status}] ${response.statusText} on url ${response.url}`
}