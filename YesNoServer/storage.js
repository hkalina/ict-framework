
const fs          = require('fs');
const persistence = require('persistencejs');

var mysqlHost = process.env.OPENSHIFT_MYSQL_DB_HOST || 'localhost';
var mysqlPort = process.env.OPENSHIFT_MYSQL_DB_PORT || 3306;
var mysqlUser = process.env.OPENSHIFT_MYSQL_DB_USERNAME || 'adminlcf236l';
var mysqlPass = process.env.OPENSHIFT_MYSQL_DB_PASSWORD || 'f4bhc6JMWn9V';
var mysqlName = process.env.OPENSHIFT_GEAR_NAME || 'ict';

var persistenceStore = persistence.StoreConfig.init(persistence, { adaptor: 'mysql' });
persistenceStore.config(persistence, mysqlHost, mysqlPort, mysqlName, mysqlUser, mysqlPass);

var entities = {};

entities.Card = persistence.define('Card', {
    name: "TEXT",
    text: "TEXT",
    color: "TEXT",
    image: "TEXT",
    sound: "TEXT",
});

entities.Pair = persistence.define('Pair', {});

entities.List = persistence.define('List', {
    name: "TEXT"
});

entities.User = persistence.define('User', {
    username: "TEXT",
    password: "TEXT"
});
entities.User.index('username');

entities.Pair.hasOne('left', entities.Card);
entities.Pair.hasOne('right', entities.Card);
entities.Pair.hasOne('list', entities.List);

entities.List.hasMany('pairs', entities.Pair, 'list');

entities.Card.hasOne('owner', entities.User);
entities.List.hasOne('owner', entities.User);

persistence.debug = true;

var session = persistenceStore.getSession();
session.schemaSync(function() {
  console.log("schema synced");
  session.close();
});

module.exports = {
  persistence: persistence,
  persistenceStore: persistenceStore,
  entities: entities
};

