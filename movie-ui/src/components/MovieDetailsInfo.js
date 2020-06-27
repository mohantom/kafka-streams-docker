import React from "react";

const MovieDetailsInfo = ({ heading, subheading, isLink }) => {
  return (
    <div className="column">
      <p className="heading">
        {isLink && (
          <strong>
            <a href={heading} target="_blank" rel="noopener noreferrer">
              IMDB Link
            </a>
          </strong>
        )}
        {!isLink && <strong>{heading}</strong>}
      </p>

      <p className="subheading">{subheading}</p>
    </div>
  );
};

export default MovieDetailsInfo;
