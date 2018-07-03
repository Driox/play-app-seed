
import { CALL_API } from 'redux/middleware/api'

export function updateFavoredLanguage(user_id, lng) {
    return {
      [CALL_API]: {
        types: async_actions("UPDATE_LNG"),
        endpoint: `/users/${user_id}/lng/${lng}`,
        method: "post"
      }
    };
  }
  
function async_actions(value) {
  return [value + "_REQUEST", value + "_SUCCESS", value + "_FAILURE"];
}
