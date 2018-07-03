
const initialState = {
  error_msg: ""
};

export default function errorReducer(state = initialState, action) {

  if(action.type.endsWith("FAILURE")){
    var err = "";
    if(action.error) {
        err = action.error;
    } else {
        err = action.response.error.errors[0];
    }
    console.error(err);

    return Object.assign({}, state, {
          error_msg : err
        });
  } else {
    return state;
  }
}
