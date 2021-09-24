FROM mongo

COPY appConfig.json /appConfig.json

CMD mongoimport --host mongo --db datagate --collection appConfig --drop --type json --file /appConfig.json --jsonArray