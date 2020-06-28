import React, { useState, useEffect, useContext } from "react";
import _ from "lodash";
import { AgGridReact } from "@ag-grid-community/react";
import { AllCommunityModules } from "@ag-grid-community/all-modules";
import { MovieContext } from "../context/context";
import Highcharts from "highcharts";
import HighchartsReact from "highcharts-react-official";

import "@ag-grid-community/all-modules/dist/styles/ag-grid.css";
import "@ag-grid-community/all-modules/dist/styles/ag-theme-balham.css";

const gridDefs = [
  {
    headerName: "",
    lockPosition: true,
    valueGetter: params => params.node.rowIndex + 1,
    cellClass: "locked-col",
    width: 50,
    suppressNavigable: true
  },
  { headerName: "Title", field: "title", sortable: true, filter: true },
  {
    headerName: "Rate",
    field: "rating",
    sortable: true,
    filter: true
  },
  {
    headerName: "Genre",
    field: "genre",
    sortable: true,
    filter: true
  },
  {
    headerName: "Year",
    field: "year",
    sortable: true,
    filter: true
  },
  {
    headerName: "Country",
    field: "country",
    sortable: true,
    filter: true
  }
];

const createCountByRatePieOptions = function(filterMovies) {
  return {
    chart: {
      plotBackgroundColor: null,
      plotBorderWidth: null,
      plotShadow: false,
      type: "pie"
    },
    title: {
      text: "Movie Count by Rate"
    },
    subtitle: {
      text: "Source: OMDB.com"
    },
    tooltip: {
      pointFormat: "{series.name}: <b>{point.percentage:.1f}%</b>"
    },
    plotOptions: {
      series: {
        cursor: "pointer",
        point: {
          events: {
            click: function() {
              const filter = { rating: +this.name };
              filterMovies(filter);
            }
          }
        }
      }
    },
    series: [
      {
        name: "Count",
        colorByPoint: true,
        data: []
      }
    ]
  };
};

const createCountByYearBarOptions = function() {
  return {
    chart: {
      type: "column"
    },
    title: {
      text: "Movie Count By Year"
    },
    subtitle: {
      text: "Source: TMDB.com"
    },
    xAxis: {
      categories: [],
      crosshair: true
    },
    yAxis: {
      min: 0,
      title: {
        text: "Count"
      }
    },
    tooltip: {
      headerFormat: '<span style="font-size:10px">{point.key}</span><table>',
      pointFormat:
        '<tr><td style="color:{series.color};padding:0">{series.name}: </td>' +
        '<td style="padding:0"><b>{point.y:.f}</b></td></tr>',
      footerFormat: "</table>",
      shared: true,
      useHTML: true
    },
    plotOptions: {
      column: {
        pointPadding: 0.2,
        borderWidth: 0
      },
      series: {
        cursor: "pointer",
        point: {
          events: {
            click: function() {
              // vm.filters.release_year = this.category;
            }
          }
        }
      }
    },
    series: [
      {
        name: "Count",
        data: []
      }
    ]
  };
};

const MovieStats = props => {
  const movieContext = useContext(MovieContext);
  const { getMoviesTopRated } = movieContext;

  const [movieStats, setMovieStats] = useState({
    moviesToprated: [],
    gridColumnDefs: [],
    countByYearBarOptions: {},
    countByYearPieOptions: {},

    filters: { year: undefined, rating: undefined },
  });

  const {
    moviesToprated,
    gridColumnDefs,
    filters,
    countByYearBarOptions,
    countByYearPieOptions
  } = movieStats;

  useEffect(() => {
    getMoviesTopRated().then(movies => {
      const movieCountByRate = _.chain(movies)
        .countBy("rating")
        .map((count, rating) => {
          return {
            name: rating,
            y: count
          };
        })
        .value();

      const movieCountByYear2 = _.chain(movies)
        .countBy("year")
        .value();

      const pieOptions = createCountByRatePieOptions(filterMovies);
      pieOptions.series[0].data = movieCountByRate;

      const barOptions = createCountByYearBarOptions();
      barOptions.xAxis.categories = _.keys(movieCountByYear2);
      barOptions.series[0].data = _.values(movieCountByYear2);

      setMovieStats({
        moviesToprated: movies,
        gridColumnDefs: gridDefs,
        filters: { year: undefined, rating: undefined },
        countByYearPieOptions: pieOptions,
        countByYearBarOptions: barOptions
      });

      console.log("finished loading top 250 rated movies.");
    });
  }, []);

  const clearFilters = () => {
    const newfilters = _.reduce(
      filters,
      (result, v, k) => {
        result[k] = undefined;
        return result;
      },
      {}
    );

    setMovieStats({
      ...movieStats,
      filters: newfilters
    });
  };

  const filterMovies = filter => {
    const newFilters = { ...filters, ...filter };

    const filteredMovies = _.filter(
      movieStats.moviesToprated,
      _.pickBy(filters, v => _.isString(v) || _.isNumber(v))
    );

    setMovieStats({
      ...movieStats,
      moviesToprated: filteredMovies,
      filters: newFilters
    });
  };

  const hasFilter = () => {
    return _.some(filters, _.isString);
  };

  return (
    <div className="hero is-fullheight">
      <div className="hero body">
        <div className="container">
          <div className="columns">
            <div className="column is-6">
              <HighchartsReact
                highcharts={Highcharts}
                options={countByYearPieOptions}
              />
            </div>
            <div className="column is-6">
              <HighchartsReact
                highcharts={Highcharts}
                options={countByYearBarOptions}
              />
            </div>
          </div>

          <h2 className="title is-primary">
            Top 250 Rated Movies
            <button className="button is-primary" onClick={clearFilters}>
              Clear Filters
            </button>
          </h2>

          <div
            className="ag-theme-balham"
            style={{
              height: "500px",
              width: "1000px"
            }}
          >
            <AgGridReact
              columnDefs={gridColumnDefs}
              rowData={moviesToprated}
              modules={AllCommunityModules}
            ></AgGridReact>
          </div>
        </div>
      </div>
    </div>
  );
};

export default MovieStats;
