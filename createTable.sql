CREATE TABLE flights
(
  "flightid" character varying(10) NOT NULL,
  track geometry NOT NULL,
  CONSTRAINT simple_pl PRIMARY KEY (flightid)
)