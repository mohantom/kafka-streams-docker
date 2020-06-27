import React from "react";

const About = () => {
  const notes = {
    1: `Every year since 2008, the number of contributions to our database has increased. With over 200,000 developers and companies using our platform, TMDb has become a premiere source for metadata.`,
    2: `Along with extensive metadata for movies, TV shows and people, we also offer one of the best selections of high resolution posters and fanart. On average, over 1,000 images are added every single day.`,
    3: `We're international. While we officially support 39 languages we also have extensive regional data. Every single day TMDb is used in over 180 countries.`,
    4: `Our community is second to none. Between our staff and community moderators, we're always here to help. We're passionate about making sure your experience on TMDb is nothing short of amazing.`,
    5: `Trusted platform. Every single day our service is used by millions of people while we process over 3 billion requests. We've proven for years that this is a service that can be trusted and relied on.`
  };

  return (
    <div className="about main-content">
      <section className="hero">
        <div className="hero-body">
          <div className="container">
            <div className="content">
              <h2>Hi there,</h2>
              <img
                src="https://www.themoviedb.org/assets/2/v4/marketing/deadpool-06f2a06d7a418ec887300397b6861383bf1e3b72f604ddd5f75bce170e81dce9.png"
                alt="poster"
                width="902"
                height="298"
              />

              <h3>
                Let's talk about TMDb (copied from
                <a href="https://www.themoviedb.org/about">TMDb</a>)
              </h3>
              <p>
                The Movie Database (TMDb) is a <em>community built</em> movie
                and TV database. Every piece of data has been added by our
                amazing community dating back to 2008. TMDb's strong
                <em>international focus</em> and breadth of data is largely
                unmatched and something we're incredibly proud of. Put simply,
                we live and breathe community and that's precisely what makes us
                different.
              </p>

              <h4>The TMDb Advantage</h4>
              <div className="wrapper">
                <div>
                  <div>1</div>
                  <p>{notes[1]}</p>
                </div>

                <div>
                  <div>2</div>
                  <p>{notes[2]}</p>
                </div>

                <div>
                  <div>3</div>
                  <p>{notes[3]}</p>
                </div>

                <div>
                  <div>4</div>
                  <p>{notes[4]}</p>
                </div>

                <div>
                  <div>5</div>
                  <p>{notes[5]}</p>
                </div>
              </div>

              <button className="contact button white">Contact TMDb</button>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
};

export default About;
