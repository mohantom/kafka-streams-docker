import React from "react";
import ReactDOM from "react-dom";
import "./index.css";
import App from "./App";
import * as serviceWorker from "./serviceWorker";
import { BrowserRouter } from "react-router-dom";
import { auth } from "./firebase";
import MovieState from "./context/movie/movieState";
import AuthState from "./context/auth/authState";

let app;
auth.onAuthStateChanged(user => {
  if (!app) {
    const app = (
      <AuthState>
        <MovieState>
          <BrowserRouter>
            <App />
          </BrowserRouter>
        </MovieState>
      </AuthState>
    );
    ReactDOM.render(app, document.getElementById("root"));
  }
});

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
