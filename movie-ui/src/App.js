import React, { Suspense, useContext, useEffect } from "react";
import { Route, Switch } from "react-router-dom";
import "bulma/css/bulma.css";
import "./App.css";
import MovieHome from "./views/MovieHome";
import MovieOscar from "./views/MovieOscar";
import MovieDetails from "./views/MovieDetails";
import About from "./views/About";
import Login from "./views/Login";
import MovieStats from "./views/MovieStats";
import NavBar from "./shared/NavBar";
import PrivateRoute from "./shared/PrivateRoute";

function App(props) {
  const routes = (
    <Switch>
      <PrivateRoute exact path="/" component={MovieHome} />
      <PrivateRoute exact path="/oscar" component={MovieOscar} />
      {/* <Route path="/home" exact component={MovieHome} /> */}
      <PrivateRoute path="/movie/:imdbid" exact component={MovieDetails} />
      <PrivateRoute path="/moviestats" exact component={MovieStats} />

      <Route path="/about" exact component={About} />
      <Route path="/login" exact component={Login} />

      <Route path="*" exact render={props => <h1>Page Not Found</h1>} />
    </Switch>
  );

  return (
    <div className="App">
      <div>
        <NavBar id="navbar" />
        <Suspense fallback={<p>Loading...</p>}>{routes}</Suspense>
      </div>
    </div>
  );
}

export default App;
