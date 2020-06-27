import React, { useReducer } from "react";
import firebase from "firebase/app";
import { AuthContext } from "../context";
import AuthReducer from "./authReducer";
import { SET_USER } from "../actionTypes";

const AuthState = props => {
  const { email } = firebase.auth().currentUser || {};
  const initialState = {
    email: email
  };

  const [state, dispatch] = useReducer(AuthReducer, initialState);

  const setUser = email => {
    dispatch({ type: SET_USER, payload: email });
  };

  const signup = user => {
    return firebase
      .auth()
      .createUserWithEmailAndPassword(user.email, user.password)
      .then(data => {
        setUser(user.email);
        console.log("data: " + data);
      })
      .catch(err => {
        console.log(err.message);
      });
  };

  const login = user => {
    return firebase
      .auth()
      .signInWithEmailAndPassword(user.email, user.password)
      .then(data => {
        console.log("Log in Form submitted!", data);
        setUser(user.email);
        // this.$router.replace({ name: "home" });
      })
      .catch(err => {
        console.log(err.message);
      });
  };

  const logout = () => {
    return firebase
      .auth()
      .signOut()
      .then(() => {
        setUser("");
      });
  };

  return (
    <AuthContext.Provider
      value={{
        email: state.email,
        signup,
        login,
        setUser,
        logout
      }}
    >
      {props.children}
    </AuthContext.Provider>
  );
};

export default AuthState;
