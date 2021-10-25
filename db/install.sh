export PGUSER=postgres
cd /data

psql -d postgres < /data/init.sql -U postgres
psql -U postgres -d postgres -c "\copy public.movies FROM '/data/ml-latest-small/movies.csv' with (format csv,header true, delimiter ',');"
psql -U postgres -d postgres -c "\copy public.links FROM '/data/ml-latest-small/links.csv' with (format csv,header true, delimiter ',');"
psql -U postgres -d postgres -c "\copy public.ratings FROM '/data/ml-latest-small/ratings.csv' with (format csv,header true, delimiter ',');"
psql -U postgres -d postgres -c "\copy public.tags FROM '/data/ml-latest-small/tags.csv' with (format csv,header true, delimiter ',');"