import React from "react";

const MovieDetailsPoster = ({ posterPath, posterSize }) => {
  const basePosterUrl = {
    small: `http://image.tmdb.org/t/p/w185/`,
    medium: `http://image.tmdb.org/t/p/w500/`
  };

  return (
    <div class="column">
      <img src={basePosterUrl[posterSize] + posterPath} alt="poster" />
    </div>
  );
};

export default MovieDetailsPoster;
