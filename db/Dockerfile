FROM postgres:13-alpine


RUN mkdir /data
RUN wget https://files.grouplens.org/datasets/movielens/ml-latest-small.zip -P /data
RUN unzip /data/ml-latest-small.zip -d /data
RUN rm /data/ml-latest-small.zip

COPY install.sh /docker-entrypoint-initdb.d/
