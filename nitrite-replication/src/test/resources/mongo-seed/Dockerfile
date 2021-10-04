FROM mongo

COPY appConfig.json /appConfig.json

CMD mongoimport --host mongo --db datagate --collection serverConfigs --drop --type json --file /serverConfig.json --jsonArray
CMD mongoimport --host mongo --db datagate --collection appConfigs --drop --type json --file /appConfig.json --jsonArray