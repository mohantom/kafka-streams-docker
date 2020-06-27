import {
  SET_USER
} from "../actionTypes";

export default (state, action) => {
  switch (action.type) {
    case SET_USER:
      return {
        ...state,
        email: action.payload
      };

    default:
      return state;
  }
};
