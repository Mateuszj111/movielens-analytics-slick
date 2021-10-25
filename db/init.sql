CREATE TABLE IF NOT EXISTS public.movies
(
    "movieId" integer NOT NULL,
    title character varying COLLATE pg_catalog."default" NOT NULL,
    genres character varying COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT movies_pkey PRIMARY KEY ("movieId")
);

CREATE TABLE IF NOT EXISTS public.links
(
    "movieId" integer NOT NULL,
    "imdbId" character varying COLLATE pg_catalog."default" NOT NULL,
    "tmdbId" integer,
    CONSTRAINT links_pkey PRIMARY KEY ("movieId")
);

CREATE TABLE IF NOT EXISTS public.ratings
(
    "userId" integer NOT NULL,
    "movieId" integer NOT NULL,
    rating double precision NOT NULL,
    "timestamp" bigint NOT NULL
);


CREATE TABLE IF NOT EXISTS public.tags
(
    "userId" integer NOT NULL,
    "movieId" integer NOT NULL,
    tag character varying COLLATE pg_catalog."default",
    "timestamp" bigint NOT NULL
);


ALTER TABLE public.movies
    OWNER to postgres;

ALTER TABLE public.links
    OWNER to postgres;

ALTER TABLE public.ratings
    OWNER to postgres;

ALTER TABLE public.tags
    OWNER to postgres;