import React, { useContext, useRef } from "react";
import { AuthContext } from "../context/context";

const Login = props => {
  const context = useContext(AuthContext);
  const emailRef = useRef();
  const passwordRef = useRef();

  const onSubmit = e => {
    e.preventDefault();
    const email = emailRef.current.value;
    const password = passwordRef.current.value;
    const user = {email: email, password: password};

    if (props.location.state && props.location.state.type === "signup") {
      context.signup(user).then(() => {
        props.history.push({ pathname: "/home", state: { email: email } });
      });
    } else {
      context.login(user).then(() => {
        props.history.push({ pathname: "/home", state: { email: email } });
      });
    }
  };

  const resetForm = () => {
    emailRef.current.value = '';
    passwordRef.current.value = '';
  };

  return (
    <div className="hero-body">
      <div className="container">
        <div className="columns is-centered">
          <div className="column is-8-tablet is-6-desktop is-4-widescreen">
            <form onSubmit={onSubmit}>
              <div className="form-group">
                <label htmlFor="email">Email:</label>
                <input
                  type="email"
                  name="email"
                  ref={emailRef}
                />
              </div>

              <div className="form-group">
                <label htmlFor="password">Password:</label>
                <input
                  type="password"
                  name="password"
                  ref={passwordRef}
                />
              </div>

              <br />

              <div className="buttons">
                <span className="icon is-small">
                  <i className="fas fa-check"></i>
                </span>
                <input
                  type="submit"
                  className="button is-success"
                  value={
                    props.location.state &&
                    props.location.state.type === "signup"
                      ? "Sign Up"
                      : "Log In"
                  }
                />
                <button className="button is-warning" onClick={resetForm}>
                  <span className="icon is-small">
                    <i className="fas fa-redo"></i>
                  </span>
                  <span>Reset</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;
