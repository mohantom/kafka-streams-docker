import axios from "axios";

const baseMovieUrl = `https://api.themoviedb.org/3/`;
const mongoMovieUrl = `http://localhost:8040/mongo/`;
const apiKey = `&api_key=db92a48cc98dd05417a4d990dd1e47aa`;

const myAxios = axios.create({
  baseURL: baseMovieUrl,
  timeout: 1000
  // headers: {'X-Custom-Header': 'foobar'}
});

const mongoAxios = axios.create({
  baseURL: mongoMovieUrl,
  timeout: 1000
});

myAxios.interceptors.request.use(
  config => {
    // perform a task before the request is sent
    console.log("Request was sent");
    config.url += apiKey;

    return config;
  },
  error => {
    // handle the error
    return Promise.reject(error);
  }
);


// use URI and URLSearchParams: https://developers.google.com/web/updates/2016/01/urlsearchparams?hl=en
const movieUrls = {
  // mongo app
  popular: page => `movie/all?size=100&sortField=year&direction=DESC&page=${page}`,
  search: title => `movie/query?title=${title}`,
  toprated: page => `movie/all?size=250&sortField=rating&direction=DESC&page=${page}`,
  play: () => `movie/play`,

  // tmdb
  findMovieId: imdbid => `/find/${imdbid}?external_source=imdb_id`,
  details: movieid => `/movie/${movieid}?append_to_response=videos`,
  review: movieid => `/movie/${movieid}/reviews?`,
  imdbLink: imdbid => `https://www.imdb.com/title/${imdbid}`
};

export { mongoAxios, movieUrls };
export default myAxios;
