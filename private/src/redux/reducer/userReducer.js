
const initialState = {
  user: {
    id: 0,
    email: "test@gmail.com",
    firstName: "Jean",
    lastName: "Dupont",
    language: "fr",
    avatar_url: "http://placehold.it/24x24"
  }
};


export default function userReducer(state = initialState, action) {
  switch (action.type) {
    case "GET_USER_SUCCESS": 
      return mergeUser(action.response.result);
    default:
      return state;
  }
}

var mergeUser = function(newUser) {
  if(newUser) {
    return {
      id: newUser.id,
      email: newUser.email,
      firstName: newUser.firstName,
      lastName: newUser.lastName,
      language: newUser.language,
      avatar_url: newUser.avatar_url || "http://placehold.it/24x24"
    }
  } else {
    return initialState.user;
  }
}
