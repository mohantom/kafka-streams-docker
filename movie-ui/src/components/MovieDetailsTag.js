import React from "react";

const MovieDetailsTag = ({ tagName, tagValue }) => {
  return (
    <p className="is-larger">
      <span className="tag is-info column is-1"> {tagName} </span>
      <strong>&nbsp; {tagValue}</strong>
    </p>
  );
};

export default MovieDetailsTag;
