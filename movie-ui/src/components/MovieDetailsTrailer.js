import React from "react";

const MovieDetailsTrailer = ({ video }) => {
  const baseYoutubeUrl = `https://www.youtube.com/embed/`;
  return (
    <div className="column">
      <iframe
        width="100%"
        height="auto"
        src={baseYoutubeUrl + video.key}
        allowfullscreen
        title={video.id}
      ></iframe>
    </div>
  );
};

export default MovieDetailsTrailer;
