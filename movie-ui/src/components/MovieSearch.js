import React, { useState, useContext } from "react";
import { MovieContext } from "../context/context";

const MovieSearch = () => {
  const [searchText, setSearchText] = useState("");
  const context = useContext(MovieContext);

  const updateSearchText = e => {
    setSearchText(e.target.value);
  };

  const searchMovie = () => {
    context.searchMovie(searchText);
  };

  const filterMovies = (e) => {
    context.filterMovies(e.target.value);
  }

  return (
    <div className="columns">
      <div className="column is-8">
        <div className="field has-addons">
          <div className="control is-expanded">
            <input
              className="input has-text-centered"
              type="search"
              placeholder="Search movies..."
              onChange={updateSearchText}
            />
          </div>
          <div className="control">
            <div className="button is-primary" onClick={searchMovie}>
              Search
            </div>
          </div>
        </div>
      </div>

      <div className="column is-4">
        <div className="field has-addons">
          <div className="control is-expanded">
            <input className="input" type="search" placeholder="Filter Movie" onChange={filterMovies}/>
          </div>
        </div>
      </div>
    </div>
  );
};

export default MovieSearch;
